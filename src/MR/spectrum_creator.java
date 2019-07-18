package MR;

import edu.stanford.rsl.conrad.physics.PolychromaticXRaySpectrum;
import edu.stanford.rsl.conrad.physics.absorption.PolychromaticAbsorptionModel;
import edu.stanford.rsl.conrad.physics.detector.MaterialPathLengthDetector;
import edu.stanford.rsl.conrad.physics.detector.PolychromaticDetectorWithNoise;
import edu.stanford.rsl.conrad.physics.detector.SimplePolychromaticDetector;
import edu.stanford.rsl.conrad.utils.VisualizationUtil;
import ij.ImageJ;
import ij.gui.Plot;

/**
 * helper class to generate different spectra or entire models. POLY120 and POLY80 are tailored to Zeego setup in 06/2019
 * @author rohleder
 *
 */
class spectrum_creator{
	// CONSTANTS HYPERPARAMETERS
	static final int NUMBER_OF_MATERIALS = 2; // bone and water or iodine and bone
	static final int LOWER_ENERGY = 80; // [kv] as in Mueller 2018, Fast kV switching
	static final int HIGHER_ENERGY = 120; // [kv] as in Mueller 2018, Fast kV switching
	/**
	 * creates spectra modeled after zeego tube at 80 or 120 kv peak voltage
	 * @param t
	 * @return zeego spectrum 
	 */
	public static PolychromaticXRaySpectrum create_zeego_spectrum(projType t) {
		
		// material projection types have enum ordinal < 6 (first 6 are material based)
		if(	t == projType.MATERIAL ) {
			// should never enter the spectrum creator as material projection is desired
			System.err.println("material images dont need a polychromatic spectrum");
			System.exit(1);
		}
		// now create the spectrum with either 80 or 120 kv max voltage
		PolychromaticXRaySpectrum s;
		if(	t == projType.POLY80 	 || 
			t == projType.POLY80n) {
			s = new PolychromaticXRaySpectrum(10, 150, 1, LOWER_ENERGY, "W", 2.5, 1.2, 12, 0, 0, 0, 2.5);
		}else {
			s = new PolychromaticXRaySpectrum(10, 150, 1, HIGHER_ENERGY, "W", 2.5, 1.2, 12, 0, 0, 0, 2.5);
		}
		return s;
	}
	
	/**
	 * configures an AbsorptionModel to equip an detector with.
	 * @param t
	 * @return Absorption Model fittet to Zeego
	 */
	public static PolychromaticAbsorptionModel configureAbsorbtionModel(projType t) {
		PolychromaticAbsorptionModel mo = new PolychromaticAbsorptionModel();
		PolychromaticXRaySpectrum  spectrum = spectrum_creator.create_zeego_spectrum(t);
		mo.setInputSpectrum(spectrum);
		return mo;
	}
	
	/**
	 * creates plot of PolychromaticXRaySpectrum
	 * @param p PolychromaticXRaySpectrum to visualize
	 * @param name
	 */
	public static void vizualizeSpectrum(PolychromaticXRaySpectrum spectra[], String name[]) {
		double max_value = -1;
		int min = (int) spectra[0].getMin();
		int max = (int) spectra[0].getMax();
		int delta = (int) spectra[0].getDelta();
		double[][] values = new double[spectra.length][(max-min)/delta];
		
		for (int i = 0; i < spectra.length; i++) {
			PolychromaticXRaySpectrum p = spectra[i];
			
			int counter = 0;
			for (int j = (int) p.getMin(); j < (int) p.getMax(); j += p.getDelta()) {
				values[i][counter] = p.getIntensity(j);
				max_value = (max_value>values[i][counter]) ? max_value : values[i][counter];
				counter++;
			}
		}
		new ImageJ();
		for (int i = 0; i < spectra.length; i++) {
			Plot spectrum = VisualizationUtil.createPlot(name[i], values[i]);
			// TODO set same yaxis scale
			//VisualizationUtil.createPlot(ybar, values[i], title, xLabel, yLabel)
			//spectrum.y
			spectrum.show();
		}
	}
	
	public static void main(String []args) {
		vizualizeSpectrum(new PolychromaticXRaySpectrum[] {create_zeego_spectrum(projType.POLY120n), 
			create_zeego_spectrum(projType.POLY80n)}, new String[] {"POLY120n", "POLY80n"});
	}
}








