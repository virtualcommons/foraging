package edu.asu.commons.foraging.model;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.Random;

/**
 * $Id$
 * 
 * Enum representing a cardinal Direction in a 2-D grid.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

public enum Direction {

    NONE(0, 0) {
        public Direction opposite() {
            return NONE;
        }
    },
    LEFT(-1, 0) {
        public Direction opposite() {
            return RIGHT;
        }
    },
    RIGHT(1, 0) {
        public Direction opposite() {
            return LEFT;
        }
    },
    UP(0, -1) {
        public Direction opposite() {
            return DOWN;
        }
    },
    DOWN(0, 1) {
        public Direction opposite() {
            return UP;
        }
    };

    // modifiers to be applied to a given Point.
    private final int dx;
    private final int dy;

    private Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Returns the next Point that would result from moving in this Direction from the given point.
     * @param point 
     * @return the next Point that would result from moving in this Direction from the given point.
     */
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
    
    public static Direction towards(Point a, Point b) {
        int dx = a.x - b.x;
        int dy = a.y - b.y;
        if (dx > 0) {
            return Direction.LEFT;
        }
        else if (dx < 0) {
            return Direction.RIGHT;
        }
        else if (dy > 0) {
            return Direction.UP;
        }
        else if (dy < 0) {
            return Direction.DOWN;
        }
        else {
            return Direction.NONE;
        }
        
    }

    /**
     * Currently supports WASD and IJKL and keyboard arrow keys for up, left, down, right, respectively.
     * @param keyCode
     * @return appropriate Direction for the given keyCode
     */
    public static Direction valueOf(final int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
            case KeyEvent.VK_I:
                return Direction.UP;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
            case KeyEvent.VK_J:
                return Direction.LEFT;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
            case KeyEvent.VK_K:
                return Direction.DOWN;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
            case KeyEvent.VK_L:
                return Direction.RIGHT;
            default:
                return null;
        }
    }
}
