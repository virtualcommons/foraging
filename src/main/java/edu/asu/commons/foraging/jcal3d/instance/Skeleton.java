package edu.asu.commons.foraging.jcal3d.instance;

import java.util.Vector;

import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.jcal3d.core.CoreBone;
import edu.asu.commons.foraging.jcal3d.core.CoreSkeleton;


public class Skeleton {

	protected CoreSkeleton coreSkeleton = null;
	protected Vector<Bone> bones = new Vector<Bone>();
	protected boolean boundingBoxesComputed = false;
	  
	/*****************************************************************************/
	/** Constructs the skeleton instance.
	  *
	  * This function is the default constructor of the skeleton instance.
	  *****************************************************************************/

	public Skeleton(CoreSkeleton coreSkeleton) {
		//FIXME: Make sure that coreSkeleton is not null	  
		this.coreSkeleton = coreSkeleton;

		// clone the skeleton structure of the core skeleton
		Vector<CoreBone> coreBones = coreSkeleton.getCoreBones();

		// get the number of bones
		int boneCount = coreBones.size();

		//reserve space in the bone vector
		this.bones.setSize(boneCount);

		// clone every core bone
		for(int boneId = 0; boneId < boneCount; ++boneId)
		{
			Bone bone = new Bone(coreBones.get(boneId));

			// set skeleton in the bone instance
			bone.setSkeleton(this);

			// insert bone into bone vector
			bones.setElementAt(bone, boneId);
		}
	}

	 /*****************************************************************************/
	/** Calculates the state of the skeleton instance.
	  *
	  * This function calculates the state of the skeleton instance by recursively
	  * calculating the states of its bones.
	  *****************************************************************************/

	public void calculateState() {
	  // calculate all bone states of the skeleton
	  Vector<Integer> rootCoreBoneIds = coreSkeleton.getRootCoreBoneIds();
	  
	  for(int coreBoneIndex = 0; coreBoneIndex < rootCoreBoneIds.size(); coreBoneIndex++) {
		  bones.get(rootCoreBoneIds.get(coreBoneIndex)).calculateState();
	  }
	  	boundingBoxesComputed = false;
	}

	 /*****************************************************************************/
	/** Clears the state of the skeleton instance.
	  *
	  * This function clears the state of the skeleton instance by recursively
	  * clearing the states of its bones.
	  *****************************************************************************/

	public void clearState() {
	  // clear all bone states of the skeleton	  
	  for(int boneIndex = 0; boneIndex < bones.size(); ++boneIndex)
	  {
		  bones.get(boneIndex).clearState();
	  }
	  boundingBoxesComputed=false;
	}


	 /*****************************************************************************/
	/** Provides access to a bone.
	  *
	  * This function returns the bone with the given ID.
	  *
	  * @param boneId The ID of the bone that should be returned.
	  *
	  * @return One of the following values:
	  *         \li a pointer to the bone
	  *         \li \b 0 if an error happend
	  *****************************************************************************/

	public Bone getBone(int boneId) {
	  return bones.get(boneId);
	}

	 /*****************************************************************************/
	/** Provides access to the core skeleton.
	  *
	  * This function returns the core skeleton on which this skeleton instance is
	  * based on.
	  *
	  * @return One of the following values:
	  *         \li a pointer to the core skeleton
	  *         \li \b 0 if an error happend
	  *****************************************************************************/

	public CoreSkeleton getCoreSkeleton() {
	  return coreSkeleton;
	}

	 /*****************************************************************************/
	/** Returns the bone vector.
	  *
	  * This function returns the vector that contains all bones of the skeleton
	  * instance.
	  *
	  * @return A reference to the bone vector.
	  *****************************************************************************/

	public Vector<Bone> getBones() {
	  return bones;
	}

	 /*****************************************************************************/
	/** Locks the state of the skeleton instance.
	  *
	  * This function locks the state of the skeleton instance by recursively
	  * locking the states of its bones.
	  *****************************************************************************/

	public void lockState() {
	  // lock all bone states of the skeleton	  
		for(int boneIndex = 0; boneIndex < bones.size(); ++boneIndex)
		  {
			  bones.get(boneIndex).lockState();
		  }
	}

	/*****************************************************************************/
	/** Calculates axis aligned bounding box of skeleton bones
	  *
	  * @param min The vector where the min values of bb are stored.
	  * @param man The vector where the max values of bb are stored.
	  *
	  *****************************************************************************/
	public Point3D getBoneMinBoundingBox() {
		Point3D min = new Point3D();
		if(!boundingBoxesComputed)
		  {
			  calculateBoundingBoxes();
		  }

		  if (bones.size() > 0) {
		    Vector3D translation = bones.get(0).getTranslationAbsolute();

		    min.x = translation.x;
		    min.y = translation.y;
		    min.z = translation.z;		   
		  }

		  for(int boneIndex = 1; boneIndex < bones.size(); ++boneIndex) {
		    Vector3D translation = bones.get(boneIndex).getTranslationAbsolute();

		    if (translation.x < min.x)
		      min.x = translation.x;
		    
		    if (translation.y < min.y)
		      min.y = translation.y;

		    if (translation.z < min.z)
		      min.z = translation.z;

		  }
		  return min;
	}
	
	public Point3D getBoneMaxBoundingBox() {
		Point3D max = new Point3D();
		if(!boundingBoxesComputed)
		  {
			  calculateBoundingBoxes();
		  }

		  if (bones.size() > 0) {
		    Vector3D translation = bones.get(0).getTranslationAbsolute();

		    max.x = translation.x;
		    max.y = translation.y;
		    max.z = translation.z;		   
		  }

		  for(int boneIndex = 1; boneIndex < bones.size(); ++boneIndex) {
		    Vector3D translation = bones.get(boneIndex).getTranslationAbsolute();

		    if (translation.x > max.x)
		    	max.x = translation.x;
		    
		    if (translation.y > max.y)
		    	max.y = translation.y;

		    if (translation.z > max.z)
		    	max.z = translation.z;

		  }
		  return max;
	}
	
	
	 /*****************************************************************************/
	/** Calculates bounding boxes.
	  *
	  * This function Calculates the bounding box of every bone in the Skeleton.
	  *
	  *****************************************************************************/
	public void calculateBoundingBoxes() {
	   if(boundingBoxesComputed) 
		   return;

	   for(int boneId = 0; boneId < bones.size(); ++boneId) {
	      bones.get(boneId).calculateBoundingBox();
	   }
	   boundingBoxesComputed=true;
	}

//	****************************************************************************//
//	****************************************************************************//
//	****************************************************************************//
//	 DEBUG-/TEST-CODE                                                           //
//	****************************************************************************//
//	****************************************************************************//
//	****************************************************************************//

	public Vector<Point3D> getBonePoints() {
	  Vector<Point3D> points = new Vector<Point3D>();
	  
	  for(int boneIndex = 0; boneIndex < bones.size(); ++boneIndex) {
	    Vector3D translation = bones.get(boneIndex).getTranslationAbsolute();
	    points.add(translation);	  
	  }

	  return points;
	}

	public Vector<Point3D> getBonePointsStatic() {
		Vector<Point3D> points = new Vector<Point3D>();
		  int nrPoints;
		  nrPoints = 0;

		  for(int boneIndex = 0; boneIndex < bones.size(); ++boneIndex) {
		    Vector3D translation = bones.get(boneIndex).getTranslationAbsolute();
		    points.add(translation);
		    nrPoints++;
		  }

		  return points;
	}

	public Vector<Point3D> getBoneLines(){
	  int nrLines;
	  nrLines = 0;
	  Vector<Point3D> points = new Vector<Point3D>();

	  for(int boneIndex = 0; boneIndex < bones.size(); ++boneIndex) {
		Bone bone = bones.get(boneIndex);
	    int parentId = bone.getCoreBone().getParentId();

	    if(parentId != -1) {
	      Bone parent = bones.get(parentId);

	      Vector3D translation = bone.getTranslationAbsolute();
	      Vector3D translationParent = parent.getTranslationAbsolute();

	      points.add(translationParent);
	      points.add(translation);
	      
	      nrLines++;
	    }
	  }

	  return points;
	}

	public Vector<Point3D> getBoneLinesStatic() {
		int nrLines;
		  nrLines = 0;
		  Vector<Point3D> points = new Vector<Point3D>();

		  for(int boneIndex = 0; boneIndex < bones.size(); ++boneIndex) {
			  Bone bone = bones.get(boneIndex);
		    int parentId = bone.getCoreBone().getParentId();

		    if(parentId != -1) {
		      Bone parent = bones.get(parentId);

		      Vector3D translation = bone.getTranslationAbsolute();
		      Vector3D translationParent = parent.getTranslationAbsolute();

		      points.add(translationParent);
		      points.add(translation);
		      
		      nrLines++;
		    }
		  }

		  return points;
	}

	public void setBone(Bone bone, int boneId) {
		bones.setElementAt(bone, boneId);		
	}

}
