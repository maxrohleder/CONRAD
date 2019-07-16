package MR;

import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;
import edu.stanford.rsl.conrad.geometry.trajectories.Trajectory;
import edu.stanford.rsl.conrad.opencl.OpenCLMaterialPathLengthPhantomRenderer;
import edu.stanford.rsl.conrad.opencl.OpenCLProjectionPhantomRenderer;
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
 * Static class.
 * use methods to create and visualize projections of analytical phantoms
 * @author Max
 *
 */
class projector{
	
	// CONSTANT HYPERPARAMETERS
	static final boolean DEBUG = true;
	static boolean is_configured = false;
	static final int number_of_projections = 200;

	// ZEEGO CONFIGURATION
	static final double width_mm = 381.92;
	static final double heigth_mm = 295.68;
	static final int width_px = 1240; // number of pixels
	static final int heigth_px = 960;
	static final double pxX_mm = 0.308;
	static final double pxY_mm = 0.308;
	
	// ZEEGO CONFIGURATION downsampled to 1/4
	static final int width_px_s = 310; // number of pixels
	static final int heigth_px_s = 240;
	static final double pxX_mm_s = width_mm/width_px_s;
	static final double pxY_mm_s = heigth_mm/heigth_px_s;
	
	// this objects renderer 
	projType prot;
	boolean useGPU;
	public ParallelProjectionPhantomRenderer parrallel_cpu_renderer;
	public OpenCLMaterialPathLengthPhantomRenderer gpu_mat_renderer;
	public OpenCLProjectionPhantomRenderer gpu_renderer;
	private static PhantomRenderer phantom_renderer; // motherclass of all renderers
	
	public projector(boolean use_gpu, projType pro) {
		this.prot = pro;
		this.useGPU = use_gpu;
		if(use_gpu && pro == projType.MATERIAL) {
			// use opencl material renderer
			System.err.println("[projector] not implemented yet");
			System.exit(0);
		} else if(use_gpu && pro != projType.MATERIAL){
			// use the opencl projector
			Configuration.loadConfiguration();
			this.gpu_renderer = new OpenCLProjectionPhantomRenderer();
		} else if(!use_gpu && pro == projType.MATERIAL){
			// use Material Path Length renderer
			System.err.println("[projector] not implemented yet");
			System.exit(0);
		} else {
			// use parrallel renderer
			parrallel_cpu_renderer = new ParallelProjectionPhantomRenderer();
		}
		//if(cnfg.DEBUG) System.out.println("using " + parrallel_cpu_renderer + gpu_mat_renderer + gpu_renderer);
	}
	
	public static void main(String[] args) {
		System.out.println("BEGIN TESTING");
		
		// create phantom and renderer
		AnalyticPhantom phantom = new MECT();
		projector cpu_renderer = new projector(true, projType.POLY120);
			
		configure_Zeego(width_px, heigth_px, number_of_projections, phantom);
		if(DEBUG) inspect_global_conf();

		// create cpu projection image in 120 kv
		Grid3D highEnergyProjections = cpu_renderer.create_projection(phantom);
		if(DEBUG) inspect_global_conf();

		
		// visualize projection data
		helpers.showGrid(highEnergyProjections, "Poly120_test");
		//showGrid(lowEnergyProjections);
		
		

		System.out.println("END TESTING");
	}
	
	/**
	 * creates projection domain images 
	 * @param t chooses the attenuation model. option listed in enum
	 * @param energy modulates spectrum in all non-material projections
	 */
	public Grid3D create_projection(AnalyticPhantom phantom) {
		Grid3D result = null;
		long projStart = Time.currentMonotonicTimeMillis();
		// prerequisites
		Configuration.loadConfiguration();
		Configuration conf = Configuration.getGlobalConfiguration();
		
		if(this.useGPU && this.prot != projType.MATERIAL) {
			// load the phantom
			this.gpu_renderer.setPhantom(phantom);
			try {
				// now select decive and greate opencl context
				this.gpu_renderer.configure();
				// computation (has some output per default)
				result = PhantomRenderer.generateProjections(this.gpu_renderer);
			} catch (Exception e) {
				// oops
				e.printStackTrace();
			}
		} else if(!this.useGPU && this.prot != projType.MATERIAL) {
			// set the detector type
			XRayDetector dect = createDetector(this.prot);
			conf.setDetector(dect);
			
			// create a phantom at 80 and 120 kv
			try {
				parrallel_cpu_renderer.configure(phantom, dect);
				
				if(DEBUG) System.out.println("BEGIN RENDERING");
				result = PhantomRenderer.generateProjections(this.parrallel_cpu_renderer);	
				if(DEBUG) System.out.println("END RENDERING");
	
			} catch (Exception e) {
				// could come from phantom_renderer.configure()
				e.printStackTrace();
				System.exit(1);
			}
		}
		if(DEBUG) {
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
		XRayDetector dect;
		if( t == projType.MATERIAL) {
			dect = new MaterialPathLengthDetector();
			// TODO set number of Materials
			if(DEBUG) System.out.println("configured detector mat"); 
		}
		else {
			if(	t == projType.POLY80 || t == projType.POLY120) {
				dect = new SimplePolychromaticDetector();
				dect.setNameString("Simple PolyChromaticDetector");
				if(DEBUG) System.out.println("configured simple detector " + t); 
			}
			else {
				dect = new PolychromaticDetectorWithNoise();
				dect.setNameString("Noisy PolyChromaticDetector");
				if(DEBUG) System.out.println("configured noisy detector " + t); 
			}
			PolychromaticAbsorptionModel mo = spectrum_creator.configureAbsorbtionModel(t);
			dect.setModel(mo);
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
	/**
	 * set config to real parameters without downsampling
	 */
	public static void configure_Zeego() {
		// calculate pixel dimensions to match physical zeego size
		double pxX_mm_scaled = projector.width_mm / width_px_s;
		double pxy_mm_scaled = projector.heigth_mm / heigth_px_s;
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
	 * @param width detector (resolution)
	 * @param heigth detector
	 * @param Adx pixel x dimension in mm
	 * @param Ady pixel y dimension in mm
	 * @param numProj
	 */
	public static void configure_Zeego(int w, int h, int numProj, AnalyticPhantom phantom) {
		// calculate pixel dimensions to match physical zeego size
		double pxX_mm_scaled = projector.width_mm / w;
		double pxy_mm_scaled = projector.heigth_mm / h;
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
		
		conf.setGeometry(geometry);
		Configuration.setGlobalConfiguration(conf);
		if(cnfg.DEBUG) System.out.println("scaling resolution to (" + h + ", " + w + ") (h, w)");
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

		if(DEBUG) System.out.println("scaling to " + (((double)width_px_new*(double)heigth_px_new)/num_px)*100 + "% (target: " + percentage*100 + "%)");
	
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
		
		conf.setGeometry(geometry);
		Configuration.setGlobalConfiguration(conf);
	}
}
















