package edu.asu.commons.foraging.jcal3d.instance;

import java.util.ListIterator;

import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.jcal3d.core.CoreBone;
import edu.asu.commons.foraging.jcal3d.misc.BoundingBox;
import edu.asu.commons.foraging.jcal3d.misc.Matrix;
import edu.asu.commons.foraging.jcal3d.misc.Plane;
import edu.asu.commons.foraging.jcal3d.misc.Quaternion;


public class Bone {
	  protected CoreBone coreBone;
	  protected Skeleton skeleton = null;
	  protected float accumulatedWeight;
	  protected float accumulatedWeightAbsolute;
	  protected Vector3D translation;
	  protected Quaternion rotation;
	  protected Vector3D translationAbsolute;
	  protected Quaternion rotationAbsolute;
	  protected Vector3D translationBoneSpace;
	  protected Quaternion rotationBoneSpace;
	  protected Matrix transformMatrix;  
	  protected BoundingBox boundingBox = new BoundingBox();
	  
	  public Bone(CoreBone coreBone) {
		  //FIXME: Make sure that coreBone is not null
		  //assert(coreBone);
		  this.coreBone = coreBone;
		  clearState();
	  }
	 
	/** Interpolates the current state to another state.
	  *
	  * This function interpolates the current state (relative translation and
	  * rotation) of the bone instance to another state of a given weight.
	  *
	  * @param weight The blending weight.
	  * @param translation The relative translation to be interpolated to.
	  * @param rotation The relative rotation to be interpolated to.
	  *****************************************************************************/
	public void blendState(float weight, Vector3D translation, Quaternion rotation) {
	  if(accumulatedWeightAbsolute == 0.0f) {
	    // it is the first state, so we can just copy it into the bone state
	    translationAbsolute = translation;
	    rotationAbsolute = rotation;

	    accumulatedWeightAbsolute = weight;
	  }
	  else {
	    // it is not the first state, so blend all attributes
	    float factor;
	    factor = weight / (accumulatedWeightAbsolute + weight);

	    translationAbsolute.blend(factor, translation);
	    rotationAbsolute.blend(factor, rotation);

	    accumulatedWeightAbsolute += weight;
	  }
	}

	 /*****************************************************************************/
	/** Calculates the current state.
	  *
	  * This function calculates the current state (absolute translation and
	  * rotation, as well as the bone space transformation) of the bone instance
	  * and all its children.
	  *****************************************************************************/
	public void calculateState() {
	  // check if the bone was not touched by any active animation
	  if(accumulatedWeight == 0.0f) {
	    // set the bone to the initial skeleton state
	    translation = coreBone.getTranslation();
	    rotation = coreBone.getRotation();
	  }

	  // get parent bone id
	  int parentId;
	  parentId = coreBone.getParentId();

	  if(parentId == -1) {
	    // no parent, this means absolute state == relative state
	    translationAbsolute = translation;
	    rotationAbsolute = rotation;
	  }
	  else {
	    // get the parent bone
	    Bone parent = skeleton.getBone(parentId);

	    // transform relative state with the absolute state of the parent
	    translationAbsolute = translation.multiply(parent.getRotationAbsolute()).add(parent.getTranslationAbsolute());
	    rotationAbsolute = rotation.multiply(parent.getRotationAbsolute());	    
	  }

	  // calculate the bone space transformation
	  translationBoneSpace = coreBone.getTranslationBoneSpace();
	  translationBoneSpace = translationBoneSpace.multiply(rotationAbsolute);
	  translationBoneSpace = translationBoneSpace.add(translationAbsolute);	  
	  
	  rotationBoneSpace = coreBone.getRotationBoneSpace();
	  rotationBoneSpace = rotationBoneSpace.multiply(rotationAbsolute);
	  
	  // Generate the vertex transform.  If I ever add support for bone-scaling
	  // to Cal3D, this step will become significantly more complex.
	  transformMatrix = Matrix.convert(rotationBoneSpace);

	  // calculate all child bones	 
	  ListIterator<Integer> boneIterator = coreBone.getListChildId().listIterator();
	  while(boneIterator.hasNext()) {	  
		  skeleton.getBone(boneIterator.next()).calculateState();
	  }
	}

	 /*****************************************************************************/
	/** Clears the current state.
	  *
	  * This function clears the current state (absolute translation and rotation)
	  * of the bone instance and all its children.
	  *****************************************************************************/
	public void clearState() {
	  accumulatedWeight = 0.0f;
	  accumulatedWeightAbsolute = 0.0f;
	}


	 /*****************************************************************************/
	/** Provides access to the core bone.
	  *
	  * This function returns the core bone on which this bone instance is based on.
	  *
	  * @return One of the following values:
	  *         \li a pointer to the core bone
	  *         \li \b 0 if an error happend
	  *****************************************************************************/
	public CoreBone getCoreBone() {
	  return coreBone;
	}

	 /*****************************************************************************/
	/** Resets the bone to its core state
	  *
	  * This function changes the state of the bone to its default non-animated
	  * position and orientation. Child bones are unaffected and may be animated
	  * independently. 
	  *****************************************************************************/

	public void setCoreState() {
	   // set the bone to the initial skeleton state
	   translation = coreBone.getTranslation();
	   rotation = coreBone.getRotation();

	   // set the appropriate weights
	   accumulatedWeightAbsolute = 1.0f;
	   accumulatedWeight = 1.0f ;

	   calculateState() ;
	}

	 /*****************************************************************************/
	/** Resets the bone and children to core states
	  *
	  * This function changes the state of the bone to its default non-animated
	  * position and orientation. All child bones are also set in this manner.
	  *****************************************************************************/

	public void setCoreStateRecursive() {
	  // set the bone to the initial skeleton state
	  translation = coreBone.getTranslation();
	  rotation = coreBone.getRotation();

	  // set the appropriate weights
	  accumulatedWeightAbsolute = 1.0f;
	  accumulatedWeight = 1.0f ;

	  // set core state for all child bones
	  ListIterator<Integer> iteratorChildId = coreBone.getListChildId().listIterator();
	  while(iteratorChildId.hasNext()) {
	    skeleton.getBone(iteratorChildId.next()).setCoreStateRecursive();
	  }

	  calculateState() ;
	}

	 /*****************************************************************************/
	/** Sets the current rotation.
	  *
	  * This function sets the current relative rotation of the bone instance.
	  * Caveat: For this change to appear, calculateState() must be called 
	  * afterwards.
	  *****************************************************************************/
	public void setRotation(Quaternion rotation){
	  this.rotation = rotation;
	  accumulatedWeightAbsolute = 1.0f;
	  accumulatedWeight = 1.0f ;
	}

	 /*****************************************************************************/
	/** Returns the current rotation.
	  *
	  * This function returns the current relative rotation of the bone instance.
	  *
	  * @return The relative rotation to the parent as quaternion.
	  *****************************************************************************/

	public Quaternion getRotation() {
	  return rotation;
	}

	 /*****************************************************************************/
	/** Returns the current absolute rotation.
	  *
	  * This function returns the current absolute rotation of the bone instance.
	  *
	  * @return The absolute rotation to the parent as quaternion.
	  *****************************************************************************/
	public Quaternion getRotationAbsolute() {
	  return rotationAbsolute;
	}

	 /*****************************************************************************/
	/** Returns the current bone space rotation.
	  *
	  * This function returns the current rotation to bring a point into the bone
	  * instance space.
	  *
	  * @return The rotation to bring a point into bone space.
	  *****************************************************************************/

	public Quaternion getRotationBoneSpace() {
	  return rotationBoneSpace;
	}

	 /*****************************************************************************/
	/** Sets the current translation.
	  *
	  * This function sets the current relative translation of the bone instance.
	  * Caveat: For this change to appear, calculateState() must be called 
	  * afterwards.
	  *****************************************************************************/

	public void setTranslation(Vector3D translation) {
	  this.translation = translation;
	  accumulatedWeightAbsolute = 1.0f;
	  accumulatedWeight = 1.0f ;
	}

	 /*****************************************************************************/
	/** Returns the current translation.
	  *
	  * This function returns the current relative translation of the bone instance.
	  *
	  * @return The relative translation to the parent as quaternion.
	  *****************************************************************************/

	public Vector3D getTranslation() {
	  return translation;
	}

	 /*****************************************************************************/
	/** Returns the current absolute translation.
	  *
	  * This function returns the current absolute translation of the bone instance.
	  *
	  * @return The absolute translation to the parent as quaternion.
	  *****************************************************************************/

	public Vector3D getTranslationAbsolute() {
	  return translationAbsolute;
	}

	 /*****************************************************************************/
	/** Returns the current bone space translation.
	  *
	  * This function returns the current translation to bring a point into the
	  *bone instance space.
	  *
	  * @return The translation to bring a point into bone space.
	  *****************************************************************************/

	public Vector3D getTranslationBoneSpace() {
	  return translationBoneSpace;
	}

	 /*****************************************************************************/
	/** Returns the current bone space translation.
	  *
	  * This function returns the current translation to bring a point into the
	  *bone instance space.
	  *
	  * @return The translation to bring a point into bone space.
	  *****************************************************************************/

	public Matrix getTransformMatrix() {
	  return transformMatrix;
	}


	 /*****************************************************************************/
	/** Locks the current state.
	  *
	  * This function locks the current state (absolute translation and rotation)
	  * of the bone instance and all its children.
	  *****************************************************************************/
	public void lockState()	{
	  // clamp accumulated weight
	  if(accumulatedWeightAbsolute > 1.0f - accumulatedWeight) {
	    accumulatedWeightAbsolute = 1.0f - accumulatedWeight;
	  }

	  if(accumulatedWeightAbsolute > 0.0f) {
	    if(accumulatedWeight == 0.0f) {
	      // it is the first state, so we can just copy it into the bone state
	      translation = translationAbsolute;
	      rotation = rotationAbsolute;

	      accumulatedWeight = accumulatedWeightAbsolute;
	    }
	    else {
	      // it is not the first state, so blend all attributes
	      float factor;
	      factor = accumulatedWeightAbsolute / (accumulatedWeight + accumulatedWeightAbsolute);

	      translation.blend(factor, translationAbsolute);
	      rotation.blend(factor, rotationAbsolute);

	      accumulatedWeight += accumulatedWeightAbsolute;
	    }

	    accumulatedWeightAbsolute = 0.0f;
	  }
	}

	 /*****************************************************************************/
	/** Sets the skeleton.
	  *
	  * This function sets the skeleton to which the bone instance is attached to.
	  *
	  * @param pSkeleton The skeleton to which the bone instance should be
	  *                  attached to.
	  *****************************************************************************/

	public void setSkeleton(Skeleton skeleton) {
	  this.skeleton = skeleton;
	}

	 /*****************************************************************************/
	/** Calculates the bounding box.
	  *
	  * This function Calculates the bounding box of the bone instance.
	  *
	  *****************************************************************************/

	public void calculateBoundingBox() {
	   if(!getCoreBone().isBoundingBoxPrecomputed())
		   return;

	   Vector3D dir = new Vector3D(1.0f,0.0f,0.0f);
	   dir = dir.multiply(getTransformMatrix());
	   boundingBox.plane[0] = new Plane();
	   boundingBox.plane[0].setNormal(dir);

	   dir = new Vector3D(-1.0f,0.0f,0.0f);
	   dir = dir.multiply(getTransformMatrix());
	   boundingBox.plane[1] = new Plane();
	   boundingBox.plane[1].setNormal(dir);

	   dir = new Vector3D(0.0f,1.0f,0.0f);
	   dir = dir.multiply(getTransformMatrix());
	   boundingBox.plane[2] = new Plane();
	   boundingBox.plane[2].setNormal(dir);

	   dir = new Vector3D(0.0f,-1.0f,0.0f);
	   dir = dir.multiply(getTransformMatrix());
	   boundingBox.plane[3] = new Plane();
	   boundingBox.plane[3].setNormal(dir);

	   dir = new Vector3D(0.0f,0.0f,1.0f);
	   dir = dir.multiply(getTransformMatrix());
	   boundingBox.plane[4] = new Plane();
	   boundingBox.plane[4].setNormal(dir);

	   dir = new Vector3D(0.0f,0.0f,-1.0f);
	   dir = dir.multiply(getTransformMatrix());
	   boundingBox.plane[5] = new Plane();
	   boundingBox.plane[5].setNormal(dir);
	   
	   int i;
	   
	   for(i=0;i <6; i++)
	   {
	       Point3D position = getCoreBone().getBoundingData(i);	      
	       position = position.multiply(getTransformMatrix());
	       position = position.add(getTranslationBoneSpace());

	       int planeId;
	       for(planeId = 0; planeId < 6; ++planeId)
	       {
	          if(boundingBox.plane[planeId].eval(position) < 0.0f)
	          {
	             boundingBox.plane[planeId].setPosition(position);
	          }
	       }
	       
	   }
	}

	 /*****************************************************************************/
	/** Returns the current bounding box.
	  *
	  * This function returns the current bounding box of the bone instance.
	  *
	  * @return bounding box.
	  *****************************************************************************/


	public BoundingBox getBoundingBox() {
	   return boundingBox;
	}
}
