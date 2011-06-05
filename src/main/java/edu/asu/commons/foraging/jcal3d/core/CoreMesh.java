package edu.asu.commons.foraging.jcal3d.core;

import java.util.Vector;

import edu.asu.commons.foraging.jcal3d.misc.Error;


public class CoreMesh {

	protected Vector<CoreSubmesh> coreSubmeshes = new Vector<CoreSubmesh>();
	protected String name = new String();
	protected String filename = new String();
	protected int referenceCount;
		
	
	/* Adds a core submesh.
	 *
	 * This function adds a core submesh to the core mesh instance.
	 *
	 * @param pCoreSubmesh A pointer to the core submesh that should be added.
	 *
	 * @return One of the following values:
	 *         \li the assigned submesh \b ID of the added core submesh
	 *         \li \b -1 if an error happend
	 *****************************************************************************/
	public int addCoreSubmesh(CoreSubmesh coreSubmesh) {
	  // get next bone id
	  int submeshId = coreSubmeshes.size();
	  coreSubmeshes.add(coreSubmesh);
	  return submeshId;
	}

	/* Provides access to a core submesh.
	 *
	 * This function returns the core submesh with the given ID.
	 *
	 * @param id The ID of the core submesh that should be returned.
	 *
	 * @return One of the following values:
	 *         \li a pointer to the core submesh
	 *         \li \b 0 if an error happend
	 */
	public CoreSubmesh getCoreSubmesh(int id) {
		return coreSubmeshes.get(id);
	}
	
	/* Returns the number of core submeshes.
	 *
	 * This function returns the number of core submeshes in the core mesh
	 * instance.
	 *
	 * @return The number of core submeshes.
	 */
	public int getCoreSubmeshCount() {
	  return coreSubmeshes.size();
	}

	/* Returns the core submesh vector.
	 *
	 * This function returns the vector that contains all core submeshes of the
	 * core mesh instance.
	 *
	 * @return A reference to the core submesh vector.
	 */
	public Vector<CoreSubmesh> getCoreSubmeshes() {
	  return coreSubmeshes;
	}

	/* Adds a core submesh.
	 *
	 * This function adds a core mesh as a blend target.
	 * It adds appropriate CalCoreSubMorphTargets to each of the core sub meshes.
	 *
	 * @param pCoreMesh A pointer to the core mesh that shoulb become a blend target.
	 *
	 * @return One of the following values:
	 *         \li the assigned morph target \b ID of the added blend target
	 *         \li \b -1 if an error happend
	 */
	public int addAsMorphTarget(CoreMesh coreMesh)	{
	  //Check if the numbers of vertices allow a blending
	  Vector<CoreSubmesh> otherCoreSubmeshes = coreMesh.getCoreSubmeshes();
	  if (coreSubmeshes.size() != otherCoreSubmeshes.size()) {
		  Error.setLastError(Error.INTERNAL, "", -1, "");
		  return -1;
	  }
	  if (coreSubmeshes.size() == 0) {
		  Error.setLastError(Error.INTERNAL, "", -1, "");
		  return -1;
	  }
	  
	  int subMorphTargetID = coreSubmeshes.get(0).getCoreSubMorphTargetCount();
	  for (int coreSubmeshIndex = 0; coreSubmeshIndex < coreSubmeshes.size(); coreSubmeshIndex++) {
		  if(coreSubmeshes.get(coreSubmeshIndex).getVertexCount() != otherCoreSubmeshes.get(coreSubmeshIndex).getVertexCount()) {
			  Error.setLastError(Error.INTERNAL, "", -1, "");
			  return -1;
		  }	    
	  }
	  //Adding the blend targets to each of the core sub meshes
	  for (int coreSubmeshIndex = 0; coreSubmeshIndex < coreSubmeshes.size(); ++coreSubmeshIndex) {
		  int vertexCount = otherCoreSubmeshes.get(coreSubmeshIndex).getVertexCount();
		  CoreSubMorphTarget coreSubMorphTarget = new CoreSubMorphTarget();
		  
		  if(!coreSubMorphTarget.reserve(vertexCount)) return -1;
		    Vector<CoreSubmesh.Vertex> vertices = otherCoreSubmeshes.get(coreSubmeshIndex).getVertices();
		    
		    for(int i = 0; i < vertexCount; ++i)
		    {
		      CoreSubMorphTarget.BlendVertex blendVertex = coreSubMorphTarget.new BlendVertex(); 
		      blendVertex.position = vertices.get(i).position;
		      blendVertex.normal = vertices.get(i).normal;
		      if(!coreSubMorphTarget.setBlendVertex(i, blendVertex)) return -1;		      
		    } 
	  }	  
	  return subMorphTargetID;
	}

	/* Scale the Mesh.
	 *
	 * This function rescale all the data that are in the core mesh instance.
	 *
	 * @param factor A float with the scale factor
	 *
	 */
	public void scale(float factor) {		
		for(int coreSubmeshIndex = 0; coreSubmeshIndex < coreSubmeshes.size(); ++coreSubmeshIndex) {
			coreSubmeshes.get(coreSubmeshIndex).scale(factor);    
		}
	}

	/* 
	 * Set the name of the file in which the core mesh is stored, if any.
	 *
	 * @param filename The path of the file.
	 */
	public void setFileName(String filename) {
	  this.filename = filename;
	}

	/* 
	 * Get the name of the file in which the core mesh is stored, if any.
	 *
	 * @return One of the following values:
	 *         \li \b empty string if the mesh was not stored in a file
	 *         \li \b the path of the file
	 *
	 */
	public String getFileName() {
	  return filename;
	}

	/* 
	 * Set the symbolic name of the core mesh.
	 *
	 * @param name A symbolic name.
	 */
	public void setName(String name) {
	  this.name = name;
	}

	/* 
	 * Get the symbolic name the core mesh.
	 *
	 * @return One of the following values:
	 *         \li \b empty string if the mesh was no associated to a symbolic name
	 *         \li \b the symbolic name
	 *
	 */
	public String getName() {
	  return name;
	}

	/* 
	 * Increment the reference counter the core mesh.
	 *
	 */
	public void incRef() {
	  referenceCount++;
	}

	/* 
	 * Decrement the reference counter the core mesh.
	 *
	 * @return One of the following values:
	 *         \li \b true if there are nomore reference
	 *         \li \b false if there are another reference
	 *
	 */
	public boolean decRef() {
	  referenceCount--;
	  return (referenceCount <= 0); 
	}
}
