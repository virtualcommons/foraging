package edu.asu.commons.foraging.model;

import java.awt.Point;
import java.io.Serializable;

/**
 * $Id$
 * 
 * A simple resource struct encapsulating an age and a 2D Point where the resource resides.
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>, Deepali Bhagvat
 * @version $Revision$
 */

public class Resource implements Serializable {
    
    private static final long serialVersionUID = 2834902347558439570L;
    private final Point position;
    private int age;
    
    public Resource(Point point) {
        this(point, 0);
    }
    
    public Resource(Point point, int age) {
        this.position = point;
        this.age = age;
    }
    
    public Resource(int x, int y, int age) {
        this(new Point(x, y), age);
    }

    public int getAge() {
        return age;
    }

    public Point getPosition() {
        return position;
    }
    
    public int getX() {
        return position.x;
    }
    
    public int getY() {
        return position.y;
    }
    
    public boolean equals(Object o) {
        if (o instanceof Resource) {
            return equals((Resource) o);
        }
        return false;
    }
    
    public boolean equals(Point p) {
        return position.equals(p); 
    }
    
    public boolean equals(Resource resource) {
        return position.equals(resource.position);
    }
    
    public int hashCode() {
        return position.hashCode();
    }
    
    public void decreaseAge() {
    	updateAge(-1);
    }
    
    private void updateAge(int years) {
        age += years;
    }
    
    public void setAge(int age) {
        this.age = age;
    }

    public String toString() {
        return String.format("[%d, %d] - %d", position.x, position.y, age);
    }
}
