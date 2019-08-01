package MR;

import edu.stanford.rsl.conrad.utils.Configuration;
import edu.stanford.rsl.conrad.utils.RegKeys;
import edu.stanford.rsl.conrad.filtering.CosineWeightingTool;
import edu.stanford.rsl.conrad.filtering.ImageFilteringTool;
import edu.stanford.rsl.conrad.filtering.RampFilteringTool;
import edu.stanford.rsl.conrad.filtering.redundancy.ParkerWeightingTool;
import edu.stanford.rsl.conrad.geometry.Projection.CameraAxisDirection;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;
import edu.stanford.rsl.conrad.geometry.trajectories.CircularTrajectory;
import edu.stanford.rsl.conrad.geometry.trajectories.Trajectory;
import edu.stanford.rsl.conrad.io.ImagePlusDataSink;
import edu.stanford.rsl.conrad.numerics.SimpleVector;
import edu.stanford.rsl.conrad.phantom.AnalyticPhantom;
import edu.stanford.rsl.conrad.physics.PolychromaticXRaySpectrum;
import edu.stanford.rsl.conrad.physics.absorption.PolychromaticAbsorptionModel;
import edu.stanford.rsl.conrad.physics.detector.MaterialPathLengthDetector;
import edu.stanford.rsl.conrad.physics.detector.SimplePolychromaticDetector;
import edu.stanford.rsl.conrad.physics.detector.XRayDetector;
import edu.stanford.rsl.conrad.physics.materials.database.MaterialsDB;
import edu.stanford.rsl.conrad.reconstruction.VOIBasedReconstructionFilter;


public class zeego {
	// DEFAULT CONFIG PATH
	public static final String rootDirPath = "/home/mr/Documents/bachelor/data/simulation/config";
	public static final String matConfFilename = "MAT.xml";		// relative to configDir
	public static final String poly80ConfFilename = "POLY80.xml";
	public static final String poly120ConfFilename = "POLY120.xml";
	
	/**
	 * generates three config files in the above specified location
	 * @param args unused
	 */
	public static void main(String args[]) {
		// modify all necessary parameters and save configs for mat, poly80 and poly120
		generateConfigFiles(rootDirPath, matConfFilename, poly80ConfFilename, poly120ConfFilename);
		// to target folder
		System.out.println("done!");
	}
	
	/**
	 * generates three config files in the above specified location
	 * @param args unused
	 */
	public static void generateConfigFiles(String configDir, String m, String p80, String p120) {
		// save material projection rendering config
		saveDefaultConfig(configDir + "/" + m, projType.MATERIAL);
		
		//save detector settings for polychromatic spectral projections
		saveDefaultConfig(configDir + "/" + p80, projType.POLY80);
		saveDefaultConfig(configDir + "/" + p120, projType.POLY120);


	}
	
	// PROJECTION SETTINGS
	static final int number_of_projections = 10;
	static final int NUMBER_OF_MATERIALS = 2; // bone and water or iodine and bone
	static final String MATERIALS[] = {"bone", "iodine"};
	static final int LOWER_ENERGY = 80; // [kv] as in Mueller 2018, Fast kV switching
	static final int HIGHER_ENERGY = 120; // [kv] as in Mueller 2018, Fast kV switching

	// REAL DETECTOR CONFIGURATION
	static final double width_mm = 381.92; // should always remain the same
	static final double heigth_mm = 295.68;
	static final int width_px = 1240; // number of pixels
	static final int heigth_px = 960;
	static final double pxX_mm = 0.308;
	static final double pxY_mm = 0.308;	

	
	// DETECTOR DOWNSAMPLED BY 1/4
	static final int width_px_s = 310; // pixels downsampled
	static final int heigth_px_s = 240;
	static final double pxX_mm_s = width_mm/width_px_s; // detector size kept the same by increasing pixel size
	static final double pxY_mm_s = heigth_mm/heigth_px_s;

	private static double stad = 600.0;
	private static double deltaAng = 1.0;
	private static double dectOffsetX = 0.0;
	private static double dectOffsetY = 0.0;
	private static CameraAxisDirection uDirection = CameraAxisDirection.DETECTORMOTION_PLUS;
	private static CameraAxisDirection vDirection = CameraAxisDirection.ROTATIONAXIS_PLUS;
	private static SimpleVector rotationAxis = new SimpleVector(0.0, 0.0, 1.0);


	/**
	 * zeego detector has 1240x960 resolution. physical size is 381.92mm x 295.68mm.
	 * set CONRAD configuration to real parameters without downsampling
	 */
	public static void configure() {
		// calculate pixel dimensions to match physical zeego size
		double pxX_mm_scaled = width_mm / width_px_s;
		double pxy_mm_scaled = heigth_mm / heigth_px_s;
		// load global conf
		Configuration.loadConfiguration();
		Configuration conf = Configuration.getGlobalConfiguration();
		Trajectory geometry = conf.getGeometry();
		// set detector params
		geometry.setDetectorHeight(heigth_px_s);
		geometry.setDetectorWidth(width_px_s);
		geometry.setPixelDimensionX(pxX_mm_scaled);
		geometry.setPixelDimensionY(pxy_mm_scaled);
		geometry.setNumProjectionMatrices(number_of_projections);
			
		// set volume params
		//geometry.setOriginInPixelsX(-100);
		//geometry.setOriginInPixelsY(-100);
		//geometry.setOriginInPixelsZ(-100);
		geometry.setOriginInWorld(new PointND(100, 100, 83));
		if(cnfg.DEBUG) System.out.println("set origin to (" + geometry.getOriginInPixelsX()
											+ ", " + geometry.getOriginInPixelsY()
											+ ", " + geometry.getOriginInPixelsZ() + ") (x, y, z)");
		if(cnfg.DEBUG) System.out.println("world origin is (" + geometry.getOriginX()
											+ ", " + geometry.getOriginY()
											+ ", " + geometry.getOriginZ() + ") (x, y, z)");
		
		conf.setGeometry(geometry);
		Configuration.setGlobalConfiguration(conf);
		if(cnfg.DEBUG) System.out.println("scaling resolution to (" + heigth_px_s + ", " + width_px_s + ") (h, w)");
	}
	

	
	/**
	 * set CONRAD detector resolution while maintaining zeego real world size
	 * @param width detector (resolution)
	 * @param heigth detector
	 * @param numProj
	 */
	public static void configure(int w, int h, int numProj, AnalyticPhantom phantom) {
		// calculate pixel dimensions to match physical zeego size
		double pxX_mm_scaled = width_mm / w;
		double pxy_mm_scaled = heigth_mm / h;
		// load global conf
		Configuration.loadConfiguration();
		Configuration conf = Configuration.getGlobalConfiguration();
		Trajectory geometry = conf.getGeometry();
		// set detector params
		geometry.setDetectorHeight(h);
		geometry.setDetectorWidth(w);
		geometry.setPixelDimensionX(pxX_mm_scaled);
		geometry.setPixelDimensionY(pxy_mm_scaled);
		geometry.setNumProjectionMatrices(numProj);
			
		// set volume params
		//geometry.setOriginInPixelsX(-100);
		//geometry.setOriginInPixelsY(-100);
		//geometry.setOriginInPixelsZ(-100);
		//geometry.setOriginInWorld(new PointND(100, 100, 100));
		geometry.setOriginToPhantomCenter(phantom);
		if(cnfg.DEBUG) System.out.println("set origin to (" + geometry.getOriginInPixelsX()
											+ ", " + geometry.getOriginInPixelsY()
											+ ", " + geometry.getOriginInPixelsZ() + ") (x, y, z)");
		if(cnfg.DEBUG) System.out.println("world origin is (" + geometry.getOriginX()
											+ ", " + geometry.getOriginY()
											+ ", " + geometry.getOriginZ() + ") (x, y, z)");
		
		// compute projection matrices
		CircularTrajectory new_geo = new CircularTrajectory(geometry);
		new_geo.setTrajectory(	number_of_projections, 
								stad,  			// source axis distance
								deltaAng, 		// angular increment
								dectOffsetX, 	// default is zero
								dectOffsetY, 	// default is zero
								uDirection, 	// enum defined in CircularTrajectory
								vDirection,		// enum defined in CircularTrajectory
								rotationAxis);	// SimpleVector (default is z axis (0, 0, 1))
		
		conf.setGeometry(new_geo);
		Configuration.setGlobalConfiguration(conf);
		if(cnfg.DEBUG) System.out.println("scaling resolution to (" + h + ", " + w + ") (h, w)");
	}
	
	public static void default_configuration(int numProj, AnalyticPhantom phantom) {
		configure(width_px, heigth_px, numProj, phantom);
	}
	
	public static void downsampled_configuration(int numProj, AnalyticPhantom phantom) {
		configure(width_px_s, heigth_px_s, numProj, phantom);
	}
	
	public static void custom_configuration(int w, int h, int numProj, AnalyticPhantom phantom) {
		configure(w, h, numProj, phantom);
	}

	/**
	 * zeego detector has 1240x960 resolution. physical size is 381.92x295.68.
	 * @param percentage [0.1, 1] downsample percentage of pixels remaining (0.9 = 90% of pixels remain)
	 */
	public static void configure(double percentage) {
		assert (percentage <= 1 && percentage >= 0.1);
		// real zeego parameters
		int num_px = width_px*heigth_px;
		
		double ratio = ((double)heigth_px/(double)width_px);
		double width_mm = width_px*pxX_mm;
		double heigth_mm = heigth_px*pxY_mm;
		
		// calculate new parameters
		int width_px_new = (int)(Math.round(Math.sqrt(percentage) * width_px));
		int heigth_px_new = (int) Math.round(ratio * width_px_new);
				
		double pxX_mm_scaled = width_mm / width_px_new;
		double pxy_mm_scaled = heigth_mm / heigth_px_new;

		if(cnfg.DEBUG) System.out.println("scaling to " + (((double)width_px_new*(double)heigth_px_new)/num_px)*100 + "% (target: " + percentage*100 + "%)");
	
		// load global conf
		Configuration.loadConfiguration();
		Configuration conf = Configuration.getGlobalConfiguration();
		Trajectory geometry = conf.getGeometry();
		geometry.setDetectorHeight(690);
		
		// set params
		geometry.setDetectorHeight(heigth_px_new);
		geometry.setDetectorWidth(width_px_new);
		geometry.setPixelDimensionX(pxX_mm_scaled);
		geometry.setPixelDimensionY(pxy_mm_scaled);
		
		// compute projection matrices by copy constructing
		Trajectory new_geo = new Trajectory(geometry);
		
		conf.setGeometry(new_geo);
		Configuration.setGlobalConfiguration(conf);
	}
	
	/**
	 * adapted from Configuration.initConfig()
	 * @param material 
	 */
	public static void saveDefaultConfig(String filename, projType ptype) {
		Configuration config = new Configuration();
		Trajectory geo = config.getGeometry();
		if(ptype == projType.POLY120) {
			
		}
		config.setDetector(createDetector(ptype));
		geo = new Trajectory();
		geo.setDetectorUDirection(CameraAxisDirection.DETECTORMOTION_PLUS);
		geo.setDetectorVDirection(CameraAxisDirection.ROTATIONAXIS_PLUS);
		geo.setDetectorHeight(480);
		geo.setDetectorWidth(620);
		geo.setSourceToAxisDistance(600.0);
		geo.setSourceToDetectorDistance(1200.0);
		geo.setReconDimensions(256, 256, 256);
		geo.setPixelDimensionX(1);
		geo.setPixelDimensionY(1);
		geo.setVoxelSpacingX(1.0);
		geo.setVoxelSpacingY(1.0);
		geo.setVoxelSpacingZ(1.0);
		geo.setAverageAngularIncrement(1.0);
		geo.setProjectionStackSize(200);
		config.setGeometry(geo);
		config.resetRegistry();
		config.setRegistryEntry(RegKeys.PATH_TO_CALIBRATION, "C:\\calibration");
		config.setRegistryEntry(RegKeys.OPENCL_DEVICE_SELECTION, "GeForce GTX 1660 Ti NVIDIA Corporation");
		config.setRegistryEntry(RegKeys.PATH_TO_CONTROL, "C:\\control");
		config.setRegistryEntry(RegKeys.MAX_THREADS, "10");
		config.setRegistryEntry(RegKeys.XCAT_PATH, "E:\\phantom data\\numeric phantoms\\xcat\\NCAT2.0_PC");
		ImageFilteringTool[] standardPipeline = new ImageFilteringTool[] {
				new CosineWeightingTool(),
				new ParkerWeightingTool(),
				new RampFilteringTool(),
				new VOIBasedReconstructionFilter()
		};
		config.setFilterPipeline(standardPipeline);
//		config.setFilterPipeline(ImageFilteringTool.getFilterTools());
		config.setCitationFormat(Configuration.MEDLINE_CITATION_FORMAT);
		config.setSink(new ImagePlusDataSink());
		
		int numProjectionMatrices = config.getGeometry().getProjectionStackSize();
		double sourceToAxisDistance = config.getGeometry().getSourceToAxisDistance();
		double averageAngularIncrement = config.getGeometry().getAverageAngularIncrement();
		double detectorOffsetU = config.getGeometry().getDetectorOffsetU();
		double detectorOffsetV = config.getGeometry().getDetectorOffsetV();
		CameraAxisDirection uDirection = config.getGeometry().getDetectorUDirection();
		CameraAxisDirection vDirection = config.getGeometry().getDetectorVDirection();
		SimpleVector rotationAxis = new SimpleVector(0, 0, 1);
		Trajectory geom = new CircularTrajectory(config.getGeometry());
		geom.setSecondaryAngleArray(null);
		((CircularTrajectory)geom).setTrajectory(numProjectionMatrices, sourceToAxisDistance, averageAngularIncrement, detectorOffsetU, detectorOffsetV, uDirection, vDirection, rotationAxis);
		if (geom != null){
			config.setGeometry(geom);
		}
		Configuration.setGlobalConfiguration(config);
		Configuration.saveConfiguration(Configuration.getGlobalConfiguration(), filename);
	}
	
	/**
	 * configures a detector and attenuation type(which will influence the outcome image)
	 * @param t chooses between MaterialPathLengthDetector or PolyChromaticDetector with either 80 or 120 kv spectrum (see enum for options)
	 * @return fully configured detector
	 */
	private static XRayDetector createDetector(projType t) {
		XRayDetector dect;
		if( t == projType.MATERIAL) {
			MaterialPathLengthDetector dectm = new MaterialPathLengthDetector();
			dectm.setNumberOfMaterials(NUMBER_OF_MATERIALS);
			dectm.setConfigured(true);
			dectm.init();
			dectm.setNames(MATERIALS);
			dect = dectm;
			if(cnfg.DEBUG) System.out.println("configured detector mat"); 
		}
		else {

			dect = new SimplePolychromaticDetector();
			dect.setNameString("Simple PolyChromaticDetector");
			if(cnfg.DEBUG) System.out.println("configured simple detector " + t); 

			PolychromaticAbsorptionModel mo = configureAbsorbtionModel(t);
			dect.setModel(mo);
		}
		return dect;
	}
	
	/**
	 * creates spectra modeled after zeego tube at 80 or 120 kv peak voltage
	 * @param t
	 * @return zeego spectrum 
	 */
	public static PolychromaticXRaySpectrum create_zeego_spectrum(projType t) {
		
		// material projection types have enum ordinal < 6 (first 6 are material based)
		if(	t == projType.MATERIAL ) {
			// should never enter the spectrum creator as material projection is desired
			System.err.println("material images dont need a polychromatic spectrum");
			System.exit(1);
		}
		// now create the spectrum with either 80 or 120 kv max voltage
		PolychromaticXRaySpectrum s;
		if(	t == projType.POLY80 ) {
			s = new PolychromaticXRaySpectrum(10, 150, 1, LOWER_ENERGY, "W", 2.5, 1.2, 12, 0, 0, 0, 2.5);
		}else {
			s = new PolychromaticXRaySpectrum(10, 150, 1, HIGHER_ENERGY, "W", 2.5, 1.2, 12, 0, 0, 0, 2.5);
		}
		return s;
	}
	
	/**
	 * configures an AbsorptionModel to equip an detector with.
	 * @param t
	 * @return Absorption Model fittet to Zeego
	 */
	public static PolychromaticAbsorptionModel configureAbsorbtionModel(projType t) {
		PolychromaticAbsorptionModel mo = new PolychromaticAbsorptionModel();
		PolychromaticXRaySpectrum  spectrum = spectrum_creator.create_zeego_spectrum(t);
		mo.setInputSpectrum(spectrum);
		return mo;
	}
}
