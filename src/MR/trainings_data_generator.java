package MR;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.concurrent.TimeUnit;

import IceInternal.Time;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.phantom.AnalyticPhantom;


class data_generator{
	// ADAPT THESE FIELDS TO YOUR SYSTEM. (for details read the readme)
	public static String DATA_FOLDER = "I:\\bachelor\\data\\training\\simulation";
	public static int X = 1240; // original resolution on the zeego detector in RSL is 1240x960 px
	public static int Y = 960;
	public static int NUMBER_OF_SAMPLES = 1;
	public static int NUMBER_OF_MATERIALS = 2;
	
	// system variables
	private static Path root;
	private static int serial_number = 0;
	private static double scale = 0.08;
	private static int[] resolution = new int[] {Y, X}; // 4th of zeego res
	private static int target_number = NUMBER_OF_SAMPLES;
	
	// static system constants
	private static final projType[] noiseless = {projType.POLY80, projType.POLY120, projType.MATERIAL};
	private static final projType[] noisy = {projType.POLY80n, projType.POLY120n, projType.MATERIAL};
	
	public static void main(String[] args) {
		System.out.println("STARTING GENERATION OF " + target_number + " SAMPLES");
		root = Paths.get(DATA_FOLDER);
		
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
	 * creates a new folder in format specified in readme (mmddhhmmss_<serial_number>)
	 * @param millies timestamp to create folder of
	 * @return Path object containing a newly created directory according to current time
	 */
	private static Path next_folder(Date creation) {
		String creation_formattet = creation.getMonth();
		Path folder = Path.of(creation.toString());
		return root.resolve(folder);
	}


	private static void save_sample(Grid3D[] data, String foldername) {
		System.out.println("sample saved in folder: " + foldername);
		// TODO safe grids using CONRAD?
	}

}