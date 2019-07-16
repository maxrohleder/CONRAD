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

import java.io.IOException;

import IceInternal.Time;
import MR.spectrum_creator;


/**
 * Static class.
 * use methods to create and visualize projections of analytical phantoms
 * @author Max
 *
 */
class projector{
	
	// CONSTANTS HYPERPARAMETERS
	static boolean is_configured = false;

	// ZEEGO CONFIGURATION
	static final double width_mm = 381.92;
	static final double heigth_mm = 295.68;
	static final int width_px = 1240; // number of pixels
	static final int heigth_px = 960;
	static final double pxX_mm = 0.308;
	static final double pxY_mm = 0.308;
	
	// members
	private ParallelProjectionPhantomRenderer phantom_renderer;
	
	public projector() {
		this.phantom_renderer = new ParallelProjectionPhantomRenderer();
	}
	
	public static void main(String[] args) {
		System.out.println("BEGIN TESTING");
		
		// set detector to 8% of the resolution as normal
		//configure_Zeego(0.08);
		configure_Zeego((int)690/4, (int) 1240/4);
		
		// inspecting configuration
		if(cnfg.DEBUG) inspect_global_conf();
		
		// create phantom
		AnalyticPhantom phantom = new MECT();
		
		// create projection image in 120 kv
		projector p = new projector();
		System.out.println("starting projection");
		Grid3D highEnergyProjections = p.create_projection(phantom, projType.POLY80n);
		
		// create projection image in 80 kv
		//Grid3D lowEnergyProjections = create_projection(phantom, projType.POLY80);

		
		// visualize projection data
		showGrid(highEnergyProjections, "Poly120_test");
		//showGrid(lowEnergyProjections);
		
		

		System.out.println("END TESTING");
	}
	
	/**
	 * generate one sample set of two training and one target image
	 * @param phantom	Analytic phantom to project
	 * @param t			type of desired projection
	 * @param scale		scale parameter in range from 0.01 to 1 (default 0.08)
	 * @return			three images in Grid3D representation
	 */
	public Grid3D[] generate_sample(AnalyticPhantom phantom, projType[] t, double scale) {
		assert(t.length == 3);
		if (!is_configured) configure_Zeego(scale);
		// creating the projections
		Grid3D[] sample_set = new Grid3D[3];
		sample_set[0] = create_projection(phantom, t[0]);
		sample_set[1] = create_projection(phantom, t[1]);
		sample_set[2] = create_projection(phantom, t[2]);
		return sample_set;
	}
	
	/**
	 * helper visualizing a Grid3D
	 * @param img
	 */
	public static void showGrid(Grid3D img, String name) {
		ImagePlus image = ImageUtil.wrapGrid3D(img, name);
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
	public Grid3D create_projection(AnalyticPhantom phantom, projType t) {
		long projStart = Time.currentMonotonicTimeMillis();
		// prerequisites
		Configuration.loadConfiguration();
		Configuration conf = Configuration.getGlobalConfiguration();
		
		// set the detector type
		XRayDetector dect = createDetector(t);
		conf.setDetector(dect);
		if(cnfg.DEBUG) inspect_global_conf();
		
		
		// create a phantom at 80 and 120 kv
		Grid3D result = null;
		try {
			// try to circumvent that : phantom_renderer.configure();
			phantom_renderer.configure(phantom, dect);
			
			if(cnfg.DEBUG) System.out.println("BEGIN RENDERING");
			result = PhantomRenderer.generateProjections(phantom_renderer);	
			if(cnfg.DEBUG) System.out.println("END RENDERING");

		} catch (Exception e) {
			// could come from phantom_renderer.configure()
			e.printStackTrace();
			System.exit(1);
		}
		if(cnfg.DEBUG) {
			System.out.println("projecting took " + (Time.currentMonotonicTimeMillis() - projStart) / 1000+ "s");
		}
		return result;
	}
	
	/**
	 * configures a detector and attenuation type(which will influence the outcome image)
	 * @param t chooses between MaterialPathLengthDetector or PolyChromaticDetector with either 80 or 120 kv spectrum (see enum for options)
	 * @return fully configured detector
	 */
	private static XRayDetector createDetector(projType t) {
		long dectStart = Time.currentMonotonicTimeMillis();
		XRayDetector dect = null;
		switch(t) {
		  case MATERIAL:
			// NOT IMPLEMENTED PROPERLY
			dect = new MaterialPathLengthDetector();
			if(cnfg.DEBUG) System.out.println("configured detector mat"); 
		    break;
		  case POLY80:
			  System.err.println("[projector] POLY80 not implemented yet");
			  System.exit(0);
			  break;
		  case POLY80n:
			dect = new PolychromaticDetectorWithNoise();
			dect.setNameString("noisy PolyChromaticDetector @ 80kv");
			//TODO set PolyChromaticSpectrum (maybe just load it.. generation takes long)
			PolychromaticAbsorptionModel mo80 = spectrum_creator.configureAbsorbtionModel(t);
			dect.setModel(mo80);
			if(cnfg.DEBUG) System.out.println("configured detector poly80n"); 
		    break;
		  case POLY120:
			  System.err.println("[projector] POLY120 not implemented yet");
			  System.exit(0);
			  break;
		  case POLY120n:
			dect = new PolychromaticDetectorWithNoise();
			dect.setNameString("noisy PolyChromaticDetector @ 120kv");
			PolychromaticAbsorptionModel mo120 = spectrum_creator.configureAbsorbtionModel(t);
			dect.setModel(mo120);		
			if(cnfg.DEBUG) System.out.println("configured detector poly120n"); 
			break;
		  default:
			// NOT IMPLEMENTED PROPERLY
			dect = new SimpleMonochromaticDetector();
			if(cnfg.DEBUG) System.out.println("SOMETHING wrong monochromatic wasnt planned to work"); 
		}
		if(cnfg.DEBUG) {
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
		if(cnfg.DEBUG) System.out.println("setting resolution to 100% zeego standard");
	}
	/**
	 * zeego detector has 1240x960 resolution. physical size is 381.92x295.68.
	 * @param percentage [0.1, 1] downsample percentage of pixels remaining (0.9 = 90% of pixels remain)
	 */
	public static void configure_Zeego(double percentage) {
		assert (percentage <= 1 && percentage >= 0.1);
		// real zeego parameters
		int num_px = projector.width_px*projector.heigth_px;
		
		double ratio = ((double)projector.heigth_px/(double)projector.width_px);
		double width_mm = projector.width_px*projector.pxX_mm;
		double heigth_mm = projector.heigth_px*projector.pxY_mm;
		
		// calculate new parameters
		int width_px_new = (int)(Math.round(Math.sqrt(percentage) * projector.width_px));
		int heigth_px_new = (int) Math.round(ratio * width_px_new);
				
		double pxX_mm_scaled = width_mm / width_px_new;
		double pxy_mm_scaled = heigth_mm / heigth_px_new;

		if(cnfg.DEBUG) System.out.println("scalingr resolution to " + (((double)width_px_new*(double)heigth_px_new)/num_px)*100 + "% (target: " + percentage*100 + "%)");
	
		// load global conf
		Configuration.loadConfiguration();
		Configuration conf = Configuration.getGlobalConfiguration();
		Trajectory geometry = conf.getGeometry();
		
		// set params
		geometry.setDetectorHeight(heigth_px_new);
		geometry.setDetectorWidth(width_px_new);
		geometry.setPixelDimensionX(pxX_mm_scaled);
		geometry.setPixelDimensionY(pxy_mm_scaled);
		
		conf.setGeometry(geometry);
		Configuration.setGlobalConfiguration(conf);
	}
	
	/**
	 * sets detector resolution in pixels
	 * @param h	y detector resolution 	(reality 960)
	 * @param w detector width in px	(reality 1240)
	 */
	public static void configure_Zeego(int h, int w) {

		double pxX_mm_scaled = projector.width_mm / w;
		double pxy_mm_scaled = projector.heigth_mm / h;

		if(cnfg.DEBUG) System.out.println("scaling resolution to (" + h + ", " + w + ") (h, w)");
	
		// load global conf
		Configuration.loadConfiguration();
		Configuration conf = Configuration.getGlobalConfiguration();
		Trajectory geometry = conf.getGeometry();
		
		// set params
		geometry.setDetectorHeight(h);
		geometry.setDetectorWidth(w);
		geometry.setPixelDimensionX(pxX_mm_scaled);
		geometry.setPixelDimensionY(pxy_mm_scaled);
		
		conf.setGeometry(geometry);
		Configuration.setGlobalConfiguration(conf);
	}
	
	public static String encodeSetting(projType t) {
		String s = "";
		switch(t) {
		case MATERIAL:
			s = "mat_";
			break;
		case MATERIALn:
			s = "mat_n_";
			break;
		case POLY120:
			s = "poly120_";
			break;
		case POLY120n:
			s = "poly120_n_";
			break;
		case POLY80:
			s = "poly80_";
			break;
		case POLY80n:
			s = "poly80_n_";
			break;
		default:
			s = "UNIDENTIFIED";
			break;
		}
		return s;
	}
}
















