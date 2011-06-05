package edu.asu.commons.foraging.jcal3d.core;

import edu.asu.commons.foraging.jcal3d.instance.Model;

public interface AnimationCallback {	
	    
	public void animationUpdate(float anim_time, Model model);
	public void animationComplete(Model model);
	
}
