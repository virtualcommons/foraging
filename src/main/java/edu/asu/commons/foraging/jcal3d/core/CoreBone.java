package edu.asu.commons.foraging.jcal3d.core;

import java.util.LinkedList;
import java.util.Vector;

import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.jcal3d.misc.BoundingBox;
import edu.asu.commons.foraging.jcal3d.misc.Plane;
import edu.asu.commons.foraging.jcal3d.misc.Quaternion;



public class CoreBone {

	  protected String name;
	  protected CoreSkeleton coreSkeleton = null;
	  protected int parentId = -1;
	  protected LinkedList<Integer> childBoneIds = new LinkedList<Integer>();
	  protected Vector3D translation;
	  protected Quaternion rotation;
	  protected Vector3D translationAbsolute;
	  protected Quaternion rotationAbsolute;
	  protected Vector3D translationBoneSpace;
	  protected Quaternion rotationBoneSpace;
	  protected Object userData = null;
//
	  protected BoundingBox boundingBox = new BoundingBox();
	  protected Point3D boundingPosition[] = new Point3D[6];
	  protected boolean boundingBoxPrecomputed = false;
	  	  
	  /* Constructs the core bone instance.
	   *
	   * This function is the default constructor of the core bone instance.
	   */
	  public CoreBone(String name) {
		  this.name = name;		  	  
	  }
	  
	  //############ Add Methods ##################	  
	  /* Adds a child ID.
	   *
	   * This function adds a core bone ID to the child ID list of the core bone
	   * instance.
	   *
	   * @param childId The ID of the core bone ID that shoud be added to the child
	   *                ID list.
	   *
	   * @return One of the following values:
	   *         \li \b true if successful
	   *         \li \b false if an error happend
	   */
	  public boolean addChildId(int childId) {
		  childBoneIds.add(childId);
		  return true;
	  }

	  
	/* Calculates the current state.
	 *
	 * This function calculates the current state (absolute translation and
	 * rotation) of the core bone instance and all its children.
	 */
	public void calculateState() {
	  if(parentId == -1) {
	    // no parent, this means absolute state == relative state
	    translationAbsolute = translation;
	    rotationAbsolute = rotation;
	  }
	  else
	  {
		  // get the parent bone
		  CoreBone parent = coreSkeleton.getCoreBone(parentId);

		  // transform relative state with the absolute state of the parent
		  translationAbsolute = translation.multiply(parent.getRotationAbsolute()).add(parent.getTranslationAbsolute());
		  rotationAbsolute = rotation.multiply(parent.getRotationAbsolute());
	  }

	  // calculate all child bones	  
	  for(int childId = 0; childId < childBoneIds.size(); ++childId) {
		  coreSkeleton.getCoreBone(childBoneIds.get(childId)).calculateState();
	  }
	  return;
	}
	
	/* Calculates the bounding box.
	 *
	 * This function Calculates the bounding box of the core bone instance.
	 *
	 * @param pCoreModel The coreModel (needed for vertices data.
	 */
	public void calculateBoundingBox(CoreModel coreModel) {
	   int boneId =  coreSkeleton.getCoreBoneId(name);
	   boolean bBoundsComputed = false;
	   int planeId;
	   
	   Quaternion rot = new Quaternion(rotationBoneSpace);	  
	   rot.invert();
	   
	   Vector3D dir = new Vector3D(1.0f, 0.0f, 0.0f);
	   dir = dir.multiply(rot);
	   boundingBox.plane[0] = new Plane();
	   boundingBox.plane[0].setNormal(dir);

	   dir = new Vector3D(-1.0f,0.0f,0.0f);
	   dir = dir.multiply(rot);
	   boundingBox.plane[1] = new Plane();
	   boundingBox.plane[1].setNormal(dir);

	   dir = new Vector3D(0.0f,1.0f,0.0f);
	   dir = dir.multiply(rot);
	   boundingBox.plane[2] = new Plane();
	   boundingBox.plane[2].setNormal(dir);

	   dir = new Vector3D(0.0f,-1.0f,0.0f);
	   dir = dir.multiply(rot);
	   boundingBox.plane[3] = new Plane();
	   boundingBox.plane[3].setNormal(dir);

	   dir = new Vector3D(0.0f,0.0f,1.0f);
	   dir = dir.multiply(rot);
	   boundingBox.plane[4] = new Plane();
	   boundingBox.plane[4].setNormal(dir);

	   dir = new Vector3D(0.0f,0.0f,-1.0f);
	   dir = dir.multiply(rot);
	   boundingBox.plane[5] = new Plane();
	   boundingBox.plane[5].setNormal(dir);
	   	   
	   for(int meshId = 0; meshId < coreModel.getCoreMeshCount(); ++meshId) {
	       CoreMesh coreMesh = coreModel.getCoreMesh(meshId);
		   
	       for(int submeshId = 0; submeshId < coreMesh.getCoreSubmeshCount(); submeshId++) {
			   CoreSubmesh coreSubmesh = coreMesh.getCoreSubmesh(submeshId);
			   
			   if(coreSubmesh.getSpringCount() == 0) {				   
				   Vector<CoreSubmesh.Vertex> vertices =  coreSubmesh.getVertices();
				   for(int vertexId = 0; vertexId < vertices.size(); ++vertexId) {
					   for(int influenceId = 0; influenceId < vertices.get(vertexId).vectorInfluence.size(); ++influenceId) {
						   if(vertices.get(vertexId).vectorInfluence.get(influenceId).boneId == boneId && vertices.get(vertexId).vectorInfluence.get(influenceId).weight > 0.5f)
						   {						   
							   for(planeId = 0; planeId < 6; ++planeId)
							   {
								   if(boundingBox.plane[planeId].eval( vertices.get(vertexId).position ) < 0.0f)
								   {
									   boundingBox.plane[planeId].setPosition(vertices.get(vertexId).position);
									   boundingPosition[planeId]=vertices.get(vertexId).position;
									   bBoundsComputed=true;
								   }
//								   else {
//									   boundingBox.plane[planeId].setPosition(new Point3D());
//									   boundingPosition[planeId] = new Point3D();
//								   }
							   }
						   }
					   }
				   }	
			   }
		   }
	   }

	   // To handle bones with no vertices assigned 
	   if(!bBoundsComputed) 
	   { 
		   for(planeId = 0; planeId < 6; ++planeId) 
		   { 
			   boundingBox.plane[planeId].setPosition(translation); 
			   boundingPosition[planeId] = translation; 
		   } 
	   } 
	   
	   boundingBoxPrecomputed = true;
	}


	//############### get Methods ################
	/* Returns the child ID list.
	 *
	 * This function returns the list that contains all child IDs of the core bone
	 * instance.
	 *
	 * @return A reference to the child ID list.
	 */
	public LinkedList<Integer> getListChildId() {
	  return childBoneIds;
	}
	
	/* Returns the name.
	 *
	 * This function returns the name of the core bone instance.
	 *
	 * @return The name as string.
	 */
	public String getName() {
	  return this.name;
	}
	
	/* Returns the parent ID.
	 *
	 * This function returns the parent ID of the core bone instance.
	 *
	 * @return One of the following values:
	 *         \li the \b ID of the parent
	 *         \li \b -1 if the core bone instance is a root core bone
	 */
	public int getParentId() {
	  return this.parentId;
	}
	 
	/* Returns the rotation.
	 *
	 * This function returns the relative rotation of the core bone instance.
	 *
	 * @return The relative rotation to the parent as quaternion.
	 */
	public Quaternion getRotation() {
	  return rotation;
	}
	 
	/* Returns the absolute rotation.
	 *
	 * This function returns the absolute rotation of the core bone instance.
	 *
	 * @return The absolute rotation to the parent as quaternion.
	 */
	public Quaternion getRotationAbsolute()	{
	  return rotationAbsolute;
	}
	 
	/* Returns the bone space rotation.
	 *
	 * This function returns the rotation to bring a point into the core bone
	 * instance space.
	 *
	 * @return The rotation to bring a point into bone space.
	 */
	public Quaternion getRotationBoneSpace() {
	  return rotationBoneSpace;
	}

	/* Returns the translation.
	 *
	 * This function returns the relative translation of the core bone instance.
	 *
	 * @return The relative translation to the parent as quaternion.
	 */
	public Vector3D getTranslation() {
	  return translation;
	}

	/* Returns the absolute translation.
	 *
	 * This function returns the absolute translation of the core bone instance.
	 *
	 * @return The absolute translation to the parent as quaternion.
	 */
	public Vector3D getTranslationAbsolute() {
	  return translationAbsolute;
	}

	/* Returns the bone space translation.
	 *
	 * This function returns the translation to bring a point into the core bone
	 * instance space.
	 *
	 * @return The translation to bring a point into bone space.
	 */
	public Vector3D getTranslationBoneSpace() {
	  return translationBoneSpace;
	}

	/* Provides access to the core skeleton.
	 *
	 * This function returns the core skeleton.
	 *
	 * @return One of the following values:
	 *         \li a pointer to the core skeleton
	 *         \li \b 0 if an error happend
	 */
	public CoreSkeleton getCoreSkeleton() {
	  return coreSkeleton;
	}

	/* Returns the current bounding box.
	 *
	 * This function returns the current bounding box of the core bone instance.
	 *
	 * @return bounding box.
	 */
	public BoundingBox getBoundingBox() {
	   return boundingBox;
	}

	public Point3D getBoundingData(int planeId) {
	   return boundingPosition[planeId];
	}


	/* Provides access to the user data.
	 *
	 * This function returns the user data stored in the core bone instance.
	 *
	 * @return The user data stored in the core bone instance.
	 */
	public Object getUserData() {
	  return userData;
	}
	 
	//################ Set Methods ########################
	/* Sets the core skeleton.
	 *
	 * This function sets the core skeleton to which the core bone instance is
	 * attached to.
	 *
	 * @param pCoreSkeleton The core skeleton to which the core bone instance
	 *                      should be attached to.
	 */
	public void setCoreSkeleton(CoreSkeleton coreSkeleton) {
		this.coreSkeleton = coreSkeleton;
	}

	/* Sets the parent ID.
	 *
	 * This function sets the parent ID of the core bone instance.
	 *
	 * @param parentId The ID of the parent that should be set.
	 */
	public void setParentId(int parentId) {
	  this.parentId = parentId;
	}
	 
	/* Sets the rotation.
	 *
	 * This function sets the relative rotation of the core bone instance.
	 *
	 * @param rotation The relative rotation to the parent as quaternion.
	 */
	public void setRotation(Quaternion rotation) {
		this.rotation = rotation;
	}

	/* Sets the bone space rotation.
	 *
	 * This function sets the rotation that brings a point into the core bone
	 * instance space.
	 *
	 * @param rotation The rotation that brings a point into bone space.
	 */
	public void setRotationBoneSpace(Quaternion rotation) {
		this.rotationBoneSpace = rotation;
	}

	/* Sets the translation.
	 *
	 * This function sets the relative translation of the core bone instance.
	 *
	 * @param translation The relative translation to the parent as vector.
	 */
	public void setTranslation(Vector3D translation) {
		this.translation = translation;
	}

	/* Sets the bone space translation.
	 *
	 * This function sets the translation that brings a point into the core bone
	 * instance space.
	 *
	 * @param translation The translation that brings a point into bone space.
	 */
	public void setTranslationBoneSpace(Vector3D translation) {
		this.translationBoneSpace = translation;
	}

	/* Stores user data.
	 *
	 * This function stores user data in the core bone instance.
	 *
	 * @param userData The user data that should be stored.
	 */
	public void setUserData(Object userData) {
	  this.userData = userData;
	}
	 
	public boolean isBoundingBoxPrecomputed() {
		return boundingBoxPrecomputed;
	}

	/* Scale the core bone.
	 *
	 * This function rescale all the data that are in the core bone instance and
	 * in his childs.
	 *
	 * @param factor A float with the scale factor
	 *
	 */
	public void scale(float factor) {
		translation = (Vector3D)translation.multiply(factor);
		translationAbsolute = (Vector3D)translationAbsolute.multiply(factor);
		translationBoneSpace = (Vector3D)translationBoneSpace.multiply(factor);
		
		// calculate all child bones		
		for(int childBoneId = 0; childBoneId < childBoneIds.size(); ++childBoneId) {
			coreSkeleton.getCoreBone(childBoneId).scale(factor);
		}
	}	
}
