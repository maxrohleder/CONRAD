package MR;

import edu.stanford.rsl.conrad.geometry.trajectories.Trajectory;
import edu.stanford.rsl.conrad.phantom.MECT;
import edu.stanford.rsl.conrad.physics.detector.MaterialPathLengthDetector;
import edu.stanford.rsl.conrad.utils.Configuration;

class projection_testing{
	// CONSTANTS
	static final boolean DEBUG = true;
	static final int NUMBER_OF_MATERIALS = 2;
	
	public static void main(String[] args) {
		System.out.println("BEGIN TESTING");
		
		// prerequisites
		Configuration.loadConfiguration();
		Configuration conf = Configuration.getGlobalConfiguration();
		
		//create a phantom of segments
		
		
		// inspecting configuration
		if(DEBUG) inspect_conf(conf);	
		
		// creating a material path length detector
		MaterialPathLengthDetector matDect = new MaterialPathLengthDetector();
		conf.setDetector(matDect);
		
		System.out.println("END TESTING");
	}
	
	public static void inspect_conf(Configuration conf) {
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