package edu.asu.commons.foraging.jcal3d.core;

import java.util.Vector;

import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Vector3D;

public class CoreSubmesh {
	
	public class TextureCoordinate {
		public float u, v;
	}

	public class TangentSpace {
		public Vector3D tangent;
		public float crossFactor;  // To get the binormal, use ((N x T) * crossFactor)
		
		public TangentSpace() {}
	}

	public class Influence {
		public int boneId;
		public float weight;
	};

	public class PhysicalProperty {
		public float weight;
	};

	public class Vertex {
	    public Point3D position = new Point3D();
	    public Vector3D normal = new Vector3D();
	    public Vector<Influence> vectorInfluence = new Vector<Influence>();
	    public int collapseId;
	    public int faceCollapseCount;
	}

	public class Face {
		//could be short
		public int vertexId[] = new int[3];
	}
		  
	// The core submesh Spring.
	public class Spring {
		public int vertexId[] = new int[2];
		public float springCoefficient;
		public float idleLength;
	}

	protected Vector<Vertex> vertices = new Vector<Vertex>();
	protected Vector<Boolean> tangentsEnabled = new Vector<Boolean>();
	protected Vector<Vector<TangentSpace>> vectorTangentSpaces = new Vector<Vector<TangentSpace>>();
	protected Vector<Vector<TextureCoordinate>> vectorTextureCoordinates = new Vector<Vector<TextureCoordinate>>();
	protected Vector<PhysicalProperty> physicalProperties = new Vector<PhysicalProperty>();
	protected Vector<Face> faces = new Vector<Face>();
	protected Vector<Spring> springs = new Vector<Spring>();
	protected Vector<CoreSubMorphTarget> coreSubMorphTargets = new Vector<CoreSubMorphTarget>();
	protected int coreMaterialThreadId;
	protected int lodCount;
	  
	/* Returns the ID of the core material thread.
	 *
	 * This function returns the ID of the core material thread of this core
	 * submesh instance.
	 *
	 * @return The ID of the core material thread.
	 */
	public int getCoreMaterialThreadId()	{
	  return coreMaterialThreadId;
	}

	/* Returns the number of faces.
	 *
	 * This function returns the number of faces in the core submesh instance.
	 *
	 * @return The number of faces.
	 */
	public int getFaceCount() {
	  return faces.size();
	}

	/* Returns the number of LOD steps.
	 *
	 * This function returns the number of LOD steps in the core submesh instance.
	 *
	 * @return The number of LOD steps.
	 */
	public int getLodCount() {
	  return lodCount;
	}

	/* Returns the number of springs.
	 *
	 * This function returns the number of springs in the core submesh instance.
	 *
	 * @return The number of springs.
	 */
	public int getSpringCount() {
	  return springs.size();
	}
	 
	/* Returns true if tangent vectors are enabled.
	 *
	 * This function returns true if the core submesh contains tangent vectors.
	 *
	 * @return True if tangent vectors are enabled.
	 */
	public boolean isTangentsEnabled(int mapId) {
	  if((mapId < 0) || (mapId >= (int)tangentsEnabled.size())) return false;

	  return tangentsEnabled.get(mapId);
	}

	public void updateTangentVector(int v0, int v1, int v2, int mapId) {
	  Vector<Vertex> vertices = getVertices();
	  Vector<TextureCoordinate> textureCoordinates = vectorTextureCoordinates.get(mapId);

	  // Step 1. Compute the approximate tangent vector.
	  double du1 = textureCoordinates.get(v1).u - textureCoordinates.get(v0).u;
	  double dv1 = textureCoordinates.get(v1).v - textureCoordinates.get(v0).v;
	  double du2 = textureCoordinates.get(v2).u - textureCoordinates.get(v0).u;
	  double dv2 = textureCoordinates.get(v2).v - textureCoordinates.get(v0).v;

	  double prod1 = (du1*dv2-dv1*du2);
	  double prod2 = (du2*dv1-dv2*du1);
	  if ((Math.abs(prod1) < 0.000001)||(Math.abs(prod2) < 0.000001)) return;

	  double x = dv2/prod1;
	  double y = dv1/prod2;

	  Vector3D vec1 = (Vector3D)vertices.get(v1).position.subtract(vertices.get(v0).position);
	  Vector3D vec2 = (Vector3D)vertices.get(v2).position.subtract(vertices.get(v0).position);
	  Vector3D tangent = (Vector3D)vec1.multiply((float)x).add(vec2.multiply((float)y));

	  // Step 2. Orthonormalize the tangent.
	  double component = tangent.multiply(vertices.get(v0).normal);
	  tangent = (Vector3D)tangent.subtract(vertices.get(v0).normal.multiply((float)component));
	  tangent.normalize();

	  // Step 3: Add the estimated tangent to the overall estimate for the vertex.
	  //FIXME: Check is this modifies the vector element
	  vectorTangentSpaces.get(mapId).get(v0).tangent = vectorTangentSpaces.get(mapId).get(v0).tangent.add(tangent);
	}

	/* Enables (and calculates) or disables the storage of tangent spaces.
	 *
	 * This function enables or disables the storage of tangent space bases.
	 */
	public boolean enableTangents(int mapId, boolean enabled) {
	  if((mapId < 0) || (mapId >= (int)tangentsEnabled.size())) return false;
	  
	  tangentsEnabled.setElementAt(enabled, mapId);

	  if(!enabled)
	  {
		  vectorTangentSpaces.get(mapId).clear();
		  return true;
	  }

	  //vectorTangentSpaces.get(mapId).reserve(vertices.size());
	  vectorTangentSpaces.get(mapId).setSize(vertices.size());

	  int tangentId;
	  for(tangentId = 0; tangentId < (int)vectorTangentSpaces.get(mapId).size(); tangentId++) {
		  vectorTangentSpaces.get(mapId).get(tangentId).tangent= new Vector3D();
		  vectorTangentSpaces.get(mapId).get(tangentId).crossFactor = 1;
	  }

	  int faceId;
	  for( faceId = 0; faceId < (int) faces.size(); faceId++) {
	    updateTangentVector(faces.get(faceId).vertexId[0], faces.get(faceId).vertexId[1], faces.get(faceId).vertexId[2], mapId);
	    updateTangentVector(faces.get(faceId).vertexId[1], faces.get(faceId).vertexId[2], faces.get(faceId).vertexId[0], mapId);
	    updateTangentVector(faces.get(faceId).vertexId[2], faces.get(faceId).vertexId[0], faces.get(faceId).vertexId[1], mapId);
	  }

	  for(tangentId = 0; tangentId < (int)vectorTangentSpaces.get(mapId).size(); tangentId++) {
		  vectorTangentSpaces.get(mapId).get(tangentId).tangent.normalize();
	  }
	  
	  return true;
	}

	/* Returns the face vector.
	 *
	 * This function returns the vector that contains all faces of the core submesh
	 * instance.
	 *
	 * @return A reference to the face vector.
	 */
	public Vector<Face> getFaces() {
	  return faces;
	}

	/* Returns the physical property vector.
	 *
	 * This function returns the vector that contains all physical properties of
	 * the core submesh instance.
	 *
	 * @return A reference to the physical property vector.
	 */
	public Vector<PhysicalProperty> getPhysicalProperties() {
	  return physicalProperties;
	}

	/* Returns the spring vector.
	 *
	 * This function returns the vector that contains all springs of the core
	 * submesh instance.
	 *
	 * @return A reference to the spring vector.
	 */
	public Vector<Spring> getSprings() {
	  return springs;
	}

	/* Returns the texture coordinate vector-vector.
	 *
	 * This function returns the vector that contains all texture coordinate
	 * vectors of the core submesh instance. This vector contains another vector
	 * because there can be more than one texture map at each vertex.
	 *
	 * @return A reference to the texture coordinate vector-vector.
	 */
	public Vector<Vector<TextureCoordinate>> getVectorTextureCoordinates() {
	  return vectorTextureCoordinates;
	}

	/* Returns the tangent space vector-vector.
	 *
	 * This function returns the vector that contains all tangent space bases of
	 * the core submesh instance. This vector contains another vector
	 * because there can be more than one texture map at each vertex.
	 *
	 * @return A reference to the tangent space vector-vector.
	 */
	public Vector<Vector<TangentSpace>> getVectorTangentSpaces() {
	  return vectorTangentSpaces;
	}

	/* Returns the vertex vector.
	 *
	 * This function returns the vector that contains all vertices of the core
	 * submesh instance.
	 *
	 * @return A reference to the vertex vector.
	 */
	public Vector<Vertex> getVertices() {
	  return vertices;
	}

	/* Returns the number of vertices.
	 *
	 * This function returns the number of vertices in the core submesh instance.
	 *
	 * @return The number of vertices.
	 */
	public int getVertexCount() {
	  return vertices.size();
	}

	/* Reserves memory for the vertices, faces and texture coordinates.
	 *
	 * This function reserves memory for the vertices, faces, texture coordinates
	 * and springs of the core submesh instance.
	 *
	 * @param vertexCount The number of vertices that this core submesh instance
	 *                    should be able to hold.
	 * @param textureCoordinateCount The number of texture coordinates that this
	 *                               core submesh instance should be able to hold.
	 * @param faceCount The number of faces that this core submesh instance should
	 *                  be able to hold.
	 * @param springCount The number of springs that this core submesh instance
	 *                  should be able to hold.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean reserve(int vertexCount, int textureCoordinateCount, int faceCount, int springCount)
	{
	  // reserve the space needed in all the vectors
	  vertices.setSize(vertexCount);	  
	  tangentsEnabled.setSize(textureCoordinateCount);
	  vectorTangentSpaces.setSize(textureCoordinateCount);
	  vectorTextureCoordinates.setSize(textureCoordinateCount);
	  
	  int textureCoordinateId;
	  for(textureCoordinateId = 0; textureCoordinateId < textureCoordinateCount; ++textureCoordinateId)
	  {		  
		  Vector<TextureCoordinate> textureCoordinates = new Vector<TextureCoordinate>();
		  textureCoordinates.setSize(vertexCount);
		  vectorTextureCoordinates.setElementAt(textureCoordinates, textureCoordinateId);
		  
		  if (tangentsEnabled.get(textureCoordinateId) != null)
		  {
			  vectorTangentSpaces.get(textureCoordinateId).setSize(vertexCount);			  
		  }
		  else
		  {
			 vectorTangentSpaces.setElementAt(new Vector<TangentSpace>(), textureCoordinateId);
		  }
	  }

	  faces.setSize(faceCount);
	  springs.setSize(springCount);

	  // reserve the space for the physical properties if we have springs in the core submesh instance
	  if(springCount > 0)
	  {
		  physicalProperties.setSize(vertexCount);		  
	  }
	  return true;
	}
	
	public void setCoreMaterialThreadId(int coreMaterialThreadId) {
		this.coreMaterialThreadId = coreMaterialThreadId;
	}

	/* Sets a specified face.
	 *
	 * This function sets a specified face in the core submesh instance.
	 *
	 * @param faceId  The ID of the face.
	 * @param face The face that should be set.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean setFace(int faceId, Face face) {
	  if((faceId < 0) || (faceId >= faces.size())) return false;

	  faces.setElementAt(face, faceId);

	  return true;
	}

	/* Sets the number of LOD steps.
	 *
	 * This function sets the number of LOD steps of the core submesh instance.
	 *
	 * @param lodCount The number of LOD steps that should be set.
	 */
	public void setLodCount(int lodCount) {
		this.lodCount = lodCount;
	}

	/* Sets the tangent vector associated with a specified texture coordinate pair.
	 *
	 * This function sets the tangent vector associated with a specified
	 * texture coordinate pair in the core submesh instance.
	 *
	 * @param vertexId  The ID of the vertex.
	 * @param textureCoordinateId The ID of the texture coordinate channel.
	 * @param tangent   The tangent vector that should be stored.
	 * @param crossFactor The cross-product factor that should be stored.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean setTangentSpace(int vertexId, int textureCoordinateId, Vector3D tangent, float crossFactor) {
	  if((vertexId < 0) || (vertexId >= vertices.size())) return false;
	  if((textureCoordinateId < 0) || (textureCoordinateId >= vectorTextureCoordinates.size())) return false;
	  if(!tangentsEnabled.get(textureCoordinateId)) return false;
	  
	  vectorTangentSpaces.get(textureCoordinateId).get(vertexId).tangent = tangent;
	  vectorTangentSpaces.get(textureCoordinateId).get(vertexId).crossFactor = crossFactor;
	  return true;
	}

	/* Sets a specified physical property.
	 *
	 * This function sets a specified physical property in the core submesh
	 * instance.
	 *
	 * @param vertexId  The ID of the vertex.
	 * @param physicalProperty The physical property that should be set.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean setPhysicalProperty(int vertexId, PhysicalProperty physicalProperty)	{
	  if((vertexId < 0) || (vertexId >= physicalProperties.size())) return false;
	  physicalProperties.setElementAt(physicalProperty, vertexId);
	  return true;
	}

	/* Sets a specified spring.
	 *
	 * This function sets a specified spring in the core submesh instance.
	 *
	 * @param springId  The ID of the spring.
	 * @param spring The spring that should be set.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean setSpring(int springId, Spring spring) {
	  if((springId < 0) || (springId >= springs.size())) return false;
	  springs.setElementAt(spring, springId);
	  return true;
	}
	
	/* Sets a specified texture coordinate.
	 *
	 * This function sets a specified texture coordinate in the core submesh
	 * instance.
	 *
	 * @param vertexId  The ID of the vertex.
	 * @param textureCoordinateId  The ID of the texture coordinate.
	 * @param textureCoordinate The texture coordinate that should be set.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean setTextureCoordinate(int vertexId, int textureCoordinateId, TextureCoordinate textureCoordinate)	{
	  if((textureCoordinateId < 0) || (textureCoordinateId >= vectorTextureCoordinates.size())) return false;
	  if((vertexId < 0) || (vertexId >= vectorTextureCoordinates.get(textureCoordinateId).size())) return false;

	  vectorTextureCoordinates.get(textureCoordinateId).setElementAt(textureCoordinate, vertexId);

	  return true;
	}

	/* Sets a specified vertex.
	 *
	 * This function sets a specified vertex in the core submesh instance.
	 *
	 * @param vertexId  The ID of the vertex.
	 * @param vertex The vertex that should be set.
	 *
	 * @return One of the following values:
	 *         \li \b true if successful
	 *         \li \b false if an error happend
	 */
	public boolean setVertex(int vertexId, Vertex vertex) {
	  if((vertexId < 0) || (vertexId >= vertices.size())) return false;
	  vertices.setElementAt(vertex, vertexId);
	  return true;
	}

	/* Adds a core sub morph target.
	 *
	 * This function adds a core sub morph target to the core sub mesh instance.
	 *
	 * @param pCoreSubMorphTarget A pointer to the core sub morph target that should be added.
	 *
	 * @return One of the following values:
	 *         \li the assigned sub morph target \b ID of the added core sub morph target
	 *         \li \b -1 if an error happend
	 */
	public int addCoreSubMorphTarget(CoreSubMorphTarget coreSubMorphTarget) {
	  // get next sub morph target id
	  int subMorphTargetId;
	  subMorphTargetId = coreSubMorphTargets.size();
	  coreSubMorphTargets.add(coreSubMorphTarget);
	  return subMorphTargetId;
	}

	/* Provides access to a core sub morph target.
	 *
	 * This function returns the core sub morph target with the given ID.
	 *
	 * @param id The ID of the core sub morph target that should be returned.
	 *
	 * @return One of the following values:
	 *         \li a pointer to the core sub morph target
	 *         \li \b 0 if an error happend
	 */
	public CoreSubMorphTarget getCoreSubMorphTarget(int id)	{
	  if((id < 0) || (id >= coreSubMorphTargets.size()))
	  {
	    return null;
	  }

	  return coreSubMorphTargets.get(id);
	}

	/* Returns the number of core sub morph targets.
	 *
	 * This function returns the number of core sub morph targets in the core sub mesh
	 * instance.
	 *
	 * @return The number of core sub morph targets.
	 */
	public int getCoreSubMorphTargetCount() {
	  return coreSubMorphTargets.size();
	}
	 
	/* Returns the core sub morph target vector.
	 *
	 * This function returns the vector that contains all core sub morph target
	 *  of the core submesh instance.
	 *
	 * @return A reference to the core sub morph target vector.
	 */
	public Vector<CoreSubMorphTarget> getCoreSubMorphTargets() {
	  return coreSubMorphTargets;
	}

	/* Scale the Submesh.
	 *
	 * This function rescale all the data that are in the core submesh instance.
	 *
	 * @param factor A float with the scale factor
	 *
	 */
	public void scale(float factor) {
	  // rescale all vertices
	  for(int vertexId = 0; vertexId < vertices.size() ; vertexId++) {
		  vertices.get(vertexId).position = vertices.get(vertexId).position.multiply(factor);		
	  }

	  if(springs.size() != 0) {

	    // There is a problem when we resize and that there is
	    // a spring system, I was unable to solve this
	    // problem, so I disable the spring system
	    // if the scale are too big

	    if( Math.abs(factor - 1.0f) > 0.10) {
	      springs.clear();
	      physicalProperties.clear();
	    }


	/*		
			for(vertexId = 0; vertexId < m_vectorVertex.size() ; vertexId++)
			{
				//m_vectorPhysicalProperty[vertexId].weight *= factor;
				m_vectorPhysicalProperty[vertexId].weight *= factor*factor;
				//m_vectorPhysicalProperty[vertexId].weight *= 0.5f;
			}


			int springId;
			for(springId = 0; springId < m_vectorVertex.size() ; springId++)
			{
				//m_vectorSpring[springId].idleLength*=factor;
				CalVector distance = m_vectorVertex[m_vectorSpring[springId].vertexId[1]].position - m_vectorVertex[m_vectorSpring[springId].vertexId[0]].position;
				
				m_vectorSpring[springId].idleLength = distance.length();		
			}

	   */
	  }		

	}
}
