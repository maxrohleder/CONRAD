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

import java.io.File;
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
		projector p = new projector(projector.rootDirPath, "MAT.xml", "POLY80.xml", "POLY120.xml");

		// create material projections
		Grid3D materialProjections = p.computeMaterialGrids(phantom);
		
		System.out.println(RegKeys.RENDER_PHANTOM_VOLUME_AUTO_CENTER);
		
		//Configuration.saveConfiguration(Configuration.getGlobalConfiguration(), CONRAD_CONFIG);
		
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
	private Configuration matConf, poly80Conf, poly120Conf;
	
	// defaults (can be changed by constructor)
	private static final String rootDirPath = "/home/mr/Documents/bachelor/CONRAD/src/MR/config_dir";
	private static final String matConfFilename = "MAT.xml";		// relative to configDir
	private static final String poly80ConfFilename = "POLY80.xml";
	private static final String poly120ConfFilename = "POLY120.xml";

	// material renderer
	public OpenCLMaterialPathLengthPhantomRenderer gpu_mat_renderer;

	
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
	 * @param configDir directory must include files named like the other parameters
	 * @param m configuration xml file for material projection rendering
	 * @param p80 configuration xml file for lower energy projection
	 * @param p120 configuration xml file for higher energy projection
	 */
	public projector(String configDir, String m, String p80, String p120) {

		// configure projector with xml file
		initConfigsFromFile(configDir, m, p80, p120);		
	}
	

	

	private void initConfigsFromFile(String configDir, String m, String p80, String p120) {
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
				System.err.println("[projector] at least one of the required config files is missing! Aborting");
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
	
	public Grid3D getPolchromaticImageFromMaterialGrid(MultiChannelGrid3D MatData, projType p) {
		
		// set the detector from desired configuration
		Configuration tmp = (p==projType.POLY80) ? poly80Conf : poly120Conf;
		Configuration.setGlobalConfiguration(tmp);
		
		// create filter (reads detector from global config)
		SimulateXRayDetector filter = new SimulateXRayDetector();
		try { filter.configure(); } catch (Exception e) { e.printStackTrace(); }
		
		int s[] = MatData.getSize();
		Grid3D attenuationBasedProjections = new Grid3D(s[0], s[1], s[2]);
		
		// iterate over all subgrids/projections, apply filter and store attenuation
		Grid2D attenuatedProjection = null;
		for (int i = 0; i < s[0]; i++) {
			try {
				attenuatedProjection = filter.applyToolToImage(MatData.getSubGrid(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
			attenuationBasedProjections.setSubGrid(i, attenuatedProjection);			
		}
		return attenuationBasedProjections;
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
















