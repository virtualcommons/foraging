package edu.asu.commons.foraging.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Vector;

import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.jcal3d.instance.Submesh;


public class Buffer {
	public static FloatBuffer getPointBuffer(Vector<Point3D> points) {
		int pointCount = points.size();
		float[] pointArray = new float[pointCount * 3];
		
		int arrayIndex = 0;
		for (int vectorIndex = 0; vectorIndex < pointCount; ++vectorIndex) {
			Point3D point = points.get(vectorIndex);			
			pointArray[arrayIndex++] = point.x;
			pointArray[arrayIndex++] = point.y;
			pointArray[arrayIndex++] = point.z;
		}		
		
		FloatBuffer pointBuffer = ByteBuffer.allocateDirect(pointArray.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		pointBuffer.put(pointArray);
		
		return pointBuffer;
	}
	
	public static FloatBuffer getNomalBuffer(Vector<Vector3D> normals) {
		int normalCount = normals.size();
		float[] normalArray = new float[normalCount * 3];
		
		int arrayIndex = 0;
		for (int vectorIndex = 0; vectorIndex < normalCount; ++vectorIndex) {
			Point3D point = normals.get(vectorIndex);			
			normalArray[arrayIndex++] = point.x;
			normalArray[arrayIndex++] = point.y;
			normalArray[arrayIndex++] = point.z;
		}		
		FloatBuffer normalBuffer = ByteBuffer.allocateDirect(normalArray.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		normalBuffer.put(normalArray);
		
		return normalBuffer;
	}
	
	public static IntBuffer getFaceBuffer(Vector<Submesh.Face> faces) {
		int faceCount = faces.size();
		int[] faceArray = new int[faceCount * 3];
		
		int arrayIndex = 0;
		for (int vectorIndex = 0; vectorIndex < faceCount; ++vectorIndex) {
			Submesh.Face face = faces.get(vectorIndex);
			faceArray[arrayIndex++] = face.vertexId[0];
			faceArray[arrayIndex++] = face.vertexId[1];
			faceArray[arrayIndex++] = face.vertexId[2];
		}
		
		IntBuffer faceBuffer = ByteBuffer.allocateDirect(faceArray.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
		faceBuffer.put(faceArray);
		
		return faceBuffer;
	}
}
