package edu.stanford.rsl.conrad.phantom; 

import java.util.HashMap;

import edu.stanford.rsl.conrad.geometry.shapes.simple.Cylinder;
import edu.stanford.rsl.conrad.geometry.transforms.Translation;
import edu.stanford.rsl.conrad.physics.PhysicalObject;
import edu.stanford.rsl.conrad.physics.materials.database.MaterialsDB;

/**
 * 
 * @author Rohleder on Jun 11, 2019
 * 
 * This Class models the Multi-Energy CT Phantom by GAMMEX Inc. Model 1472 used to 
 * assess system capabilities at Stanford RSL. It consists of a 400x300mm diameter ellipsoid base 
 * cylinder with several 28.5mm diameter cylindrical rods, which can be equipped with 
 * different materials. For more detail see the instruction manual.
 */
public class MECT extends AnalyticPhantom{
	/**
	 * serialVersion in scheme ddmmyy
	 */
	private static final long serialVersionUID = 110619L;
	private double scale_factor = 1;
	private static final double x_outer_mm = 200; //radius
	private static final double y_outer_mm = 150; 
	private static final double x_inner_mm = 100; 
	private static final double y_inner_mm = 100; 
	private static final double z_mm = 165;
	private static final double rod_size_mm = 14.25;		
	

	@Override
	public String getBibtexCitation() {
		// TODO Auto-generated method stub
		return "GAMMEX Model 1472";
	}

	@Override
	public String getMedlineCitation() {
		// TODO Auto-generated method stub
		return "GAMMEX Model 1472";
	}

	@Override
	public String getName() {
		// shown in list of projectable phantoms in gui
		return "Multi Energy CT Phantom";
	}
	
	/**
	 *   ----------
	 *  / 27 20 21 \
	 * | 26  1 2 22 |
	 *  \ 25 24 23 /
	 *   ----------
	 *   setting only inner 10 rods as bone nummerated as shown above using on
	 */
	public MECT() {
		this(false);
	}
	
	/**
	 *   -------------
	 *  /15 27 20 21 10\
	 * |14 26 1 2 22 11|
	 *  \13 25 24 23 12/
	 *   -------------
	 *  @param use_outer if true uses all 16 rods as bone inserts
	*/
	public MECT(boolean use_outer) {
		PhysicalObject MECT = new PhysicalObject();
		MECT.setMaterial(MaterialsDB.getMaterial("water"));
		// create main ellipsiod object with dims 400x300x165 mm
		Cylinder mainCylinder;
		if(use_outer) {
			mainCylinder = new Cylinder(x_outer_mm*scale_factor, y_outer_mm*scale_factor, z_mm*scale_factor);
		}else {
			mainCylinder = new Cylinder(x_inner_mm*scale_factor, y_inner_mm*scale_factor, z_mm*scale_factor);

		}
		MECT.setShape(mainCylinder);
		add(MECT);
		/*
		 * now configure the rods. this default case features bone rods and two
		 * Polyethylene rods in the center. The real phantom can be equipped with 19
		 * different materials 
		*/

		// set inner rods to bone too
		for(int i = 20; i < 28; i++) {
			setRodMaterial(i, "bone");
		}
		
		// set outer rods to bone
		if(use_outer) {
			for(int i = 10; i < 16; i++) {
				setRodMaterial(i, "bone");
			}
		}
		// for testing purposes set bullseye and nose to polyethylene
		setRodMaterial(1, "bone");
		setRodMaterial(2, "bone");
		
	}
	
	
	/**
	 * sets rods from hashmap
	 * @param rods has to configure all rods by their identifier specified in setRodMaterial
	 */
	public MECT(HashMap<Integer, String> rods) {
		PhysicalObject MECT = new PhysicalObject();
		MECT.setMaterial(MaterialsDB.getMaterial("water"));
		// create main ellipsiod object with dims 400x300x165 mm
		Cylinder mainCylinder;
		if(rods.size() == 16) {
			mainCylinder = new Cylinder(x_outer_mm*scale_factor, y_outer_mm*scale_factor, z_mm*scale_factor);
		}else if(rods.size() == 10){
			mainCylinder = new Cylinder(x_inner_mm*scale_factor, y_inner_mm*scale_factor, z_mm*scale_factor);
		}else {
			System.err.print("[MECT] need to provide all rod materials. Assuming small Phantom size now...");
			mainCylinder = new Cylinder(x_inner_mm*scale_factor, y_inner_mm*scale_factor, z_mm*scale_factor);
		}
		MECT.setShape(mainCylinder);
		add(MECT); // add it to prioritizable scene
		
		// set all rods configured in constructor parameter
		for(int key : rods.keySet()) {
			setRodMaterial(key, rods.get(key));
		}
	}
	
	/**
	 * 
	 * @param rodIdent - [10-15] outer rods starting north west. [20-27] inner rods starting north. [1] bullseye. [2] below bullseye.
	 * @param Material - any Material Name in edu.stanford.rsl.conrad.physics.materials.database.MaterialsDB
	 * 
	 * @return indicates success (if false maybe material does not exist)
	 */
	public boolean setRodMaterial(int rodIdent, String Material) {

		// define a new subobject to the current phantom
		PhysicalObject SubObject = new PhysicalObject();
		SubObject.setMaterial(MaterialsDB.getMaterial(Material));
		// define the geometry of subobject which is a cylinder
		Cylinder rod = new Cylinder(rod_size_mm*scale_factor, rod_size_mm*scale_factor, z_mm*scale_factor);
		// translate based on above defined coding
		Translation translation = _getTranslationOfIdentNr(rodIdent);
		rod.applyTransform(translation);
		SubObject.setShape(rod);
		boolean ret = add(SubObject);
		return ret;
	}
	
	private Translation _getTranslationOfIdentNr(int rodIdent) {
		double radius_mm;
		double start_angle = Math.PI/2; // start north
		
		if(rodIdent >= 20 && rodIdent <= 27) {
			// inner rods r = 75mm; ang_increment = pi/4
			radius_mm = 75;
			rodIdent -= 20;
		}else if(rodIdent >= 10 && rodIdent <= 15) {
			// outer rods r = 140; ang_increment = pi/4; have to translate between 3rd and 4rd rod
			radius_mm = 140f;
			rodIdent -= 10;
			if(rodIdent < 3) {
				rodIdent += 1;
			}else {
				rodIdent += 2;
			}
		}else if(rodIdent == 2) {
			radius_mm = 37.5f;
			rodIdent = 4;
		}else {
			// default case is bullseye position
			radius_mm = 0f;
		}
		double dx = Math.cos(start_angle - rodIdent*(Math.PI/4)) * radius_mm;
		double dy = Math.sin(start_angle - rodIdent*(Math.PI/4)) * radius_mm;
		Translation ret = new Translation(dx*scale_factor, dy*scale_factor, 0);
		return ret;
	}
	
	public void set_scale(double scale) {
		this.scale_factor = scale;
	}
}




















