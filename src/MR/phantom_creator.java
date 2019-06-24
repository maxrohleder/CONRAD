package MR;

import edu.stanford.rsl.conrad.phantom.AnalyticPhantom;
import edu.stanford.rsl.conrad.phantom.MECT;

class phantom_creator{
	// creates new random phantom in bounds given
	public static AnalyticPhantom create_new_phantom(int x_bound, int y_bound) {
		// TODO Add capability of creating random geometries
		return new MECT();
	}
}