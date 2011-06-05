package edu.asu.commons.foraging.jcal3d.instance;

import java.util.Vector;

import edu.asu.commons.foraging.jcal3d.core.CoreAnimation;

public class Animation {
	public static int TYPE_NONE = 0;
	public static int TYPE_CYCLE = 1;
	public static int TYPE_POSE = 2;
	public static int TYPE_ACTION = 3;
	
	public static int STATE_NONE = 0;
	public static int STATE_SYNC = 1;
	public static int STATE_ASYNC = 2;
	public static int STATE_IN = 3;
	public static int STATE_STEADY = 4;
	public static int STATE_OUT = 5;
	public static int STATE_STOPPED = 6;
	
	protected CoreAnimation coreAnimation;
	protected Vector<Float> lastCallbackTimes = new Vector<Float>();
	protected int type;
	protected int state;
	protected float time;
	protected float timeFactor;
	protected float weight;
	
	public Animation(CoreAnimation coreAnimation) {
		this.coreAnimation = coreAnimation;
		this.type = TYPE_NONE;
		this.state = STATE_NONE;
		this.time = 0.0f;
		this.timeFactor = 1.0f;
		this.weight = 0.0f;
		
		//FIXME: Make sure that coreAnimation is not null
		
		Vector<CoreAnimation.CallbackRecord> list = coreAnimation.getCallbackList();
	  for (int callbackIndex = 0; callbackIndex < list.size(); callbackIndex++)
	    lastCallbackTimes.add(0.0f);  // build up the last called list
	}

	/** Provides access to the core animation.
	  *
	  * This function returns the core animation on which this animation instance
	  * is based on.
	  *
	  * @return One of the following values:
	  *         \li a pointer to the core animation
	  *         \li \b 0 if an error happend
	  */
	public CoreAnimation getCoreAnimation() {
	  return coreAnimation;
	}

	/** Returns the state.
	  *
	  * This function returns the state of the animation instance.
	  *
	  * @return One of the following states:
	  *         \li \b STATE_NONE
	  *         \li \b STATE_SYNC
	  *         \li \b STATE_ASYNC
	  *         \li \b STATE_IN
	  *         \li \b STATE_STEADY
	  *         \li \b STATE_OUT
	  */
	public int getState() {
	  return state;
	}

	/** Returns the time.
	  *
	  * This function returns the time of the animation instance.
	  *
	  * @return The time in seconds.
	  */
	public float getTime() {
	  return time;
	}
	 
	/** Returns the type.
	  *
	  * This function returns the type of the animation instance.
	  *
	  * @return One of the following types:
	  *         \li \b TYPE_NONE
	  *         \li \b TYPE_CYCLE
	  *         \li \b TYPE_POSE
	  *         \li \b TYPE_ACTION
	  */
	public int getType() {
	  return type;
	}
	
	/** Returns the weight.
	  *
	  * This function returns the weight of the animation instance.
	  *
	  * @return The weight.
	  */
	public float getWeight() {
	  return weight;
	}

	/** Set the time.
	  *
	  * This function set the time of the animation instance.
	  *
	  */
	public void setTime(float time) {
	    this.time = time;
	}

	/** Set the time factor.
	  * 
	  * This function sets the time factor of the animation instance.
	  * this time factor affect only sync animation
	  *
	  */
	public void setTimeFactor(float timeFactor)	{
	    this.timeFactor = timeFactor;
	}
	
	/** Get the time factor.
	  * 
	  * This function return the time factor of the animation instance.
	  *
	  */
	public float getTimeFactor() {
	    return timeFactor;
	}

	public void checkCallbacks(float animationTime, Model model) {
	  Vector<CoreAnimation.CallbackRecord> list = coreAnimation.getCallbackList();
	  
	  for (int callbackIndex = 0; callbackIndex < list.size(); callbackIndex++) {
	    if (animationTime > 0 && animationTime < lastCallbackTimes.get(callbackIndex))  // looped
	        lastCallbackTimes.setElementAt(lastCallbackTimes.get(callbackIndex) - coreAnimation.getDuration(), callbackIndex);
	    else if (animationTime < 0 && animationTime > lastCallbackTimes.get(callbackIndex))     // reverse-looped  
	    	lastCallbackTimes.setElementAt(lastCallbackTimes.get(callbackIndex) + coreAnimation.getDuration(), callbackIndex);
	  
	    if ((animationTime >= 0 && animationTime >= lastCallbackTimes.get(callbackIndex) + list.get(callbackIndex).minInterval) ||
	        (animationTime <  0 && animationTime <= lastCallbackTimes.get(callbackIndex) - list.get(callbackIndex).minInterval))
	    {
	      list.get(callbackIndex).callback.animationUpdate(animationTime,model);
	      lastCallbackTimes.setElementAt(animationTime, callbackIndex);
	    }
	  }
	}

	public void completeCallbacks(Model model) {
	  Vector<CoreAnimation.CallbackRecord> list = coreAnimation.getCallbackList();
	  for (int callbackIndex = 0; callbackIndex < list.size(); callbackIndex++)
	    list.get(callbackIndex).callback.animationComplete(model);
	}

	protected void setType(int type) {
	    this.type = type;
	}

	protected void setState(int state) {
	    this.state = state;
	}

	protected void setWeight(float weight) {
		this.weight = weight;
	}

}
