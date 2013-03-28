package edu.asu.commons.foraging.data;

import java.awt.Point;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;

import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SaveFileProcessor.Base;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.event.TokenCollectedEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;

public class ForagingRuleProcessor extends Base {

    // rules are based on ForagingStrategy enum and ordered accordingly
    // rule 1: collect tokens for 10 seconds than wait 10 seconds
    // rule 2: private property, 4 quadrants
    // rule 3: wait 60 seconds
    // rule 4: collect 40 tokens then wait 30 seconds 
    private static class RuleData {
        private int illegalRuleOneTokens = 0;
        private int illegalRuleThreeTokens = 0;
        private int illegalRuleFourTokens = 0;
        private int totalTokensCollected = 0;
        private int q1Tokens = 0;
        private int q2Tokens = 0;
        private int q3Tokens = 0;
        private int q4Tokens = 0;
        private int nextLegalCollectionTime = -1;
        private int ruleFourTokenInterval = 1;
        
        public void addTokenCollected(Point location, int elapsedTimeInSeconds) {
            if (isIllegalTokenCollectionForRuleOne(elapsedTimeInSeconds)) {
                illegalRuleOneTokens++;
            }
            if (isIllegalTokenCollectionForRuleThree(elapsedTimeInSeconds)) {
                illegalRuleThreeTokens++;
            }
            totalTokensCollected++;
            addRuleTwoTokens(location);
            checkRuleFourTokens(elapsedTimeInSeconds);
        }
        
        private void checkRuleFourTokens(int elapsedTimeInSeconds) {
            if (isIllegalRuleFourInterval(elapsedTimeInSeconds)) {
                illegalRuleFourTokens++;
            }
        }
        
        private boolean isIllegalRuleFourInterval(int elapsedTimeInSeconds) {
            if (totalTokensCollected < 40) {
                return false;
            }
            int allowedTokens = ruleFourTokenInterval * 40;
            // first check if we've reached our token goal
            if (totalTokensCollected == allowedTokens) {
                // next allowable time is 30 seconds from now. 
                nextLegalCollectionTime = elapsedTimeInSeconds + 30;
                // the next number of allowable tokens is N * 40
                ruleFourTokenInterval++;
            }
//            System.err.println(String.format("allowed tokens: %d, total tokens collected: %d, next legal collection time: %d, token interval %d, legal collection? %s",
//                    allowedTokens,
//                    totalTokensCollected,
//                    nextLegalCollectionTime,
//                    ruleFourTokenInterval,
//                    (elapsedTimeInSeconds < nextLegalCollectionTime)));
            return elapsedTimeInSeconds < nextLegalCollectionTime;
        }
        
        private void addRuleTwoTokens(Point location) {
            if (location.x < 13) {
                if (location.y < 13) {
                    q1Tokens++;
                }
                else {
                    q2Tokens++;
                }
            }
            else {
                if (location.y < 13) {
                    q3Tokens++;
                }
                else {
                    q4Tokens++;
                }
            }
        }

        private boolean isIllegalTokenCollectionForRuleThree(int elapsedTimeInSeconds) {
            return elapsedTimeInSeconds <= 60;
        }

        /**
         * Returns true if the elapsed time is in the following interval ranges:
         * 0-10, 20-30, 40-50, 60-70, and so on.
         *
         * @param elapsedTimeInSeconds
         * @return
         */
        private boolean isIllegalTokenCollectionForRuleOne(int elapsedTimeInSeconds) {
            int interval = elapsedTimeInSeconds / 10;
            int intervalModTwo = interval % 2;
            return intervalModTwo == 0;
        }
        
        public double getRuleOneBreaking() {
            return (double) illegalRuleOneTokens / (double) totalTokensCollected;
        }
        public double getRuleThreeBreaking() {
            return (double) illegalRuleThreeTokens / (double) totalTokensCollected;
        }
        public double getRuleFourBreaking() {
            return (double) illegalRuleFourTokens / (double) totalTokensCollected;
        }

        @Override
        public String toString() {
            return String.format("illegal R1 tokens: %d, illegal R2 tokens: %d, total tokens: %d, [%d, %d, %d, %d]", 
                    illegalRuleOneTokens, illegalRuleFourTokens, totalTokensCollected, q1Tokens, q2Tokens, q3Tokens, q4Tokens);
        }

    }

    @Override
    public String getOutputFileExtension() {
        return "-rule-data.txt";
    }

    @Override
    public void process(SavedRoundData savedRoundData, PrintWriter writer) {
        SortedSet<PersistableEvent> actions = savedRoundData.getActions();
        ServerDataModel dataModel = (ServerDataModel) savedRoundData.getDataModel();
        Map<Identifier, ClientData> clientDataMap = dataModel.getClientDataMap();
        Map<ClientData, RuleData> dataMap = new TreeMap<ClientData, RuleData>(new Comparator<ClientData>() {
            public int compare(ClientData a, ClientData b) {
                return a.getId().getStationId().compareTo(b.getId().getStationId());
            }
        });
        for (ClientData data: clientDataMap.values()) {
            dataMap.put(data, new RuleData());
        }
        for (PersistableEvent event: actions) {
            if (event instanceof TokenCollectedEvent) {
                TokenCollectedEvent tokenCollectedEvent = (TokenCollectedEvent) event;
                ClientData clientData = clientDataMap.get(event.getId());
                Point location = tokenCollectedEvent.getLocation();
                long elapsedTimeInSeconds = savedRoundData.getElapsedTimeInSeconds(event);
                dataMap.get(clientData).addTokenCollected(location, (int) elapsedTimeInSeconds);
            }
        }
        writer.println("Participant, 10 Second Rule, 60 Second Rule, 40 Second Rule, Q1 Tokens, Q2 Tokens, Q3 Tokens, Q4 Tokens");
        for (Map.Entry<ClientData, RuleData> entry: dataMap.entrySet()) {
            RuleData data = entry.getValue();
            String line = String.format("%s, %3.2f, %3.2f, %3.2f, %d, %d, %d, %d",
                    entry.getKey(),
                    data.getRuleOneBreaking(),
                    data.getRuleThreeBreaking(),
                    data.getRuleFourBreaking(),
                    data.q1Tokens,
                    data.q2Tokens,
                    data.q3Tokens,
                    data.q4Tokens
                    );
            System.err.println(line);
            writer.println(line);
        }
    }

}
