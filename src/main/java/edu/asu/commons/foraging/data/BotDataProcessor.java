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
import edu.asu.commons.util.Utils;

import java.awt.Point;
import java.io.PrintWriter;
import java.util.Map;
import java.util.SortedSet;
import java.util.logging.Logger;

public class BotDataProcessor extends SaveFileProcessor.Base {

    private Logger logger = Logger.getLogger(getClass().getName());

    public BotDataProcessor() {
        setSecondsPerInterval(1);
    }

    @Override
    public void process(SavedRoundData savedRoundData, PrintWriter writer) {
       	RoundConfiguration roundConfiguration = (RoundConfiguration) savedRoundData.getRoundParameters();
        SortedSet<PersistableEvent> actions = savedRoundData.getActions();
        ServerDataModel dataModel = (ServerDataModel) savedRoundData.getDataModel();
        dataModel.reinitialize(roundConfiguration);
        // generate summarized statistics
        // Time (100-200ms resolution), Subject Number, X, Y, Number of tokens collected, Distance to bot, current velocity
        // see https://github.com/virtualcommons/foraging/issues/19
        writer.println(
                Utils.join(',', "Time", "Subject ID", "X", "Y", "Tokens collected", "Velocity",
                        "Distance to bot", "Bot X", "Bot Y", "Bot Tokens", "Bot velocity")
        );
        Map<Identifier, Actor> actorMap = dataModel.getActorMap();
        Map<Identifier, ClientMovementTokenCount> clientMovement = ClientMovementTokenCount.createMap(dataModel);
        double distanceToBot = 0.0d;
        int actionsTaken = 0;
        int botActionsTaken = 0;
        assert actorMap.size() == 2;
        ClientData client = null;
        Bot bot = null;
        for (Actor actor: actorMap.values()) {
            if (actor instanceof Bot) {
                bot = (Bot) actor;
                bot.initialize(roundConfiguration);
            }
            else if (actor instanceof ClientData) {
                client = (ClientData) actor;
            }
        }
        logger.info("XXX: bot map: " + actorMap);
        for (PersistableEvent event: savedRoundData.getActions()) {
            long secondsElapsed = savedRoundData.getElapsedTimeInSeconds(event);
            logger.info("Inspecting event: " + event);
            if (isIntervalElapsed(secondsElapsed)) {
                // write out aggregated stats
                Point clientPosition = client.getPosition();
                Point botPosition = bot.getPosition();
                distanceToBot = clientPosition.distanceSq(botPosition);
                writer.println(
                        Utils.join(',',
                                secondsElapsed,
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
            if (event instanceof TokenCollectedEvent) {

            }
            if (id instanceof BotIdentifier) {
                botActionsTaken++;
            }
            else if (id instanceof SocketIdentifier) {
                actionsTaken++;
            }
            dataModel.apply(event);
        }
    }

    @Override
    public String getOutputFileExtension() {
        return "-bot-data.csv";
    }
}
