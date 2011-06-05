package edu.asu.commons.foraging.jcal3d.core;

import java.util.Vector;

import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Vector3D;

public class CoreSubMorphTarget {

	public class BlendVertex {
		public Point3D position;
	    public Vector3D normal;
	}
	  
	protected Vector<BlendVertex> blendVertices = new Vector<BlendVertex>();
		
	/* Returns the blend vertex vector.
	 *
	 * This function returns the vector that contains all blend vertices of the core
	 * sub morph target instance.
	 *
	 * @return A reference to the blend vertex vector.
	 */
	public Vector<BlendVertex> getBlendVertices() {
		return blendVertices;
	}

	/* Returns the number of blend vertices.
	 *
	 * This function returns the number of blend vertices in the 
	 * core sub morph target instance.
	 *
	 * @return The number of blend vertices.
	 */
	public int getBlendVertexCount() {
	  return blendVertices.size();
	}

	/* Reserves memory for the blend vertices.
	 *
	 * This function reserves memory for the blend vertices
	 * of the core sub morph target instance.
	 *
	 * @param blendVertexCount The number of blend vertices that
	 *                    this core sub morph target instance should be able to hold.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean reserve(int blendVertexCount) {
	  // reserve the space needed in all the vectors
	  blendVertices.setSize(blendVertexCount);
	  return true;
	}

	/* Sets a specified blend vertex.
	 *
	 * This function sets a specified blend vertex in the core sub morph target instance.
	 *
	 * @param vertexId  The ID of the vertex.
	 * @param vertex The vertex that should be set.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean setBlendVertex(int blendVertexId, BlendVertex blendVertex) {
	  if((blendVertexId < 0) || (blendVertexId >= blendVertices.size())) return false;
	  blendVertices.setElementAt(blendVertex, blendVertexId);
	  return true;
	}

}
