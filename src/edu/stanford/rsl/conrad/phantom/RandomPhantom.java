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
 * It can be used to generate training data for learning based material decomposition. 
 * @see MR.DataGenerator
 */
public class RandomPhantom extends AnalyticPhantom{

	/**
	 * 
	 */
	private static final long serialVersionUID = 250819L;
	static final List<String> shapes = List.of("Cylinder", "Box", "Sphere", "Cone", "Ellipsoid", "Pyramide");
	private static final List<String> ConfiguredMaterials = List.of(	"calcium50mg",
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
													"solidwater");
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
		this(200, 200, 165);
	}

	public RandomPhantom(int x_bound, int y_bound, int z_bound) {
		PhysicalObject RP = new PhysicalObject();
		RP.setMaterial(MaterialsDB.getMaterial("solidwater"));
		SimpleSurface s = getShape(shapes.get(rand(0, shapes.size()-1)), x_bound, y_bound, z_bound);
		RP.setShape(s);
		add(RP);
		int numberOfObjects = rand(5, 10);
		int dx = (int )(x_bound / (numberOfObjects+1));
		int dy = (int )(y_bound / (numberOfObjects+1));
		int dz = (int )(z_bound / (numberOfObjects+1));
		System.out.println("Phantom consists of:");
		for (int i = 0; i < numberOfObjects; i++) {
			int xi = x_bound - (i+1 * dx) + rand(-30, 30);
			int yi = y_bound - (i+1 * dy) + rand(-30, 30);
			int zi = z_bound - (i+1 * dz) + rand(-30, 30);
			String type = shapes.get(rand(0, shapes.size()-1));
			String mat = ConfiguredMaterials.get(rand(0, ConfiguredMaterials.size()-1));
			PhysicalObject obj = new PhysicalObject();
			obj.setMaterial(MaterialsDB.getMaterial(mat));
			obj.setShape(getShape(type, xi, yi, zi));
			obj.applyTransform(shiftAndRotate(x_bound-xi, y_bound-yi, z_bound-zi));
			add(obj);
			System.out.println("\t" + type + " of material " + mat);
		}

		
	}
	
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



















