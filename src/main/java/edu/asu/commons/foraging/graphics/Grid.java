package edu.asu.commons.foraging.graphics;

import java.awt.Point;
import java.io.Serializable;
import java.util.Vector;

/**
 * The Grid class encapsulates a 2D array used to store data and methods to access this data.
 * 
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 * @version 
 * 
 */
public class Grid implements Serializable {
	private static final long serialVersionUID = 7143620584118160745L;
	
	/**
	 * 2-d array used to store data
	 */
	protected Vector<Vector<Object>> nodes = new Vector<Vector<Object>>();
    
	/**
	 * Inserts a new row at a specified index in the grid
	 * @param rowIndex index of the row to be inserted
	 */
    public void insertRow(int rowIndex) {
		nodes.insertElementAt(new Vector<Object>(), rowIndex);		
	}
	
    /**
     * Adds the specified no. of rows each with the specified no. of columns at the end of the grid
     * @param rows no. of rows to be added
     * @param cols no. of columns in each row
     */
	public void addRows(int rows, int cols) {
		for (int rowCount = 0; rowCount < rows; rowCount++) {
            Vector<Object> v = new Vector<Object>();
            v.setSize(cols);
			nodes.add(v);
        }
	}
	
	/**
	 * Inserts an object at the specified location in the grid
	 * @param rowIndex index of a row where an object is to be inserted 
	 * @param colIndex index of a column where an object is to be inserted
	 * @param node object to be inserted
	 */
	public void insertNode(int rowIndex, int colIndex, Object node) {		
		nodes.get(rowIndex).insertElementAt(node, colIndex);		
	}

	/**
	 * Sets an object at the specified location in the grid
	 * @param rowIndex index of a row where an object is to be set
	 * @param colIndex index of a column where an object is to be set
	 * @param node object to be set
	 */
	public void setNode(int rowIndex, int colIndex, Object node) {
		nodes.get(rowIndex).setElementAt(node, colIndex);		
	}
	
	/**
	 * Returns an object in the grid at the specified location
	 * @param rowIndex index of a row where the object is present 
	 * @param colIndex index of a column where the object is present
	 * @return object
	 */
	public Object getNode(int rowIndex, int colIndex) {
		if (nodes.get(rowIndex).size() > colIndex)
			return nodes.get(rowIndex).get(colIndex);
		return null;
	}
    
	/**
	 * Returns an object whose coordinates are specified by point
	 * @param point 3D coordinates
	 * @return object
	 */
    public Object getNode(Point point) {
        return getNode(point.y, point.x);
    }
	
    /**
     * Returns no. of rows in the grid
     * @return no. of rows
     */
	public int getRows() {
		return nodes.size();
	}
	
	/**
	 * Returns no. of columns in the grid
	 * @return no. of columns
	 */
	public int getColumns() {
		return nodes.get(getRows()-1).size();
	}

	/**
	 * Returns no. of columns in the specified row
	 * @param rowIndex index of a row
	 * @return no. of columns
	 */
	public int getRowSize(int rowIndex) {
		return nodes.get(rowIndex).size();
	}

	/**
	 * Sets an object in the grid to null 
	 * @param p 3D coordinates of an object which needs to be removed
	 */
    public void remove(Point p) {
        nodes.get(p.y).set(p.x, null);
    }		
}
