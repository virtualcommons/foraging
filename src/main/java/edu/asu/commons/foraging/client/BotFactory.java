package edu.asu.commons.foraging.client;

import edu.asu.commons.foraging.model.GroupDataModel;


public class BotFactory {

    public static final BotFactory INSTANCE = new BotFactory();

    private BotFactory() {
    }

    public final static BotFactory getInstance() {
        return INSTANCE;
    }
    
    public Bot create(BotType botType, int botNumber, GroupDataModel groupDataModel) {
        Bot bot = null;
        switch (botType) {
            case AGGRESSIVE:
                bot = new AggressiveBot();
            case COOPERATIVE:
                bot = new CooperativeBot();
            case RANDOM:
            default:
                bot = new RandomBot();
        }
        bot.setBotNumber(botNumber);
        bot.setGroupDataModel(groupDataModel);
        return bot;
    }

}
