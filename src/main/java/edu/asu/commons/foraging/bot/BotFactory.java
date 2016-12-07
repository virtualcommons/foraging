package edu.asu.commons.foraging.bot;

import edu.asu.commons.foraging.model.GroupDataModel;


public class BotFactory {

    public static final BotFactory INSTANCE = new BotFactory();

    private BotFactory() {
    }

    public final static BotFactory getInstance() {
        return INSTANCE;
    }

    public Bot create(int botNumber, GroupDataModel groupDataModel, int actionsPerSecond, double movementProbability, double harvestProbability) {
        Bot bot = new NormalBot(actionsPerSecond, movementProbability, harvestProbability);
        bot.setBotNumber(botNumber);
        bot.setGroupDataModel(groupDataModel);
        return bot;
    }
    
    public Bot create(BotType botType, int botNumber, GroupDataModel groupDataModel) {
        Bot bot = null;
        switch (botType) {
            case AGGRESSIVE:
                bot = new AggressiveBot();
                break;
            case COOPERATIVE:
                bot = new CooperativeBot();
                break;
            case NORMAL:
                bot = new NormalBot();
                break;
            case RANDOM:
            default:
                bot = new RandomBot();
                break;
        }
        bot.setBotNumber(botNumber);
        bot.setGroupDataModel(groupDataModel);
        return bot;
    }

}
