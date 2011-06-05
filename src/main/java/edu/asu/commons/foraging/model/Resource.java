package edu.asu.commons.foraging.model;

import java.awt.Point;
import java.io.Serializable;

import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.util.Duration;

/**
 * $Id: Resource.java 51 2008-09-09 01:06:15Z alllee $
 * 
 * A simple resource struct encapsulating an age and a 2D Point where the resource resides.
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>, Deepali Bhagvat
 * @version $Revision: 51 $
 */

public class Resource implements Serializable {
    
    private static final long serialVersionUID = 2834902347558439570L;
    private transient static RoundConfiguration configuration;
    private final Point position;
    private int age;
    
    private Duration fruitHarvestDelay;
    
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

    public void increaseAge() {
        int fruitingAge = configuration.getMaximumResourceAge() - 1;
        if ( age < fruitingAge
                || (age == fruitingAge && canFruit())) 
        {
            updateAge(1);
        }
    }
    
    private boolean canFruit() {
        return fruitHarvestDelay == null || fruitHarvestDelay.hasExpired();
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

    public void harvestFruits() {
        decreaseAge();
        if (fruitHarvestDelay == null) {
            fruitHarvestDelay = Duration.create(configuration.getFruitHarvestDelay());
        }
        fruitHarvestDelay.start();
    }
    
    public static void setConfiguration(RoundConfiguration configuration) {
        Resource.configuration = configuration;
    }

}
