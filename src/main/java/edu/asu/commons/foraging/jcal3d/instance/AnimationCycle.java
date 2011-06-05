package edu.asu.commons.foraging.jcal3d.instance;

import edu.asu.commons.foraging.jcal3d.core.CoreAnimation;

public class AnimationCycle extends Animation {
	 protected float targetDelay;
	 protected float targetWeight;
	 
	 public AnimationCycle(CoreAnimation coreAnimation) {
		 super(coreAnimation);
		 setType(TYPE_CYCLE);
		 setState(STATE_SYNC);

		 // set default weights and delay
		 setWeight(0.0f);
		 targetDelay = 0.0f;
		 targetWeight = 0.0f;
	 }

	 /** Interpolates the weight of the animation cycle instance.
	   *
	   * This function interpolates the weight of the animation cycle instance to a
	   * new value in a given amount of time.
	   *
	   * @param weight The weight to interpolate the animation cycle instance to.
	   * @param delay The time in seconds until the new weight should be reached.
	   *
	   * @return One of the following values:
	   *         \li \b true if successful
	   *         \li \b false if an error happend
	   */
	 public boolean blend(float weight, float delay) {
		 targetWeight = weight;
		 targetDelay = delay;

		 return true;
	 }

	 /** Puts the animation cycle instance into async state.
	   *
	   * This function puts the animation cycle instance into async state, which
	   * means that it will end after the current running cycle.
	   *
	   * @param time The time in seconds at which the animation cycle instance was
	   *             unlinked from the global mixer animation cycle.
	   * @param duration The current duration of the global mixer animation cycle in
	   *                 seconds at the time of the unlinking.
	   */
	 public void setAsync(float time, float duration) {
	   // check if thie animation cycle is already async
	   if(getState() != STATE_ASYNC) {
	     if(duration == 0.0f) {
	       setTimeFactor(1.0f);
	       setTime(0.0f);
	     }
	     else {
	       setTimeFactor(getCoreAnimation().getDuration() / duration);
	       setTime(time * getTimeFactor());
	     }
	     setState(STATE_ASYNC);
	   }
	 }

	 /** Updates the animation cycle instance.
	   *
	   * This function updates the animation cycle instance for a given amount of
	   * time.
	   *
	   * @param deltaTime The elapsed time in seconds since the last update.
	   *
	   * @return One of the following values:
	   *         \li \b true if the animation cycle instance is still active
	   *         \li \b false if the execution of the animation cycle instance has
	   *             ended
	   */
	 public boolean update(float deltaTime) {
	   if(targetDelay <= Math.abs(deltaTime)) {
	     // we reached target delay, set to full weight
	     setWeight(targetWeight);
	     targetDelay = 0.0f;

	     // check if we reached the cycles end
	     if(getWeight() == 0.0f) {
	       return false;
	     }
	   }
	   else {
	     // not reached target delay yet, interpolate between current and target weight
	     float factor;
	     factor = deltaTime / targetDelay;
	     setWeight((1.0f - factor) * getWeight() + factor * targetWeight);
	     targetDelay -= deltaTime;
	   }

	   // update animation cycle time if it is in async state
	   if(getState() == STATE_ASYNC) {
	     setTime(getTime() + deltaTime * getTimeFactor());
	     if(getTime() >= getCoreAnimation().getDuration()) {
	       //setTime(fmod(getTime(), getCoreAnimation().getDuration()));
	    	 //FIXME: Replace the code below if there is a function equivalent to fmod in Java
	    	 float time = getTime();
	    	 float duration = getCoreAnimation().getDuration();
	    	 setTime( time - ((int)(time/duration))* duration );
	     }
	     if (getTime() < 0)
	       setTime(getTime() + getCoreAnimation().getDuration());
	   }
	   return true;
	 }

}
