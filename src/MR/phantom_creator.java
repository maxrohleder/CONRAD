package MR;

import edu.stanford.rsl.conrad.phantom.AnalyticPhantom;
import edu.stanford.rsl.conrad.phantom.MECT;
import MR.cnfg;

class phantom_creator{
	// creates new random phantom in bounds given
	public static AnalyticPhantom create_new_phantom(int x_bound, int y_bound) {
		// TODO Add capability of creating random geometries
		if(cnfg.DEBUG) System.out.println("printing");
		return new MECT();
	}
}