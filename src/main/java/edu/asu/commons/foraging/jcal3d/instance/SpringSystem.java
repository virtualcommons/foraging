package edu.asu.commons.foraging.jcal3d.instance;

import java.util.Vector;

import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.jcal3d.core.CoreSubmesh;
import edu.asu.commons.foraging.jcal3d.misc.BoundingBox;


public class SpringSystem {	
	 protected Model model;
	 protected Vector3D gravity;  
	 protected Vector3D force;  
	 protected boolean collision;

	 /*****************************************************************************/
	 /** Constructs the spring system instance.
	   *
	   * This function is the default constructor of the spring system instance.
	   *****************************************************************************/
	 public SpringSystem(Model model) {
		 //FIXME: Make sure that model is not null	   
		 this.model = model;
	   
		 gravity = new Vector3D(0.0f, 0.0f, -98.1f);
		 // We add this force to simulate some movement
		 force = new Vector3D(0.0f, 0.5f, 0.0f);
		 collision = false;
	 }

	  /*****************************************************************************/
	 /** Calculates the forces on each unbound vertex.
	   *
	   * This function calculates the forces on each unbound vertex of a specific
	   * submesh.
	   *
	   * @param pSubmesh A pointer to the submesh from which the forces should be
	   *                 calculated.
	   * @param deltaTime The elapsed time in seconds since the last calculation.
	   *****************************************************************************/

	 public void calculateForces(Submesh submesh, float deltaTime) {
	   // get the vertex vector of the submesh
	   Vector<Point3D> submeshVertices = submesh.getVertices();

	   // get the vertex vector of the submesh
	   Vector<Submesh.PhysicalProperty> physicalProperties = submesh.getPhysicalProperties();

	   // get the physical property vector of the core submesh
	   Vector<CoreSubmesh.PhysicalProperty> corePhysicalProperties = submesh.getCoreSubmesh().getPhysicalProperties();

	   // loop through all the vertices	   
	   for(int vertexId = 0; vertexId < submeshVertices.size(); ++vertexId) {
	     // get the physical property of the vertex
	     Submesh.PhysicalProperty physicalProperty = physicalProperties.get(vertexId);

	     // get the physical property of the core vertex
	     CoreSubmesh.PhysicalProperty corePhysicalProperty = corePhysicalProperties.get(vertexId);

	     // only take vertices with a weight > 0 into account
	     if(corePhysicalProperty.weight > 0.0f)
	     {
	 		physicalProperty.force = force.add(gravity.multiply(corePhysicalProperty.weight));		
	     }
	   }
	 }

	  /*****************************************************************************/
	 /** Calculates the vertices influenced by the spring system instance.
	   *
	   * This function calculates the vertices influenced by the spring system
	   * instance.
	   *
	   * @param pSubmesh A pointer to the submesh from which the vertices should be
	   *                 calculated.
	   * @param deltaTime The elapsed time in seconds since the last calculation.
	   *****************************************************************************/

	 public void calculateVertices(Submesh submesh, float deltaTime) {
	   // get the vertex vector of the submesh
	   Vector<Point3D> submeshVertices = submesh.getVertices();

	   // get the physical property vector of the submesh
	   Vector<Submesh.PhysicalProperty> physicalProperties = submesh.getPhysicalProperties();

	   // get the physical property vector of the core submesh
	   Vector<CoreSubmesh.PhysicalProperty> corePhysicalProperties = submesh.getCoreSubmesh().getPhysicalProperties();

	   // loop through all the vertices
	   for(int vertexId = 0; vertexId < (int)submeshVertices.size(); ++vertexId) {
	     // get the vertex
	     Point3D vertex = submeshVertices.get(vertexId);

	     // get the physical property of the vertex
	     Submesh.PhysicalProperty physicalProperty = physicalProperties.get(vertexId);

	     // get the physical property of the core vertex
	     CoreSubmesh.PhysicalProperty corePhysicalProperty = corePhysicalProperties.get(vertexId);

	     // store current position for later use	     
	     Point3D position = physicalProperty.position;

	     // only take vertices with a weight > 0 into account
	     if(corePhysicalProperty.weight > 0.0f)
	     {
	       // do the Verlet step
	       physicalProperty.position = physicalProperty.position.add(position.subtract(physicalProperty.positionOld).multiply(0.99f).add( physicalProperty.force.divide(corePhysicalProperty.weight).multiply(deltaTime * deltaTime) ));

	 		Skeleton skeleton = model.getSkeleton();
	 		
	 		if(collision) {
	 			Vector<Bone> bones =  skeleton.getBones();
	 				 			
	 			for(int boneId = 0; boneId < bones.size(); boneId++) {
	 				BoundingBox p = bones.get(boneId).getBoundingBox();
	 				boolean in = true;
	 				float min = Float.MAX_VALUE;
	 				int index = -1;
	 					 				
	 				for(int faceId = 0; faceId < 6 ; faceId++) {				
	 					if(p.plane[faceId].eval(physicalProperty.position)<=0)
	 					{
	 						in=false;
	 					}
	 					else
	 					{
	 						float dist=p.plane[faceId].dist(physicalProperty.position);
	 						if(dist<min)
	 						{
	 							index=faceId;
	 							min=dist;
	 						}
	 					}
	 				}
	 				
	 				if(in && index!=-1)
	 				{
	 					Vector3D normal = new Vector3D(p.plane[index].a, p.plane[index].b, p.plane[index].c);
	 					normal.normalize();
	 					physicalProperty.position = physicalProperty.position.subtract( normal.multiply(min) );
	 				}
	 				
	 				in=true;
	 				
	 				for(int faceId=0; faceId < 6 ; faceId++) {				
	 					if(p.plane[faceId].eval(physicalProperty.position) < 0 )
	 					{
	 						in=false;				
	 					}
	 				}
	 				if(in)
	 				{
	 					physicalProperty.position = submeshVertices.get(vertexId);
	 				}
	 			}
	 		}

	     }
	     else
	     {
	       physicalProperty.position = submeshVertices.get(vertexId);
	     }

	     // make the current position the old one
	     physicalProperty.positionOld = position;

	     // set the new position of the vertex
	     vertex = physicalProperty.position;

	     // clear the accumulated force on the vertex
	     physicalProperty.force.clear();
	   }

	   // get the spring vector of the core submesh
	   Vector<CoreSubmesh.Spring> springs = submesh.getCoreSubmesh().getSprings();

	   // iterate a few times to relax the constraints
	   
	   final int iterationCount = 2;
	   for(int iterationIndex = 0; iterationIndex < iterationCount; ++iterationIndex)
	   {
	     // loop through all the springs	     
	     for(int springIndex = 0; springIndex < springs.size(); ++springIndex)
	     {
	       // get the spring
	       CoreSubmesh.Spring spring = springs.get(springIndex);

	       // compute the difference between the two spring vertices	       
	       Vector3D distance = new Vector3D( submeshVertices.get(spring.vertexId[1]).subtract(submeshVertices.get(spring.vertexId[0])) );

	       // get the current length of the spring	       
	       float length = distance.length();

	       if(length > 0.0f)
	       {
	       	/*if (spring.springCoefficient == 0)
	       	{ 
	       	 	vectorVertex[spring.vertexId[1]] = vectorVertex[spring.vertexId[0]];  
	       	 	vectorPhysicalProperty[spring.vertexId[1]].position = vectorVertex[spring.vertexId[0]]; 
	       	} 
	       	else
	 	{*/
	 	   float factor[] = new float[2];
	 	   factor[0] = (length - spring.idleLength) / length;
	 	   factor[1] = factor[0];
	 	   
	 	   if(corePhysicalProperties.get(spring.vertexId[0]).weight > 0.0f)
	 	   {
	               factor[0] /= 2.0f;
	               factor[1] /= 2.0f;
	            }
	            else
	            {
	              factor[0] = 0.0f;
	            }
	            
	            if(corePhysicalProperties.get(spring.vertexId[1]).weight <= 0.0f)
	            {
	               factor[0] *= 2.0f;
	               factor[1] = 0.0f;
	            }

	            submeshVertices.setElementAt(submeshVertices.get(spring.vertexId[0]).add(distance.multiply(factor[0])), spring.vertexId[0]);
	            physicalProperties.get(spring.vertexId[0]).position = submeshVertices.get(spring.vertexId[0]);

	            submeshVertices.setElementAt(submeshVertices.get(spring.vertexId[1]).subtract( distance.multiply(factor[1]) ), spring.vertexId[1]);
	            physicalProperties.get(spring.vertexId[1]).position = submeshVertices.get(spring.vertexId[1]);
	         //}
	       }
	     }
	   }
	 /* DEBUG-CODE ********************
	   CalVector spherePosition(Sphere.x, Sphere.y, Sphere.z);
	   float sphereRadius = Sphere.radius;

	   // loop through all the vertices
	   for(vertexId = 0; vertexId < (int)vectorVertex.size(); ++vertexId)
	   {
	     // get the vertex
	     CalVector& vertex = vectorVertex[vertexId];

	     // get the physical property of the vertex
	     CalSubmesh::PhysicalProperty& physicalProperty = vectorPhysicalProperty[vertexId];

	     // get the physical property of the core vertex
	     CalCoreSubmesh::PhysicalProperty& corePhysicalProperty = vectorCorePhysicalProperty[vertexId];

	     // only take vertices with a weight > 0 into account
	     if(corePhysicalProperty.weight > 0.0f)
	     {
	       CalVector position;
	       position = physicalProperty.position;
	       position -= spherePosition;

	       float length;
	       length = position.normalize();

	       if(length < sphereRadius)
	       {
	         position *= sphereRadius;
	         position += spherePosition;

	         physicalProperty.position = position;
	         physicalProperty.positionOld = position;
	         vertex = physicalProperty.position;
	       }
	     }
	   }
	 *********************************/
	 }


	  /*****************************************************************************/
	 /** Updates all the spring systems in the attached meshes.
	   *
	   * This functon updates all the spring systems in the attached meshes.
	   *****************************************************************************/

	 public void update(float deltaTime) {
	   // get the attached meshes vector
	   Vector<Mesh> meshes = model.getMeshes();

	   // loop through all the attached meshes	   
	   for(int meshIndex = 0; meshIndex < meshes.size(); ++meshIndex)
	   {
		   Mesh mesh = meshes.get(meshIndex);
	     // get the ssubmesh vector of the mesh
	     Vector<Submesh> submeshes = mesh.getSubmeshes();

	     // loop through all the submeshes of the mesh	     
	     for(int submeshIndex = 0; submeshIndex < submeshes.size(); ++submeshIndex)
	     {
	    	 Submesh submesh = submeshes.get(submeshIndex);
	       // check if the submesh contains a spring system
	       if(submesh.getCoreSubmesh().getSpringCount() > 0 && submesh.hasInternalData())
	       {
	         // calculate the new forces on each unbound vertex
	         calculateForces(submesh, deltaTime);

	         // calculate the vertices influenced by the spring system
	         calculateVertices(submesh, deltaTime);
	       }
	     }
	   }
	 }


	  /*****************************************************************************/
	 /** Returns the gravity vector.
	   *
	   * This function returns the gravity vector of the spring system instance.
	   *
	   * @return the gravity vector as vector.
	   *****************************************************************************/

	 public Vector3D getGravityVector() {
	 	return gravity;
	 }

	  /*****************************************************************************/
	 /** Returns the force vector.
	   *
	   * This function returns the force vector of the spring system instance.
	   *
	   * @return the force vector as vector.
	   *****************************************************************************/


	 public Vector3D getForceVector() {
	 	return force;
	 }

	  /*****************************************************************************/
	 /** Sets the gravity vector.
	   *
	   * This function sets the gravity vector of the spring system instance.
	   *
	   * @param vGravity the gravity vector as vector.
	   *****************************************************************************/

	 public void setGravityVector(Vector3D vGravity) {
	 	this.gravity = vGravity;
	 }

	  /*****************************************************************************/
	 /** Sets the force vector.
	   *
	   * This function sets the force vector of the spring system instance.
	   *
	   * @param vForce the force vector as vector.
	   *****************************************************************************/

	 public void setForceVector(Vector3D vForce)
	 {
	 	this.force = vForce;
	 }

	  /*****************************************************************************/
	 /** Enable or disable the collision system
	   *
	   * @param collision true to enable the collision system else false
	   *****************************************************************************/

	 public void setCollisionDetection(boolean collision)
	 {
	 	this.collision = collision;
	 }

}
