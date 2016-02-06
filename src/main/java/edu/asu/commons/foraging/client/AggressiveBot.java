package edu.asu.commons.foraging.client;


public class AggressiveBot extends Bot.SimpleBot {
    
    private double harvestProbability;
    private double movementProbability;
    private int actionsPerSecond;
    
    public AggressiveBot() {
        this(0.9d, 10, 0.9d);
    }
    
    public AggressiveBot(double harvestProbability, int actionsPerSecond, double movementProbability) {
        this.harvestProbability = harvestProbability;
        this.actionsPerSecond = actionsPerSecond;
        this.movementProbability = movementProbability;
    }
    
    public void setHarvestProbability(double harvestProbability) {
        this.harvestProbability = harvestProbability;
    }

    public void setMovementProbability(double movementProbability) {
        this.movementProbability = movementProbability;
    }

    public void setActionsPerSecond(int actionsPerSecond) {
        this.actionsPerSecond = actionsPerSecond;
    }

    public BotType getBotType() {
        return BotType.AGGRESSIVE;
    }

    public int getActionsPerSecond() {
        return actionsPerSecond;
    }

    public double getMovementProbability() {
        return movementProbability;
    }
    
    public double getHarvestProbability() {
        return harvestProbability;
    }

}
