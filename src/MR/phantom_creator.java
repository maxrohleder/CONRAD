package MR;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.io.filefilter.IOFileFilter;

import static java.util.Map.entry;

import java.util.Arrays;

import edu.stanford.rsl.conrad.phantom.AnalyticPhantom;
import edu.stanford.rsl.conrad.phantom.MECT;
import edu.stanford.rsl.conrad.physics.PhysicalObject;
import edu.stanford.rsl.conrad.physics.materials.Element;
import edu.stanford.rsl.conrad.physics.materials.Material;
import edu.stanford.rsl.conrad.physics.materials.Mixture;
import edu.stanford.rsl.conrad.physics.materials.database.MaterialsDB;
import edu.stanford.rsl.conrad.physics.materials.utils.MaterialUtils;
import edu.stanford.rsl.conrad.physics.materials.utils.WeightedAtomicComposition;
import edu.stanford.rsl.conrad.utils.DoubleArrayUtil;
import edu.stanford.rsl.tutorial.RotationalAngiography.ResidualMotionCompensation.registration.bUnwarpJ_.IODialog;

class phantom_creator {
	public static void main(String args[]) {
		String[] elements = { "H", "O", "C", "N", "Cl", "Ca", "I", "Si", "B", "Na", "Mg", "Fe" };
		double[] solidWater = 	{ 0.0841, 0.1849, 0.6697, 0.0216, 0.0013, 0.0143, 0.0000, 0.0108, 0.0005, 0.0017, 0.0110, 0.0000 };
		double[] iod5mg =  		{ 0.0837, 0.1839, 0.6668, 0.0214, 0.0013, 0.0142, 0.0050, 0.0107, 0.0005, 0.0017, 0.0110, 0.0000 };
		double[] iod10mg = 		{ 0.0832, 0.1824, 0.6643, 0.0212, 0.0013, 0.0140, 0.0099, 0.0106, 0.0005, 0.0017, 0.0109, 0.0000 };
		double[] iod15mg = 		{ 0.0827, 0.1809, 0.6618, 0.0211, 0.0013, 0.0139, 0.0148, 0.0105, 0.0005, 0.0017, 0.0108, 0.0000 };
		double[] calcium50mg =  { 0.0709, 0.2307, 0.6267, 0.0270, 0.0012, 0.0436};
		double[] calcium100mg = { 0.0633, 0.2574, 0.5724, 0.0241, 0.0010, 0.0818};
		double[] calcium300mg = { 0.0403, 0.3381, 0.4080, 0.0154, 0.0007, 0.1976};
		double[] deltaIod5mg = 	{ -0.0004, -0.0010, -0.0029, -0.0002, 0.0000, -0.0001, 0.0050, -0.0001, 0.0000, 0.0000, 0.0000, 0.0000 };

		// adding custom materials to the DB
		putCustomMaterials();		
		
		String[] HO = { "H", "O" };
		double[] H2O = { 0.2, 0.8 };
		double zeff_H2O = zeff(HO, H2O);
		double zeff_Material_H2O = zeff(MaterialsDB.getMaterial("water"));
		double zeff_solidWater = zeff(elements, solidWater);
		double zeff_Material_solidWater = zeff(MaterialsDB.getMaterial("solidWater"));
		System.out.println("Z own:\t\t" + zeff_H2O + "\nZ matDB:\t" + zeff_Material_H2O + 
						"\nZ solid water:\t" + zeff_solidWater + "\nZ matDB sWater:\t" + zeff_Material_solidWater);
		
		// actuall experiments
//		double zeff_gammexiod5mg = zeff(elements, iod5mg);
//		double zeff_gammexiod10mg = zeff(elements, iod10mg);
//		double zeff_gammexiod15mg = zeff(elements, iod15mg);
//		double zeff_iod5mg = zeff(new String[] {"I", "O", "H" }, new double[] {0.005, 0.795, 0.2});
//		double zeff_iod10mg = zeff(new String[] {"I", "O", "H" }, new double[] {0.01, 0.79, 0.2});
//		double zeff_iod15mg = zeff(new String[] {"I", "O", "H" }, new double[] {0.015, 0.785, 0.2});		
//		double zeff_gammexca50mg = zeff(elements, calcium50mg);
//		double zeff_gammexca100mg = zeff(elements, calcium100mg);
//		double zeff_gammexca300mg = zeff(elements, calcium300mg);
//		double zeff_ca50mg = zeff(new String[] {"Ca", "O", "H" }, new double[] {0.05, 0.76, 0.19});
//		double zeff_ca100mg = zeff(new String[] {"Ca", "O", "H" }, new double[] {0.1, 0.72, 0.18});
//		double zeff_ca300mg = zeff(new String[] {"Ca", "O", "H" }, new double[] {0.3, 0.56, 0.14});		
//		
//		System.out.println("--------WATER--------");
//		System.out.println("real: " + zeff_H2O + " gammex " + zeff_solidWater);
//		System.out.println("--------IODINE--------");
//		System.out.println("real: " + zeff_iod5mg + " gammex " + zeff_gammexiod5mg);
//		System.out.println("real: " + zeff_iod10mg + " gammex " + zeff_gammexiod10mg);
//		System.out.println("real: " + zeff_iod15mg + " gammex " + zeff_gammexiod15mg);
//		System.out.println("--------CALCIUM--------");
//		System.out.println("real: " + zeff_ca50mg + " gammex " + zeff_gammexca50mg);
//		System.out.println("real: " + zeff_ca100mg + " gammex " + zeff_gammexca100mg);
//		System.out.println("real: " + zeff_ca300mg + " gammex " + zeff_gammexca300mg);
		
		
//		Mixture iodine5mgml = new Mixture();
//		iodine5mgml.setName("iodine5mgml");
//		MaterialsDB.put(iodine5mgml);
		
		// test new materials
		
		

		//		createCustomMaterials();
	}
	static final Map<String, Integer> ZLUT = Map.ofEntries(
		    entry("H", 1),
		    entry("O", 8),
		    entry("C", 6), 	
		    entry("N", 7), 
		    entry("Cl", 17), 
		    entry("Ca", 20),
		    entry("I",  53),
		    entry("Si", 14),
		    entry("B", 5),
		    entry("Na", 11),
		    entry("Mg", 12),
		    entry("Fe", 26));
    
	// creates new random phantom in bounds given
	public static AnalyticPhantom create_new_phantom(int x_bound, int y_bound) {
		// TODO Add capability of creating random geometries
		Map<Integer, String> rodConfiguration = getBoneConfig();
		return new MECT(rodConfiguration);
	}

	private static Map<Integer, String> getBoneConfig() {
		Map<Integer, String> conf = new HashMap<Integer, String>();
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

	private static void putCustomMaterials() {
        List<String> existingMaterials = Arrays.asList(MaterialsDB.getMaterials());
		String[] elements = { "H", "O", "C", "N", "Cl", "Ca", "I", "Si", "B", "Na", "Mg", "Fe" };
		
		// add 5 mg/ml iodine rod insert material. TODO find out density
		if(!existingMaterials.contains("gammexiod5mgml")) {
			double[] iod5mg =  		{ 0.0837, 0.1839, 0.6668, 0.0214, 0.0013, 0.0142, 0.0050, 0.0107, 0.0005, 0.0017, 0.0110, 0.0000 };
			WeightedAtomicComposition iodine5mg = new WeightedAtomicComposition();
			for (int i = 0; i < elements.length; i++) {
				if(iod5mg[i] > 0) iodine5mg.add(elements[i], iod5mg[i]);
			}
			Material GammexIod5mg = MaterialUtils.newMaterial("gammexiod5mgml", 1.0, iodine5mg);
			MaterialsDB.put(GammexIod5mg);
		}
		// add 10 mg/ml iodine rod insert material. TODO find out density			
		if(!existingMaterials.contains("gammexiod10mgml")) {
			double[] iod10mg = 		{ 0.0832, 0.1824, 0.6643, 0.0212, 0.0013, 0.0140, 0.0099, 0.0106, 0.0005, 0.0017, 0.0109, 0.0000 };
			WeightedAtomicComposition iodine10mg = new WeightedAtomicComposition();
			for (int i = 0; i < elements.length; i++) {
				if(iod10mg[i] > 0) iodine10mg.add(elements[i], iod10mg[i]);
			}
			Material GammexIod10mg = MaterialUtils.newMaterial("gammexiod10mgml", 1.0, iodine10mg);
			MaterialsDB.put(GammexIod10mg);
		}
		// add 15 mg/ml iodine rod insert material. TODO find out density
		if(!existingMaterials.contains("gammexiod15mgml")) {
			double[] iod15mg = 		{ 0.0827, 0.1809, 0.6618, 0.0211, 0.0013, 0.0139, 0.0148, 0.0105, 0.0005, 0.0017, 0.0108, 0.0000 };
			WeightedAtomicComposition iodine15mg = new WeightedAtomicComposition();
			for (int i = 0; i < elements.length; i++) {
				if(iod15mg[i] > 0) iodine15mg.add(elements[i], iod15mg[i]);
			}
			Material GammexIod15mg = MaterialUtils.newMaterial("gammexiod15mgml", 1.0, iodine15mg);
			MaterialsDB.put(GammexIod15mg);
		}
		// add solid water material (used for phantom and one insert. TODO find out density
		if(!existingMaterials.contains("solidwater")) {
			double[] solidWater = 	{ 0.0841, 0.1849, 0.6697, 0.0216, 0.0013, 0.0143, 0.0000, 0.0108, 0.0005, 0.0017, 0.0110, 0.0000 };
			WeightedAtomicComposition swater = new WeightedAtomicComposition();
			for (int i = 0; i < elements.length; i++) {
				if(solidWater[i] > 0) swater.add(elements[i], solidWater[i]);
			}
			Material GammexSolidWater = MaterialUtils.newMaterial("solidwater", 1.0, swater);
			MaterialsDB.put(GammexSolidWater);
		}
		// add 5 mg/ml iodine in water mixture
		if(!existingMaterials.contains("iodine5mg")) {
			WeightedAtomicComposition iodine5mgtowater = new WeightedAtomicComposition("H2O", 0.9950);
			iodine5mgtowater.add("I", 0.0050);
			Material iodine5mg = MaterialUtils.newMaterial("iodine5mg", 1.0, iodine5mgtowater);
			MaterialsDB.put(iodine5mg);
		}		
	}

	/**
	 * calculates the rough effective Z number for compound materials
	 * 
	 * @param elements    constituent elements
	 * @param percentages element wise percentages. Must sum up to one.
	 * @return effective Z of compound
	 */
	private static double zeff(String[] elements, double[] percentages) {
		assert elements.length == percentages.length;
		double sum = 0;
		for (int i = 0; i < elements.length; i++) {
			if(i >= percentages.length) break;
			sum += percentages[i]*Math.pow(phantom_creator.ZLUT.get(elements[i]), 2.94);
		}
		return Math.pow(sum, (1/2.94));
	}
	
	/**
	 * calculates the rough effective Z number for mixtures
	 * 
	 * @param elements    constituent elements
	 * @param percentages element wise percentages. Must sum up to one.
	 * @return effective Z of compound
	 */
	private static double zeff(Material m) {
		double sum = 0;
		TreeMap<String, Double> wac = m.getWeightedAtomicComposition().getCompositionTable();
		double totalWeight = calculateTotalElectronCount(wac);
		for(String element : wac.keySet()) {
			int z = ZLUT.get(element);
			Element e = (Element) MaterialsDB.getMaterialWithFormula(element);
			assert z == e.getAtomicNumber();
			sum += wac.get(element)/totalWeight * Math.pow(e.getAtomicNumber(), 2.94);
		}
		return Math.pow(sum, (1/2.94));
	}

	private static double calculateTotalElectronCount(TreeMap<String, Double> wac) {
		//DoubleArrayUtil.sum(wac.values().stream().mapToDouble(Double::doubleValue).toArray());
		double sum = 0;
		for(String element : wac.keySet()) {
			Element e = (Element) MaterialsDB.getMaterialWithFormula(element);
			// deviding away the weigth of each element
			sum += e.getAtomicNumber();
		}
		return sum;
	}
}