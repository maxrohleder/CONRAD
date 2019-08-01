package MR;

import java.util.HashMap;

import edu.stanford.rsl.conrad.phantom.AnalyticPhantom;
import edu.stanford.rsl.conrad.phantom.MECT;

class phantom_creator{
	// creates new random phantom in bounds given
	public static AnalyticPhantom create_new_phantom(int x_bound, int y_bound) {
		// TODO Add capability of creating random geometries
		HashMap<Integer, String> rodConfiguration = getBoneConfig();
		return new MECT(rodConfiguration);
	}

	private static HashMap<Integer, String> getBoneConfig() {
		HashMap<Integer, String> conf = new HashMap<Integer, String>();
		conf.put(20, "bone"); 
		conf.put(21, "bone");
		conf.put(22, "bone");
		conf.put(23, "bone");
		conf.put(24, "bone");
		conf.put(25, "bone");
		conf.put(26, "bone");
		conf.put(27, "bone");
		conf.put(1, "bone");		
		conf.put(2, "bone");		
		return conf;
	}
	
	private static boolean createCustomMaterials() {
		
		return true;
	}
}