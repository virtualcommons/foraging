package edu.asu.commons.foraging.ui;

import java.awt.Point;
import java.io.Serializable;

/**
 * $Id: Circle.java 416 2009-12-25 05:17:14Z alllee $
 * 
 * Simple Circle class given a Point center and radius and providing methods to detect
 * if a point is within the circle.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 416 $
 */
public class Circle implements Serializable {
	
	private static final long serialVersionUID = 6400834001276229287L;
	
	private static final double FUDGE_FACTOR = 0.1d;
	
	private Point center;
    private final double radius;
    
    public Circle(Point center, double radius) {
        this.center = center;
        this.radius = radius;
    }
    
    public boolean contains(Point point) {
        if (point == null) {
            throw new IllegalArgumentException("Null point passed to Circle.contains()");
        }
        return center.distance(point) <= (radius + FUDGE_FACTOR);
    }
    
    public void setCenter(Point center) {
        this.center = center;
    }
    
    
}
