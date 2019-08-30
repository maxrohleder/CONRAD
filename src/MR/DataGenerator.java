package MR;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.hamcrest.core.IsInstanceOf;

import IceInternal.Time;
import MR.projector.ParrallelFilteringThread;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.MultiChannelGrid2D;
import edu.stanford.rsl.conrad.parallel.ParallelThread;
import edu.stanford.rsl.conrad.parallel.ParallelThreadExecutor;
import edu.stanford.rsl.conrad.parallel.ParallelizableRunnable;
import edu.stanford.rsl.conrad.phantom.AnalyticPhantom;
import edu.stanford.rsl.conrad.utils.CONRAD;
import edu.stanford.rsl.conrad.utils.Configuration;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.ImageJ;


class DataGenerator{
	// ADAPT THESE FIELDS TO YOUR SYSTEM. (for details read the readme)
	public static String DATA_FOLDER = "/home/mr/Documents/bachelor/data/simulation";
	public static String CONFIG_FOLDER = DATA_FOLDER + "/config";
	public static int NUMBER_OF_SAMPLES = 1;
	private static int serial_number = 1;

	
	// system variables
	private static File root;
	private static File conf;
	
	// File names
	private static String MatConf = "MAT.xml";
	private static String Pol80Conf = "POLY80.xml";
	private static String Pol120Conf = "POLY120.xml";
	

	public static void main(String[] args) {
		root = new File(DATA_FOLDER);
		conf = new File(CONFIG_FOLDER);
		if(!root.isDirectory()) {
			if(!root.mkdirs()) {
				System.err.println("couldnt create root directory");
				System.err.println(root);
			}
		}
		if(!conf.isDirectory()) {
			if(!conf.mkdirs()) {
				System.err.println("couldnt create root directory");
				System.err.println(conf);
			}
		}
		
		// generate config files
		zeego.generateConfigFiles(CONFIG_FOLDER, MatConf, Pol80Conf, Pol120Conf);
		Grid3D mat, lowerEnergy, higherEnergy;
		
		System.out.println("STARTING GENERATION OF " + NUMBER_OF_SAMPLES + " SAMPLES");
		// lets get working baby!
		for (int i = 0; i < NUMBER_OF_SAMPLES; i++) {
			File sample_dir = nextFolder();
			
			// use methods in phantom_creator to generate a new random phantom 
//			AnalyticPhantom phantom = phantom_creator.createRandomPhantom(200, 200, 165);
			AnalyticPhantom phantom = phantom_creator.getEvaluationPhantom();
			
			// initialize the projector from files
			projector p = new projector(CONFIG_FOLDER, MatConf, Pol80Conf, Pol120Conf);
			
			// create and save material path length projections
			mat = p.computeMaterialGrids(phantom);
			saveSample(mat, sample_dir, p, projType.MATERIAL);
			
			// lower energy projections
			lowerEnergy = p.getPolchromaticImageFromMaterialGrid(mat, projType.POLY80);
			saveSample(lowerEnergy, sample_dir, p, projType.POLY80);

			// lower energy projections
			higherEnergy = p.getPolchromaticImageFromMaterialGrid(mat, projType.POLY120);
			saveSample(higherEnergy, sample_dir, p, projType.POLY120);
			}
		System.out.println("GENERATED " + NUMBER_OF_SAMPLES + " SAMPLES");
		new ImageJ();
	}
	
	/**
	 * creates a new folder in format specified in readme (mmddhhmmss_<serial_number>)
	 * @param millies timestamp to create folder of
	 * @return Path object containing a newly created directory according to current time
	 */
	private static File nextFolder() {
		// packs current time in wrapper class
		Calendar creation_time = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");
		File folder = new File(root, dateFormat.format(creation_time.getTime()) + "_" + serial_number);
		if(!folder.mkdirs()) {
			System.err.println("[data_generator.next_folder]couldnt create folder:\n\t" + folder.toString());
			System.exit(0);
		}
		serial_number++;
		return folder;
	}


	private static void saveSample(Grid3D data, File folder, projector pro, projType p) {
		File filename = null;
		String rawSizeFormat = data.getSize()[0] + "x" + data.getSize()[1] + "x" + data.getSize()[2];
		if(p == projType.MATERIAL) {
			if(data.getSubGrid(0) instanceof MultiChannelGrid2D) {
				int s[] = data.getSize();
				Grid3D water = new Grid3D(s[0], s[1], s[2]);
				Grid3D iodine = new Grid3D(s[0], s[1], s[2]);
				// map the channel information onto water and iodine parts
				pro.split(water, iodine, data);
				File waterFolder = new File(folder, "water");
				File iodineFolder = new File(folder, "iodine");
				if(waterFolder.mkdirs()) saveSample(water, waterFolder, pro, p);
				if(iodineFolder.mkdirs()) saveSample(iodine, iodineFolder, pro, p);
				return; // nothing to save.
			} else {
				filename = new File(folder, "MAT_"+rawSizeFormat+".raw");
			}
		} else if(p == projType.POLY80){
			filename = new File(folder, "POLY80_"+rawSizeFormat+".raw");
		} else {
			filename = new File(folder, "POLY120_"+rawSizeFormat+".raw");
		}
		ImageUtil.saveAs(data, filename.getAbsolutePath());
	}

}
	
