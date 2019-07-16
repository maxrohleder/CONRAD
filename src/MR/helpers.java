package MR; 

import edu.stanford.rsl.conrad.phantom.AnalyticPhantom;
import edu.stanford.rsl.conrad.physics.PolychromaticXRaySpectrum;
import edu.stanford.rsl.conrad.physics.absorption.PolychromaticAbsorptionModel;
import edu.stanford.rsl.conrad.utils.UserUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import MR.data_generator;

/**
 * helper class to generate different spectra or entire models. POLY120 and POLY80 are tailored to Zeego setup in 06/2019
 * @author rohleder
 *
 */
class generate_simulations{
	public static void main(String []args) {
		
		data_generator gen = new data_generator(200, (int) 690/4, (int) 1240/4);
				
		Path output_path = Paths.get("/home/max/Documents/Stanford/bachelor/data");	
		
		AnalyticPhantom phantom = phantom_creator.create_new_phantom(300,  400);
		
		gen.generate_samples(phantom, output_path, true);
				
		System.out.println(Files.exists(output_path) + " " + Files.isDirectory(output_path));
	}
}

/*
 * USEFULL STUFF
 * 			boolean a = UserUtil.queryBoolean("<YES or NO question>?");
 * 			edu.stanford.rsl.conrad.io.STLFileUtil // safe prioritizable scene to stl
 * 			edu.stanford.rsl.conrad.utils.VisualizationUtil // show grids and plots


 */


