package edu.asu.commons.foraging.model;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.Random;

/**
 * $Id: Direction.java 4 2008-07-25 22:51:44Z alllee $
 * 
 * Enum representing a cardinal Direction in a 2-D grid.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 4 $
 */

public enum Direction {
    
    LEFT(-1, 0) { public Direction opposite() { return RIGHT; } }, 
    RIGHT(1, 0) { public Direction opposite() { return LEFT; } }, 
    UP(0, -1) { public Direction opposite() { return DOWN; } }, 
    DOWN(0, 1) { public Direction opposite() { return UP; } }; 
    
    // modifiers to be applied to a given Point.
    private final int dx;
    private final int dy;

    private Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public Point apply(Point point) {
        return new Point(point.x + dx, point.y + dy);
    }
    
    public abstract Direction opposite();

    public String toString() {
        return name().toLowerCase();
    }

    private transient static Random rng;
    
    /**
     * @return a randomly selected Direction
     */
    public static synchronized Direction random() {
        if (rng == null) {
            rng = new Random();
        }
        return Direction.values()[rng.nextInt(4)];
    }

    public static Direction valueOf(final int keyCode) {
        // XXX: maybe think of a clever way to mask this out into an array
        // index so we can just do something like
        // return DIRECTIONS[mask(keyCode)];
        switch (keyCode) {
        case KeyEvent.VK_LEFT:
            return Direction.LEFT;
        case KeyEvent.VK_RIGHT:
            return Direction.RIGHT;
        case KeyEvent.VK_UP:
            return Direction.UP;
        case KeyEvent.VK_DOWN:
            return Direction.DOWN;
        default:
            return null;
        }
    }
}
