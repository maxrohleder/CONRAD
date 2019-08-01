package MR;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.hamcrest.core.IsInstanceOf;

import IceInternal.Time;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.MultiChannelGrid2D;
import edu.stanford.rsl.conrad.phantom.AnalyticPhantom;
import edu.stanford.rsl.conrad.utils.Configuration;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import ij.ImageJ;


class data_generator{
	// ADAPT THESE FIELDS TO YOUR SYSTEM. (for details read the readme)
	public static String DATA_FOLDER = "/home/mr/Documents/bachelor/data/simulation";
	public static String CONFIG_FOLDER = DATA_FOLDER + "/config";
	public static int NUMBER_OF_SAMPLES = 4;
	
	// system variables
	private static File root;
	private static File conf;
	private static int serial_number = 0;
	
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
		
		// if there are already existing files, modify starting point
		serial_number = 8;
		
		System.out.println("STARTING GENERATION OF " + NUMBER_OF_SAMPLES + " SAMPLES");
		// lets get working baby!
		for (int i = 0; i < NUMBER_OF_SAMPLES; i++) {
			File sample_dir = next_folder();
			
			// use methods in phantom_creator to generate a new random phantom 
			AnalyticPhantom phantom = phantom_creator.create_new_phantom(300, 400);
			
			// initialize the projector from files
			projector p = new projector(CONFIG_FOLDER, MatConf, Pol80Conf, Pol120Conf);
			
			// create and save material path length projections
			mat = p.computeMaterialGrids(phantom);
			save_sample(mat, sample_dir, projType.MATERIAL);
			
			// lower energy projections
			lowerEnergy = p.getPolchromaticImageFromMaterialGrid(mat, projType.POLY80);
			save_sample(lowerEnergy, sample_dir, projType.POLY80);

			// lower energy projections
			higherEnergy = p.getPolchromaticImageFromMaterialGrid(mat, projType.POLY120);
			save_sample(higherEnergy, sample_dir, projType.POLY120);
			}
		System.out.println("GENERATED " + NUMBER_OF_SAMPLES + " SAMPLES");
		new ImageJ();
	}
	
	/**
	 * creates a new folder in format specified in readme (mmddhhmmss_<serial_number>)
	 * @param millies timestamp to create folder of
	 * @return Path object containing a newly created directory according to current time
	 */
	private static File next_folder() {
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


	private static void save_sample(Grid3D data, File folder, projType p) {
		File filename = null;
		String rawSizeFormat = data.getSize()[0] + "x" + data.getSize()[1] + "x" + data.getSize()[2];
		if(p == projType.MATERIAL) {
			if(data.getSubGrid(0) instanceof MultiChannelGrid2D) {
				int numChannels = ((MultiChannelGrid2D) data.getSubGrid(0)).getNumberOfChannels();
				int s[] = data.getSize();
				Grid3D splitted = new Grid3D(s[0], s[1], s[2]);
				for(int c = 0; c < numChannels; ++c) {
					for (int z = 0; z < data.getSize()[2]; z++) {
						Grid2D channelc = ((MultiChannelGrid2D) data.getSubGrid(z)).getChannel(c);
						assert (!(channelc instanceof MultiChannelGrid2D)); // avoid recursion deadlock
						splitted.setSubGrid(z, channelc);
					}
					// recursively save channels in new folders
					File f = new File(folder, ((MultiChannelGrid2D) data.getSubGrid(0)).getChannelNames()[c]);
					if(f.mkdirs()) save_sample(splitted, f, p);
				}
				// dont save the Grid3D with all channels
				return;
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