package edu.asu.commons.foraging.data;

import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.model.Direction;
import edu.asu.commons.net.Identifier;

/**
 * $Id: ClientMovementStatistics.java 526 2010-08-06 01:25:27Z alllee $
 * 
 * Helper class to keep track of client movements. 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 526 $
 */
class ClientMovementStatistics {
    private final Identifier id;
    private Direction lastDirection;
    // distribution of moves, each entry in the array represents the number of times they've moved.
    private int[] movementDistribution;
    private int currentMoveCount = 0;
    private int allMoves;

    public ClientMovementStatistics(Identifier id, RoundConfiguration configuration) {
        this.id = id;
        int maximumNumberOfMoves = Math.max(configuration.getBoardSize().height, configuration.getBoardSize().width);
        movementDistribution = new int[maximumNumberOfMoves];
    }

    void incrementMovementDistribution() {
        int movementDistributionIndex = Math.min(currentMoveCount, movementDistribution.length-1);
        if (movementDistributionIndex > 0) {
            movementDistribution[movementDistributionIndex-1]++;
        }
        currentMoveCount = 1;
    }

    public synchronized void move(Direction direction) {
        if (direction != null) {
            allMoves++;
        }
        if (lastDirection == null) {
            lastDirection = direction;
            currentMoveCount = 1;                   
        }
        else if (lastDirection.equals(direction)) {
            currentMoveCount++;
        }
        else {
            lastDirection = direction;
            incrementMovementDistribution();
        }
    }

    public Integer[] getMovementDistribution() {
        Integer[] rv = new Integer[movementDistribution.length];
        for (int i = 0; i < movementDistribution.length; i++) {
            rv[i] = Integer.valueOf(movementDistribution[i]);
        }
        return rv;
    }

    public void validate() {
        int sum = 0;
        for (int i = 0; i < movementDistribution.length; i++) {
            // i+1 to offset zero-based array index
            sum += ((i+1) * movementDistribution[i]);
        }
        if (allMoves != sum) {
            throw new RuntimeException("Identifier id: " + id + " -- allMoves: " + allMoves + " not equal to sum of all moves: " + sum);
        }
    }
}