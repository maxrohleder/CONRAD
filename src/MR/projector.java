package MR;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.MultiChannelGrid2D;
import edu.stanford.rsl.conrad.data.numeric.MultiChannelGrid3D;
import edu.stanford.rsl.conrad.filtering.SimulateXRayDetector;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;
import edu.stanford.rsl.conrad.geometry.trajectories.Trajectory;
import edu.stanford.rsl.conrad.opencl.OpenCLMaterialPathLengthPhantomRenderer;
import edu.stanford.rsl.conrad.opencl.OpenCLProjectionPhantomRenderer;
import edu.stanford.rsl.conrad.opencl.OpenCLUtil;
import edu.stanford.rsl.conrad.parallel.ParallelThread;
import edu.stanford.rsl.conrad.parallel.ParallelThreadExecutor;
import edu.stanford.rsl.conrad.parallel.ParallelizableRunnable;
import edu.stanford.rsl.conrad.parallel.SimpleParallelThread;
import edu.stanford.rsl.conrad.phantom.AnalyticPhantom;
import edu.stanford.rsl.conrad.phantom.MECT;
import edu.stanford.rsl.conrad.phantom.renderer.ParallelProjectionPhantomRenderer;
import edu.stanford.rsl.conrad.phantom.renderer.PhantomRenderer;
import edu.stanford.rsl.conrad.physics.PhysicalObject;
import edu.stanford.rsl.conrad.physics.absorption.PolychromaticAbsorptionModel;
import edu.stanford.rsl.conrad.physics.detector.*;
import edu.stanford.rsl.conrad.physics.materials.Material;
import edu.stanford.rsl.conrad.utils.CONRAD;
import edu.stanford.rsl.conrad.utils.Configuration;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import edu.stanford.rsl.conrad.utils.RegKeys;
import ij.ImagePlus;
import ij.measure.Calibration;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;

import IceInternal.Time;
import MR.spectrum_creator;


/**
 * Static class. Wraps 4 different renderer. All Children of PhantomRenderer.
 * use methods to create and visualize projections of analytical phantoms
 * @author Max
 *
 */
class projector{
	
	/**
	 * test main function. actual functionality will be in trainings_data_generator.java
	 * @param args
	 */
	public static void main(String[] args) {
		
		// create phantom and renderer
		AnalyticPhantom phantom = new MECT();
		projector p = new projector(projector.rootDirPath, "MAT.xml", "POLY80.xml", "POLY120.xml");

		// create material projections
		Grid3D materialProjections = p.computeMaterialGrids(phantom);
		Configuration.saveConfiguration(Configuration.getGlobalConfiguration(), projector.rootDirPath+"/MAT.xml");
			
		// filter together the channels to obtain attenuation information
		Grid3D highEnergyProjections = p.getPolchromaticImageFromMaterialGrid(materialProjections, projType.POLY120);
		//Grid3D lowEnergyProjections = p.getPolchromaticImageFromMaterialGrid(materialProjections, projType.POLY80);
		
		// visualize projection data
		helpers.showGrid(materialProjections, "MATERIAL");
		helpers.showGrid(highEnergyProjections, "POLY120");
		//helpers.showGrid(lowEnergyProjections, "POLY80");
		
	}

	// rendering options
	private Configuration matConf, poly80Conf, poly120Conf;
	
	// defaults (can be changed by constructor)
	private static final String rootDirPath = "/home/mr/Documents/bachelor/CONRAD/src/MR/config_dir";
	private static final String matConfFilename = "MAT.xml";		// relative to configDir
	private static final String poly80ConfFilename = "POLY80.xml";
	private static final String poly120ConfFilename = "POLY120.xml";

	// material renderer
	public OpenCLMaterialPathLengthPhantomRenderer gpu_mat_renderer;
	
	/**
	 * parallelization class for faster polychromatic filtering (maxThreads faster)
	 * @author rohleder
	 */
	class ParrallelFilteringThread extends SimpleParallelThread{
		int maxThreads;
		Grid3D input;
		Grid3D output;		

		public ParrallelFilteringThread(int threadNum, int maxThreads, Grid3D in, Grid3D out) {
			super(threadNum);
			this.maxThreads = maxThreads;
			this.input = in;
			this.output = out;
		}

		@Override
		public void execute() {
			// create filter (reads detector from global config)
			SimulateXRayDetector filter = new SimulateXRayDetector();
			try { filter.configure(); } catch (Exception e) { e.printStackTrace(); }
			// calculate entry and end point of this thread
			int blockSize = (int)Math.ceil(((double)output.getSize()[2]) / maxThreads);
			int start = this.threadNum * blockSize;
			int end = (this.threadNum+1) * (blockSize);
			if (end > output.getSize()[2]) end = output.getSize()[2];
			for (int i = start; i < end; ++i) {
				if(i%4==0)System.out.print('.');
				try {
					this.output.setSubGrid(i, filter.applyToolToImage(this.input.getSubGrid(i)));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * parallelization class for faster decomposition of material path length images in water and iodine
	 * @author rohleder
	 */
	class ParrallelSplittingThread extends ParallelThread{
		Grid3D data;
		Grid3D w;
		Grid3D iod;
		String materialName;
		Semaphore writeControl;
		int[] cnums;
		int[] s;
		boolean map;

		public ParrallelSplittingThread(Grid3D in, Grid3D water, Grid3D iodine, int[] channels, String matName, Semaphore write, boolean mapToIodine) {
			this.data = in;
			this.s = in.getSize();
			this.w = water;
			this.iod = iodine;
			this.materialName = matName; // only for identification of thread
			this.writeControl = write;
			this.cnums = channels;
			this.map = mapToIodine;
		}
		
		@Override
		public String getProcessName() {
			return "MaterialSplittingThread for " + this.materialName;
		}

		@Override
		public void execute() {
			// Typically just one channel per worker, but written this way for machines with few cores or applications with many materials
			for(int c: cnums) {
				String material = ((MultiChannelGrid2D) data.getSubGrid(0)).getChannelNames()[c];
				for (int z = 0; z < this.s[2]; z++) {
					Grid2D channelc = ((MultiChannelGrid2D) data.getSubGrid(z)).getChannel(c);	
					for (int i = 0; i < this.s[0]; i++) {
						for (int j = 0; j < this.s[1]; j++) {
							// represent material as iodine and water based on their attenuation behavior
							float[] bases = phantom_creator.MaterialBasisTransform(channelc.getAtIndex(i, j), material, this.map);
							if(bases[0] != 0 || bases[1] != 0) {
								try {
									this.writeControl.acquire();
									this.iod.addAtIndex(i, j, z, bases[0]);
									this.w.addAtIndex(i, j, z, bases[1]);
									this.writeControl.release();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}	
					}
				}
				System.out.print(".");
			}
		}
	}
	
	/**
	 * creates a wrapper around MaterialPathLengthRenderer, which enables easy filtering to 
	 * PolyChromaticImages from default parameters
	 */
	public projector() {
		this(rootDirPath);
	}
	
	/**
	 * creates a wrapper around MaterialPathLengthRenderer, which enables easy filtering to PolyChromaticImages
	 * @param configDir directory must include config files names MAT.xml, POLY80.xml and POLY120.xml
	 */
	public projector(String configDir) {
		this(configDir, matConfFilename, poly80ConfFilename, poly120ConfFilename);
	}
	
	/**
	 * creates a wrapper around MaterialPathLengthRenderer, which enables easy filtering to PolyChromaticImages
	 * @param configDir directory must include files on top level. Example Absolutpath: <configDir>/<m>
	 * @param m configuration xml file for material projection rendering. Example Absolutpath: <configDir>/<m>
	 * @param p80 configuration xml file for lower energy projection. Example Absolutpath: <configDir>/<p80>
	 * @param p120 configuration xml file for higher energy projection. Example Absolutpath: <configDir>/<p120>
	 */
	public projector(String configDir, String m, String p80, String p120) {
		// configure projector with xml files
		File root = new File(configDir);
		if(!root.isDirectory()) {
			System.err.println("[projector] config directory does not exist");
			System.exit(0);
		} else {
			File materialConfFile = new File(root, m);
			File poly80ConfFile = new File(root, p80);
			File poly120ConfFile = new File(root, p120);
			
			if(materialConfFile.isFile() && poly80ConfFile.isFile() && poly120ConfFile.isFile()) {
				matConf = Configuration.loadConfiguration(materialConfFile.getAbsolutePath());
				poly80Conf = Configuration.loadConfiguration(poly80ConfFile.getAbsolutePath());
				poly120Conf = Configuration.loadConfiguration(poly120ConfFile.getAbsolutePath());
			} else {
				System.err.println("[projector] at least one of the following files is missing! Aborting");
				System.err.println(materialConfFile.getAbsolutePath());
				System.err.println(poly80ConfFile.getAbsolutePath());
				System.err.println(poly120ConfFile.getAbsolutePath());
				System.exit(0);
			}
		}			
	}
	

	/**
	 * creates projection domain images with the renderer specified in constructor
	 * @param energy modulates spectrum in all non-material projections
	 * @throws Exception 
	 */
	public Grid3D computeMaterialGrids(AnalyticPhantom phantom) {
		
		// load the Configuration for material projection
		Configuration.setGlobalConfiguration(matConf);
		
		// init the renderer
		gpu_mat_renderer = new OpenCLMaterialPathLengthPhantomRenderer();
		
		// configure the phantom renderer
		gpu_mat_renderer.configure(phantom);
		
		// start the rendering
		Grid3D result = null;
		try {
			result = PhantomRenderer.generateProjections(this.gpu_mat_renderer);
		} catch (Exception e) {
			// the actual block sizes of the phantom to render were to big
			e.printStackTrace();
			System.exit(0);
		}

		return result;
	}
	
	public Grid3D getPolchromaticImageFromMaterialGrid(Grid3D materialProjections, projType p) {
		
		// start filtering
		if(cnfg.DEBUG) System.out.println("filtering process for " + p.toString());
		
		// set the detector from desired configuration
		Configuration tmp = (p==projType.POLY80) ? poly80Conf : poly120Conf;
		Configuration.setGlobalConfiguration(tmp);
		
		// create empty output grid
		int s[] = materialProjections.getSize();
		Grid3D attenuationBasedProjections = new Grid3D(s[0], s[1], s[2]);
		
		// split the work into numThreads pieces
		int numWorkers = CONRAD.getNumberOfThreads();
		ParallelizableRunnable [] workloadPartitions = new ParallelizableRunnable[numWorkers];
		if(cnfg.DEBUG) System.out.println("filtering 200 projections with " + numWorkers + " workers");
		for (int j= 0; j<numWorkers; j++) {
			workloadPartitions[j]= new ParrallelFilteringThread(j, numWorkers, materialProjections, attenuationBasedProjections);
		}
		ParallelThreadExecutor workdistributor = new ParallelThreadExecutor(workloadPartitions);
		
		try {
			workdistributor.execute();
			System.out.println("done");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return attenuationBasedProjections;
	}
	

	public void split(Grid3D water, Grid3D iodine, Grid3D data, boolean map) {
		System.out.println("splitting the material images in parrallel");
		// split the work into numThreads pieces
		int numChannels = ((MultiChannelGrid2D) data.getSubGrid(0)).getNumberOfChannels();
		int numWorkers = (CONRAD.getNumberOfThreads()<numChannels) ? CONRAD.getNumberOfThreads() : numChannels;
		
		ParallelizableRunnable [] workloadPartitions = new ParallelizableRunnable[numWorkers];
		Semaphore writeCtl = new Semaphore(1);
		for (int j= 0; j<numWorkers; j++) {
			int[] channelsToWorkOn;
			String matString = ""; // only needed to name the working thread
			if(numChannels == numWorkers) {
				channelsToWorkOn = new int[] {j};
				matString += ((MultiChannelGrid2D) data.getSubGrid(0)).getChannelNames()[j];
			}else {
				List<Integer> ctwo = new LinkedList<Integer>();
				for(int i = j; j<numChannels; j += numWorkers) {
					ctwo.add(i);
					matString += ((MultiChannelGrid2D) data.getSubGrid(0)).getChannelNames()[j];
				}
				channelsToWorkOn = ctwo.stream().mapToInt(Integer::intValue).toArray();
			}
			workloadPartitions[j] = new ParrallelSplittingThread(data, water, iodine, channelsToWorkOn, matString, writeCtl, map);
		}
		ParallelThreadExecutor workdistributor = new ParallelThreadExecutor(workloadPartitions);
		
		try {
			workdistributor.execute();
			System.out.println("done");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * helper class to inspect current state of Configuration
	 */
	public static void inspect_global_conf() {
		// load global conf
		Configuration.loadConfiguration();
		Configuration conf = Configuration.getGlobalConfiguration();
		// inspect device settings
		int pxY = conf.getGeometry().getDetectorHeight();
		int pxX = conf.getGeometry().getDetectorWidth();
		double h = pxY * conf.getGeometry().getPixelDimensionY();
		double w = pxX * conf.getGeometry().getPixelDimensionX();
		System.out.println("current detector and spectrum:\n\tname:\t\t" + conf.getDetector());
		System.out.println("\tdims (h, w):\t" + pxY + ", " + pxX + " [px]");
		System.out.println("\tpixelsizes (y, x):\t" + conf.getGeometry().getPixelDimensionY() + ", " + conf.getGeometry().getPixelDimensionX() + " [mm]");
		System.out.println("\tsize (h, w):\t" + h + ", " + w + " [cm]");
		System.out.println("current X-Ray tube:\n\tvoltage:\t" + conf.getVoltage());
		System.out.println("\tcutOffFreq:\t" + conf.getCutOffFrequency());
		// geometrical circumstances
		Trajectory geo = conf.getGeometry();
		System.out.print("geometrical attributes:\n");
		System.out.println("\torigin (x, y, z):\t" + conf.getGeometry().getOriginX() + ", " + conf.getGeometry().getOriginY() + ", " +  conf.getGeometry().getOriginZ() + "[mm]");
		System.out.println("\torigin px (x, y, z):\t" + conf.getGeometry().getOriginInPixelsX() + ", " + conf.getGeometry().getOriginInPixelsY() + ", " +  conf.getGeometry().getOriginInPixelsZ()+ "[px]");
		System.out.println("\tfocal length:\t" + geo.getSourceToDetectorDistance()+ "[cm]");
		System.out.println("\tincrement:\t" + geo.getAverageAngularIncrement()+ "[rad]");
		// reconstruction parameters
		System.out.println("reconstruction params:");
		System.out.println("\t#projectionMatrices:\t" 	+ geo.getNumProjectionMatrices());
		System.out.println("\tdims (z, y, x):\t\t" 		+ geo.getReconDimensionZ() + ", " +
														+ geo.getReconDimensionY() + ", " +
														+ geo.getReconDimensionX() + " [px]");
		System.out.println("\tvoxel size (z, y, x):\t" 	+ geo.getVoxelSpacingZ() + ", " +
														+ geo.getVoxelSpacingY() + ", " +
														+ geo.getVoxelSpacingX() + " [cm]");	

	}
	
}





/*
 * 		
		WHACK LINEAR APPROACH
		
		// create filter (reads detector from global config)
		SimulateXRayDetector filter = new SimulateXRayDetector();
		try { filter.configure(); } catch (Exception e) { e.printStackTrace(); }
		
		// iterate over all subgrids/projections, apply filter and store attenuation
		Grid2D attenuatedProjection = null;
		for (int i = 0; i < s[2]; i++) {
			try {
				attenuatedProjection = filter.applyToolToImage(materialProjections.getSubGrid(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
			attenuationBasedProjections.setSubGrid(i, attenuatedProjection);
			//if(cnfg.DEBUG) System.out.println("processed projection " + i + " / " + s[2]);
			if(i%10 == 0) System.out.print('.');
		}
		System.out.println("done");
 */










