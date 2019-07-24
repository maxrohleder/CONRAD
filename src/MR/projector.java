package MR;

import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.MultiChannelGrid3D;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;
import edu.stanford.rsl.conrad.geometry.trajectories.Trajectory;
import edu.stanford.rsl.conrad.opencl.OpenCLMaterialPathLengthPhantomRenderer;
import edu.stanford.rsl.conrad.opencl.OpenCLProjectionPhantomRenderer;
import edu.stanford.rsl.conrad.opencl.OpenCLUtil;
import edu.stanford.rsl.conrad.phantom.AnalyticPhantom;
import edu.stanford.rsl.conrad.phantom.MECT;
import edu.stanford.rsl.conrad.phantom.renderer.ParallelProjectionPhantomRenderer;
import edu.stanford.rsl.conrad.phantom.renderer.PhantomRenderer;
import edu.stanford.rsl.conrad.physics.PhysicalObject;
import edu.stanford.rsl.conrad.physics.absorption.PolychromaticAbsorptionModel;
import edu.stanford.rsl.conrad.physics.detector.*;
import edu.stanford.rsl.conrad.physics.materials.Material;
import edu.stanford.rsl.conrad.utils.Configuration;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import edu.stanford.rsl.conrad.utils.RegKeys;
import ij.ImagePlus;
import ij.measure.Calibration;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

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
		projector p = new projector(projType.MATERIAL);

		// create material projections
		Grid3D materialProjections = p.computeMaterialGrids(phantom);
		
		Configuration.saveConfiguration(Configuration.getGlobalConfiguration(), CONRAD_CONFIG);
		
		//MultiChannelGrid3D gridded = p.toChannels(materialProjections);
		
		// filter together the channels to obtain attenuation information
		//Grid3D highEnergyProjections = p.getPolchromaticImageFromMaterialGrid(gridded, projType.POLY120);
		//Grid3D lowEnergyProjections = p.getPolchromaticImageFromMaterialGrid(gridded, projType.POLY80);
		
		// visualize projection data
		helpers.showGrid(materialProjections, "MATERIAL");
		//helpers.showGrid(highEnergyProjections, "POLY120");
		//helpers.showGrid(lowEnergyProjections, "POLY80");
		
	}

	private MultiChannelGrid3D toChannels(Grid3D materialProjections) {
		//TODO implement
		Configuration conf = Configuration.getGlobalConfiguration();
		int x = conf.getGeometry().getDetectorWidth();
		int y = conf.getGeometry().getDetectorHeight();
		int z = conf.getGeometry().getNumProjectionMatrices();
		int[] xyzc = materialProjections.getSize();
		int c = xyzc[3];
		MultiChannelGrid3D channeled = new MultiChannelGrid3D(x, y, z, c);
		return channeled;
	}


	// rendering options
	static String CONRAD_CONFIG = "/home/mr/Documents/bachelor/CONRAD/src/MR/config_dir/MAT_OPENCL_RSL_2MAT.xml";
	projType prot;
	boolean useGPU;


	// material renderer
	public OpenCLMaterialPathLengthPhantomRenderer gpu_mat_renderer;
	
	public projector(projType pro) {
		this.prot = pro;
		// choose renderer based on params
		if(pro == projType.MATERIAL) {
			// configure projector with xml file
			Configuration conf = Configuration.loadConfiguration(CONRAD_CONFIG);
			Configuration.setGlobalConfiguration(conf);
			if(cnfg.DEBUG) System.out.println(RegKeys.OPENCL_DEVICE_SELECTION);
			// do the actual raytracing and calculate material images
			gpu_mat_renderer = new OpenCLMaterialPathLengthPhantomRenderer();
		} else {
			// use MultiChannelMaterialFiltering to create POLY80 and POLY120 images
			System.err.println("[projector] constructor not implemented yet");
			System.exit(0);
		}

		//if(cnfg.DEBUG) System.out.println("using " + parrallel_cpu_renderer + gpu_mat_renderer + gpu_renderer);
	}
	

	/**
	 * creates projection domain images with the renderer specified in constructor
	 * @param energy modulates spectrum in all non-material projections
	 * @throws Exception 
	 */
	public Grid3D computeMaterialGrids(AnalyticPhantom phantom) {
		
		// configure the phantom renderer
		gpu_mat_renderer.configure(phantom);
		
		//start the projection
		long projStart = Time.currentMonotonicTimeMillis();
		
		Grid3D result = null;
		try {
			result = PhantomRenderer.generateProjections(this.gpu_mat_renderer);
		} catch (Exception e) {
			// the actual block sizes of the phantom to render were to big
			e.printStackTrace();
			System.exit(0);
		}

		if(cnfg.DEBUG) {
			System.out.println("projecting took " + (Time.currentMonotonicTimeMillis() - projStart) / 1000+ "s");
		}
		return result;
	}
	
	public Grid3D getPolchromaticImageFromMaterialGrid(MultiChannelGrid3D MatData, projType p) {
		System.err.println("method not implemented yet - returning dummy");
		//TODO implement
		Configuration conf = Configuration.getGlobalConfiguration();
		int x = conf.getGeometry().getDetectorWidth();
		int y = conf.getGeometry().getDetectorHeight();
		int z = conf.getGeometry().getNumProjectionMatrices();
		return new Grid3D(x, y, z);
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
















