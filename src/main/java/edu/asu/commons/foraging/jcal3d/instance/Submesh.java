package edu.asu.commons.foraging.jcal3d.instance;

import java.util.Vector;

import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.jcal3d.core.CoreSubmesh;


public class Submesh {
	public class PhysicalProperty {
	    Point3D position;
	    Point3D positionOld;
	    Vector3D force = new Vector3D();
	}

	public class TangentSpace {
	    Vector3D tangent;
	    float crossFactor;
	    
	    public TangentSpace() {}
	}

	public class Face {
	    public int vertexId[] = new int[3];
	}
	
	protected CoreSubmesh coreSubmesh;
	protected Vector<Float> morphTargetWeights = new Vector<Float>();
	protected Vector<Point3D> vertices = new Vector<Point3D>();
	protected Vector<Vector3D> normals = new Vector<Vector3D>();
	protected Vector<Vector<TangentSpace>> vectorTangentSpaces = new Vector<Vector<TangentSpace>>();
	protected Vector<Face> faces = new Vector<Face>();
	protected Vector<PhysicalProperty> physicalProperties = new Vector<PhysicalProperty>();
	protected int vertexCount;
	protected int faceCount;
	protected int coreMaterialId;
	protected boolean internalData;
	
	public Submesh(CoreSubmesh coreSubmesh){
	  //FIXME: Make sure the coreSubmesh is not null		

	  this.coreSubmesh = coreSubmesh;
	  
	  // reserve memory for the face vector
	  faces.setSize(coreSubmesh.getFaceCount());	  

	  // set the initial lod level
	  setLodLevel(1.0f);

	  // set the initial material id
	  coreMaterialId = -1;
	  
	  //Setting the morph target weights
	 morphTargetWeights.setSize(coreSubmesh.getCoreSubMorphTargetCount());	  
	 int morphTargetId;
	 for(morphTargetId = 0; morphTargetId < coreSubmesh.getCoreSubMorphTargetCount(); ++morphTargetId) {
		 morphTargetWeights.setElementAt(0.0f, morphTargetId);
	 }

	 // check if the submesh instance must handle the vertex and normal data internally
	 if(coreSubmesh.getSpringCount() > 0) {
	    vertices.setSize(coreSubmesh.getVertexCount());	    
	    normals.setSize(coreSubmesh.getVertexCount());
	    vectorTangentSpaces.setSize(coreSubmesh.getVectorTangentSpaces().size());
	    physicalProperties.setSize(coreSubmesh.getVertexCount());    

	    // get the vertex vector of the core submesh
	    Vector<CoreSubmesh.Vertex> vertices = coreSubmesh.getVertices();

	    // copy the data from the core submesh as default values
	    int vertexId;
	    for(vertexId = 0; vertexId < coreSubmesh.getVertexCount(); ++vertexId) {
	      // copy the vertex data
	      this.vertices.setElementAt(vertices.get(vertexId).position, vertexId);
	      PhysicalProperty physicalProperty = new PhysicalProperty();
	      physicalProperty.position = vertices.get(vertexId).position;
	      physicalProperty.positionOld = vertices.get(vertexId).position;
	      this.physicalProperties.setElementAt(physicalProperty, vertexId);

	      // copy the normal data
	      this.normals.setElementAt(vertices.get(vertexId).normal, vertexId);
	    }
	    internalData = true;
	  }
	  else
	  {
	    internalData = false;
	  }
	}
	
	/* Returns the core material ID.
	 *
	 * This function returns the core material ID of the submesh instance.
	 *
	 * @return One of the following values:
	 *         \li the \b ID of the core material
	 *         \li \b -1 if an error happend
	 */
	public int getCoreMaterialId() {
	  return coreMaterialId;
	}

	/* Provides access to the core submesh.
	 *
	 * This function returns the core submesh on which this submesh instance is
	 * based on.
	 *
	 * @return One of the following values:
	 *         \li a pointer to the core submesh
	 *         \li \b 0 if an error happend
	 */
	public CoreSubmesh getCoreSubmesh() {
	  return coreSubmesh;
	}

	/* Returns the number of faces.
	 *
	 * This function returns the number of faces in the submesh instance.
	 *
	 * @return The number of faces.
	 */
	public int getFaceCount() {
	  return faceCount;
	}

	/* Provides access to the face data.
	 *
	 * This function returns the face data (vertex indices) of the submesh
	 * instance. The LOD setting of the submesh instance is taken into account.
	 *
	 * @param pFaceBuffer A pointer to the user-provided buffer where the face
	 *                    data is written to.
	 *
	 * @return The number of faces written to the buffer.
	 */	
	public Vector<Face> getFaces() {
		return faces;
	}

	/* Returns the normal vector.
	 *
	 * This function returns the vector that contains all normals of the submesh
	 * instance.
	 *
	 * @return A reference to the normal vector.
	 */
	public Vector<Vector3D> getNormals() {
	  return normals;
	}

	/* Returns the tangent space vector-vector.
	 *
	 * This function returns the vector that contains all tangent space bases of
	 * the submesh instance. This vector contains another vector
	 * because there can be more than one texture map at each vertex.
	 *
	 * @return A reference to the tangent space vector-vector.
	 */
	public Vector<Vector<Submesh.TangentSpace>> getVectorTangentSpaces() {
	  return vectorTangentSpaces;
	}
	 
	/* Returns the physical property vector.
	 *
	 * This function returns the vector that contains all physical properties of
	 * the submesh instance.
	 *
	 * @return A reference to the physical property vector.
	 */
	public Vector<Submesh.PhysicalProperty> getPhysicalProperties() {
	  return physicalProperties;
	}


	/* Returns the vertex vector.
	 *
	 * This function returns the vector that contains all vertices of the submesh
	 * instance.
	 *
	 * @return A reference to the vertex vector.
	 */
	public Vector<Point3D> getVertices() {
	  return vertices;
	}

	/* Returns the number of vertices.
	 *
	 * This function returns the number of vertices in the submesh instance.
	 *
	 * @return The number of vertices.
	 */
	public int getVertexCount() {
	  return vertexCount;
	}

	/* Returns if the submesh instance handles vertex data internally.
	 *
	 * This function returns wheter the submesh instance handles vertex data
	 * internally.
	 *
	 * @return One of the following values:
	 *         \li \b true if vertex data is handled internally
	 *         \li \b false if not
	 */
	public boolean hasInternalData() {
	  return internalData;
	}

	/** Disable internal data (and thus springs system)
	 *
	 */
	public void disableInternalData() {
	  if(internalData)
	  {
	    vertices.clear();
	    normals.clear();
	    vectorTangentSpaces.clear();
	    physicalProperties.clear();
	    internalData = false;
	  }
	}
	
	/** Returns true if tangent vectors are enabled.
	  *
	  * This function returns true if the submesh contains tangent vectors.
	  *
	  * @return True if tangent vectors are enabled.
	  */
	public boolean isTangentsEnabled(int mapId) {
		return coreSubmesh.isTangentsEnabled(mapId);
	}
	 
	/** Enables (and calculates) or disables the storage of tangent spaces.
	  *
	  * This function enables or disables the storage of tangent space bases.
	  */
	public boolean enableTangents(int mapId, boolean enabled) {
	  if(!coreSubmesh.enableTangents(mapId,enabled))
	    return false;

	  if(internalData)
	    return true;

	  if(!enabled)
	  {
	    vectorTangentSpaces.get(mapId).clear();
	    return true;
	  }

	  vectorTangentSpaces.get(mapId).setSize(coreSubmesh.getVertexCount());	  
		
	  // get the tangent space vector of the core submesh
	  Vector<CoreSubmesh.TangentSpace> tangentSpaces = coreSubmesh.getVectorTangentSpaces().get(mapId);

	  // copy the data from the core submesh as default values
	  int vertexId;
	  for(vertexId = 0; vertexId < coreSubmesh.getVertexCount(); vertexId++)
	  {      
	    // copy the tangent space data
	    vectorTangentSpaces.get(mapId).get(vertexId).tangent = tangentSpaces.get(vertexId).tangent;
	    vectorTangentSpaces.get(mapId).get(vertexId).crossFactor = tangentSpaces.get(vertexId).crossFactor;
	  }
	  return true;    
	}

	/** Sets the core material ID.
	  *
	  * This function sets the core material ID of the submesh instance.
	  *
	  * @param coreMaterialId The core material ID that should be set.
	  */
	public void setCoreMaterialId(int coreMaterialId) {
		if (coreMaterialId != -1)
			this.coreMaterialId = coreMaterialId;
	}
	 
	/** Sets the LOD level.
	  *
	  * This function sets the LOD level of the submesh instance.
	  *
	  * @param lodLevel The LOD level in the range [0.0, 1.0].
	  */
	public void setLodLevel(float lodLevel) {
	  // clamp the lod level to [0.0, 1.0]
	  if(lodLevel < 0.0f) lodLevel = 0.0f;
	  if(lodLevel > 1.0f) lodLevel = 1.0f;

	  // get the lod count of the core submesh
	  int lodCount;
	  lodCount = coreSubmesh.getLodCount();

	  // calculate the target lod count
	  lodCount = (int)((1.0f - lodLevel) * lodCount);

	  // calculate the new number of vertices
	  vertexCount = coreSubmesh.getVertexCount() - lodCount;

	  // get face vector of the core submesh
	  Vector<CoreSubmesh.Face> coreFaces = coreSubmesh.getFaces();

	  // get face vector of the core submesh
	  Vector<CoreSubmesh.Vertex> vertices = coreSubmesh.getVertices();

	  // calculate the new number of faces
	  this.faceCount = coreFaces.size();

	  int vertexId;
	  for(vertexId = vertices.size() - 1; vertexId >= vertexCount; vertexId--) {
	    faceCount -= vertices.get(vertexId).faceCollapseCount;
	  }

	  // fill the face vector with the collapsed vertex ids
	  int faceId;
	  for(faceId = 0; faceId < faceCount; ++faceId) {	    
		Submesh.Face face = this.new Face();		  
	    for(vertexId = 0; vertexId < 3; ++vertexId)
	    {
	      // get the vertex id
	      int collapsedVertexId;
	      collapsedVertexId = coreFaces.get(faceId).vertexId[vertexId];

	      // collapse the vertex id until it fits into the current lod level
	      while(collapsedVertexId >= vertexCount) collapsedVertexId = vertices.get(collapsedVertexId).collapseId;

	      // store the collapse vertex id in the submesh face vector	      
	      face.vertexId[vertexId] = collapsedVertexId;	       
	    }
	    this.faces.setElementAt(face, faceId);
	  }
	}
	
	/** Sets weight of a morph target with the given id.
	  *
	  * @param blendId The morph target id.
	  * @param weight The weight to be set.
	  */
	public void setMorphTargetWeight(int blendId, float weight)	{
		morphTargetWeights.setElementAt(weight, blendId);
	}

	/** Gets weight of a morph target with the given id.
	  *
	  * @param blendId The morph target id.
	  * @return The weight of the morph target.
	  */
	public float getMorphTargetWeight(int blendId) {
		return morphTargetWeights.get(blendId);
	}

	/** Gets weight of the base vertices.
	  *
	  * @return The weight of the base vertices.
	  */
	public float getBaseWeight() {
	  float baseWeight = 1.0f;
	  int morphTargetCount = getMorphTargetWeightCount();
	  int morphTargetId;
	  for(morphTargetId=0; morphTargetId < morphTargetCount;++morphTargetId)
	  {
	    baseWeight -= morphTargetWeights.get(morphTargetId);
	  }
	  return baseWeight;
	}

	/** Returns the morph target weight vector.
	  *
	  * This function returns the vector that contains all weights for
	  * each morph target instance.
	  *
	  * @return A reference to the weight vector.
	  */
	public Vector<Float> getVectorMorphTargetWeight() {
	  return morphTargetWeights;
	}

	/** Returns the number of weights.
	  *
	  * This function returns the number of weights.
	  *
	  * @return The number of weights.
	  */
	public int getMorphTargetWeightCount() {
	  return morphTargetWeights.size();
	}

	public void setVertices(Vector<Point3D> vertices) {
		this.vertices = vertices;		
	}
	
	public void setNormals(Vector<Vector3D> normals) {
		this.normals = normals;		
	}

	public void setTangentSpaceVector(Vector<TangentSpace> tangentSpaces, int mapId) {
		this.vectorTangentSpaces.setElementAt(tangentSpaces, mapId);
		
	}
}
