package edu.asu.commons.foraging.jcal3d.core;

import java.util.ListIterator;
import java.util.Vector;

import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.jcal3d.misc.Error;
import edu.asu.commons.foraging.jcal3d.misc.Quaternion;



public class CoreTrack {

	// The index of the associated CoreBone in the CoreSkeleton.
	protected int coreBoneId = -1;

	//List of keyframes, always sorted by time.
	protected Vector<CoreKeyframe> keyframes = new Vector<CoreKeyframe>();

	//Constructor
	public CoreTrack() {
		
	}

	/* Adds a core keyframe.
	 *
	 * This function adds a core keyframe to the core track instance.
	 *
	 * @param pCoreKeyframe A pointer to the core keyframe that should be added.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean addCoreKeyframe(CoreKeyframe coreKeyframe) {
	  keyframes.add(coreKeyframe);	  
	  int index = keyframes.size() - 1;
	  while (index > 0 && keyframes.get(index).getTime() < keyframes.get(index - 1).getTime()) {
		  CoreKeyframe temp = keyframes.get(index);
		  keyframes.setElementAt(keyframes.get(index - 1), index);
		  keyframes.setElementAt(temp, index - 1);
		  --index;
	  }

	  return true;
	}
	
	/* Returns the ID of the core bone.
	 *
	 * This function returns the ID of the core bone to which the core track
	 * instance is attached to.
	 *
	 * @return One of the following values:
	 *         \li the \b ID of the core bone
	 *         \li \b -1 if an error happend
	 */
	public int getCoreBoneId() {
	  return coreBoneId;
	}

	/* Returns a specified state.
	 * 
	 * Thes 2 functions return the state (translation and rotation of the core bone)
	 * for the specified time and duration.
	 *
	 * @param time The time in seconds at which the state should be returned.
	 * @param translation A reference to the translation reference that will be
	 *                    filled with the specified state.
	 * @param rotation A reference to the rotation reference that will be filled
	 *                 with the specified state.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public Vector3D getTranslation(float time) {
	  ListIterator<CoreKeyframe> iteratorCoreKeyframeAfter;

	  // get the keyframe after the requested time
	  iteratorCoreKeyframeAfter = getUpperBound(time);

	  //check if the time is after the last keyframe
	  if(! iteratorCoreKeyframeAfter.hasNext()) {
		  // return the last keyframe state		  
		  return keyframes.lastElement().getTranslation();    
	  }

	  // check if the time is before the first keyframe
	  if(! iteratorCoreKeyframeAfter.hasPrevious()) {
		  // return the first keyframe state
		  return keyframes.firstElement().getTranslation();    
	  }

	  // get the keyframe before the requested one
	  CoreKeyframe coreKeyframeAfter = iteratorCoreKeyframeAfter.next();
	  iteratorCoreKeyframeAfter.previous(); //points to coreKeyframeAfter
	  CoreKeyframe coreKeyframeBefore = iteratorCoreKeyframeAfter.previous();//points to coreKeyframeBefore
	  
	  // calculate the blending factor between the two keyframe states
	  float blendFactor;
	  blendFactor = (time - coreKeyframeBefore.getTime()) / (coreKeyframeAfter.getTime() - coreKeyframeBefore.getTime());

	  // blend between the two keyframes
	  Vector3D translation = coreKeyframeBefore.getTranslation();
	  translation = translation.blend(blendFactor, coreKeyframeAfter.getTranslation());

	  return translation;
	}
	
	public Quaternion getRotation(float time) {
		ListIterator<CoreKeyframe> iteratorCoreKeyframeAfter;

		// get the keyframe after the requested time
		iteratorCoreKeyframeAfter = getUpperBound(time);

		//check if the time is after the last keyframe
		if(! iteratorCoreKeyframeAfter.hasNext()) {
			// return the last keyframe state		  
			return keyframes.lastElement().getRotation();    
		}

		// check if the time is before the first keyframe
		if(! iteratorCoreKeyframeAfter.hasPrevious()) {
			// return the first keyframe state
			return keyframes.firstElement().getRotation();    
		}

		// get the keyframe before the requested one
		CoreKeyframe coreKeyframeAfter = iteratorCoreKeyframeAfter.next();	  
		iteratorCoreKeyframeAfter.previous();//points to coreKeyframeAfter
		CoreKeyframe coreKeyframeBefore = iteratorCoreKeyframeAfter.previous();//points to coreKeyframeAfter--
		  
		// calculate the blending factor between the two keyframe states
		float blendFactor;
		blendFactor = (time - coreKeyframeBefore.getTime()) / (coreKeyframeAfter.getTime() - coreKeyframeBefore.getTime());

		// blend between the two keyframes
		Quaternion rotation = coreKeyframeBefore.getRotation();
		rotation = rotation.blend(blendFactor, coreKeyframeAfter.getRotation());

		return rotation;
	}
	
	/* Sets the ID of the core bone.
	 *
	 * This function sets the ID of the core bone to which the core track instance
	 * is attached to.
	 *
	 * @param coreBoneId The ID of the bone to which the core track instance should
	 *                   be attached to.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean setCoreBoneId(int coreBoneId) {
	  if(coreBoneId < 0)
	  {
	    Error.setLastError(Error.INVALID_HANDLE, "", -1, "");
	    return false;
	  }

	  this.coreBoneId = coreBoneId;

	  return true;
	}

	public int getCoreKeyframeCount() {
	  return keyframes.size();
	}

	public CoreKeyframe getCoreKeyframe(int index) {
	  return keyframes.get(index);
	}

	/* Scale the core track.
	 *
	 * This function rescale all the data that are in the core track instance.
	 *
	 * @param factor A float with the scale factor
	 *
	 */
	public void scale(float factor) {
	  for(int keyframeId = 0; keyframeId < keyframes.size(); keyframeId++) {
		  Vector3D translation = keyframes.get(keyframeId).getTranslation();
		  translation = (Vector3D)translation.multiply(factor);
		  keyframes.get(keyframeId).setTranslation(translation);
	  }

	}
	
	private ListIterator<CoreKeyframe> getUpperBound(float time) {
		int lowerBound = 0;
		int upperBound = keyframes.size()-1;

		while(lowerBound < upperBound-1) {
			int middle = (lowerBound + upperBound)/2;
			if(time >= keyframes.get(middle).getTime()) {
				lowerBound = middle;
			}
			else {
				upperBound = middle;
			}
		}

		//FIXME: Make sure that this is never null
		return keyframes.listIterator(upperBound);
	}

}
