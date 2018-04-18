package edu.asu.commons.foraging.bot;

import edu.asu.commons.foraging.model.GroupDataModel;


public class BotFactory {

    public static final BotFactory INSTANCE = new BotFactory();

    private BotFactory() {
    }

    public final static BotFactory getInstance() {
        return INSTANCE;
    }

    public Bot create(GroupDataModel groupDataModel, int botNumber) {
        return create(BotType.CUSTOM, groupDataModel, botNumber);
    }

    public Bot create(BotType botType, GroupDataModel groupDataModel, int botNumber) {
        return botType.create().setGroupDataModel(groupDataModel).setBotNumber(botNumber);
    }

}
