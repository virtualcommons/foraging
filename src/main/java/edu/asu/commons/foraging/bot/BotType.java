package edu.asu.commons.foraging.bot;

public enum BotType {
    AGGRESSIVE, COOPERATIVE, RANDOM, CUSTOM;

    public Bot create() {
        switch (this) {
            case AGGRESSIVE:
                return new AggressiveBot();
            case COOPERATIVE:
                return new CooperativeBot();
            case RANDOM:
                return new RandomBot();
            case CUSTOM:
            default:
                return new CustomBot();
        }
    }
}