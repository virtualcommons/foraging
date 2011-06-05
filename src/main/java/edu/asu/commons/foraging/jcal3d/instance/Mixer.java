package edu.asu.commons.foraging.jcal3d.instance;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.jcal3d.core.CoreAnimation;
import edu.asu.commons.foraging.jcal3d.core.CoreKeyframe;
import edu.asu.commons.foraging.jcal3d.core.CoreTrack;
import edu.asu.commons.foraging.jcal3d.misc.Error;
import edu.asu.commons.foraging.jcal3d.misc.Quaternion;


public class Mixer implements AbstractMixer{

	protected Model model;
	protected Vector<Animation> animations = new Vector<Animation>();
	protected LinkedList<AnimationAction> animationActionList = new LinkedList<AnimationAction>();
	protected LinkedList<AnimationCycle> animationCycleList = new LinkedList<AnimationCycle>();
	protected float animationTime;
	protected float animationDuration;
	protected float timeFactor;
	  
	/* Constructs the mixer instance.
	 *
	 * This function is the default constructor of the mixer instance.
	 */
	public Mixer(Model model) {
	  //FIXME: Make sure that model is not null
	  this.model = model;

	  // build the animation table
	  int coreAnimationCount = model.getCoreModel().getCoreAnimationCount();

	  animations.setSize(coreAnimationCount);	  

	  // set the animation time/duration values to default
	  animationTime = 0.0f;
	  animationDuration = 0.0f;
	  timeFactor = 1.0f;
	}

	 /*****************************************************************************/
	/** Interpolates the weight of an animation cycle.
	  *
	  * This function interpolates the weight of an animation cycle to a new value
	  * in a given amount of time. If the specified animation cycle is not active
	  * yet, it is activated.
	  *
	  * @param id The ID of the animation cycle that should be blended.
	  * @param weight The weight to interpolate the animation cycle to.
	  * @param delay The time in seconds until the new weight should be reached.
	  *
	  * @return One of the following values:
	  *         \li \b true if successful
	  *         \li \b false if an error happend
	  *****************************************************************************/
	public boolean blendCycle(int id, float weight, float delay) {
	  if((id < 0) || (id >= (int)animations.size()))
	  {
	    Error.setLastError(Error.INVALID_HANDLE, "", -1, "");
	    return false;
	  }

	  // get the animation for the given id
	  Animation animation = animations.get(id);

	  // create a new animation instance if it is not active yet
	  if(animation == null) {
	    // take the fast way out if we are trying to clear an inactive animation
	    if(weight == 0.0f) return true;

	    // get the core animation
	    CoreAnimation coreAnimation = model.getCoreModel().getCoreAnimation(id);
	    if(coreAnimation == null) return false;

		LinkedList<CoreTrack> coreTracks = coreAnimation.getCoreTracks();

	    if(coreTracks.size() == 0) return false;
	    
		CoreTrack coreTrack = coreTracks.get(0);
	    if(coreTrack == null) return false;

		CoreKeyframe lastKeyframe = coreTrack.getCoreKeyframe(coreTrack.getCoreKeyframeCount()-1);
	    if(lastKeyframe == null) return false;

		if(lastKeyframe.getTime() < coreAnimation.getDuration()) {			
			for(int trackIndex = 0; trackIndex < coreTracks.size(); trackIndex++) {
				coreTrack = coreTracks.get(trackIndex);

	            CoreKeyframe firstKeyframe = coreTrack.getCoreKeyframe(0);
				CoreKeyframe newKeyframe = new CoreKeyframe();

				newKeyframe.setTranslation(firstKeyframe.getTranslation());
	            newKeyframe.setRotation(firstKeyframe.getRotation());
	            newKeyframe.setTime(coreAnimation.getDuration());

				coreTrack.addCoreKeyframe(newKeyframe);
			}
		}

	    // allocate a new animation cycle instance
	    AnimationCycle animationCycle = new AnimationCycle(coreAnimation);
	    // insert new animation into the tables
	    animations.setElementAt(animationCycle, id);
	    animationCycleList.addFirst(animationCycle);

	    // blend the animation
	    return animationCycle.blend(weight, delay);
	  }

	  // check if this is really a animation cycle instance
	  if(animation.getType() != Animation.TYPE_CYCLE) {
	      Error.setLastError(Error.INVALID_ANIMATION_TYPE, "", -1, "");
	      return false;
	  }

	  // clear the animation cycle from the active vector if the target weight is zero
	  if(weight == 0.0f) {
	      animations.setElementAt(null, id);
	  }

	  // cast it to an animation cycle
	  AnimationCycle animationCycle = (AnimationCycle)animation;

	  // blend the animation cycle
	  animationCycle.blend(weight, delay);
	  animationCycle.checkCallbacks(0, model);
	  return true;
	}

	 /*****************************************************************************/
	/** Fades an animation cycle out.
	  *
	  * This function fades an animation cycle out in a given amount of time.
	  *
	  * @param id The ID of the animation cycle that should be faded out.
	  * @param delay The time in seconds until the the animation cycle is
	  *              completely removed.
	  *
	  * @return One of the following values:
	  *         \li \b true if successful
	  *         \li \b false if an error happend
	  *****************************************************************************/

	public boolean clearCycle(int id, float delay) {
	  // check if the animation id is valid
	  if((id < 0) || (id >= (int)animations.size())) {
	    Error.setLastError(Error.INVALID_HANDLE, "", -1, "");
	    return false;
	  }

	  // get the animation for the given id
	  Animation animation = animations.get(id);

	  // we can only clear cycles that are active
	  if(animation == null) return true;

	  // check if this is really a animation cycle instance
	  if(animation.getType() != Animation.TYPE_CYCLE) {
	      Error.setLastError(Error.INVALID_ANIMATION_TYPE, "", -1, "");
	      return false;
	  }

	  // clear the animation cycle from the active vector
	  animations.setElementAt(null, id);

	  // cast it to an animation cycle
	  AnimationCycle animationCycle = (AnimationCycle)animation;

	  // set animation cycle to async state
	  animationCycle.setAsync(animationTime, animationDuration);

	  // blend the animation cycle
	  animationCycle.blend(0.0f, delay);
	  animationCycle.checkCallbacks(0, model);
	  return true;
	}

	/*****************************************************************************/
	/** Executes an animation action.
	  *
	  * This function executes an animation action.
	  *
	  * @param id The ID of the animation action that should be blended.
	  * @param delayIn The time in seconds until the animation action reaches the
	  *                full weight from the beginning of its execution.
	  * @param delayOut The time in seconds in which the animation action reaches
	  *                 zero weight at the end of its execution.
	  * @param weightTarget The weight to interpolate the animation action to.
	  * @param autoLock     This prevents the Action from being reset and removed
	  *                     on the last keyframe if true.
	  *
	  * @return One of the following values:
	  *         \li \b true if successful
	  *         \li \b false if an error happend
	  *****************************************************************************/
	public boolean executeAction(int id, float delayIn, float delayOut, float weightTarget, boolean autoLock) {
	  // get the core animation
	  CoreAnimation coreAnimation = model.getCoreModel().getCoreAnimation(id);
	  if(coreAnimation == null) {
	    return false;
	  }

	  // allocate a new animation action instance
	  AnimationAction animationAction = new AnimationAction(coreAnimation);
	  // insert new animation into the table
	  animationActionList.add(animationAction);

	  // execute the animation
	  animationAction.execute(delayIn, delayOut, weightTarget, autoLock);
	  animationAction.checkCallbacks(0, model);
	  return true;
	}

	/*****************************************************************************/
	/** Clears an active animation action.
	  *
	  * This function removes an animation action from the blend list.  This is
	  * particularly useful with auto-locked actions on their last frame.
	  *
	  * @param id The ID of the animation action that should be removed.
	  *
	  * @return One of the following values:
	  *         \li \b true if successful
	  *         \li \b false if an error happened or action was not found
	  *****************************************************************************/
	public boolean removeAction(int id) {
	  // get the core animation
	  CoreAnimation coreAnimation = model.getCoreModel().getCoreAnimation(id);
	  if(coreAnimation == null)
	  {
	    return false;
	  }

	  // update all active animation actions of this model
	  ListIterator<AnimationAction> iteratorAnimationAction = animationActionList.listIterator();  

	  while(iteratorAnimationAction.hasNext()) {
		  // find the specified action and remove it
		  AnimationAction animationAction = iteratorAnimationAction.next();
		  if(animationAction.getCoreAnimation().equals(coreAnimation) ) {
			  // found, so remove
			  animationAction.completeCallbacks(model);			  
			  iteratorAnimationAction.remove();
			  return true;
	    }	    
	  }
	  return false;
	}

	 /*****************************************************************************/
	/** Updates all active animations.
	  *
	  * This function updates all active animations of the mixer instance for a
	  * given amount of time.
	  *
	  * @param deltaTime The elapsed time in seconds since the last update.
	  *****************************************************************************/

	public void updateAnimation(float deltaTime) {
	  // update the current animation time
	  if(animationDuration == 0.0f) {
	    animationTime = 0.0f;
	  }
	  else {
	    animationTime += deltaTime * timeFactor;
	    if(animationTime >= animationDuration)
	    {
	      //animationTime = (float) fmod(animationTime, animationDuration);
	    	//FIXME: Replace the code below if there is a fmod equivalent in Java
	    	animationTime = animationTime - ((int)(animationTime/animationDuration))*animationDuration;
	    }
		if (animationTime < 0)
	      animationTime += animationDuration;
	  }

	  // update all active animation actions of this model
	  ListIterator<AnimationAction> iteratorAnimationAction = animationActionList.listIterator();

	  while(iteratorAnimationAction.hasNext()) {
	    // update and check if animation action is still active
		  AnimationAction animationAction = iteratorAnimationAction.next();
	    if(animationAction.update(deltaTime)) {
	      animationAction.checkCallbacks(animationTime, model);	      
	    }
	    else {
	      // animation action has ended, destroy and remove it from the animation list
	    	animationAction.completeCallbacks(model);	      
	    	iteratorAnimationAction.remove();
	    }
	  }

	  // todo: update all active animation poses of this model

	  // update the weight of all active animation cycles of this model
	  ListIterator<AnimationCycle> iteratorAnimationCycle = animationCycleList.listIterator();

	  float accumulatedWeight, accumulatedDuration;
	  accumulatedWeight = 0.0f;
	  accumulatedDuration = 0.0f;

	  while(iteratorAnimationCycle.hasNext()) {
	    // update and check if animation cycle is still active
		  AnimationCycle animationCycle = iteratorAnimationCycle.next();
	    if(animationCycle.update(deltaTime)) {
	      // check if it is in sync. if yes, update accumulated weight and duration
	      if(animationCycle.getState() == Animation.STATE_SYNC) {
	        accumulatedWeight += animationCycle.getWeight();
	        accumulatedDuration += animationCycle.getWeight() * animationCycle.getCoreAnimation().getDuration();
	      }

	      animationCycle.checkCallbacks(animationTime, model);	      
	    }
	    else {
	      // animation cycle has ended, destroy and remove it from the animation list
	      animationCycle.completeCallbacks(model);	      
	      iteratorAnimationCycle.remove();
	    }
	  }

	  // adjust the global animation cycle duration
	  if(accumulatedWeight > 0.0f)
	  {
	    animationDuration = accumulatedDuration / accumulatedWeight;
	  }
	  else
	  {
	    animationDuration = 0.0f;
	  }	  
	}

	public void updateSkeleton() {
	  // get the skeleton we need to update	
	   Skeleton skeleton = model.getSkeleton();
	  if(skeleton == null) return;

	  // clear the skeleton state
	  skeleton.clearState();
	  
	  // loop through all animation actions	  
	  for(int animationActionIndex = 0; animationActionIndex < animationActionList.size(); ++animationActionIndex) {
	    // get the core animation instance
	    CoreAnimation coreAnimation = animationActionList.get(animationActionIndex).getCoreAnimation();

	    // get the list of core tracks of above core animation
	    LinkedList<CoreTrack> coreTracks = coreAnimation.getCoreTracks();

	    // loop through all core tracks of the core animation	    
	    for(int coreTrackIndex = 0; coreTrackIndex < coreTracks.size(); ++coreTrackIndex) {
	    	int boneId = coreTracks.get(coreTrackIndex).getCoreBoneId();
	    	Bone bone = skeleton.getBones().get(boneId);
	    	
	      // get the current translation and rotation
	      float animationTime = animationActionList.get(animationActionIndex).getTime();
	      Vector3D translation = coreTracks.get(coreTrackIndex).getTranslation(animationTime);
	      Quaternion rotation = coreTracks.get(coreTrackIndex).getRotation(animationTime);
	      
	      //get the appropriate bone of the track and blend the bone state with the new state
	      bone.blendState(animationActionList.get(animationActionIndex).getWeight(), translation, rotation);
	      skeleton.setBone(bone, boneId);
	    }
	  }

	  // lock the skeleton state
	 skeleton.lockState();

	  //loop through all animation cycles	  
	  for(int animationCycleIndex = 0; animationCycleIndex < animationCycleList.size(); ++animationCycleIndex) {
	    // get the core animation instance
	    CoreAnimation coreAnimation = animationCycleList.get(animationCycleIndex).getCoreAnimation();

	    // calculate adjusted time
	    float animationTime;
	    if(animationCycleList.get(animationCycleIndex).getState() == Animation.STATE_SYNC)
	    {
	      if(animationDuration == 0.0f) {
	        animationTime = 0.0f;
	      }
	      else {
	        animationTime = this.animationTime * coreAnimation.getDuration() / animationDuration;
	      }
	    }
	    else {
	      animationTime = animationCycleList.get(animationCycleIndex).getTime();
	    }	        

	    // get the list of core tracks of above core animation
	    LinkedList<CoreTrack> coreTracks = coreAnimation.getCoreTracks();

	    // loop through all core tracks of the core animation	    
	    for(int coreTrackIndex = 0; coreTrackIndex < coreTracks.size(); ++coreTrackIndex) {
	      // get the appropriate bone of the track
	    	int boneId = coreTracks.get(coreTrackIndex).getCoreBoneId();
	    	Bone bone = skeleton.getBones().get(boneId);	      

	      // get the current translation and rotation
	      Vector3D translation = coreTracks.get(coreTrackIndex).getTranslation(animationTime);
	      Quaternion rotation = coreTracks.get(coreTrackIndex).getRotation(animationTime);
	      	      
	      // blend the bone state with the new state
	      float weight = animationCycleList.get(animationCycleIndex).getWeight();
	      bone.blendState(weight, translation, rotation);
	    }
	  }

	  // lock the skeleton state
	  skeleton.lockState();

	  // let the skeleton calculate its final state
	  skeleton.calculateState();	  
	}

	/*****************************************************************************/
	/** Returns the animation time.
	  *
	  * This function returns the animation time of the mixer instance.
	  *
	  * @return The animation time in seconds.
	  *****************************************************************************/


	public float getAnimationTime() {
		return animationTime;
	}

	/*****************************************************************************/
	/** Returns the animation duration.
	  *
	  * This function returns the animation duration of the mixer instance.
	  *
	  * @return The animation duration in seconds.
	  *****************************************************************************/


	public float getAnimationDuration() {
		return animationDuration;
	}


	/*****************************************************************************/
	/** Sets the animation time.
	  *
	  * This function sets the animation time of the mixer instance.
	  *
	  *****************************************************************************/


	public void setAnimationTime(float animationTime) {
		this.animationTime = animationTime;
	}

	/*****************************************************************************/
	/** Set the time factor.
	  * 
	  * This function sets the time factor of the mixer instance.
	  * this time factor affect only sync animation
	  *
	  *****************************************************************************/
	public void setTimeFactor(float timeFactor) {
	    this.timeFactor = timeFactor;
	}

	/*****************************************************************************/
	/** Get the time factor.
	  * 
	  * This function return the time factor of the mixer instance.
	  *
	  *****************************************************************************/

	public float getTimeFactor() {
	    return timeFactor;
	}

	public boolean isDefaultMixer() { 
		return true; 
	}
}
