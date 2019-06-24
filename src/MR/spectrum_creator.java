package MR;

import edu.stanford.rsl.conrad.physics.PolychromaticXRaySpectrum;
import edu.stanford.rsl.conrad.physics.absorption.PolychromaticAbsorptionModel;

/**
 * helper class to generate different spectra or entire models. POLY120 and POLY80 are tailored to Zeego setup in 06/2019
 * @author rohleder
 *
 */
class spectrum_creator{
	// CONSTANTS HYPERPARAMETERS
	static final boolean DEBUG = true;
	static final int NUMBER_OF_MATERIALS = 2; // bone and water or iodine and bone
	static final int LOWER_ENERGY = 81; // [kv] as in Mueller 2018, Fast kV switching
	static final int HIGHER_ENERGY = 125; // [kv] as in Mueller 2018, Fast kV switching
	public static PolychromaticXRaySpectrum create_zeego_spectrum(projType t) {
		
		PolychromaticXRaySpectrum s;
		switch(t) {
		  case POLY80:
			  s = new PolychromaticXRaySpectrum(10, 150, 1, LOWER_ENERGY, "W", 2.5, 1.2, 12, 0, 0, 0, 2.5);
			  break;
		  case POLY120:
			  s = new PolychromaticXRaySpectrum(10, 150, 1, HIGHER_ENERGY, "W", 2.5, 1.2, 12, 0, 0, 0, 2.5);
			  break;
		  default:
			  s = new PolychromaticXRaySpectrum();
			  break;
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
}








