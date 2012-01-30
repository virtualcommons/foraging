package edu.asu.commons.foraging.visualization.forestry.va;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import edu.asu.commons.foraging.fileplugin.OBJFilePlugin;
import edu.asu.commons.foraging.graphics.BoundingBox;
import edu.asu.commons.foraging.graphics.Matrix4;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Triangulation;
import edu.asu.commons.foraging.graphics.Vector3D;
import edu.asu.commons.foraging.util.Tuple3f;
import edu.asu.commons.foraging.util.Tuple3i;

/**
 * The TreeModel class is used to create tree geometry according to its age. This is then buffered for rendering. This is a model type of class
 * in the MVC architecture. 
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 * @version $Revision$
 *
 */
public class TreeModel {

	/**
	 * Tree trunk
	 */
	protected TreeBranchModel trunk = null;
	
	/**
	 * Tree foliage
	 */
	private Vector<LeafClusterModel> leafClusters = new Vector<LeafClusterModel>();
	
	/**
	 * Tree age
	 */
	protected int age = 0;
	
	//Trunk parameters according to age of the tree
	/**
	 * Array storing trunk base radii values for tree ages 0 to 10
	 */
	public static float[] trunkBaseRadii = 	{0.0f, 0.125f, 0.25f, 0.352f, 0.50f, 0.75f, 1.00f, 1.25f, 1.5f, 1.5f, 1.5f};
	
	/**
	 * Array storing trunk top radii values for tree ages 0 to 10
	 */
	public static float[] trunkTopRadii = 	{0.0f, 0.075f, 0.20f, 0.30f, 0.425f, 0.6f, 0.75f, 1.00f, 1.25f, 1.25f, 1.25f};
	
	/**
	 * Array storing trunk length values for tree ages 0 to 10
	 */
	public static float[] trunkLengths = 	{0.0f, 4.00f, 6.00f, 8.0f, 10.0f, 12.0f, 14.0f, 15.0f, 16.0f, 16.0f, 16.0f};

	/**
	 * Creates a new tree model by creating a trunk and 3 child branches.
	 */
	public TreeModel() {
		LeafClusterModel.setRollMatrix(new Matrix4(35, new Vector3D(1, 0, 0)));
		
		//Create trunk
		trunk = new TreeBranchModel(0, trunkBaseRadii[0], trunkTopRadii[0], trunkLengths[0], new Point3D(0, 0, 0), new Tuple3f(0, 0, 0), null, this);
		
		//Create 3 branches
		trunk.createChildren();
	}
	
	/**
	 * Returns age of the tree
	 * @return age of this tree
	 */
	public int getAge() {
		return age;
	}
	
	/**
	 * Sets age of the tree
	 * @param age new age of the tree
	 */
	public void setAge(int age) {
		this.age = age;
	}
	
	/**
	 * Populates buffers with branch and leaf geometry information
	 * @param branchVertexBuffer buffer storing branch vertex coordinates
	 * @param leafVertexBuffer buffer storing leaf vertex coordinates
	 */
	public void populateGeometryInfo(FloatBuffer branchVertexBuffer, FloatBuffer leafVertexBuffer) {			
		trunk.updateParamValues();		
		trunk.fillGeometryInfo(branchVertexBuffer);
		
		TreeBranchModel branch = null;
		int index;
		for (index = 0; index < trunk.children.size(); index++) {
			branch = trunk.children.get(index);
			branch.updateParamValues();
			branch.fillGeometryInfo(branchVertexBuffer);
		}			
		
		LeafClusterModel leafCluster = null;
		for (index = 0; index < leafClusters.size(); index++) {
			leafCluster = leafClusters.get(index);
			leafCluster.updateParamValues();
			if (index % 3 == 0)
				leafCluster.fillGeometryInfo(leafVertexBuffer, true);
			else
				leafCluster.fillGeometryInfo(leafVertexBuffer, false);
		}
		
		branchVertexBuffer.flip();
		leafVertexBuffer.flip();
	}
	
	/**
	 * Populates buffer with vertex normals of the branch triangulation
	 * @param branchVertexBuffer holds branch vertex information (input)
	 * @param branchIndexBuffer holds branch index information (input)
	 * @param branchNormalBuffer holds branch normal information (output)
	 */
	public void populateBranchNormalInfo(FloatBuffer branchVertexBuffer, ShortBuffer branchIndexBuffer, FloatBuffer branchNormalBuffer) {
		
		//Create a triangulated object using vertex buffer and index buffer
		Triangulation branchStructure = new Triangulation();
		
		int index;
		//Fill vertices
		for (index = 0; index < branchVertexBuffer.limit(); index+=3) {
			branchStructure.addVertex(new Point3D(branchVertexBuffer.get(), branchVertexBuffer.get(), branchVertexBuffer.get()));
		}
		//Fill faces
		for (index = 0; index < branchIndexBuffer.limit(); index+=3) {
			branchStructure.addFace(new Tuple3i(branchIndexBuffer.get()+1, branchIndexBuffer.get()+1, branchIndexBuffer.get()+1));
		}
		//Calculate normals
		branchStructure.calculateNormals();
		
		//Fill the normals array with this information
		Vector3D normal = null;
		for(index = 0; index < branchStructure.getNoVertexNormals(); index++) {
			normal = branchStructure.getNormal(index);
			branchNormalBuffer.put(normal.x);
			branchNormalBuffer.put(normal.y);
			branchNormalBuffer.put(normal.z);
		}
		
		branchVertexBuffer.flip();
		branchIndexBuffer.flip();
		branchNormalBuffer.flip();
	}
	
	/**
	 * Populates buffer with vertex normals of the foliage triangulation
	 * @param leafVertexBuffer holds leaf vertex information (input)
	 * @param leafIndexBuffer holds leaf index information (input)
	 * @param leafNormalBuffer holds leaf normal information (output)
	 */
	public void populateLeafNormalInfo(FloatBuffer leafVertexBuffer, ByteBuffer leafIndexBuffer, FloatBuffer leafNormalBuffer) {
		//Create a triangulated object using vertex buffer and index buffer
		Triangulation leafCluster = new Triangulation();
		
		int index;
		//Fill vertices
		for (index = 0; index < leafVertexBuffer.limit(); index+=3) {
			leafCluster.addVertex(new Point3D(leafVertexBuffer.get(), leafVertexBuffer.get(), leafVertexBuffer.get()));
		}
		//Fill faces
		for (index = 0; index < leafIndexBuffer.limit(); index+=3) {
			leafCluster.addFace(new Tuple3i(leafIndexBuffer.get()+1, leafIndexBuffer.get()+1, leafIndexBuffer.get()+1));
		}
		//Calculate normals
		leafCluster.calculateNormals();
		
		//Fill the normals array with this information
		Vector3D normal = null;
		for(index = 0; index < leafCluster.getNoVertexNormals(); index++) {
			normal = leafCluster.getNormal(index);
			leafNormalBuffer.put(normal.x);
			leafNormalBuffer.put(normal.y);
			leafNormalBuffer.put(normal.z);
		}
		
		leafVertexBuffer.flip();
		leafIndexBuffer.flip();
		leafNormalBuffer.flip();
	}
	
	/**
	 * Populates fruit vertices, indices and normals
	 * @param fruitVertexBuffer buffer storing fruit vertex infromation
	 * @param fruitIndexBuffer buffer storing fruit index information
	 * @param fruitNormalBuffer buffer storing fruit normal information
	 */
	public void populateFruitGeometryInfo(FloatBuffer fruitVertexBuffer, IntBuffer fruitIndexBuffer, FloatBuffer fruitNormalBuffer) {
				
		Triangulation fruitTemplate = new Triangulation(); 
		OBJFilePlugin.readFile("data/forestry/fruit1.obj", fruitTemplate);
		fruitTemplate.calculateNormals();
		
		LeafClusterModel leafCluster = null;
		//BoundingBox bb = null;
		int nIndices = 0;
		for (int index = 0; index < leafClusters.size(); index++) {
			leafCluster = leafClusters.get(index);
			Point3D point = leafCluster.getLeft().add(leafCluster.getBottom()).divide(2.0f);
			point.y = 0;
			
			nIndices += Fruit.fillGeometryInfo(point, fruitTemplate, fruitVertexBuffer, fruitIndexBuffer, nIndices);
		}		
		
		//Fill normals
		for (int index = 0; index < 9; index++) {
			for (int normalIndex = 0; normalIndex < fruitTemplate.getNoVertices(); normalIndex++) {
				BoundingBox.addVertex2VA(fruitTemplate.getNormal(normalIndex), fruitNormalBuffer);
			}
		}
		
		fruitVertexBuffer.flip();
		fruitIndexBuffer.flip();
		fruitNormalBuffer.flip();
	}
	
	/**
	 * Adds leaf clusters to the tree foliage
	 * @param parentBranch parent branch to which the leaves are attached
	 */
	public void addLeafClusters(TreeBranchModel parentBranch) {
		leafClusters.add( new LeafClusterModel(0, parentBranch, this) );
		leafClusters.add( new LeafClusterModel(120, parentBranch, this) );
		leafClusters.add( new LeafClusterModel(240, parentBranch, this) );
	}

}
