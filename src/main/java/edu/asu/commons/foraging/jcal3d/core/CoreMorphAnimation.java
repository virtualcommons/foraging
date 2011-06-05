package edu.asu.commons.foraging.jcal3d.core;

import java.util.Vector;

public class CoreMorphAnimation {
	Vector<Integer> coreMeshIds = new Vector<Integer>();
	Vector<Integer> morphTargetIds = new Vector<Integer>();
	
	/* Adds a core mesh ID and a morph target ID of that core mesh.
	 *
	 * @param coreMeshID A core mesh ID that should be added.
	 * @param morphTargetID A morph target ID that should be added.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean addMorphTarget(int coreMeshID,int morphTargetID)	{
		coreMeshIds.add(coreMeshID);
		morphTargetIds.add(morphTargetID);
		return true;
	}

	/* Returns the core mesh ID list.
	 *
	 * This function returns the list that contains all core mesh IDs of the core
	 * morph animation instance.
	 *
	 * @return A reference to the core mesh ID list.
	 */
	public Vector<Integer> getCoreMeshIds() {
	  return coreMeshIds;
	}
	 
	/* Returns the morph target ID list.
	 *
	 * This function returns the list that contains all morph target  IDs of the core
	 * morph animation instance.
	 *
	 * @return A reference to the morph target ID list.
	 */
	public Vector<Integer> getMorphTargetIds() {
	  return morphTargetIds;
	}

}
