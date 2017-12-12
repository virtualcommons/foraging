package edu.asu.commons.foraging.data;

import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SaveFileProcessor;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.bot.Bot;
import edu.asu.commons.foraging.bot.BotIdentifier;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.MovementEvent;
import edu.asu.commons.foraging.model.Actor;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.net.SocketIdentifier;
import edu.asu.commons.util.Pair;
import edu.asu.commons.util.Utils;

import java.awt.Point;
import java.io.PrintWriter;
import java.util.Map;
import java.util.SortedSet;
import java.util.logging.Logger;

public class BotSummaryDataProcessor extends BotDataProcessor {

    private Logger logger = Logger.getLogger(getClass().getName());

    public BotSummaryDataProcessor() {
        super(500);
    }

    @Override
    public void process(SavedRoundData savedRoundData, PrintWriter writer) {
       	RoundConfiguration roundConfiguration = (RoundConfiguration) savedRoundData.getRoundParameters();
        SortedSet<PersistableEvent> actions = savedRoundData.getActions();
        ServerDataModel dataModel = (ServerDataModel) savedRoundData.getDataModel();
        // generate summarized statistics
        writer.println(
                Utils.join(',', "Time", "Elapsed Time Midnight",
                        "Subject ID", "Subject Total Moves", "Subject Total Tokens",
                        "Bot Total Tokens", "Bot Total Moves",
                        "Time of collapsed resource"
                )
        );
        Map<Identifier, Actor> actorMap = dataModel.getActorMap();
        Map<Identifier, ClientMovementTokenCount> clientMovement = ClientMovementTokenCount.createMap(dataModel);
        int actionsTaken = 0;
        int botActionsTaken = 0;
        assert actorMap.size() == 2;
        Pair<Bot, ClientData> pair = getBotAndClient(actorMap.values(), roundConfiguration);
        Bot bot = pair.getFirst();
        ClientData client = pair.getSecond();
        for (PersistableEvent event: savedRoundData.getActions()) {
            long millisecondsElapsed = savedRoundData.getElapsedTime(event);
            if (isIntervalElapsed(millisecondsElapsed)) {
                // write out aggregated stats
                Point clientPosition = client.getPosition();
                Point botPosition = bot.getPosition();
                double distanceToBot = clientPosition.distance(botPosition);
                writer.println(
                        Utils.join(',',
                                getIntervalEnd(),
                                savedRoundData.getElapsedTimeRelativeToMidnight(getIntervalEnd()),
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
        return "-summary-bot-data.csv";
    }
}
