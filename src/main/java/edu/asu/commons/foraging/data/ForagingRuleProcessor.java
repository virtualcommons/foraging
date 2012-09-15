package edu.asu.commons.foraging.data;

import java.awt.Point;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import edu.asu.commons.event.PersistableEvent;
import edu.asu.commons.experiment.SaveFileProcessor.Base;
import edu.asu.commons.experiment.SavedRoundData;
import edu.asu.commons.foraging.conf.RoundConfiguration;
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
        private int illegalRuleOneTokens;
        private int illegalRuleFourTokens;
        private int totalTokensCollected;
        private int q1Tokens;
        private int q2Tokens;
        private int q3Tokens;
        private int q4Tokens;
        private int ruleFourTimestamp = -1;
        
        public void addTokenCollected(Point location, int elapsedTimeInSeconds) {
            if (isIllegalTokenCollectionForRuleOne(elapsedTimeInSeconds)) {
                illegalRuleOneTokens++;
            }
            if (isIllegalTokenCollectionForRuleThree(elapsedTimeInSeconds)) {
                
            }
            totalTokensCollected++;
            addRuleTwoTokens(location);
            checkRuleFourTokens(elapsedTimeInSeconds);
        }
        
        private void checkRuleFourTokens(int elapsedTimeInSeconds) {
            if (totalTokensCollected < 40) {
                return;
            }
            if (totalTokensCollected < 80 && ruleFourTimestamp == -1) {
                // 40 tokens have been collected, from now on each token collected is illegal until 30 seconds from ruleFourTimestamp have passed.
                ruleFourTimestamp = elapsedTimeInSeconds;
                return;
            }
            if (elapsedTimeInSeconds < ruleFourTimestamp + 30) {
                illegalRuleFourTokens++;
                return;
            }
            // FIXME: need to finish implementing rule four logic
                        
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

        @Override
        public String toString() {
            return String.format("illegal R1 tokens: %d, illegal R2 tokens: %d, total tokens: %d, [%d, %d, %d, %d]", 
                    illegalRuleOneTokens, illegalRuleFourTokens, totalTokensCollected, q1Tokens, q2Tokens, q3Tokens, q4Tokens);
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
        Map<ClientData, RuleData> dataMap = new HashMap<ClientData, RuleData>();
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
    }

}
