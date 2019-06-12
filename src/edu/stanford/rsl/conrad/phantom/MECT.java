package edu.stanford.rsl.conrad.phantom; 

import edu.stanford.rsl.conrad.geometry.shapes.simple.Cylinder;
import edu.stanford.rsl.conrad.geometry.transforms.Translation;
import edu.stanford.rsl.conrad.physics.PhysicalObject;
import edu.stanford.rsl.conrad.physics.materials.database.MaterialsDB;

/**
 * 
 * @author Rohleder on Jun 11, 2019
 * 
 * This Class models the Multi-Energy CT Phantom by GAMMEX Inc. Model 1472 used to 
 * assess system capabilities at Stanford RSL. It consists of a 400x300mm ellipsoid base 
 * cylinder with several 28.5mm diameter cylindrical rods, which can be equipped with 
 * different materials. For more detail see the instruction manual.
 */
public class MECT extends AnalyticPhantom{
	/**
	 * serialVersion in scheme ddmmyy
	 */
	private static final long serialVersionUID = 110619L;
	//private Cylinder[] innerRods;
	//private Cylinder[] outerRods;

	@Override
	public String getBibtexCitation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMedlineCitation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return "Multi Energy CT Phantom";
	}
	
	/**
	 *   -----------
	 *  /15 14 7  8  10\
	 * |14 13  15  9  11|
	 *  \13 12 11 10 12/
	 *   ------------
	 *   setting rots nummerated as shown above
	 */
	public MECT() {
		PhysicalObject MECT = new PhysicalObject();
		MECT.setMaterial(MaterialsDB.getMaterial("water"));
		Cylinder mainCylinder = new Cylinder(400, 300, 165);
		MECT.setShape(mainCylinder);
		add(MECT);
		
		// set outer rods to bone
		for(int i = 10; i < 16; i++) {
			setRodMaterial(i, "bone");
		}
		
		// set inner rods to bone too
		for(int i = 20; i < 28; i++) {
			setRodMaterial(i, "bone");
		}
		
		// for testing purposes set bullseye and nose to polyethylene
		setRodMaterial(1, "Polyethylene");
		setRodMaterial(2, "Polyethylene");
		
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
		Cylinder rod = new Cylinder(28.5, 28.5, 165);
		// translate based on above defined coding
		Translation translation = _getTranslationOfIdentNr(rodIdent);
		rod.applyTransform(translation);
		SubObject.setShape(rod);
		boolean ret = add(SubObject);
		return ret;
	}
	
	private static Translation _getTranslationOfIdentNr(int rodIdent) {
		float radius_mm;
		double start_angle = Math.PI/2; // start north
		
		if(rodIdent >= 20 && rodIdent <= 27) {
			// inner rods r = 75mm; ang_increment = pi/4
			radius_mm = 75f;
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
		Translation ret = new Translation(dx, dy, 0);
		return ret;
	}
}




















