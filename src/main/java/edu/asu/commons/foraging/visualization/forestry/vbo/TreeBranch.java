package edu.asu.commons.foraging.visualization.forestry.vbo;

import java.io.Serializable;
import java.nio.FloatBuffer;
import java.util.Vector;

import javax.media.opengl.GLAutoDrawable;

import edu.asu.commons.foraging.graphics.BoundingBox;
import edu.asu.commons.foraging.graphics.Matrix4;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Ray;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.util.Tuple3f;

public class TreeBranch implements Serializable {	
	private static final long serialVersionUID = 999939891255787138L;
	private int level;
	protected float baseRadius;
	protected float topRadius;
	protected float length;
	protected float lengthCone; //Length if the conical frustum is extended to a cone
	protected Tuple3f angles;
	protected Point3D baseCenter;
	private BoundingBox boundingBox_ws = new BoundingBox(false);
	private Matrix4 transformation_os2ws = null;
	private Matrix4 rotation_os2ws = null;
				
	protected Tree parentTree;
	protected TreeBranch parentBranch = null;
	
	protected transient Vector<TreeBranch> children = new Vector<TreeBranch>();
		
	public TreeBranch(int level, float baseRadius, float topRadius, float length, Point3D baseCenter, Tuple3f angles, TreeBranch parentBranch, Tree parentTree) {
		this.level = level;
		this.baseRadius = baseRadius;
		this.topRadius = topRadius;
		this.length = length;
		this.baseCenter = baseCenter;
		this.angles = angles;
						
		this.parentTree = parentTree;
		this.parentBranch = parentBranch;
		
		rotation_os2ws = new Matrix4(angles.a, new Vector3D(1, 0, 0)).multiply( new Matrix4(angles.b, new Vector3D(0, 1, 0)).multiply(new Matrix4(angles.c, new Vector3D(0, 0, 1))));
		calculateTransformation();
		calculateLengthCone();
	}
	
	public boolean isInitialized() {
		return this.baseRadius != 0;
	}

	public int create(FloatBuffer branchVertices, FloatBuffer normalBuffer, FloatBuffer textureCoordBuffer) {
//	FloatBuffer branchAttributes, FloatBuffer branchBaseCenters, FloatBuffer branchTransformationsRow1, FloatBuffer branchTransformationsRow2, FloatBuffer branchTransformationsRow3, FloatBuffer branchTransformationsRow4) {
		int nBranchVertices = 0;
		int treeAge = parentTree.getAge();
		float angleAroundZ = 35;
		
		if (level*2+3 <= treeAge) {
			//Add child branches
			
			float scaleFactor = 0.7f; //1.0f-0.05f*treeAge;
			float newLength = length * scaleFactor;
			float newBaseRadius = baseRadius * scaleFactor;
			float newTopRadius = topRadius * scaleFactor;
			
			TreeBranch childBranch;
			
			//branch1
			childBranch = new TreeBranch(level+1, newBaseRadius, newTopRadius, newLength, 
					new Point3D(0, length, 0), new Tuple3f(0, 0, angleAroundZ), this, parentTree);
			children.add(childBranch);
			nBranchVertices += childBranch.updateGeometry(branchVertices, normalBuffer, textureCoordBuffer); 
//			branchAttributes, branchBaseCenters, branchTransformationsRow1, branchTransformationsRow2, branchTransformationsRow3, branchTransformationsRow4);
			nBranchVertices += childBranch.create(branchVertices, normalBuffer, textureCoordBuffer);
//			branchAttributes, branchBaseCenters, branchTransformationsRow1, branchTransformationsRow2, branchTransformationsRow3, branchTransformationsRow4);
			
			//branch2			
			childBranch = new TreeBranch(level+1, newBaseRadius, newTopRadius, newLength,
					new Point3D(0, length, 0), new Tuple3f(0, 120, angleAroundZ), this, parentTree);
			children.add(childBranch);
			nBranchVertices += childBranch.updateGeometry(branchVertices, normalBuffer, textureCoordBuffer);
//			branchAttributes, branchBaseCenters, branchTransformationsRow1, branchTransformationsRow2, branchTransformationsRow3, branchTransformationsRow4);
			nBranchVertices += childBranch.create(branchVertices, normalBuffer, textureCoordBuffer);
//			branchAttributes, branchBaseCenters, branchTransformationsRow1, branchTransformationsRow2, branchTransformationsRow3, branchTransformationsRow4);
			
			//branch 3			
			childBranch = new TreeBranch(level+1, newBaseRadius, newTopRadius, newLength,
					new Point3D(0, length, 0), new Tuple3f(0, 240, angleAroundZ), this, parentTree);
			children.add(childBranch);
			nBranchVertices += childBranch.updateGeometry(branchVertices, normalBuffer, textureCoordBuffer);
			//branchAttributes, branchBaseCenters, branchTransformationsRow1, branchTransformationsRow2, branchTransformationsRow3, branchTransformationsRow4);
			nBranchVertices += childBranch.create(branchVertices, normalBuffer, textureCoordBuffer);
//			branchAttributes, branchBaseCenters, branchTransformationsRow1, branchTransformationsRow2, branchTransformationsRow3, branchTransformationsRow4);			
		}
		else if (treeAge > 1){
			parentTree.addLeafCluster( this );
		}
		
		return nBranchVertices;
	}
	
	public int grow(FloatBuffer branchVertices, FloatBuffer normalBuffer, FloatBuffer textureCoordBuffer) { 
//	FloatBuffer branchAttributes, FloatBuffer branchBaseCenters, FloatBuffer branchTransformationsRow1, FloatBuffer branchTransformationsRow2, FloatBuffer branchTransformationsRow3, FloatBuffer branchTransformationsRow4) {
		int nBranchVertices = 0;
		int treeAge = parentTree.getAge();
		int nChildren = children.size();
		float angleAroundZ = 35;
		
		//If child branches are present, update their parameter values		
		if (nChildren != 0){
			for (int childIndex = 0; childIndex < nChildren; childIndex++) {
				TreeBranch childBranch = children.get(childIndex);
				nBranchVertices += childBranch.updateParamValues(branchVertices, normalBuffer, textureCoordBuffer);
//				branchAttributes, branchBaseCenters, branchTransformationsRow1, branchTransformationsRow2, branchTransformationsRow3, branchTransformationsRow4);
				nBranchVertices += childBranch.grow(branchVertices, normalBuffer, textureCoordBuffer);
//				branchAttributes, branchBaseCenters, branchTransformationsRow1, branchTransformationsRow2, branchTransformationsRow3, branchTransformationsRow4);
			}
		}
		else {
			//child branches are not present
			if (treeAge == (level*2+3)) { 	
				//Add child branches if age is odd
				//Add leaves to these child branches
				float scaleFactor = 0.7f; //1.0f-0.05f*treeAge;
				float newLength = length * scaleFactor;
				float newBaseRadius = baseRadius * scaleFactor;
				float newTopRadius = topRadius * scaleFactor;
								
				TreeBranch childBranch;
				
				//branch1
				childBranch = new TreeBranch(level+1, newBaseRadius, newTopRadius, newLength, 
						new Point3D(0, length, 0), new Tuple3f(0, 0, angleAroundZ), this, parentTree);
				children.add(childBranch);
				nBranchVertices += childBranch.updateGeometry(branchVertices, normalBuffer, textureCoordBuffer); 
//				branchAttributes, branchBaseCenters, branchTransformationsRow1, branchTransformationsRow2, branchTransformationsRow3, branchTransformationsRow4);				
				parentTree.addLeafCluster( childBranch );
								
				//branch2			
				childBranch = new TreeBranch(level+1, newBaseRadius, newTopRadius, newLength,
						new Point3D(0, length, 0), new Tuple3f(0, 120, angleAroundZ), this, parentTree);
				children.add(childBranch);
				nBranchVertices += childBranch.updateGeometry(branchVertices, normalBuffer, textureCoordBuffer);
//				branchAttributes, branchBaseCenters, branchTransformationsRow1, branchTransformationsRow2, branchTransformationsRow3, branchTransformationsRow4);
				parentTree.addLeafCluster( childBranch );
				
				//branch 3			
				childBranch = new TreeBranch(level+1, newBaseRadius, newTopRadius, newLength,
						new Point3D(0, length, 0), new Tuple3f(0, 240, angleAroundZ), this, parentTree);
				children.add(childBranch);
				nBranchVertices += childBranch.updateGeometry(branchVertices, normalBuffer, textureCoordBuffer);
//				branchAttributes, branchBaseCenters, branchTransformationsRow1, branchTransformationsRow2, branchTransformationsRow3, branchTransformationsRow4);
				parentTree.addLeafCluster( childBranch );
			}
			//Child branches are not present. Add leaves
			else if (treeAge > 1){
				parentTree.addLeafCluster( this );
			}
		}	
		
		return nBranchVertices;
	}
		
	public int updateParamValues(FloatBuffer branchVertices, FloatBuffer normalBuffer, FloatBuffer textureCoordBuffer) {
//	FloatBuffer branchAttributes, FloatBuffer branchBaseCenters, FloatBuffer branchTransformationsRow1, FloatBuffer branchTransformationsRow2, FloatBuffer branchTransformationsRow3, FloatBuffer branchTransformationsRow4) {
		int age = parentTree.getAge();
		
		//trunk
		if (parentBranch == null) {
			baseRadius = Tree.trunkBaseRadii[age];
			topRadius = Tree.trunkTopRadii[age];
			length = Tree.trunkLengths[age];
			calculateLengthCone();
		}
		//other branches
		else {
			float scaleFactor = 0.7f;
			
			//parameter values
			baseRadius = parentBranch.topRadius;
			topRadius = parentBranch.topRadius * scaleFactor;
			length = parentBranch.length * scaleFactor;	
			calculateLengthCone();			
			baseCenter.y = parentBranch.length * 0.9f;
		
			//transformation
			calculateTransformation();
		}
		
		//bounding box
		return updateGeometry(branchVertices, normalBuffer, textureCoordBuffer); 
//		branchAttributes, branchBaseCenters, branchTransformationsRow1, branchTransformationsRow2, branchTransformationsRow3, branchTransformationsRow4);	
	}
	
	
	//###################### Bounding box related functions ###################
	public int updateGeometry(FloatBuffer branchVertices, FloatBuffer normalBuffer, FloatBuffer textureCoordBuffer) {
//	FloatBuffer branchAttributes, FloatBuffer branchBaseCenters, FloatBuffer branchTransformationsRow1, FloatBuffer branchTransformationsRow2, FloatBuffer branchTransformationsRow3, FloatBuffer branchTransformationsRow4) {
		//Update bounding box coordinates
		boundingBox_ws.setBLF(-baseRadius, 0, baseRadius);
		boundingBox_ws.setBRF(baseRadius, 0, baseRadius);
		boundingBox_ws.setBLB(-baseRadius, 0, -baseRadius);
		boundingBox_ws.setBRB(baseRadius, 0, -baseRadius);
		boundingBox_ws.setTRF(topRadius, length, topRadius);
		boundingBox_ws.setTLF(-topRadius, length, topRadius);
		boundingBox_ws.setTRB(topRadius, length, -topRadius);
		boundingBox_ws.setTLB(-topRadius, length, -topRadius);
		
		//Transform bounding box coordinates to world space 
		boundingBox_ws.transform(transformation_os2ws);	
		
//		addBranchVertex(branchVertices, boundingBox_ws.getBRF());
//		addBranchVertex(branchVertices, boundingBox_ws.getTRF());
//		addBranchVertex(branchVertices, boundingBox_ws.getBLF());
//		addBranchVertex(branchVertices, boundingBox_ws.getBLF());		
//		addBranchVertex(branchVertices, boundingBox_ws.getTRF());
//		addBranchVertex(branchVertices, boundingBox_ws.getTLF());
//		
//		addBranchVertex(branchVertices, boundingBox_ws.getBLF());
//		addBranchVertex(branchVertices, boundingBox_ws.getTLF());
//		addBranchVertex(branchVertices, boundingBox_ws.getBLB());
//		addBranchVertex(branchVertices, boundingBox_ws.getBLB());
//		addBranchVertex(branchVertices, boundingBox_ws.getTLF());
//		addBranchVertex(branchVertices, boundingBox_ws.getTLB());
//		
//		addBranchVertex(branchVertices, boundingBox_ws.getBLB());
//		addBranchVertex(branchVertices, boundingBox_ws.getTLB());
//		addBranchVertex(branchVertices, boundingBox_ws.getBRB());
//		addBranchVertex(branchVertices, boundingBox_ws.getBRB());
//		addBranchVertex(branchVertices, boundingBox_ws.getTLB());
//		addBranchVertex(branchVertices, boundingBox_ws.getTRB());
//		
//		addBranchVertex(branchVertices, boundingBox_ws.getBRB());
//		addBranchVertex(branchVertices, boundingBox_ws.getTRB());
//		addBranchVertex(branchVertices, boundingBox_ws.getBRF());
//		addBranchVertex(branchVertices, boundingBox_ws.getBRF());
//		addBranchVertex(branchVertices, boundingBox_ws.getTRB());
//		addBranchVertex(branchVertices, boundingBox_ws.getTRF());
		
		boundingBox_ws.populateVBOWithVerticesInfo(branchVertices, normalBuffer, textureCoordBuffer);
		
		int nBranchVertices = 24;
//		Matrix4 ws2osTransformation = getWs2osTransformation();
//		Point3D baseCenter_ws = getBaseCenterWs();
//		for (int counter = 0; counter < nBranchVertices; counter++) {
//			addBranchAttributes(branchAttributes);
//			addBranchBaseCenter(branchBaseCenters, baseCenter_ws);
//			addBranchTransformation(branchTransformationsRow1, branchTransformationsRow2, branchTransformationsRow3, branchTransformationsRow4, ws2osTransformation);
//		}		
		
		return nBranchVertices;		
	}
	
//	private void addBranchVertex(FloatBuffer branchVertices, Point3D vertex) {
//		branchVertices.put(vertex.x);
//		branchVertices.put(vertex.y);
//		branchVertices.put(vertex.z);
//	}
	
//	private void addBranchAttributes(FloatBuffer branchAttributes) {
//		branchAttributes.put(baseRadius);
//		branchAttributes.put(topRadius);
//		branchAttributes.put(length);		
//		branchAttributes.put(lengthCone);
//		/*//For debugging
//		branchAttributes.put(1.0f);
//		branchAttributes.put(0.5f);
//		branchAttributes.put(0.0f);*/
//	}
//	
//	private void addBranchBaseCenter(FloatBuffer branchBaseCenters, Point3D baseCenter_ws) {		
//		branchBaseCenters.put(baseCenter_ws.x);
//		branchBaseCenters.put(baseCenter_ws.y);
//		branchBaseCenters.put(baseCenter_ws.z);
//		
//		/*//For debugging
//		branchBaseCenters.put(0.0f);
//		branchBaseCenters.put(1.0f);
//		branchBaseCenters.put(1.0f);*/
//	}
//	
//	private void addBranchTransformation(FloatBuffer branchTransformationsRow1, FloatBuffer branchTransformationsRow2, 
//			FloatBuffer branchTransformationsRow3, FloatBuffer branchTransformationsRow4, Matrix4 ws2osTransformation) {		
//		
//		for (int col = 0; col < 4; col++)
//			branchTransformationsRow1.put(ws2osTransformation.mm[col][0]);
//		for (int col = 0; col < 4; col++)
//			branchTransformationsRow2.put(ws2osTransformation.mm[col][1]);
//		for (int col = 0; col < 4; col++)
//			branchTransformationsRow3.put(ws2osTransformation.mm[col][2]);
//		for (int col = 0; col < 4; col++)
//			branchTransformationsRow4.put(ws2osTransformation.mm[col][3]);
//		
///*		//For debugging
//		branchTransformationsRow1.put(1.0f);
//		branchTransformationsRow1.put(1.0f);
//		branchTransformationsRow1.put(1.0f);
//		branchTransformationsRow1.put(1.0f);
//		
//		branchTransformationsRow2.put(1.0f);
//		branchTransformationsRow2.put(0.0f);
//		branchTransformationsRow2.put(0.0f);
//		branchTransformationsRow2.put(1.0f);
//		
//		branchTransformationsRow3.put(0.0f);
//		branchTransformationsRow3.put(1.0f);
//		branchTransformationsRow3.put(0.0f);
//		branchTransformationsRow3.put(1.0f);
//		
//		branchTransformationsRow4.put(0.0f);
//		branchTransformationsRow4.put(0.0f);
//		branchTransformationsRow4.put(1.0f);
//		branchTransformationsRow4.put(1.0f);
//*/
//	}
	
	public BoundingBox getBoundingBox_WS() {		
		return boundingBox_ws;
	}

	public void displayBoundingBox(GLAutoDrawable drawable) {
		boundingBox_ws.display(drawable);
		int nChildren = children.size();
		if (nChildren != 0){
			for (int childIndex = 0; childIndex < nChildren; childIndex++) {
				children.get(childIndex).displayBoundingBox(drawable);
			}
		}
	}
	
	//################## Getter methods ###################
//	public float getLength() {
//		return length;
//	}
	
	public Matrix4 getOs2wsTransformation() {
		return transformation_os2ws;
	}
	
	public Matrix4 getWs2osTransformation() {
		Matrix4 transformation_ws2os = new Matrix4();
		transformation_os2ws.getInverse(transformation_ws2os);
		return transformation_ws2os;
	}
	
	public Point3D getBaseCenterWs() {
		if (parentBranch != null)
			return parentBranch.getOs2wsTransformation().multiply(baseCenter);
		else
			return parentTree.getTransformation().multiply(baseCenter);
	}
	
	public float getBaseRadius() {
		return baseRadius;
	}
	
	public float getTopRadius() {
		return topRadius;
	}
	
	public float getLength() {
		return length;
	}
	
	private void calculateTransformation() {
		transformation_os2ws = new Matrix4(baseCenter, true).multiply(rotation_os2ws);
		//trunk
		if (parentBranch == null) {
			transformation_os2ws = parentTree.getTransformation().multiply(transformation_os2ws);
		}
		//other branches
		else {
			transformation_os2ws = parentBranch.getOs2wsTransformation().multiply(transformation_os2ws); 
		}
		
	}
	
	private Point3D getBranchTop() {
		return transformation_os2ws.multiply(new Matrix4(new Point3D(0, length, 0), true)).multiply(new Point3D());
	}
	
	private void calculateLengthCone() {
		float r1_minus_r2 = baseRadius - topRadius;	//Rmax - Rmin
		float s = (float)Math.sqrt(r1_minus_r2*r1_minus_r2 + length*length);
		float theta = (float)Math.acos(length/s);	//Height/s
		float s_cone = baseRadius/(float)Math.sin(theta);
		lengthCone = (float) Math.cos(theta)*s_cone;
		//System.out.println("barnch level " + level + " length cone " + lengthCone);
	}
	
	//#################### Fruit related methods #########################
	public void addFruit() {
		int nChildBranches = children.size(); 
		if (nChildBranches == 0) {
			parentTree.addFruit( getBranchTop().subtract(new Point3D(0.0f, 2.0f, 0.0f)) );
		}
		else {
			for (int childBranchIndex = 0; childBranchIndex < nChildBranches; childBranchIndex++) {
				children.get(childBranchIndex).addFruit();
			}
		}
	}
	
	//############### Collision related methods ##########################
	public boolean isIntersecting(Ray ray, float collisionDetectionDistance) {
		return boundingBox_ws.isIntersecting(ray, collisionDetectionDistance);
	}
	
	public boolean isIntersectingChildren(Ray ray, float collisionDetectionDistance) {
		TreeBranch childBranch;
		for (int childIndex = 0; childIndex < children.size(); childIndex++) {
			childBranch = children.get(childIndex);
			if (childBranch.isIntersecting(ray, collisionDetectionDistance) == true)
				return true;
			if (childBranch.isIntersectingChildren(ray, collisionDetectionDistance) == true)
				return true;			
		}
		return false;
	}
}
