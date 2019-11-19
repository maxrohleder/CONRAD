package edu.stanford.rsl.conrad.phantom; 

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import edu.stanford.rsl.conrad.physics.PhysicalObject;
import edu.stanford.rsl.conrad.physics.materials.database.MaterialsDB;

/**
 * 
 * @author Rohleder on Jun 11, 2019
 * 
 * This Class models random geometries composed of a set of materials.
 * These Geometries are made from simple objects defined at @see edu.stanford.rsl.conrad.geometry.shapes.simple
 * It can be used to generate training data for learning-based material decomposition. 
 * @see MR.DataGenerator
 */
public class RandomPhantom extends AnalyticPhantom{

	// needed by base class
	private static final long serialVersionUID = 250819L;
	// available shapes
	static final List<String> shapes = List.of("Cylinder", "Box", "Sphere", "Cone", "Ellipsoid", "Pyramide");
	// configured materials
	private static final List<String> ConfiguredMaterials = List.of(	
													"calcium50mg",
													"calcium100mg",
													"calcium300mg",
													"gammexcalcium50mgml",
													"gammexcalcium100mgml",
													"gammexcalcium300mgml",
													"iodine2mg",
													"iodine5mg",
													"iodine10mg",
													"iodine15mg",
													"gammexiod2mgml",
													"gammexiod5mgml",
													"gammexiod10mgml",
													"gammexiod15mgml", 
													"solidwater"
													);
	
	private static final List<String> iodineOnlyMaterials = List.of(	
													"iodine2mg",
													"iodine5mg",
													"iodine10mg",
													"iodine15mg",
													"gammexiod2mgml",
													"gammexiod5mgml",
													"gammexiod10mgml",
													"gammexiod15mgml", 
													"solidwater"
													);
	
	private static final List<String> highIodines = List.of(	
			"iodine5mg",
			"iodine10mg",
			"iodine17mg",
			"iodine35mg",
			"iodine43mg",
			"iodine87mg",
			"iodine175mg",
			"iodine262mg",
			"iodine350mg",
			"water"
			);
	
	
	public static List<String> getSupportedMaterials() {
		return ConfiguredMaterials;
	}
	
	@Override
	public String getBibtexCitation() {
		// TODO Auto-generated method stub
		return "RandomPhantom";
	}

	@Override
	public String getMedlineCitation() {
		// TODO Auto-generated method stub
		return "RandomPhantom";
	}

	@Override
	public String getName() {
		// shown in list of projectable phantoms in gui
		return "RandomPhantom";
	}
	
	public RandomPhantom() {
		this(200, 200, 165, false);
	}
	
	public RandomPhantom(boolean iodineOnly) {
		this(200, 200, 165, iodineOnly);
	}

	public RandomPhantom(int x_bound, int y_bound, int z_bound, boolean iodineOnly) {
		PhysicalObject RP = new PhysicalObject();
		RP.setMaterial(MaterialsDB.getMaterial("solidwater"));
		SimpleSurface s = getShape(shapes.get(rand(0, shapes.size()-1)), x_bound, y_bound, z_bound);
		RP.setShape(s);
		add(RP);
		int numberOfObjects = rand(3, 8);
		int dx = (int)(x_bound / (numberOfObjects * 2));
		int dy = (int)(y_bound / (numberOfObjects * 2));
		int dz = (int)(z_bound / (numberOfObjects * 2));
		System.out.println("Phantom consists of:");
		for (int i = 0; i < numberOfObjects; i++) {
			// choosing the size of the object (decreasing with amount of objects)
			// rand() asserts that there are not always the same sized objects (not to overfit on size)
			int xi = Math.min(Math.max(x_bound/2 - (i+1 * dx) + rand(-50, 50), 10), x_bound);
			int yi = Math.min(Math.max(y_bound/2 - (i+1 * dy) + rand(-50, 50), 10), y_bound);
			int zi = Math.min(Math.max(z_bound/2 - (i+1 * dz) + rand(-50, 50), 10), z_bound);
			// choosing the shape of the object
			String type = shapes.get(rand(0, shapes.size()-1));
			// choosing the material of the object
			String mat;
			if(iodineOnly) {
				mat = highIodines.get(rand(0, iodineOnlyMaterials.size()-1));
			}else {
				mat = ConfiguredMaterials.get(rand(0, ConfiguredMaterials.size()-1));
			}
			PhysicalObject obj = new PhysicalObject();
			obj.setMaterial(MaterialsDB.getMaterial(mat));
			obj.setShape(getShape(type, xi, yi, zi));
			//applying transform to the object to increase randomization
			obj.applyTransform(shiftAndRotate(x_bound-xi, y_bound-yi, z_bound-zi));
			add(obj);
			System.out.println("\t" + type + " of material " + mat);
		}

		
	}
	
	/**
	 * applies a transform  with max xyz translation bounded by parameters ijk
	 * @param i	- maximum x translation
	 * @param j - maximum y translation
	 * @param k - maximum z translation
	 * @return translation and rotation transform
	 */
	private static Transform shiftAndRotate(int i, int j, int k) {
		Translation tran = new Translation(rand(0, i), rand(0, j), rand(0, k));
		ScaleRotate Rx = new ScaleRotate(randomRotationMatrix(0));
		ScaleRotate Ry = new ScaleRotate(randomRotationMatrix(1));
		ScaleRotate Rz = new ScaleRotate(randomRotationMatrix(2));
		ComboTransform ret = new ComboTransform(tran, Rx, Ry, Rz);
		return ret;
	}
	
	private static SimpleMatrix randomRotationMatrix(int axis){
		double angle = Math.random()*Math.PI*2;
		double c = Math.cos(angle);
		double s = Math.sin(angle);
		SimpleMatrix rot = new SimpleMatrix(3,3);
		// creating 3d rotation matrices
		if(axis == 0) {
			rot.setElementValue(0, 0, 1);
			rot.setElementValue(1, 1, c);
			rot.setElementValue(2, 2, c);
			rot.setElementValue(1, 2, s);
			rot.setElementValue(2, 1, -s);
		} else if(axis == 1) {
			rot.setElementValue(0, 0, c);
			rot.setElementValue(1, 1, 1);
			rot.setElementValue(2, 2, c);
			rot.setElementValue(2, 0, s);
			rot.setElementValue(0, 2, -s);			
		} else {
			rot.setElementValue(0, 0, c);
			rot.setElementValue(1, 1, c);
			rot.setElementValue(2, 2, 1);
			rot.setElementValue(0, 1, s);
			rot.setElementValue(1, 0, -s);
		}
		return rot;
	}

	private static SimpleSurface getShape(String s, int x, int y, int z) {
		SimpleSurface ret;
		if(s == "Pyramide") {
			ret = new Pyramid(x, y, z);
		} else if(s == "Box") {
			ret = new Box(x, y, z);
		} else if(s == "Sphere") {
			ret = new Sphere(Math.min(Math.min(x, y), z));
		} else if(s == "Cone") {
			ret = new Cone(x, y, z);
		} else if(s == "Ellipsoid") {
			ret = new Ellipsoid(x, y, z);
		} else {
			ret = new Cylinder(x, y, z);
		}
		return ret;
	}

	private static int rand(int min, int max) {
		return min + (int)(Math.random() * ((max - min) + 1));
	}
}



















