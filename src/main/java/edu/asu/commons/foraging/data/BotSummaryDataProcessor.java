package edu.asu.commons.foraging.data;

import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.bot.Bot;
import edu.asu.commons.foraging.bot.BotIdentifier;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.MovementEvent;
import edu.asu.commons.foraging.model.Actor;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.net.SocketIdentifier;
import edu.asu.commons.util.Pair;
import edu.asu.commons.util.Utils;

import java.awt.Point;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
                Utils.join(',', "Start Time",
                        "Subject ID", "Subject total moves", "Subject total tokens",
                        "Bot total moves", "Bot total tokens", "Bot type",
                        "Tokens left", "Time to collapsed resource",
                        "Average distance to bot (500ms intervals)",
                        "Growth rate"
                )
        );
        Map<Identifier, Actor> actorMap = dataModel.getActorMap();
        int clientMovesTaken = 0;
        int botMovesTaken = 0;
        assert actorMap.size() == 2;
        Pair<Bot, ClientData> pair = getBotAndClient(actorMap.values(), roundConfiguration);
        Bot bot = pair.getFirst();
        ClientData client = pair.getSecond();
        int botTokens = bot.getCurrentTokens();
        int totalClientTokens = client.getTotalTokens();
        GroupDataModel group = dataModel.getGroup(client.getId());
        int tokensLeft = group.getResourceDistributionSize();
        int timeToCollapsedResource = Integer.MAX_VALUE;
        List<Double> botDistances = new ArrayList<>();
        dataModel.reinitialize(roundConfiguration);
        PersistableEvent firstEvent = savedRoundData.getActions().first();
        long startTimeRelativeToMidnight = savedRoundData.getElapsedTimeRelativeToMidnight(firstEvent.getCreationTime());
        for (PersistableEvent event: savedRoundData.getActions()) {
            long millisecondsElapsed = savedRoundData.getElapsedTime(event);
            if (isIntervalElapsed(millisecondsElapsed)) {
                // write out aggregated stats
                Point clientPosition = client.getPosition();
                Point botPosition = bot.getPosition();
                botDistances.add(clientPosition.distance(botPosition));
            }
            Identifier id = event.getId();
            if (event instanceof MovementEvent) {
                if (id instanceof BotIdentifier) {
                    botMovesTaken++;
                } else if (id instanceof SocketIdentifier) {
                    clientMovesTaken++;
                }
            }
            dataModel.apply(event);
            if (group.isResourceDistributionEmpty()) {
                timeToCollapsedResource = (int) savedRoundData.getElapsedTime(event);
            }
        }
        // double averageDistanceToBot = botDistances.stream().mapToDouble(i->i).average().orElse(0);
        double averageDistanceToBot = botDistances.stream().collect(Collectors.averagingDouble(d->d));
        writer.println(Utils.join(',',
                startTimeRelativeToMidnight,
                client.getId().getStationId(), clientMovesTaken, totalClientTokens,
                botMovesTaken, botTokens, bot.getBotType().name(),
                tokensLeft, timeToCollapsedResource,
                averageDistanceToBot,
                roundConfiguration.getRegrowthRate()
                )
        );
    }

    @Override
    public String getOutputFileExtension() {
        return "-summary-bot-data.csv";
    }
}
