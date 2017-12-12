package edu.asu.commons.foraging.data;

import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SaveFileProcessor;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.bot.Bot;
import edu.asu.commons.foraging.bot.BotIdentifier;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.*;
import edu.asu.commons.foraging.model.Actor;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.net.SocketIdentifier;
import edu.asu.commons.util.Pair;
import edu.asu.commons.util.Utils;

import java.awt.Point;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;
import java.util.logging.Logger;

public class BotDataProcessor extends SaveFileProcessor.Base {

    private Logger logger = Logger.getLogger(getClass().getName());

    public BotDataProcessor() {
        this(500);
    }

    public BotDataProcessor(long millisPerInterval) {
        super(millisPerInterval);
    }

    public Pair<Bot, ClientData> getBotAndClient(Collection<Actor> actors, RoundConfiguration roundConfiguration) {
        Bot bot = null;
        ClientData client = null;
        for (Actor actor: actors) {
            if (actor instanceof Bot) {
                bot = (Bot) actor;
                bot.initialize(roundConfiguration);
            }
            else if (actor instanceof ClientData) {
                client = (ClientData) actor;
            }
        }
        return new Pair<>(bot, client);
    }

    @Override
    public void process(SavedRoundData savedRoundData, PrintWriter writer) {
       	RoundConfiguration roundConfiguration = (RoundConfiguration) savedRoundData.getRoundParameters();
        SortedSet<PersistableEvent> actions = savedRoundData.getActions();
        ServerDataModel dataModel = (ServerDataModel) savedRoundData.getDataModel();
        long elapsedTimeRelativeToMidnightOffset = savedRoundData.getElapsedTimeRelativeToMidnight(actions.first());
        dataModel.reinitialize(roundConfiguration);
        // generate summarized statistics
        writer.println(
                Utils.join(',', "Time", "Elapsed Time Midnight", "Subject ID", "X", "Y", "Tokens collected", "Player moves",
                        "Distance to bot", "Bot X", "Bot Y", "Bot Tokens", "Bot moves")
        );
        Map<Identifier, Actor> actorMap = dataModel.getActorMap();
        int actionsTaken = 0;
        int botActionsTaken = 0;
        assert actorMap.size() == 2;
        Pair<Bot, ClientData> pair = getBotAndClient(actorMap.values(), roundConfiguration);
        Bot bot = pair.getFirst();
        if (bot == null) {
            assert roundConfiguration.isPracticeRound();
            bot = Bot.NULL;
        }

        ClientData client = pair.getSecond();
        for (PersistableEvent event: savedRoundData.getActions()) {
            long millisecondsElapsed = savedRoundData.getElapsedTime(event);
            logger.info("Inspecting event: " + event);
            if (isIntervalElapsed(millisecondsElapsed)) {
                // write out aggregated stats
                Point clientPosition = client.getPosition();
                Point botPosition = bot.getPosition();
                double distanceToBot = clientPosition.distance(botPosition);
                writer.println(
                        Utils.join(',',
                                getIntervalEnd(),
                                elapsedTimeRelativeToMidnightOffset + getIntervalEnd(),
                                client.getId().getStationId(),
                                clientPosition.x,
                                clientPosition.y,
                                client.getCurrentTokens(),
                                actionsTaken,
                                distanceToBot,
                                botPosition.x,
                                botPosition.y,
                                bot.getCurrentTokens(),
                                botActionsTaken
                                )
                );
                actionsTaken = 0;
                botActionsTaken = 0;
            }
            Identifier id = event.getId();
            if (event instanceof MovementEvent) {
                if (id instanceof BotIdentifier) {
                    botActionsTaken++;
                } else if (id instanceof SocketIdentifier) {
                    actionsTaken++;
                }
            }
            dataModel.apply(event);
        }
    }

    @Override
    public String getOutputFileExtension() {
        return "-bot-data.csv";
    }
}
