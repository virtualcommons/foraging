package edu.asu.commons.foraging.jcal3d.misc;

import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Ray;
import edu.asu.commons.foraging.graphics.Vector3D;

public class Plane {
	public float a;
	public float b;
	public float c;
	public float d;
	
	//Default constructor
	public Plane() {}
	
	//Copy constructor
	public Plane(Plane p) {
		a = p.a;
		b = p.b;
		c = p.c;
		d = p.d;
	}
	
	public float eval(Point3D p) {
	   return p.x * a + p.y * b + p.z * c + d;
	}

	public void setPosition(Point3D p) {
	   d = -p.x * a - p.y * b - p.z * c;
	}
	
	public void translate(Point3D t) {
		d += -t.x * a - t.y * b - t.z * c;		
	}
	
	//TODO: Currently this function rotates by -90 degrees around X axis
	//which is hardcoded for trees. Make this generalized
	public void rotate(Quaternion q) {
		float temp = b;
		b = c;
		c = -temp;
	}

	public void setNormal(Vector3D v) {
	     a = v.x;
	     b = v.y;
	     c = v.z;
	     d = -1e32f;
	}

	public float dist(Point3D p) {
		return (float)Math.abs( (p.x*a+p.y*b+p.z*c+d)/Math.sqrt(a*a+b*b+c*c));
	}	
	
	public float intersectsAt(Ray ray) {
		Point3D rayStartPoint = ray.getOrigin();
		Vector3D rayDirection = ray.getDirection();
		
		float distance;
		float denominator = rayDirection.x*a + rayDirection.y*b + rayDirection.z*c;
		if (denominator == 0) 
			distance = -1;
		else 
			distance = (-rayStartPoint.x*a - rayStartPoint.y*b - rayStartPoint.z*c - d) / denominator;
		
		return distance;
	}
}

