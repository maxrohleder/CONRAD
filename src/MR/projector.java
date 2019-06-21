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
 * encapsulates all options of projection
 * @author rohleder
 */
enum attenType{
	MATERIAL, POLY120, POLY80, MONO;
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
	
	// members
	public static ParallelProjectionPhantomRenderer phantom_renderer = new ParallelProjectionPhantomRenderer();
	
	public static void main(String[] args) {
		System.out.println("BEGIN TESTING");
		
		// inspecting configuration
		if(DEBUG) inspect_global_conf();
		
		// create phantom
		AnalyticPhantom phantom = new MECT();
		
		// create projection image in 120 kv
		Grid3D highEnergyProjections = create_projection(phantom, attenType.POLY120);
		
		// visualize projection data
		showGrid(highEnergyProjections);
		
		

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
	public static Grid3D create_projection(AnalyticPhantom phantom, attenType t) {
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
	private static XRayDetector createDetector(attenType t) {
		long dectStart = Time.currentMonotonicTimeMillis();
		XRayDetector dect;
		switch(t) {
		  case MATERIAL:
			dect = new MaterialPathLengthDetector();
			if(DEBUG) System.out.println("configured detector mat"); 
		    break;
		  case POLY80:
			dect = new PolychromaticDetectorWithNoise();
			//TODO set PolyChromaticSpectrum (maybe just load it.. generation takes long)
			PolychromaticAbsorptionModel mo80 = spectrum_creator.configureAbsorbtionModel(t);
			dect.setModel(mo80);
			if(DEBUG) System.out.println("configured detector poly80"); 
		    break;
		  case POLY120:
			dect = new PolychromaticDetectorWithNoise();
			//TODO set PolyChromaticSpectrum maybe set configured to true
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
		System.out.println("current detector:\n\tname:\t\t" + conf.getDetector());
		System.out.println("\tdims (h, w):\t" + pxY + ", " + pxX + " [px]");
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
}