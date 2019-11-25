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

import edu.stanford.rsl.conrad.geometry.shapes.simple.Box;
import edu.stanford.rsl.conrad.geometry.shapes.simple.Cone;
import edu.stanford.rsl.conrad.geometry.shapes.simple.Cylinder;
import edu.stanford.rsl.conrad.geometry.shapes.simple.Ellipsoid;
import edu.stanford.rsl.conrad.geometry.shapes.simple.Pyramid;
import edu.stanford.rsl.conrad.geometry.shapes.simple.SimpleSurface;
import edu.stanford.rsl.conrad.geometry.shapes.simple.Sphere;
import edu.stanford.rsl.conrad.geometry.transforms.ComboTransform;
import edu.stanford.rsl.conrad.geometry.transforms.ScaleRotate;
import edu.stanford.rsl.conrad.geometry.transforms.Transform;
import edu.stanford.rsl.conrad.geometry.transforms.Translation;
import edu.stanford.rsl.conrad.numerics.SimpleMatrix;
import edu.stanford.rsl.conrad.phantom.AnalyticPhantom;
import edu.stanford.rsl.conrad.phantom.MECT;
import edu.stanford.rsl.conrad.phantom.RandomPhantom;
import edu.stanford.rsl.conrad.physics.PhysicalObject;
import edu.stanford.rsl.conrad.physics.absorption.PolychromaticAbsorptionModel;
import edu.stanford.rsl.conrad.physics.materials.Element;
import edu.stanford.rsl.conrad.physics.materials.Material;
import edu.stanford.rsl.conrad.physics.materials.Mixture;
import edu.stanford.rsl.conrad.physics.materials.database.MaterialsDB;
import edu.stanford.rsl.conrad.physics.materials.utils.AttenuationRetrievalMode;
import edu.stanford.rsl.conrad.physics.materials.utils.AttenuationType;
import edu.stanford.rsl.conrad.physics.materials.utils.MaterialUtils;
import edu.stanford.rsl.conrad.physics.materials.utils.WeightedAtomicComposition;
import edu.stanford.rsl.conrad.utils.Configuration;
import edu.stanford.rsl.conrad.utils.DoubleArrayUtil;
import edu.stanford.rsl.conrad.utils.VisualizationUtil;
import edu.stanford.rsl.tutorial.RotationalAngiography.ResidualMotionCompensation.registration.bUnwarpJ_.IODialog;
import ij.gui.Plot;

class phantom_creator {

	public static void main(String args[]) {
		//putCustomMaterials();
		MaterialsDB.removeMaterial("ultravistmax370");
		System.out.println("done");
		// plotRelevantAttenuationCurves();

		// String[] elements = { "H", "O", "C", "N", "Cl", "Ca", "I", "Si", "B", "Na",
		// "Mg", "Fe" };
//		double[] solidWater = 	{ 0.0841, 0.1849, 0.6697, 0.0216, 0.0013, 0.0143, 0.0000, 0.0108, 0.0005, 0.0017, 0.0110, 0.0000 };
//		double[] iod5mg =  		{ 0.0837, 0.1839, 0.6668, 0.0214, 0.0013, 0.0142, 0.0050, 0.0107, 0.0005, 0.0017, 0.0110, 0.0000 };
//		double[] iod10mg = 		{ 0.0832, 0.1824, 0.6643, 0.0212, 0.0013, 0.0140, 0.0099, 0.0106, 0.0005, 0.0017, 0.0109, 0.0000 };
//		double[] iod15mg = 		{ 0.0827, 0.1809, 0.6618, 0.0211, 0.0013, 0.0139, 0.0148, 0.0105, 0.0005, 0.0017, 0.0108, 0.0000 };
//		double[] calcium50mg =  { 0.0709, 0.2307, 0.6267, 0.0270, 0.0012, 0.0436};
//		double[] calcium100mg = { 0.0633, 0.2574, 0.5724, 0.0241, 0.0010, 0.0818};
//		double[] calcium300mg = { 0.0403, 0.3381, 0.4080, 0.0154, 0.0007, 0.1976};
//		double[] deltaIod5mg = 	{ -0.0004, -0.0010, -0.0029, -0.0002, 0.0000, -0.0001, 0.0050, -0.0001, 0.0000, 0.0000, 0.0000, 0.0000 };
//
//		// adding custom materials to the DB
//		putCustomMaterials();		
//		
//		String[] HO = { "H", "O" };
//		double[] H2O = { 0.2, 0.8 };
//		double zeff_H2O = zeff(HO, H2O);
//		double zeff_Material_H2O = zeff(MaterialsDB.getMaterial("water"));
//		double zeff_solidWater = zeff(elements, solidWater);
//		double zeff_Material_solidWater = zeff(MaterialsDB.getMaterial("solidWater"));
//		System.out.println("Z own:\t\t" + zeff_H2O + "\nZ matDB:\t" + zeff_Material_H2O + 
//						"\nZ solid water:\t" + zeff_solidWater + "\nZ matDB sWater:\t" + zeff_Material_solidWater);

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

		// createCustomMaterials();
	}

	static final Map<String, Integer> ZLUT = Map.ofEntries(entry("H", 1), entry("O", 8), entry("C", 6), entry("N", 7),
			entry("Cl", 17), entry("Ca", 20), entry("I", 53), entry("Si", 14), entry("B", 5), entry("Na", 11),
			entry("Mg", 12), entry("Fe", 26));

	// found these out by sampling calcium, iodine and water attenuations from nist
	// at 80 and 120 keV
	// and matching the calcium attenuation with a linear combination of water and
	// iodine
	// 1cm calcium ~ 0.06cm iodine + 0.81cm water
	private static final float[] calciumCoeff = new float[] { 0.0615f, 0.8139f };

	// creates new random phantom in bounds given
	public static AnalyticPhantom createRandomPhantom(int x_bound, int y_bound, int z_bound) {
		// asserts all materials exist in database
		putCustomMaterials();
		return new RandomPhantom(x_bound, y_bound, z_bound, true);
	}

	// creates new random phantom in bounds given
	public static AnalyticPhantom getEvaluationPhantom() {
		// TODO Add capability of creating random geometries
		// Map<Integer, String> rodConfiguration = getBoneConfig();
		Map<Integer, String> rodConfiguration = getComparisonConfig();
		return new MECT(rodConfiguration);
	}

	// creates new random phantom in bounds given
	public static AnalyticPhantom getIodineOnlyMECTPhantom() {
		// TODO Add capability of creating random geometries
		// Map<Integer, String> rodConfiguration = getBoneConfig();
		Map<Integer, String> rodConfiguration = getIodineOnlyConfig();
		return new MECT(rodConfiguration);
	}

	// creates new random phantom in bounds given
	public static AnalyticPhantom getHighIodineMECTPhantom() {
		// TODO Add capability of creating random geometries
		// Map<Integer, String> rodConfiguration = getBoneConfig();
		Map<Integer, String> rodConfiguration = getHighIodineConfig();
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

	private static Map<Integer, String> getComparisonConfig() {
		Map<Integer, String> conf = new HashMap<Integer, String>();
		conf.put(20, "iodine5mg");
		conf.put(21, "gammexiod5mgml");
		conf.put(22, "iodine10mg");
		conf.put(23, "gammexiod10mgml");
		conf.put(24, "iodine15mg");
		conf.put(25, "gammexiod15mgml");
		conf.put(26, "water");
		conf.put(27, "solidwater");
		conf.put(1, "water");
		conf.put(2, "water");
		return conf;
	}

	private static Map<Integer, String> getIodineOnlyConfig() {
		Map<Integer, String> conf = new HashMap<Integer, String>();
		conf.put(20, "iodine5mg");
		conf.put(21, "gammexiod5mgml");
		conf.put(22, "iodine10mg");
		conf.put(23, "gammexiod10mgml");
		conf.put(24, "iodine15mg");
		conf.put(25, "gammexiod15mgml");
		conf.put(26, "iodine2mg");
		conf.put(27, "gammexiod2mgml");
		conf.put(1, "water");
		conf.put(2, "water");
		return conf;
	}

	private static Map<Integer, String> getHighIodineConfig() {
		Map<Integer, String> conf = new HashMap<Integer, String>();
		conf.put(20, "iodine17mg");
		conf.put(21, "iodine35mg");
		conf.put(22, "iodine43mg");
		conf.put(23, "iodine87mg");
		conf.put(24, "iodine175mg");
		conf.put(25, "iodine262mg");
		conf.put(26, "iodine5mg");
		conf.put(27, "iodine10mg");
		conf.put(1, "iodine350mg");
		conf.put(2, "water");
		return conf;
	}

	/**
	 * adds the following materials to the MaterialsDB: solidwater gammexiod5mgml,
	 * gammexiod10mgml, gammexiod15mgml iodine5mg, iodine10mg, iodine15mg
	 * gammexcalcium50mgml, gammexcalcium100mgml, gammexcalcium300mgml calcium50mg,
	 * calcium100mg, calcium300mg
	 */
	private static void putCustomMaterials() {
		List<String> existingMaterials = Arrays.asList(MaterialsDB.getMaterials());
		String[] elements = { "H", "O", "C", "N", "Cl", "Ca", "I", "Si", "B", "Na", "Mg", "Fe" };
		int newlyCreated = 0;

		// in atomic units u
		/*
		 * double atomicWeightC = 12.0107; double atomicWeightH = 1.00784; double
		 * atomicWeightI = 126.90447; double atomicWeightN = 14.0067; double
		 * atomicWeightO = 15.999;
		 */

		double atomicWeightC = 6;
		double atomicWeightH = 1;
		double atomicWeightI = 53;
		double atomicWeightN = 7;
		double atomicWeightO = 8;

		if (!existingMaterials.contains("Omnipaque350")) {
			WeightedAtomicComposition op350 = new WeightedAtomicComposition("C19H26I3N3O9");
			System.out.println("Molar Mass of Omnipaque" + MaterialUtils.computeMolarMass(op350));
			Material omni350 = MaterialUtils.newMaterial("Omnipaque350", 1.209, op350);
			MaterialsDB.put(omni350);
			++newlyCreated;
		}

		if (!existingMaterials.contains("ultavistMax370")) {
			WeightedAtomicComposition uv370 = new WeightedAtomicComposition("C18H24I3N3O8");
			System.out.println("Molar Mass of Ultravist" + MaterialUtils.computeMolarMass(uv370));
			Material ultra370 = MaterialUtils.newMaterial("ultavistMax370", 1.399, uv370);
			MaterialsDB.put(ultra370);
			++newlyCreated;
		}

		// add 350 mg/ml iodine in water mixture
		if (!existingMaterials.contains("iodine350mg")) {
			WeightedAtomicComposition iodinetowater = new WeightedAtomicComposition("H2O", 0.650);
			iodinetowater.add("I", 0.350);
			Material iodwater = MaterialUtils.newMaterial("iodine10mg", 1.034, iodinetowater);
			MaterialsDB.put(iodwater);
			++newlyCreated;
		}

		// create 7 different concentrations of omnipaque350 corresponding to [Mueller
		// et al. 2016] (rapid-kV switching)
		double[] concentrations = { 0.05, 0.10, 0.125, 0.25, 0.50, 0.75, 1 };
		for (int i = 0; i < concentrations.length; i++) {
			String name = "iodine" + (int) (concentrations[i] * 350) + "mg";
			double percentIodine = (concentrations[i] * 350) / 1000;
			double percentWater = 1 - percentIodine;
			double mixDensity = percentIodine * 6.24 + percentWater;
			if (!existingMaterials.contains(name)) {
				WeightedAtomicComposition iodinetowater = new WeightedAtomicComposition("H2O", percentWater);
				iodinetowater.add("I", percentIodine);
				Material iodwater = MaterialUtils.newMaterial(name, mixDensity, iodinetowater);
				MaterialsDB.put(iodwater);
				++newlyCreated;
				System.out.println("added " + name + " with density " + mixDensity);
			}
		}

		// add 2 mg/ml iodine rod insert material.
		if (!existingMaterials.contains("gammexiod2mgml")) {
			double[] iod2mg = { 0.0840, 0.1847, 0.6683, 0.0215, 0.0013, 0.0142, 0.0020, 0.0107, 0.0005, 0.0017, 0.0110,
					0.0000 };
			WeightedAtomicComposition iodine2mg = new WeightedAtomicComposition();
			for (int i = 0; i < elements.length; i++) {
				if (iod2mg[i] > 0)
					iodine2mg.add(elements[i], iod2mg[i]);
			}
			Material GammexIod5mg = MaterialUtils.newMaterial("gammexiod2mgml", 1.027, iodine2mg);
			MaterialsDB.put(GammexIod5mg);
			++newlyCreated;
		}
		// add 5 mg/ml iodine rod insert material. TODO find out density
		if (!existingMaterials.contains("gammexiod5mgml")) {
			double[] iod5mg = { 0.0837, 0.1839, 0.6668, 0.0214, 0.0013, 0.0142, 0.0050, 0.0107, 0.0005, 0.0017, 0.0110,
					0.0000 };
			WeightedAtomicComposition iodine5mg = new WeightedAtomicComposition();
			for (int i = 0; i < elements.length; i++) {
				if (iod5mg[i] > 0)
					iodine5mg.add(elements[i], iod5mg[i]);
			}
			Material GammexIod5mg = MaterialUtils.newMaterial("gammexiod5mgml", 1.030, iodine5mg);
			MaterialsDB.put(GammexIod5mg);
			++newlyCreated;
		}
		// add 10 mg/ml iodine rod insert material. TODO find out density
		if (!existingMaterials.contains("gammexiod10mgml")) {
			double[] iod10mg = { 0.0832, 0.1824, 0.6643, 0.0212, 0.0013, 0.0140, 0.0099, 0.0106, 0.0005, 0.0017, 0.0109,
					0.0000 };
			WeightedAtomicComposition iodine10mg = new WeightedAtomicComposition();
			for (int i = 0; i < elements.length; i++) {
				if (iod10mg[i] > 0)
					iodine10mg.add(elements[i], iod10mg[i]);
			}
			Material GammexIod10mg = MaterialUtils.newMaterial("gammexiod10mgml", 1.034, iodine10mg);
			MaterialsDB.put(GammexIod10mg);
			++newlyCreated;
		}
		// add 15 mg/ml iodine rod insert material. TODO find out density
		if (!existingMaterials.contains("gammexiod15mgml")) {
			double[] iod15mg = { 0.0827, 0.1809, 0.6618, 0.0211, 0.0013, 0.0139, 0.0148, 0.0105, 0.0005, 0.0017, 0.0108,
					0.0000 };
			WeightedAtomicComposition iodine15mg = new WeightedAtomicComposition();
			for (int i = 0; i < elements.length; i++) {
				if (iod15mg[i] > 0)
					iodine15mg.add(elements[i], iod15mg[i]);
			}
			Material GammexIod15mg = MaterialUtils.newMaterial("gammexiod15mgml", 1.039, iodine15mg);
			MaterialsDB.put(GammexIod15mg);
			++newlyCreated;
		}
		// add 5 mg/ml iodine in water mixture
		if (!existingMaterials.contains("iodine2mg")) {
			WeightedAtomicComposition iodine2mgtowater = new WeightedAtomicComposition("H2O", 0.9980);
			iodine2mgtowater.add("I", 0.0020);
			Material iodine5mg = MaterialUtils.newMaterial("iodine2mg", 1.027, iodine2mgtowater);
			MaterialsDB.put(iodine5mg);
			++newlyCreated;
		}
		// add 5 mg/ml iodine in water mixture
		if (!existingMaterials.contains("iodine5mg")) {
			WeightedAtomicComposition iodine5mgtowater = new WeightedAtomicComposition("H2O", 0.9950);
			iodine5mgtowater.add("I", 0.0050);
			Material iodine5mg = MaterialUtils.newMaterial("iodine5mg", 1.030, iodine5mgtowater);
			MaterialsDB.put(iodine5mg);
			++newlyCreated;
		}
		// add 10 mg/ml iodine in water mixture
		if (!existingMaterials.contains("iodine10mg")) {
			WeightedAtomicComposition iodine10mgtowater = new WeightedAtomicComposition("H2O", 0.9900);
			iodine10mgtowater.add("I", 0.0100);
			Material iodine10mg = MaterialUtils.newMaterial("iodine10mg", 1.034, iodine10mgtowater);
			MaterialsDB.put(iodine10mg);
			++newlyCreated;
		}
		// add 15 mg/ml iodine in water mixture
		if (!existingMaterials.contains("iodine15mg")) {
			WeightedAtomicComposition iodine15mgtowater = new WeightedAtomicComposition("H2O", 0.9850);
			iodine15mgtowater.add("I", 0.0150);
			Material iodine15mg = MaterialUtils.newMaterial("iodine15mg", 1.039, iodine15mgtowater);
			MaterialsDB.put(iodine15mg);
			++newlyCreated;
		}
		// add solid water material (used for phantom and one insert. TODO find out
		// density
		if (!existingMaterials.contains("solidwater")) {
			double[] solidWater = { 0.0841, 0.1849, 0.6697, 0.0216, 0.0013, 0.0143, 0.0000, 0.0108, 0.0005, 0.0017,
					0.0110, 0.0000 };
			WeightedAtomicComposition swater = new WeightedAtomicComposition();
			for (int i = 0; i < elements.length; i++) {
				if (solidWater[i] > 0)
					swater.add(elements[i], solidWater[i]);
			}
			Material GammexSolidWater = MaterialUtils.newMaterial("solidwater", 1.0, swater);
			MaterialsDB.put(GammexSolidWater);
			++newlyCreated;
		}
		// add gammex 50 mg/ml iodine rod insert material
		if (!existingMaterials.contains("gammexcalcium50mgml")) {
			double[] calcium50mg = { 0.0709, 0.2307, 0.6267, 0.0270, 0.0012, 0.0436 };
			WeightedAtomicComposition ca = new WeightedAtomicComposition();
			for (int i = 0; i < calcium50mg.length; i++) {
				if (calcium50mg[i] > 0)
					ca.add(elements[i], calcium50mg[i]);
			}
			Material GammexCa = MaterialUtils.newMaterial("gammexcalcium50mgml", 1.168, ca);
			MaterialsDB.put(GammexCa);
			++newlyCreated;
		}
		// add 5 mg/ml iodine rod insert material. TODO find out density
		if (!existingMaterials.contains("gammexcalcium100mgml")) {
			double[] calcium100mg = { 0.0633, 0.2574, 0.5724, 0.0241, 0.0010, 0.0818 };
			WeightedAtomicComposition ca = new WeightedAtomicComposition();
			for (int i = 0; i < calcium100mg.length; i++) {
				if (calcium100mg[i] > 0)
					ca.add(elements[i], calcium100mg[i]);
			}
			Material GammexCa = MaterialUtils.newMaterial("gammexcalcium100mgml", 1.244, ca);
			MaterialsDB.put(GammexCa);
			++newlyCreated;
		}
		// add 5 mg/ml iodine rod insert material. TODO find out density
		if (!existingMaterials.contains("gammexcalcium300mgml")) {
			double[] calcium300mg = { 0.0403, 0.3381, 0.4080, 0.0154, 0.0007, 0.1976 };
			WeightedAtomicComposition ca = new WeightedAtomicComposition();
			for (int i = 0; i < calcium300mg.length; i++) {
				if (calcium300mg[i] > 0)
					ca.add(elements[i], calcium300mg[i]);
			}
			Material GammexCa = MaterialUtils.newMaterial("gammexcalcium300mgml", 1.548, ca);
			MaterialsDB.put(GammexCa);
			++newlyCreated;
		}
		// add 50 mg/ml calcium in water mixture TODO find out if densities are the same
		if (!existingMaterials.contains("calcium50mg")) {
			WeightedAtomicComposition ca = new WeightedAtomicComposition("H2O", 0.9500);
			ca.add("Ca", 0.0500);
			Material caM = MaterialUtils.newMaterial("calcium50mg", 1.168, ca);
			MaterialsDB.put(caM);
		}
		// add 100 mg/ml calcium in water mixture
		if (!existingMaterials.contains("calcium100mg")) {
			WeightedAtomicComposition ca = new WeightedAtomicComposition("H2O", 0.9000);
			ca.add("Ca", 0.1000);
			Material caM = MaterialUtils.newMaterial("calcium100mg", 1.244, ca);
			MaterialsDB.put(caM);
			++newlyCreated;
		}
		// add 300 mg/ml calcium in water mixture
		if (!existingMaterials.contains("calcium300mg")) {
			WeightedAtomicComposition ca = new WeightedAtomicComposition("H2O", 0.7000);
			ca.add("Ca", 0.3000);
			Material caM = MaterialUtils.newMaterial("calcium300mg", 1.548, ca);
			MaterialsDB.put(caM);
			++newlyCreated;
		}
		System.out.println("new materials inserted into database: " + newlyCreated);
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
			if (i >= percentages.length)
				break;
			sum += percentages[i] * Math.pow(phantom_creator.ZLUT.get(elements[i]), 2.94);
		}
		return Math.pow(sum, (1 / 2.94));
	}

	/**
	 * calculates the rough effective Z number for mixtures formular from
	 * https://en.wikipedia.org/wiki/Effective_atomic_number
	 * 
	 * @param elements    constituent elements
	 * @param percentages element wise percentages. Must sum up to one.
	 * @return effective Z of compound
	 */
	private static double zeff(Material m) {
		double sum = 0;
		TreeMap<String, Double> wac = m.getWeightedAtomicComposition().getCompositionTable();
		double totalWeight = calculateTotalElectronCount(wac);
		for (String element : wac.keySet()) {
			int z = ZLUT.get(element);
			Element e = (Element) MaterialsDB.getMaterialWithFormula(element);
			assert z == e.getAtomicNumber();
			sum += wac.get(element) / totalWeight * Math.pow(e.getAtomicNumber(), 2.94);
		}
		return Math.pow(sum, (1 / 2.94));
	}

	private static double calculateTotalElectronCount(TreeMap<String, Double> wac) {
		// DoubleArrayUtil.sum(wac.values().stream().mapToDouble(Double::doubleValue).toArray());
		double sum = 0;
		for (String element : wac.keySet()) {
			Element e = (Element) MaterialsDB.getMaterialWithFormula(element);
			// deviding away the weigth of each element
			sum += e.getAtomicNumber();
		}
		return DoubleArrayUtil.sum(wac.values().stream().mapToDouble(Double::doubleValue).toArray());
	}

	/**
	 * gives the concentration of material, to norm to. We want to normalize to 1
	 * mg/ml as being the unit concentration. So that after reconstruction of the
	 * iodine projections, we will see have mg/ml as unit for the pixel values.
	 * 
	 * @param material
	 * @return float [] {iodinepathlength, waterpathlength}
	 */
	public static float[] decodeIodineConcentrationFromName(String material) {
		if (material.contentEquals("calcium")) {
			System.err.println("use discouraged as pure calcium does not exist");
			return calciumCoeff;
		} else if (material.contentEquals("iodine")) {
			System.err.println("use discouraged as pure iodine does not exist");
			return new float[] { 1.0f, 0.0f };
		} else if (material.contentEquals("water") || material.contentEquals("solidwater")) {
			return new float[] { 0.0f, 1.0f };
		} else if (material.contentEquals("iodine2mg") || material.contentEquals("gammexiod2mgml")) {
			return new float[] { 0.002f, 0.998f };
		} else if (material.contentEquals("iodine5mg") || material.contentEquals("gammexiod5mgml")) {
			return new float[] { 0.005f, 0.995f };
		} else if (material.contentEquals("iodine10mg") || material.contentEquals("gammexiod10mgml")) {
			return new float[] { 0.01f, 0.99f };
		} else if (material.contentEquals("iodine15mg") || material.contentEquals("gammexiod15mgml")) {
			return new float[] { 0.015f, 0.985f };
		} else if (material.contentEquals("iodine17mg")) {
			return new float[] { 0.017f, 0.983f };
		} else if (material.contentEquals("iodine35mg")) {
			return new float[] { 0.035f, 0.965f };
		} else if (material.contentEquals("iodine43mg")) {
			return new float[] { 0.043f, 0.957f };
		} else if (material.contentEquals("iodine87mg")) {
			return new float[] { 0.087f, 0.913f };
		}else if (material.contentEquals("iodine175mg")) {
			return new float[] { 0.175f, 0.825f };
		}else if (material.contentEquals("iodine262mg")) {
			return new float[] { 0.262f, 0.738f };
		}else if (material.contentEquals("iodine350mg")) {
			return new float[] { 0.350f, 0.650f };
		} else if (material.contentEquals("calcium50mg") || material.contentEquals("gammexcalcium50mgml")) {
			// 5% calcium 95% water solution. 1 calcium is equivalent to 0.8149Water and
			// 0.0615Iodine
			return new float[] { 0.05f * calciumCoeff[0], 0.95f + (0.05f * calciumCoeff[1]) };
		} else if (material.contentEquals("calcium100mg") || material.contentEquals("gammexcalcium100mgml")) {
			return new float[] { 0.1f * calciumCoeff[0], 0.9f + (0.1f * calciumCoeff[1]) };
		} else if (material.contentEquals("calcium300mg") || material.contentEquals("gammexcalcium300mgml")) {
			return new float[] { 0.3f * calciumCoeff[0], 0.7f + (0.3f * calciumCoeff[1]) };
		} else {
			System.err.println("unknown material " + material);
			System.exit(0);
			return null;
		}
	}

	/**
	 * express a material of linear combination of iodine and water based on its
	 * attenuation
	 * 
	 * @param pixelValue   denoting the pathlengths of a material
	 * @param materialName As listed in materialsDB
	 * @param map          decides if mapping of non-iodine materials should happen.
	 *                     this is done via Zeff mapping to iodine and water basis
	 *                     materials
	 * @return {iodine, water} pathlengths for iodine and water to archieve the same
	 *         Zeff
	 */
	public static float[] MaterialBasisTransform(float pixelValue, String materialName, boolean map) {
		float[] coeffs = new float[2];
		if (map) {
			coeffs = decodeIodineConcentrationFromName(materialName);
		} else {
			coeffs = decodeIodineConcentrationFromName(materialName);
			if (!materialName.contains("iod")) {
				coeffs[0] = 0; //
				// TODO find out how to just indicate water percentage without mapping to it
				System.err.println("[phantom_creator:565] not implemented");
				System.exit(1);
			}
		}
		return new float[] { coeffs[0] * pixelValue, coeffs[1] * pixelValue };
	}

	/**
	 * express a material of linear combination of iodine and water
	 * 
	 * @param pixelValue   denoting the pathlengths of a material
	 * @param materialName As listed in materialsDB
	 * @return A weigthing for iodine and water to archieve the same Zeff
	 */
	public static void plotRelevantAttenuationCurves() {
		// using our absorption model to get material characteristica in our used energy
		// range
		PolychromaticAbsorptionModel mo = spectrum_creator.configureAbsorbtionModel(projType.POLY80);
		double[] energies = mo.getInputSpectrum().getPhotonEnergies();

		// get all relevant materials and display them.. can be exported that way
		Material w = MaterialsDB.getMaterial("water");
		Material muscle = MaterialsDB.getMaterial("muscle");
		Material adi = MaterialsDB.getMaterial("adipose");
		Material iod = MaterialsDB.getMaterial("iodine");
		Material cal = MaterialsDB.getMaterial("calcium");

		// get the energy dependant linear attenuation coefficient curves [1/cm]
		double[] waterAtt = mo.getAttenuationCoefficients(w);
		double[] muscleAtt = mo.getAttenuationCoefficients(muscle);
		double[] adiAtt = mo.getAttenuationCoefficients(adi);
		double[] iodAtt = mo.getAttenuationCoefficients(iod);
		double[] calAtt = mo.getAttenuationCoefficients(cal);

		// get the mass attenuation curves [g/cm^2]
		double[] waterMA = new double[energies.length];
		double[] muscleMA = new double[energies.length];
		double[] adiMA = new double[energies.length];
		double[] caMA = new double[energies.length];
		double[] IMA = new double[energies.length];
		for (int i = 0; i < energies.length; i++) {
			waterMA[i] = w.getAttenuation(energies[i], AttenuationType.TOTAL_WITH_COHERENT_ATTENUATION,
					AttenuationRetrievalMode.LOCAL_RETRIEVAL) / w.getDensity();
			muscleMA[i] = muscle.getAttenuation(energies[i], AttenuationType.TOTAL_WITH_COHERENT_ATTENUATION,
					AttenuationRetrievalMode.LOCAL_RETRIEVAL) / muscle.getDensity();
			adiMA[i] = adi.getAttenuation(energies[i], AttenuationType.TOTAL_WITH_COHERENT_ATTENUATION,
					AttenuationRetrievalMode.LOCAL_RETRIEVAL) / adi.getDensity();
			IMA[i] = iod.getAttenuation(energies[i], AttenuationType.TOTAL_WITH_COHERENT_ATTENUATION,
					AttenuationRetrievalMode.LOCAL_RETRIEVAL) / iod.getDensity();
			caMA[i] = cal.getAttenuation(energies[i], AttenuationType.TOTAL_WITH_COHERENT_ATTENUATION,
					AttenuationRetrievalMode.LOCAL_RETRIEVAL) / cal.getDensity();
		}

		// plot the extracted
		Plot attenuationCurve = VisualizationUtil.createPlot(energies, waterMA,
				"water iodine and calcium mass attenuation in [g/cm3]", "photon energies in [kV]",
				"mass attenuation in [g/cm3]");
		attenuationCurve.add("line", energies, IMA);
		attenuationCurve.add("line", energies, caMA);
		attenuationCurve.add("line", energies, muscleMA);
		attenuationCurve.add("line", energies, adiMA);

//		attenuationCurve.add("line", energies, waterAtt);
//		attenuationCurve.add("line", energies, iodAtt);
//		attenuationCurve.add("line", energies, calAtt);

		attenuationCurve.show();
	}
}
