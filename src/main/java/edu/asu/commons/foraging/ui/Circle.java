package edu.asu.commons.foraging.ui;

import java.awt.Point;
import java.io.Serializable;

/**
 * $Id$
 * 
 * Simple Circle class given a Point center and radius and providing methods to detect
 * if a point is within the circle.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class Circle implements Serializable {
	
	private static final long serialVersionUID = 6400834001276229287L;
	
	private Point center;
    private final double radius;
    
    public Circle(Point center, double radius) {
        this.center = center;
        this.radius = radius;
    }
    
    public boolean contains(Point point) {
        if (point == null) {
            return false;
        }
        return center.distance(point) <= radius;
    }
    
    public void setCenter(Point center) {
        this.center = center;
    }
    
}
