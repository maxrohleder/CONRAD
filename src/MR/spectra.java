package MR;

import edu.stanford.rsl.conrad.physics.PolychromaticXRaySpectrum;
import edu.stanford.rsl.conrad.physics.absorption.PolychromaticAbsorptionModel;
import edu.stanford.rsl.conrad.utils.VisualizationUtil;
import ij.gui.Plot;


class spectrum_creator{
	// CONSTANTS HYPERPARAMETERS
	static final boolean DEBUG = true;
	static final int NUMBER_OF_MATERIALS = 2; // bone and water or iodine and bone
	static final int LOWER_ENERGY = 81; // [kv] as in Mueller 2018, Fast kV switching
	static final int HIGHER_ENERGY = 125; // [kv] as in Mueller 2018, Fast kV switching
	public static PolychromaticXRaySpectrum create_zeego_spectrum_from_params(projType t) {
		
		PolychromaticXRaySpectrum s;
		switch(t) {
		  case  POLY80n:
		  case POLY80:
			  s = new PolychromaticXRaySpectrum(10, 150, 1, LOWER_ENERGY, "W", 2.5, 1.2, 12, 0, 0, 0, 2.5);
			  break;
		  case POLY120n:
		  case POLY120:
			  s = new PolychromaticXRaySpectrum(10, 150, 1, HIGHER_ENERGY, "W", 2.5, 1.2, 12, 0, 0, 0, 2.5);
			  break;
		  default:
			  if(cnfg.DEBUG) System.err.println("[spectrum_creator] spectrum set to default");
			  s = new PolychromaticXRaySpectrum();
			  break;
		}
		return s;
	}
	
	/**
	 * configures an AbsorptionModel to equip a detector with.
	 * @param t
	 * @return Absorption Model fittet to Zeego
	 */
	public static PolychromaticAbsorptionModel configureAbsorbtionModel(projType t) {
	
		PolychromaticAbsorptionModel mo = new PolychromaticAbsorptionModel();
		PolychromaticXRaySpectrum  spectrum = spectrum_creator.create_zeego_spectrum_from_params(t);
		mo.setInputSpectrum(spectrum);
		return mo;
	}
	
	public static void vizualizeSpectrum(PolychromaticXRaySpectrum p, String name) {
		
		System.out.println("min: " + p.getMin() + " max:" + p.getMax() + 
				" delta: " + p.getDelta() + " peak: " + p.getPeakVoltage());
		int min = (int) p.getMin();
		int max = (int) p.getMax();
		int delta = (int) p.getDelta();
		double[] values = new double[max-min/delta];
		
		
		int counter = 0;
		double max_value = -1;
		for (int i = (int) p.getMin(); i < (int) p.getMax(); i += p.getDelta()) {
			values[counter] = p.getIntensity(i);
			System.out.println(values[counter]);
			max_value = (max_value>values[counter]) ? max_value : values[counter];
			counter++;
		}
		Plot spectrum = VisualizationUtil.createPlot(name, values);
		spectrum.show();
	}
	
	public static void main(String []args) {
		vizualizeSpectrum(create_zeego_spectrum_from_params(projType.POLY80n), "POLY80");
		vizualizeSpectrum(create_zeego_spectrum_from_params(projType.POLY120n), "POLY120");
	}
}
