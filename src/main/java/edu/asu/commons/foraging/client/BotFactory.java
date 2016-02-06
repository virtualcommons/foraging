package edu.asu.commons.foraging.client;

import java.util.HashMap;
import java.util.Map;


public class BotFactory {

    private final Map<BotType, Class<? extends Bot>> botClassMap = new HashMap<>();

    public static final BotFactory INSTANCE = new BotFactory();

    private BotFactory() {
        botClassMap.put(BotType.AGGRESSIVE, AggressiveBot.class);
    }

    public Bot create(BotType botType) {
        try {
            return botClassMap.get(botType).newInstance();
        }
        catch (Exception exception) {
            exception.printStackTrace();
            throw new RuntimeException("Couldn't create bot of type " + botType, exception);
        }
    }

}
