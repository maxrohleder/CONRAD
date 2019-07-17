package MR;

import java.util.concurrent.TimeUnit;

import IceInternal.Time;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.phantom.AnalyticPhantom;

class data_generator{
	// static variables
	public static String DATA_FOLDER = "I:\\bachelor\\data\\training\\simulation"; //TODO define a location
	public static int serial_number = 0;
	public static double scale = 0.08;
	public static int[] resolution = new int[] {960/4, 1240/4}; // 4th of zeego res
	public static int target_number = 1;
	private static projType[] noiseless = {projType.POLY80, projType.POLY120, projType.MATERIAL};
	private static projType[] noisy = {projType.POLY80n, projType.POLY120n, projType.MATERIAL};
	
	public static void main(String[] args) {
		System.out.println("STARTING GENERATION OF " + target_number + " SAMPLES");
		set_data_path("I:\\bachelor\\data\\training\\simulation");
		
		long starttime = Time.currentMonotonicTimeMillis();
		double num_mins;
		for (int i = 0; i < target_number; i++) {
			// use methods in phantom_creator to generate a new random phantom 
			AnalyticPhantom phantom = phantom_creator.create_new_phantom(300, 400);
			// create and save projections of this phantom
			generate_sample(phantom, false, true, true);
			// increase serial number (for folder structure)
			serial_number++;
			num_mins = ((double)(Time.currentMonotonicTimeMillis() - starttime))/60000;
			if(cnfg.DEBUG) System.out.println("generated sample " + i + "/" + target_number + 
					" (took " + String.format("%.2f", num_mins) + "min total)");
		}
	}
	/**
	 * generates all projections of phantom and either saves or shows it.
	 * @param phantom projection source
	 * @param save true false
	 * @param show true false
	 * @param n enable noisy projection
	 */
	public static void generate_sample(AnalyticPhantom phantom, boolean save, boolean show, boolean n) {

		
		// TODO use methods in projector of same package to create a pair of three images
		// 	 1. a Polychromatic projection with 80kv max Voltage (with noise/translation/rotation)
		// 	 2. " 120kv max Voltage (with n/t/r)
		// 	 3. a MaterialPathLength image which serves as ground truth for the training process
		projType[] sample_types = (n) ? noisy : noiseless;
		Grid3D[] sample_set = new Grid3D[3];
		
		projector.configure_Zeego(pro, heigth, numProj, phantom);
		sample_set[0] = project_sample(phantom, sample_types[0], scale);
		
		// TODO save those images in a new folder in DATA_FOLDER
		if(save) save_sample(sample_set, "sample_" + serial_number);
		
		// show (for testing purposes)
		if(show) {
			helpers.showGrid(sample_set[0], "sample_" + serial_number + "(0)");
			helpers.showGrid(sample_set[1], "sample_" + serial_number + "(1)");
			helpers.showGrid(sample_set[2], "sample_" + serial_number + "(2)");
		}

	}
	
	/**
	 * generate one sample set of two training and one target image
	 * @param phantom	Analytic phantom to project
	 * @param t			type of desired projection
	 * @param scale		scale parameter in range from 0.01 to 1 (default 0.08)
	 * @return			three images in Grid3D representation
	 */
	public static Grid3D project_sample(AnalyticPhantom phantom, projType t, double scale) {
		projector.configure_Zeego(scale);
		// creating the projections
		Grid3D sample_set = projector.create_projection(phantom, t);
		return sample_set;
	}
	
	private static projType[] random_types() {
		// TODO add support for rotation translation and noise
		projType[] rantypes = {projType.POLY80, projType.POLY120, projType.MATERIAL};
		return rantypes;
	}

	private static void save_sample(Grid3D[] data, String foldername) {
		System.out.println("sample saved in folder: " + foldername);
		// TODO safe grids using CONRAD?
	}
	
	public static void set_data_path(String write_path) {
		if(projector.DEBUG) System.out.println("new datapath is:/n/t" + DATA_FOLDER);
		DATA_FOLDER = write_path;
	}
	
	public static String encodeSetting(projType t) {
		String s = "";
		switch(t) {
		case MATERIAL:
			s = "mat";
			break;
		case POLY120:
			s = "poly120";
			break;
		case POLY120n:
			s = "poly120_n";
			break;
		case POLY80:
			s = "poly80";
			break;
		case POLY80n:
			s = "poly80_n";
			break;
		default:
			s = "UNIDENTIFIED";
			break;
		}
		return s;
	}
}