package edu.asu.commons.foraging.jcal3d.instance;

import java.util.Vector;

import edu.asu.commons.foraging.jcal3d.core.CoreMesh;
import edu.asu.commons.foraging.jcal3d.core.CoreSubmesh;
import edu.asu.commons.foraging.jcal3d.misc.Error;

public class Mesh {
	protected Model model = null;
	protected CoreMesh coreMesh = null;
	protected Vector<Submesh> submeshes = new Vector<Submesh>();
	
	/* Constructs the mesh instance.
	 *
	 * This function is the default constructor of the mesh instance.
	 */
	public Mesh(CoreMesh coreMesh) {
	  //FIXME: Make sure coreMesh is not null
	  this.coreMesh = coreMesh;

	  // clone the mesh structure of the core mesh
	  Vector<CoreSubmesh> coreSubmeshes = coreMesh.getCoreSubmeshes();

	  //get the number of submeshes
	  int submeshCount = coreSubmeshes.size();

	  // reserve space in the bone vector
	  submeshes.setSize(submeshCount);

	  // clone every core submesh
	  for(int submeshId = 0; submeshId < submeshCount; ++submeshId)
	  {
	    submeshes.setElementAt(new Submesh(coreSubmeshes.get(submeshId)), submeshId);
	  }
	}
		
	/** Provides access to the core mesh.
	  *
	  * This function returns the core mesh on which this mesh instance is based on.
	  *
	  * @return One of the following values:
	  *         \li a pointer to the core mesh
	  *         \li \b 0 if an error happend
	  */
	public CoreMesh getCoreMesh() {
	  return coreMesh;
	}
	 
	/** Provides access to a submesh.
	  *
	  * This function returns the submesh with the given ID.
	  *
	  * @param id The ID of the submesh that should be returned.
	  *
	  * @return One of the following values:
	  *         \li a pointer to the submesh
	  *         \li \b 0 if an error happend
	  */
	public Submesh getSubmesh(int id){
	  if((id < 0) || (id >= (int)submeshes.size()))
	  {
	    Error.setLastError(Error.INVALID_HANDLE, "", -1, "");
	    return null;
	  }

	  return submeshes.get(id);
	}

	/** Returns the number of submeshes.
	  *
	  * This function returns the number of submeshes in the mesh instance.
	  *
	  * @return The number of submeshes.
	  */
	public int getSubmeshCount() {
	  return submeshes.size();
	}

	/** Returns the submesh vector.
	  *
	  * This function returns the vector that contains all submeshes of the mesh
	  * instance.
	  *
	  * @return A reference to the submesh vector.
	  */
	public Vector<Submesh> getSubmeshes() {
	  return submeshes;
	}

	/** Sets the LOD level.
	  *
	  * This function sets the LOD level of the mesh instance.
	  *
	  * @param lodLevel The LOD level in the range [0.0, 1.0].
	  */
	public void setLodLevel(float lodLevel)	{
	  // change lod level of every submesh
	  int submeshId;
	  for(submeshId = 0; submeshId < submeshes.size(); ++submeshId) {
	    // set the lod level in the submesh
	    submeshes.get(submeshId).setLodLevel(lodLevel);
	  }
	}

	/** Sets the material set.
	  *
	  * This function sets the material set of the mesh instance.
	  *
	  * @param setId The ID of the material set.
	  */
	public void setMaterialSet(int setId) {
	  // change material of every submesh
	  int submeshId;
	  for(submeshId = 0; submeshId < submeshes.size(); ++submeshId) {
	    // get the core material thread id of the submesh
	    int coreMaterialThreadId;
	    coreMaterialThreadId = submeshes.get(submeshId).getCoreSubmesh().getCoreMaterialThreadId();

	    // get the core material id for the given set id in the material thread
	    int coreMaterialId = model.getCoreModel().getCoreMaterialId(coreMaterialThreadId, setId);

	    // set the new core material id in the submesh
	    submeshes.get(submeshId).setCoreMaterialId(coreMaterialId);
	  }
	}

	/** Sets the model.
	  *
	  * This function sets the model to which the mesh instance is attached to.
	  *
	  * @param pModel The model to which the mesh instance should be attached to.
	  */
	public void setModel(Model model){
	  this.model = model;
	}
	
	/** Disable internal data (and thus springs system)
	  *
	  */
	public void disableInternalData() {
	  // disable internal data of every submesh
	  int submeshId;
	  for(submeshId = 0; submeshId < submeshes.size(); ++submeshId)
	  {
	    // disable internal data of the submesh
	    submeshes.get(submeshId).disableInternalData();
	  }
	}
}
