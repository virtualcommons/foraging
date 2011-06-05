package edu.asu.commons.foraging.visualization.forestry.vbo;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import edu.asu.commons.foraging.graphics.Matrix4;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.util.Tuple2f;

public class LeafCluster {
	
	private Point3D bottom;
	private Point3D top;
	private Point3D left;
	private Point3D right;
	private Matrix4 transformation_os2ws = null;
	private Tree parentTree;
	
	public static Tuple2f bottomTexCoord = new Tuple2f(0.1f, 0.9f);
	public static Tuple2f topTexCoord = new Tuple2f(0.9f, 0.1f);
	public static Tuple2f leftTexCoord = new Tuple2f(0.9f, 0.9f);
	public static Tuple2f rightTexCoord = new Tuple2f(0.1f, 0.1f);
	
	public LeafCluster(Matrix4 transformation_os2ws, Tree parentTree) {
		this.transformation_os2ws = transformation_os2ws;
		this.parentTree = parentTree;
		
		float size = 0;
		int age = parentTree.getAge();
		if (age < 6) {
			size = age;
		}
		else {
			size = age * 1.5f;
		}
		updateGeometry(size, size);
	}
	
	private void updateGeometry(float width, float height) {
		float halfWidth = width / 2.0f;
		float halfHeight = height / 2.0f;
		
		bottom = transformation_os2ws.multiply(new Point3D(0, 0, 0));		
		left = transformation_os2ws.multiply(new Point3D(-halfWidth, halfHeight, 0));
		right = transformation_os2ws.multiply(new Point3D(halfWidth, halfHeight, 0));
		top = transformation_os2ws.multiply(new Point3D(0, height, 0));
	}

	public Point3D getLeft() {
//		System.out.println("Leaf left " + left);
		return left;
	}

	public Point3D getBottom() {
//		System.out.println("Leaf bottom " + bottom);
		return bottom;
	}

	public Point3D getRight() {
//		System.out.println("Leaf right " + right);
		return right;
	}

	public Point3D getTop() {
//		System.out.println("Leaf top " + top);
		return top;
	}

	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		
//		gl.glPushMatrix();
//		gl.glRotatef(roll, 	1, 0, 0);
//		gl.glRotatef(yaw, 	0, 1, 0);
//		gl.glRotatef(pitch, 0, 0, 1);
		
		gl.glBegin(GL.GL_TRIANGLES);
		
		gl.glVertex3f(left.x, left.y, left.z);
		gl.glVertex3f(bottom.x, bottom.y, bottom.z);
		gl.glVertex3f(right.x, right.y, right.z);
		
		gl.glVertex3f(left.x, left.y, left.z);
		gl.glVertex3f(right.x, right.y, right.z);
		gl.glVertex3f(top.x, top.y, top.z);
		gl.glEnd();
//		gl.glPopMatrix();
	}
	
	
}
