package edu.asu.commons.foraging.jcal3d.misc;

import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Ray;
import edu.asu.commons.foraging.graphics.Triangle;
import edu.asu.commons.foraging.graphics.ViewSettings;

public class BoundingBox {
	public Plane plane[] = new Plane[6];
	protected Vector<Triangle> triangles = null;
	
	//Default constructor
	public BoundingBox() {
		
	}	
	
	//Copy constructor
	public BoundingBox(BoundingBox bb)
	{
		for (int planeIndex = 0; planeIndex < 6; planeIndex++) {
			plane[planeIndex] = new Plane(bb.plane[planeIndex]);
		}
	}
	
	//TODO: Also add rotation and scaling
	public void transform(Point3D translation) {
		for (int planeIndex = 0; planeIndex < 6; planeIndex++) {
			//plane[planeIndex].rotate(rotation);
			plane[planeIndex].translate(translation);
		}
		//Boundingbox extents changed. Repopulate triangle's vector
		populateTriangles();
	}
	
	public void transform(Point3D translation, int planeIndex) {
		//plane[planeIndex].rotate(rotation);
		plane[planeIndex].translate(translation);
		
		//Boundingbox extents changed. Repopulate triangle's vector
		populateTriangles();
	}
	
	private void populateTriangles() {
		triangles = new Vector<Triangle>();
		Vector<Point3D> points = computePoints();

		//plane 1
		triangles.add( new Triangle(points.elementAt(0), points.elementAt(2), points.elementAt(1)) );
		triangles.add( new Triangle(points.elementAt(1), points.elementAt(2), points.elementAt(3)) );
		
		//plane 2
		triangles.add( new Triangle(points.elementAt(4), points.elementAt(5), points.elementAt(7)) );
		triangles.add( new Triangle(points.elementAt(4), points.elementAt(7), points.elementAt(6)) );
		
		//plane 3
		//triangles.add( new Triangle(points.elementAt(0), points.elementAt(0), points.elementAt(0)) );
		//triangles.add( new Triangle(points.elementAt(0), points.elementAt(0), points.elementAt(0)) );
		
		//plane 4
		//triangles.add( new Triangle(points.elementAt(0), points.elementAt(0), points.elementAt(0)) );
		//triangles.add( new Triangle(points.elementAt(0), points.elementAt(0), points.elementAt(0)) );
		
		//plane 5
		triangles.add( new Triangle(points.elementAt(4), points.elementAt(2), points.elementAt(0)) );
		triangles.add( new Triangle(points.elementAt(6), points.elementAt(2), points.elementAt(4)) );
		
		//plane 6
		triangles.add( new Triangle(points.elementAt(5), points.elementAt(1), points.elementAt(3)) );
		triangles.add( new Triangle(points.elementAt(5), points.elementAt(3), points.elementAt(7)) );
	}
	
    	
	/* Computes points of a bounding box.
	 *
	 * This function computes the 8 points of a bounding box.
	 *
	 * @param p A pointer to CalVector[8], the 8 points of the bounding box
	 */
	public Vector<Point3D> computePoints() {
	    Matrix m = new Matrix();
	     
	    int i,j,k;	    
	    Vector<Point3D> points = new Vector<Point3D>();
	        
	    for(i=0;i<2;i++)
	       for(j=2;j<4;j++)
	           for(k=4;k<6;k++)
	        {
	           float x,y,z;
	           Point3D p = new Point3D();
	           
	           //Solve equations of three planes 
	           m.dxdx=plane[i].a;m.dxdy=plane[i].b;m.dxdz=plane[i].c;        
	           m.dydx=plane[j].a;m.dydy=plane[j].b;m.dydz=plane[j].c;        
	           m.dzdx=plane[k].a;m.dzdy=plane[k].b;m.dzdz=plane[k].c;
	           
	           float det = m.det();
	           
	           if(det!=0)
	           {
	              m.dxdx=-plane[i].d;m.dxdy=plane[i].b;m.dxdz=plane[i].c;        
	              m.dydx=-plane[j].d;m.dydy=plane[j].b;m.dydz=plane[j].c;        
	              m.dzdx=-plane[k].d;m.dzdy=plane[k].b;m.dzdz=plane[k].c;
	              
	              x=m.det()/det;

	              m.dxdx=plane[i].a;m.dxdy=-plane[i].d;m.dxdz=plane[i].c;        
	              m.dydx=plane[j].a;m.dydy=-plane[j].d;m.dydz=plane[j].c;        
	              m.dzdx=plane[k].a;m.dzdy=-plane[k].d;m.dzdz=plane[k].c;
	              
	              y=m.det()/det;

	              m.dxdx=plane[i].a;m.dxdy=plane[i].b;m.dxdz=-plane[i].d;        
	              m.dydx=plane[j].a;m.dydy=plane[j].b;m.dydz=-plane[j].d;        
	              m.dzdx=plane[k].a;m.dzdy=plane[k].b;m.dzdz=-plane[k].d;
	  
	              z=m.det()/det;
	              
	              p.x=x;p.y=y;p.z=z;
	           }
	           else {p.x=0.0f;p.y=0.0f;p.z=0.0f;}
	           
	           points.add(p);
	        } 
	    
	    return points;
	 }  
	
	public void display(GLAutoDrawable drawable) {
		//Self bounding box		
		GL gl = drawable.getGL();
			   
		gl.glEnable(GL.GL_COLOR_MATERIAL);
		gl.glColor3f(1.0f, 1.0f, 1.0f);
				
		if (triangles == null)
			populateTriangles();
		
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
		gl.glBegin(GL.GL_TRIANGLES);
		for (int triangleIndex = 0; triangleIndex < triangles.size(); triangleIndex++) {
			Triangle t = triangles.get(triangleIndex);
			Point3D a = t.a();
			Point3D b = t.b();
			Point3D c = t.c();
			gl.glVertex3f(a.x,a.y,a.z);
			gl.glVertex3f(b.x,b.y,b.z);
			gl.glVertex3f(c.x,c.y,c.z);
		}
		gl.glEnd();
		
//		gl.glBegin(GL.GL_LINES);
//		Vector<Point3D> points = computePoints();
//	
//		Point3D point = points.get(0);
//		gl.glVertex3f(point.x,point.y,point.z);
//		point = points.get(1);
//		gl.glVertex3f(point.x,point.y,point.z);
//	
//		point = points.get(0);
//		gl.glVertex3f(point.x,point.y,point.z);
//		point = points.get(2);
//		gl.glVertex3f(point.x,point.y,point.z);
//
//		  point = points.get(1);
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(3);
//		  gl.glVertex3f(point.x,point.y,point.z);
//
//		  point = points.get(2);		  
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(3);
//		  gl.glVertex3f(point.x,point.y,point.z);
//	
//		  point = points.get(4);
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(5);
//		  gl.glVertex3f(point.x,point.y,point.z);
//	
//		  point = points.get(4);
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(6);
//		  gl.glVertex3f(point.x,point.y,point.z);
//	
//		  point = points.get(5);
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(7);
//		  gl.glVertex3f(point.x,point.y,point.z);
//	
//		  point = points.get(6);
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(7);
//		  gl.glVertex3f(point.x,point.y,point.z);
//	
//		  point = points.get(0);
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(4);
//		  gl.glVertex3f(point.x,point.y,point.z);
//	
//		  point = points.get(1);
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(5);
//		  gl.glVertex3f(point.x,point.y,point.z);
//	
//		  point = points.get(2);
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(6);
//		  gl.glVertex3f(point.x,point.y,point.z);
//	
//		  point = points.get(3);
//		  gl.glVertex3f(point.x,point.y,point.z);
//		  point = points.get(7);
//		  gl.glVertex3f(point.x,point.y,point.z);  
		  		   
		  gl.glEnd();
		  
		  gl.glPolygonMode(GL.GL_FRONT_AND_BACK, ViewSettings.fillModel);
		  gl.glDisable(GL.GL_COLOR_MATERIAL);		  
	}
	
	public boolean intersects(Ray direction, float length) {
//		float distance = -1;
//		for (int planeIndex = 0; planeIndex < 6; planeIndex++) {
//			distance = plane[planeIndex].intersectsAt(direction);
//			if (distance > -1 && distance < length)
//			{
//				return true;
//			}
//		}
//		return false;
		
		for (int triangleIndex = 0; triangleIndex < triangles.size(); triangleIndex++) {
			Triangle t = triangles.get(triangleIndex);
			float param = t.getIntersectionParam(direction);
			if (param >= 0 && param <= length) {
				return true;
			}			
		}
		return false;
	}
}
