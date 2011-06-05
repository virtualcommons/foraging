package edu.asu.commons.foraging.jcal3d.instance;

import edu.asu.commons.foraging.jcal3d.core.CoreAnimation;

public class AnimationAction extends Animation {
	protected float delayIn;
	protected float delayOut;
	protected float delayTarget;
	protected float weightTarget;
	protected boolean  autoLock; 
		
	/** Constructs the animation action instance.
	  *
	  * This function is the default constructor of the animation action instance.
	  */
	public AnimationAction(CoreAnimation coreAnimation) 
	{
		super(coreAnimation);
		setType(TYPE_ACTION);
	}
	 
	/** Executes the animation action instance.
	  *
	  * This function executes the animation action instance.
	  *
	  * @param delayIn The time in seconds until the animation action instance
	  *                reaches the full weight from the beginning of its execution.
	  * @param delayOut The time in seconds in which the animation action instance
	  *                 reaches zero weight at the end of its execution.
	  * @param weightTarget No doxygen comment for this. FIXME.
	  * @param autoLock     This prevents the Action from being reset and removed
	  *                     on the last keyframe if true.
	  *
	  * @return One of the following values:
	  *         \li \b true if successful
	  *         \li \b false if an error happend
	  */
	public boolean execute(float delayIn, float delayOut, float weightTarget, boolean autoLock) {
	  setState(STATE_IN);
	  setWeight(0.0f);
	  this.delayIn = delayIn;
	  this.delayOut = delayOut;
	  setTime(0.0f);
	  this.weightTarget = weightTarget;
	  this.autoLock = autoLock;

	  return true;
	}
	 
	/** Updates the animation action instance.
	  *
	  * This function updates the animation action instance for a given amount of
	  * time.
	  *
	  * @param deltaTime The elapsed time in seconds since the last update.
	  *
	  * @return One of the following values:
	  *         \li \b true if the animation action instance is still active
	  *         \li \b false if the execution of the animation action instance has
	  *             ended
	  */
	public boolean update(float deltaTime) {
	  // update animation action time

	  if(getState() != STATE_STOPPED) {
	    setTime(getTime() + deltaTime * getTimeFactor());
	  }

	  // handle IN phase
	  if(getState() == STATE_IN)
	  {
	    // check if we are still in the IN phase
	    if(getTime() < delayIn) {
	      setWeight(getTime() / delayIn * weightTarget);
	      //m_weight = m_time / m_delayIn;
	    }
	    else
	    {
	      setState(STATE_STEADY);
	      setWeight(weightTarget);
	    }
	  }

	  // handle STEADY
	  if(getState() == STATE_STEADY)
	  {
	    // check if we reached OUT phase
	    if(!autoLock && getTime() >= getCoreAnimation().getDuration() - delayOut)
	    {
	      setState(STATE_OUT);
	    }
	    // if the anim is supposed to stay locked on last keyframe, reset the time here.
	    else if (autoLock && getTime() > getCoreAnimation().getDuration())
	    {
	      setState(STATE_STOPPED);
	      setTime(getCoreAnimation().getDuration());
	    }      
	  }

	  // handle OUT phase
	  if(getState() == STATE_OUT)
	  {
	    // check if we are still in the OUT phase
	    if(getTime() < getCoreAnimation().getDuration())
	    {
	      setWeight((getCoreAnimation().getDuration() - getTime()) / delayOut * weightTarget);
	    }
	    else
	    {
	      // we reached the end of the action animation
	      setWeight(0.0f);
	      return false;
	    }
	  }

	  return true;

	}

}
