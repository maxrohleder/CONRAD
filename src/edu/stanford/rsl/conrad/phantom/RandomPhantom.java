package edu.stanford.rsl.conrad.phantom; 

import java.util.HashMap;
import java.util.Map;

import edu.stanford.rsl.conrad.geometry.shapes.simple.Cylinder;
import edu.stanford.rsl.conrad.geometry.transforms.Translation;
import edu.stanford.rsl.conrad.physics.PhysicalObject;
import edu.stanford.rsl.conrad.physics.materials.database.MaterialsDB;
import weka.datagenerators.DataGenerator;

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
	private static final double width = 200; //radius
	private static final double height = 200; 
	private static final double depth = 200;
	private String[] materials;	

	public RandomPhantom(String[] materials) {
		this(materials, materials.length, true);
	}
	
	public RandomPhantom(String[] materials, int numberOfObjects, boolean useAllMaterials) {
		this.materials = materials;
		init(numberOfObjects);
	}

	private void init(int n) {
		
	}

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
		return "RandomPhantom";
	}
	
}




















