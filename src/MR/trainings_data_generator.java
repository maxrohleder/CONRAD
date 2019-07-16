package MR;

import java.nio.file.Path;
import java.nio.file.Files;

import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.phantom.AnalyticPhantom;

/**
 * This class generates and saves a number of threefold samples (80, 120, mat) of a random phantom and saves them in a folder named phantom_<SERIALNUMBER>/
 * @author max
 *
 */
class data_generator{

	//public double scale;
	public int[] resolution = new int[2]; // y, x
	public int NUMBER_OF_PROJECTIONS;
	// static variables
	private static projType[] noiseless = {projType.POLY80, projType.POLY120, projType.MATERIAL};
	private static projType[] noisy = {projType.POLY80n, projType.POLY120n, projType.MATERIAL};
	
	public data_generator() {
		this(200, 690, 1240);
	}
	
	public data_generator(int number_of_projections, int y, int x) {
		this.NUMBER_OF_PROJECTIONS = number_of_projections;
		this.resolution[0]= y;
		this.resolution[1] = x;
	}
	
	/**
	 * 
	 * @param phantom analytic phantom to project
	 * @param folder to safe all samples of one projection to (must be created before calling this method!)
	 * @param noise select noisy or noiseless projection
	 * @return time in seconds
	 */
	public long generate_samples(AnalyticPhantom phantom, Path folder, boolean noise) {
		if (!(Files.exists(folder) && Files.isDirectory(folder))){
			System.err.println("[data_generator] path doesnt exist or is not a directory");
			System.exit(0);
		}
		// use methods in projector of same package to create a pair of three images
		// 	 1. a Polychromatic projection with 80kv max Voltage
		// 	 2. " 120kv max Voltage
		// 	 3. a MaterialPathLength image which serves as ground truth for the training process
		long t = System.currentTimeMillis(); // time surveing
		
		projType[] sample_types = (noise) ? noisy : noiseless;
		projector p = new projector();
		projector.configure_Zeego(this.resolution[0], this.resolution[1]);

		Grid3D p1 = p.create_projection(phantom, sample_types[0]);
		// save here later
		projector.showGrid(p1, folder.toString());

		return System.currentTimeMillis() - t;
	}
	

	private static void save_sample(Grid3D[] data, Path folder) {
		System.out.println("sample saved in folder: " + folder.toString());
		// TODO safe grids using CONRAD
		projector.showGrid(data[0], folder.toString());
	}
}
