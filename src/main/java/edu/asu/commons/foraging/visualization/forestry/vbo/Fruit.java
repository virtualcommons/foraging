package edu.asu.commons.foraging.visualization.forestry.vbo;

import java.nio.FloatBuffer;

import edu.asu.commons.foraging.graphics.BoundingBox;
import edu.asu.commons.foraging.graphics.Point3D;

public class Fruit {

	private static final long serialVersionUID = -4093601236305771423L;
	
	private Point3D location = null;	//This is in world space
	private float elevationStep = 0.0f;
	private float targetElevation = 0.0f;
	private Tree parentTree = null;
	private BoundingBox boundingBox_ws = new BoundingBox(true);
	private static float radius = 0.5f; 
	private float height = radius;
	private static float minHeight = radius/5.0f;
	private static float htOfClrChange = radius/2.0f;
	
	public Fruit(Point3D location, Tree tree) {	
		this.parentTree = tree;
		this.location = location;
		updateBoundingBox();
	}
	
	private void updateBoundingBox() {
		//Update bounding box coordinates
		boundingBox_ws.setBLF(location.x-radius, location.y-height, location.z+radius);
		boundingBox_ws.setBRF(location.x+radius, location.y-height, location.z+radius);
		boundingBox_ws.setBLB(location.x-radius, location.y-height, location.z-radius);
		boundingBox_ws.setBRB(location.x+radius, location.y-height, location.z-radius);
		boundingBox_ws.setTRF(location.x+radius, location.y+height, location.z+radius);
		boundingBox_ws.setTLF(location.x-radius, location.y+height, location.z+radius);
		boundingBox_ws.setTRB(location.x+radius, location.y+height, location.z-radius);
		boundingBox_ws.setTLB(location.x-radius, location.y+height, location.z-radius);
		
		boundingBox_ws.configure();
	}

	/*public void display(GLAutoDrawable drawable) {
		boundingBox_ws.display(drawable);
	}*/
	
	public int createVBO(FloatBuffer vertexBuffer, FloatBuffer normalBuffer, FloatBuffer textureCoordBuffer) {
//	FloatBuffer attributeBuffer, FloatBuffer baseCenterBuffer, FloatBuffer transformationRow1Buffer, FloatBuffer transformationRow2Buffer, FloatBuffer transformationRow3Buffer, FloatBuffer transformationRow4Buffer) {
		
		int nVertices = boundingBox_ws.populateVBOWithVerticesInfo(vertexBuffer, normalBuffer, textureCoordBuffer);
//		for (int vertexIndex = 0; vertexIndex < nVertices; vertexIndex++) {
//			addAttributes2VBO(attributeBuffer);
//			addBaseCenter2VBO(baseCenterBuffer);
//			addTransformation2VBO(transformationRow1Buffer, transformationRow2Buffer, transformationRow3Buffer, transformationRow4Buffer);
//		}
				
		return nVertices;
	}
	
	public int updateVBO(FloatBuffer vertexBuffer, FloatBuffer normalBuffer, FloatBuffer textureCoordBuffer) { 
//	FloatBuffer attributeBuffer, FloatBuffer baseCenterBuffer, FloatBuffer transformationRow4Buffer) {		
		int nVertices = boundingBox_ws.populateVBOWithVerticesInfo(vertexBuffer, normalBuffer, textureCoordBuffer);
//		for (int vertexIndex = 0; vertexIndex < nVertices; vertexIndex++) {
//			addAttributes2VBO(attributeBuffer);
//			addBaseCenter2VBO(baseCenterBuffer);
//			
//			transformationRow4Buffer.put(location.x);
//			transformationRow4Buffer.put(location.y);
//			transformationRow4Buffer.put(location.z);
//			transformationRow4Buffer.put(1.0f);
//		}
				
		return nVertices;
	}
	
	private void addAttributes2VBO(FloatBuffer attributeBuffer) {
		attributeBuffer.put(radius);
		attributeBuffer.put(0.0f);
		attributeBuffer.put(height);
		attributeBuffer.put(0.0f);
	}
	
	private void addBaseCenter2VBO(FloatBuffer baseCenterBuffer) {
		Point3D baseCenter = location;//.subtract(new Point3D(0, radius, 0));
		baseCenterBuffer.put(baseCenter.x);
		baseCenterBuffer.put(baseCenter.y);
		baseCenterBuffer.put(baseCenter.z);
	}
	
	private void addTransformation2VBO(FloatBuffer transformationRow1Buffer, FloatBuffer transformationRow2Buffer, 
			FloatBuffer transformationRow3Buffer, FloatBuffer transformationRow4Buffer) {
		
		transformationRow1Buffer.put(1.0f);
		transformationRow1Buffer.put(0.0f);
		transformationRow1Buffer.put(0.0f);
		transformationRow1Buffer.put(0.0f);
		
		transformationRow2Buffer.put(0.0f);
		transformationRow2Buffer.put(1.0f);
		transformationRow2Buffer.put(0.0f);
		transformationRow2Buffer.put(0.0f);
		
		transformationRow3Buffer.put(0.0f);
		transformationRow3Buffer.put(0.0f);
		transformationRow3Buffer.put(1.0f);
		transformationRow3Buffer.put(0.0f);
		
		transformationRow4Buffer.put(location.x);
		transformationRow4Buffer.put(location.y);
		transformationRow4Buffer.put(location.z);
		transformationRow4Buffer.put(1.0f);		
	}
	
	public void updateElevation(long timeLeft) {
		float newElevation = location.y + elevationStep;
		if (newElevation >= targetElevation) {
			location.y = newElevation;
			
			if (timeLeft < 6000 && height > minHeight) {
				height -= 0.05f;
			}
			updateBoundingBox();
		}
	}
	
	public void calculateElevationStep(float targetElevation, int steps) {
		this.targetElevation = targetElevation;
		float radius = 1.0f;
		elevationStep = ( targetElevation - (location.y+radius) ) / steps;
	}
	
	public Point3D getLocation() {
		return location;
	}
		
	public float getHeight() {
		return height;
	}
	
	public static float getHtOfClrChange() {
		return htOfClrChange;
	}
}
