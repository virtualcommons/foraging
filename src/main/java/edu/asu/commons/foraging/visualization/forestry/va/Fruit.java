package edu.asu.commons.foraging.visualization.forestry.va;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import edu.asu.commons.foraging.graphics.BoundingBox;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.graphics.Triangulation;
import edu.asu.commons.foraging.util.Tuple3i;

/**
 * The Fruit class represents fruits grown on the trees in the forestry experiment.
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 * @version $Revision: 4 $
 *
 */
public class Fruit {
	
	/**
	 * Radius of the fruit. Since all the fruits in our experiment are of same size, we use a static field for radius.
	 */
	private static float radius = 0.5f;
	
	/**
	 * Returns bounding box of a fruit given its location in world coordinates
	 * @param location location of the fruit in world coordinates 
	 * @return bounding box of the fruit
	 */
	public static BoundingBox getBoundingBox(Point3D location) {
		BoundingBox boundingBox = new BoundingBox(true);
		boundingBox.setBLF(location.x-radius, location.y-radius, location.z+radius);
		boundingBox.setBRF(location.x+radius, location.y-radius, location.z+radius);
		boundingBox.setBLB(location.x-radius, location.y-radius, location.z-radius);
		boundingBox.setBRB(location.x+radius, location.y-radius, location.z-radius);
		boundingBox.setTRF(location.x+radius, location.y+radius, location.z+radius);
		boundingBox.setTLF(location.x-radius, location.y+radius, location.z+radius);
		boundingBox.setTRB(location.x+radius, location.y+radius, location.z-radius);
		boundingBox.setTLB(location.x-radius, location.y+radius, location.z-radius);
		
		return boundingBox;
	}
	
	/**
	 * Fills geometry information of the fruit in a buffer
	 * @param location location of the fruit in world coordinates 
	 * @param fruitTemplate template of the fruit used to instantiate all the fruits in the forestry visualization
	 * @param vertexBuffer buffer holding vertices information
	 * @param indexBuffer buffer holding indices of vertices of the traingles in fruit triangulation
	 * @param indexPos current pointer of the index buffer
	 * @return new pointer of the index buffer
	 */
	public static int fillGeometryInfo(Point3D location, Triangulation fruitTemplate, FloatBuffer vertexBuffer, IntBuffer indexBuffer, int indexPos) {

		int index;
		
		//Translate the vertices according to the location and fill them in the vertex buffer
		int nVertices = fruitTemplate.getNoVertices();
		for (index = 0; index < nVertices; index++) {
			BoundingBox.addVertex2VA(location.add(fruitTemplate.getVertex(index)), vertexBuffer); 				
		}
				
		//Fill in the vertexBuffer
		Tuple3i face;
		int nFaces = fruitTemplate.getNoFaces();
		for (index = 0; index < nFaces; index++) {
			face = fruitTemplate.getFace(index);
			indexBuffer.put((face.a-1)+indexPos);	
			indexBuffer.put((face.b-1)+indexPos);	
			indexBuffer.put((face.c-1)+indexPos);			
		}		
		return nVertices;
	}
}
