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
import edu.stanford.rsl.conrad.utils.RegKeys;
import ij.ImagePlus;
import ij.measure.Calibration;
import IceInternal.Time;
import MR.spectrum_creator;


/**
 * Static class. Wraps 4 different renderer. All Children of PhantomRenderer.
 * use methods to create and visualize projections of analytical phantoms
 * @author Max
 *
 */
class projector{

	// rendering options
	projType prot;
	boolean useGPU;
	int number_materials; // initialised at construction time
	String[] materials = {"bone", "iodine"}; // default is bone and iodine

	// different renderers 
	public ParallelProjectionPhantomRenderer parrallel_cpu_renderer;
	public OpenCLMaterialPathLengthPhantomRenderer gpu_mat_renderer;
	public OpenCLProjectionPhantomRenderer gpu_renderer;
	private static PhantomRenderer phantom_renderer; // motherclass of all renderers
	
	public projector(boolean use_gpu, projType pro) {
		// variable init
		this.prot = pro;
		this.useGPU = use_gpu;
		this.number_materials = materials.length;
		// choose renderer based on params
		if(use_gpu && pro == projType.MATERIAL) {
			// use opencl material renderer
			System.err.println("[projector] opencl material constructor not implemented yet");
			System.exit(0);
		} else if(use_gpu && pro != projType.MATERIAL){
			// use the opencl attenuation based projector
			Configuration.loadConfiguration();
			this.gpu_renderer = new OpenCLProjectionPhantomRenderer();
		} else if(!use_gpu && pro == projType.MATERIAL){
			// use Material Path Length renderer
			System.err.println("[projector] paralell material constructor not implemented yet");
			System.exit(0);
		} else {
			// use parrallel attenuation based renderer
			parrallel_cpu_renderer = new ParallelProjectionPhantomRenderer();
		}
		if(use_gpu) {
			assert RegKeys.RENDER_PHANTOM_VOLUME_AUTO_CENTER == "true";
			assert RegKeys.OPENCL_DEVICE_SELECTION == "2"; // select device there
		}
		//if(cnfg.DEBUG) System.out.println("using " + parrallel_cpu_renderer + gpu_mat_renderer + gpu_renderer);
	}
	
	public static void main(String[] args) {
		
		// create phantom and renderer
		AnalyticPhantom phantom = new MECT();
		projector p = new projector(true, projType.POLY120);
		
		// tailor projection params to zeego
		//zeego.downsampled_configuration(200, phantom);
		zeego.custom_configuration(1240, 960, 200, phantom);

		// create cpu projection image in 120 kv
		Grid3D highEnergyProjections = p.create_projection(phantom);
		
		// visualize projection data
		helpers.showGrid(highEnergyProjections, "Poly120_test");
		//showGrid(lowEnergyProjections);
		
	}
	
	/**
	 * creates projection domain images with the renderer specified in constructor
	 * @param energy modulates spectrum in all non-material projections
	 */
	public Grid3D create_projection(AnalyticPhantom phantom) {
		Grid3D result = null;
		long projStart = Time.currentMonotonicTimeMillis();
		// prerequisites
		Configuration.loadConfiguration();
		Configuration conf = Configuration.getGlobalConfiguration();
		
		if(this.useGPU && this.prot == projType.MATERIAL) {
			// USING THE OPENCL MATERIAL PATH LENGTH RENDERER
			// TODO implement
			System.err.println("[projector.create_projection] opencl material renderer not implemented");
			System.exit(0);
		} 
		else if(this.useGPU && this.prot != projType.MATERIAL) {
			// USING THE OPENCL ATTENUATION RENDERER
			// load the phantom
			this.gpu_renderer.setPhantom(phantom);
			try {
				// now select decive and greate opencl context
				this.gpu_renderer.configure();
				// check config before projection
				if (cnfg.DEBUG) inspect_global_conf();
				// computation (has some output per default)
				result = PhantomRenderer.generateProjections(this.gpu_renderer);
			} catch (Exception e) {
				// renderer config failed
				e.printStackTrace();
				System.exit(1);
			}
		} 
		else if(!this.useGPU && this.prot == projType.MATERIAL) {
			// USING THE MATERIAL PATH LENGTH DETECTOR AND PARALLEL RENDERER
			// TODO implement!
			System.err.println("[projector.createprojection] material renderer not implemented");
			System.exit(0);
		}else {
			// USING THE ATTENUATION BASED PARALLEL RENDERER
			// set the detector type and attenuation model (120 or 80)
			XRayDetector dect = createDetector(this.prot);
			conf.setDetector(dect);
			
			// create a phantom at either 80 or 120 kv or material images
			try {
				// set model and configure worker threads
				parrallel_cpu_renderer.configure(phantom, dect);
				// check config before projection
				if (cnfg.DEBUG) inspect_global_conf();
				// TODO find a way to dump config file to folder as well
				result = PhantomRenderer.generateProjections(this.parrallel_cpu_renderer);		
			} catch (Exception e) {
				// could come from phantom_renderer.configure()
				e.printStackTrace();
				System.exit(1);
			}
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
	private XRayDetector createDetector(projType t) {
		long dectStart = Time.currentMonotonicTimeMillis();
		XRayDetector dect;
		if( t == projType.MATERIAL) {
			MaterialPathLengthDetector dectm = new MaterialPathLengthDetector();
			dectm.setNumberOfMaterials(number_materials);
			dectm.setConfigured(true);
			dectm.init();
			dectm.setNames(materials);
			dect = dectm;
			if(cnfg.DEBUG) System.out.println("configured detector mat"); 
		}
		else {
			if(	t == projType.POLY80 || t == projType.POLY120) {
				dect = new SimplePolychromaticDetector();
				dect.setNameString("Simple PolyChromaticDetector");
				if(cnfg.DEBUG) System.out.println("configured simple detector " + t); 
			}
			else {
				dect = new PolychromaticDetectorWithNoise();
				dect.setNameString("Noisy PolyChromaticDetector");
				if(cnfg.DEBUG) System.out.println("configured noisy detector " + t); 
			}
			PolychromaticAbsorptionModel mo = spectrum_creator.configureAbsorbtionModel(t);
			dect.setModel(mo);
		}
		if(cnfg.DEBUG) {
			System.out.println("Detector Creation : " + (Time.currentMonotonicTimeMillis() - dectStart) + "ms");
		}
		return dect;
	}
	
	public void setMaterials(String[] mats) {
		this.materials = mats;
		this.number_materials = mats.length;
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
















