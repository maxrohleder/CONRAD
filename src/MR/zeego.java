package MR;

import edu.stanford.rsl.conrad.utils.Configuration;
import edu.stanford.rsl.conrad.geometry.Projection.CameraAxisDirection;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;
import edu.stanford.rsl.conrad.geometry.trajectories.CircularTrajectory;
import edu.stanford.rsl.conrad.geometry.trajectories.Trajectory;
import edu.stanford.rsl.conrad.numerics.SimpleVector;
import edu.stanford.rsl.conrad.phantom.AnalyticPhantom;


public class zeego {
	// PROJECTION SETTINGS
	static final int number_of_projections = 200;

	// REAL DETECTOR CONFIGURATION
	static final double width_mm = 381.92; // should always remain the same
	static final double heigth_mm = 295.68;
	static final int width_px = 1240; // number of pixels
	static final int heigth_px = 960;
	static final double pxX_mm = 0.308;
	static final double pxY_mm = 0.308;	

	
	// DETECTOR DOWNSAMPLED BY 1/4
	static final int width_px_s = 310; // pixels downsampled
	static final int heigth_px_s = 240;
	static final double pxX_mm_s = width_mm/width_px_s; // detector size kept the same by increasing pixel size
	static final double pxY_mm_s = heigth_mm/heigth_px_s;

	private static double stad = 600.0;
	private static double deltaAng = 1.0;
	private static double dectOffsetX = 0.0;
	private static double dectOffsetY = 0.0;
	private static CameraAxisDirection uDirection = CameraAxisDirection.DETECTORMOTION_PLUS;
	private static CameraAxisDirection vDirection = CameraAxisDirection.ROTATIONAXIS_PLUS;
	private static SimpleVector rotationAxis = new SimpleVector(0.0, 0.0, 1.0);

	/**
	 * zeego detector has 1240x960 resolution. physical size is 381.92mm x 295.68mm.
	 * set CONRAD configuration to real parameters without downsampling
	 */
	public static void configure() {
		// calculate pixel dimensions to match physical zeego size
		double pxX_mm_scaled = width_mm / width_px_s;
		double pxy_mm_scaled = heigth_mm / heigth_px_s;
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
	 * set CONRAD detector resolution while maintaining zeego real world size
	 * @param width detector (resolution)
	 * @param heigth detector
	 * @param numProj
	 */
	public static void configure(int w, int h, int numProj, AnalyticPhantom phantom) {
		// calculate pixel dimensions to match physical zeego size
		double pxX_mm_scaled = width_mm / w;
		double pxy_mm_scaled = heigth_mm / h;
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
		
		// compute projection matrices
		CircularTrajectory new_geo = new CircularTrajectory(geometry);
		new_geo.setTrajectory(	number_of_projections, 
								stad,  			// source axis distance
								deltaAng, 		// angular increment
								dectOffsetX, 	// default is zero
								dectOffsetY, 	// default is zero
								uDirection, 	// enum defined in CircularTrajectory
								vDirection,		// enum defined in CircularTrajectory
								rotationAxis);	// SimpleVector (default is z axis (0, 0, 1))
		
		conf.setGeometry(new_geo);
		Configuration.setGlobalConfiguration(conf);
		if(cnfg.DEBUG) System.out.println("scaling resolution to (" + h + ", " + w + ") (h, w)");
	}
	
	public static void default_configuration(int numProj, AnalyticPhantom phantom) {
		configure(width_px, heigth_px, numProj, phantom);
	}
	
	public static void downsampled_configuration(int numProj, AnalyticPhantom phantom) {
		configure(width_px_s, heigth_px_s, numProj, phantom);
	}
	
	public static void custom_configuration(int w, int h, int numProj, AnalyticPhantom phantom) {
		configure(w, h, numProj, phantom);
	}

	/**
	 * zeego detector has 1240x960 resolution. physical size is 381.92x295.68.
	 * @param percentage [0.1, 1] downsample percentage of pixels remaining (0.9 = 90% of pixels remain)
	 */
	public static void configure(double percentage) {
		assert (percentage <= 1 && percentage >= 0.1);
		// real zeego parameters
		int num_px = width_px*heigth_px;
		
		double ratio = ((double)heigth_px/(double)width_px);
		double width_mm = width_px*pxX_mm;
		double heigth_mm = heigth_px*pxY_mm;
		
		// calculate new parameters
		int width_px_new = (int)(Math.round(Math.sqrt(percentage) * width_px));
		int heigth_px_new = (int) Math.round(ratio * width_px_new);
				
		double pxX_mm_scaled = width_mm / width_px_new;
		double pxy_mm_scaled = heigth_mm / heigth_px_new;

		if(cnfg.DEBUG) System.out.println("scaling to " + (((double)width_px_new*(double)heigth_px_new)/num_px)*100 + "% (target: " + percentage*100 + "%)");
	
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
		
		// compute projection matrices by copy constructing
		Trajectory new_geo = new Trajectory(geometry);
		
		conf.setGeometry(new_geo);
		Configuration.setGlobalConfiguration(conf);
	}
}
