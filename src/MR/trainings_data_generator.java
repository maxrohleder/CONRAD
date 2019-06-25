package MR;

import java.util.concurrent.TimeUnit;

import IceInternal.Time;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.phantom.AnalyticPhantom;

class data_generator{
	public static String DATA_FOLDER = "I:\\bachelor\\trainings_data"; //TODO define a location
	public static int serial_number = 0;
	public static double scale = 0.008;
	public static int target_number = 10;
	
	public static void main(String[] args) throws InterruptedException {
		System.out.println("STARTING GENERATION OF " + target_number + " SAMPLES");
		long starttime = Time.currentMonotonicTimeMillis();
		double num_mins;
		for (int i = 0; i < 10; i++) {
			generate_sample(false, true);
			serial_number++;
			num_mins = ((double)(Time.currentMonotonicTimeMillis() - starttime))/60000;
			System.out.println("generated sample " + i + "/" + target_number + " (took " + String.format("%.2f", num_mins) + "min total)");
			TimeUnit.SECONDS.sleep(1);
		}
	}
	
	public static void generate_sample(boolean save, boolean show) {
		// TODO use methods in phantom_creator to generate a new random phantom 
		AnalyticPhantom phantom = phantom_creator.create_new_phantom(300, 400);
		
		// TODO use methods in projector of same package to create a pair of three images
		// 	 1. a Polychromatic projection with 80kv max Voltage (with noise/translation/rotation)
		// 	 2. " 120kv max Voltage (with n/t/r)
		// 	 3. a MaterialPathLength image which serves as ground truth for the training process
		projType[] sample_types = random_types();
		Grid3D[] sample_set = projector.generate_sample(phantom, sample_types, scale);
		
		// TODO save those images in a new folder in DATA_FOLDER
		if(save) save_sample(sample_set, "sample_" + serial_number);
		
		// show (for testing purposes)
		if(show) {
			projector.showGrid(sample_set[0], "sample_" + serial_number + "(0)");
			projector.showGrid(sample_set[1], "sample_" + serial_number + "(1)");
			projector.showGrid(sample_set[2], "sample_" + serial_number + "(2)");
		}

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
		DATA_FOLDER = write_path;
	}
}