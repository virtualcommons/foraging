package edu.asu.commons.foraging.bot;

public enum BotType {
    CHASE_PLAYER, IGNORE_PLAYER, RANDOM, CUSTOM;

    public Bot create() {
        switch (this) {
            case CHASE_PLAYER:
                return new ChasePlayerBot();
            case IGNORE_PLAYER:
                return new CooperativeBot();
            case RANDOM:
                return new RandomBot();
            case CUSTOM:
            default:
                return new CustomBot();
        }
    }
}