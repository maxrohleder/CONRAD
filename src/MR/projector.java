package MR;

import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.geometry.trajectories.Trajectory;
import edu.stanford.rsl.conrad.phantom.AnalyticPhantom;
import edu.stanford.rsl.conrad.phantom.MECT;
import edu.stanford.rsl.conrad.phantom.renderer.ParallelProjectionPhantomRenderer;
import edu.stanford.rsl.conrad.phantom.renderer.PhantomRenderer;
import edu.stanford.rsl.conrad.physics.absorption.PolychromaticAbsorptionModel;
import edu.stanford.rsl.conrad.physics.detector.*;
import edu.stanford.rsl.conrad.utils.Configuration;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.ImagePlus;
import ij.measure.Calibration;
import IceInternal.Time;
import MR.spectrum_creator;

/**
 * encapsulates all options of projection _n indicates added noise _r indicates added rotation _t translation
 * @author rohleder
 */
enum projType{
	MATERIAL, POLY120, POLY80, 	// noise free, polychromatic spectrum @ 120 or 80kv
	POLY120n, POLY80n, 			// noisy/rotated/translated polychromatic spectrum @ 120 or 80kv
	POLY120nr, POLY80nr, POLY120nt, POLY80nt, POLY120rt, POLY80rt,
	POLY120nrt, POLY80nrt, 
	MONO; 						// monochromatic (deprecated)
}

/**
 * Static class.
 * use methods to create and visualize projections of analytical phantoms
 * @author Max
 *
 */
class projector{
	// CONSTANTS HYPERPARAMETERS
	static final boolean DEBUG = true;
	static final int NUMBER_OF_MATERIALS = 2; // bone and water or iodine and bone
	static final int LOWER_ENERGY = 81; // [kv] as in Mueller 2018, Fast kV switching
	static final int HIGHER_ENERGY = 125; // [kv] as in Mueller 2018, Fast kV switching
	
	// ZEEGO CONFIGURATION
	static final double width_mm = 381.92;
	static final double heigth_mm = 295.68;
	static final int width_px = 1240; // number of pixels
	static final int heigth_px = 960;
	static final double pxX_mm = 0.308;
	static final double pxY_mm = 0.308;
	
	// members
	public static ParallelProjectionPhantomRenderer phantom_renderer = new ParallelProjectionPhantomRenderer();
	
	public static void main(String[] args) {
		System.out.println("BEGIN TESTING");
		
		configure_Zeego();		
		
		// inspecting configuration
		if(DEBUG) inspect_global_conf();
		
		configure_Zeego(0.3);
		
		// inspecting configuration
		if(DEBUG) inspect_global_conf();
		
		// create phantom
		//AnalyticPhantom phantom = new MECT();
		
		// create projection image in 120 kv
		//Grid3D highEnergyProjections = create_projection(phantom, projType.POLY120);
		
		// create projection image in 80 kv
		//Grid3D lowEnergyProjections = create_projection(phantom, projType.POLY80);

		
		// visualize projection data
		//showGrid(highEnergyProjections);
		//showGrid(lowEnergyProjections);
		
		

		System.out.println("END TESTING");
	}
	
	/**
	 * helper visualizing a Grid3D
	 * @param highEnergyImg
	 */
	private static void showGrid(Grid3D highEnergyImg) {
		ImagePlus image = ImageUtil.wrapGrid3D(highEnergyImg, "POLY120");
		Calibration cal = image.getCalibration();
		cal.xOrigin = Configuration.getGlobalConfiguration().getGeometry().getOriginInPixelsX();
		cal.yOrigin = Configuration.getGlobalConfiguration().getGeometry().getOriginInPixelsY();
		cal.zOrigin = Configuration.getGlobalConfiguration().getGeometry().getOriginInPixelsZ();
		cal.pixelWidth = Configuration.getGlobalConfiguration().getGeometry().getVoxelSpacingX();
		cal.pixelHeight = Configuration.getGlobalConfiguration().getGeometry().getVoxelSpacingY();
		cal.pixelDepth = Configuration.getGlobalConfiguration().getGeometry().getVoxelSpacingZ();
		image.show();
	}
	/**
	 * creates projection domain images 
	 * @param t chooses the attenuation model. option listed in enum
	 * @param energy modulates spectrum in all non-material projections
	 */
	public static Grid3D create_projection(AnalyticPhantom phantom, projType t) {
		long projStart = Time.currentMonotonicTimeMillis();
		// prerequisites
		Configuration.loadConfiguration();
		Configuration conf = Configuration.getGlobalConfiguration();
		
		// set the detector type
		XRayDetector dect = createDetector(t);
		conf.setDetector(dect);
		
		// create a phantom at 80 and 120 kv
		Grid3D result = null;
		try {
			// try to circumvent that : phantom_renderer.configure();
			phantom_renderer.configure(phantom, dect);
			
			if(DEBUG) System.out.println("BEGIN RENDERING");
			result = PhantomRenderer.generateProjections(phantom_renderer);	
			if(DEBUG) System.out.println("END RENDERING");

		} catch (Exception e) {
			// could come from phantom_renderer.configure()
			e.printStackTrace();
			System.exit(1);
		}
		if(DEBUG) {
			System.out.println("projecting took " + (Time.currentMonotonicTimeMillis() - projStart) / 1000+ "s");
		}
		return result;
	}
	
	/**
	 * 
	 * @param t chooses between MaterialPathLengthDetector or PolyChromaticDetector with either 80 or 120 kv spectrum (see enum for options)
	 * @return fully configured detector
	 */
	private static XRayDetector createDetector(projType t) {
		long dectStart = Time.currentMonotonicTimeMillis();
		XRayDetector dect;
		switch(t) {
		  case MATERIAL:
			dect = new MaterialPathLengthDetector();
			if(DEBUG) System.out.println("configured detector mat"); 
		    break;
		  case POLY80:
			dect = new PolychromaticDetectorWithNoise();
			dect.setNameString("PolyChromaticDetector @ 80kv");
			//TODO set PolyChromaticSpectrum (maybe just load it.. generation takes long)
			PolychromaticAbsorptionModel mo80 = spectrum_creator.configureAbsorbtionModel(t);
			dect.setModel(mo80);
			if(DEBUG) System.out.println("configured detector poly80"); 
		    break;
		  case POLY120:
			dect = new PolychromaticDetectorWithNoise();
			dect.setNameString("PolyChromaticDetector @ 120kv");
			PolychromaticAbsorptionModel mo120 = spectrum_creator.configureAbsorbtionModel(t);
			dect.setModel(mo120);		
			if(DEBUG) System.out.println("configured detector poly120"); 
			break;
		  default:
			dect = new SimpleMonochromaticDetector();
			if(DEBUG) System.out.println("SOMETHING wrong monochromatic wasnt planned to work"); 
		}
		if(DEBUG) {
			System.out.println("Detector Creation : " + (Time.currentMonotonicTimeMillis() - dectStart) + "ms");
		}
		return dect;
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
		System.out.println("\tpixelsixes (y, x):\t" + conf.getGeometry().getPixelDimensionY() + ", " + conf.getGeometry().getPixelDimensionX() + " [mm]");
		System.out.println("\tsize (h, w):\t" + h + ", " + w + " [cm]");
		System.out.println("current X-Ray tube:\n\tvoltage:\t" + conf.getVoltage());
		System.out.println("\tcutOffFreq:\t" + conf.getCutOffFrequency());
		// geometrical circumstances
		Trajectory geo = conf.getGeometry();
		System.out.print("geometrical attributes:\n");
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
	/**
	 * set config to real parameters without downsampling
	 */
	public static void configure_Zeego() {
		
		// load global conf
		Configuration.loadConfiguration();
		Configuration conf = Configuration.getGlobalConfiguration();
		Trajectory geometry = conf.getGeometry();
		// set params
		geometry.setDetectorHeight(heigth_px);
		geometry.setDetectorWidth(width_px);
		geometry.setPixelDimensionX(pxX_mm);
		geometry.setPixelDimensionY(pxY_mm);
		// set global conf
		conf.setGeometry(geometry);
		Configuration.setGlobalConfiguration(conf);
		if(DEBUG) System.out.println("setting resolutiont to 100% zeego standard");
	}
	/**
	 * zeego detector has 1240x960 resolution. physical size is 381.92x295.68.
	 * @param percentage [0.1, 1] downsample percentage of pixels remaining (0.9 = 90% of pixels remain)
	 */
	public static void configure_Zeego(double percentage) {
		assert (percentage <= 1 && percentage >= 0.1);
		// real zeego parameters
		int width_px = 1240; // number of pixels
		int heigth_px = 960;
		int num_px = width_px*heigth_px;
		
		double ratio = ((double)heigth_px/(double)width_px);
		double pxX_mm = 0.308;
		double pxY_mm = 0.308;
		double width_mm = width_px*pxX_mm;
		double heigth_mm = heigth_px*pxY_mm;
		
		// calculate new parameters
		width_px = (int)(Math.round(Math.sqrt(percentage) * width_px));
		heigth_px = (int) Math.round(ratio * width_px);
				
		double pxX_mm_scaled = width_mm / width_px;
		double pxy_mm_scaled = heigth_mm / heigth_px;

		if(DEBUG) System.out.println("scaling to " + ((width_px*heigth_px)/num_px)*100 + "(" + percentage*100 + ")");
	
		// load global conf
		Configuration.loadConfiguration();
		Configuration conf = Configuration.getGlobalConfiguration();
		Trajectory geometry = conf.getGeometry();
		geometry.setDetectorHeight(690);
		
		// set params
		geometry.setDetectorHeight(heigth_px);
		geometry.setDetectorWidth(width_px);
		geometry.setPixelDimensionX(pxX_mm_scaled);
		geometry.setPixelDimensionY(pxy_mm_scaled);
		
		conf.setGeometry(geometry);
		Configuration.setGlobalConfiguration(conf);
	}
	
	public static String encodeSetting(projType t) {
		String s = "";
		return s;
	}
}
















