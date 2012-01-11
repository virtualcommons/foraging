package edu.asu.commons.foraging.conf;

import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.stringtemplate.v4.ST;

import edu.asu.commons.conf.ExperimentRoundParameters;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.EnforcementMechanism;
import edu.asu.commons.foraging.rules.ForagingRule;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.util.Duration;


/**
 * $Id$
 * 
 * At some point this should be persistent database objects in a key-value store..?
 * 
 * Something like:
 * 
 * Parameter name, value, type, instructions
 * 
 * need to deal with i18n at some point as well..
 * 
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 534 $
 */
public class RoundConfiguration extends ExperimentRoundParameters.Base<ServerConfiguration> {

    private final static long serialVersionUID = 8575239803733029326L;

    public final static double DEFAULT_REGROWTH_RATE = 0.01;

    public final static int DEFAULT_ROUND_TIME = 5 * 60;

    private static final int DEFAULT_SANCTION_FLASH_DURATION = 3;

    private static final double DEFAULT_TOKEN_MOVEMENT_PROBABILITY = 0.2d;

    private static final double DEFAULT_TOKEN_BIRTH_PROBABILITY = 0.01d;

    private List<ForagingRule> selectedRules;

    public double getTrustGamePayoffIncrement() {
        return getDoubleProperty("trust-game-payoff", 0.25d);
    }

    public enum SanctionType {
        REAL_TIME, POST_ROUND, NONE;
        public static SanctionType find(String name) {
            try {
                return valueOf(name.toUpperCase().replaceAll("-", "_"));
            } catch (Exception exception) {
                return NONE;
            }
        }
    }
    
    public enum PositionType {
        LINE, SQUARE;
    }

    private final static Map<String, ExperimentType> experimentTypeMap = new HashMap<String, ExperimentType>(3);

    public enum ExperimentType {
        TWO_DIMENSIONAL("2d"), ABSTRACT("abstract"), FORESTRY("forestry");
        private final String name;

        private ExperimentType(String name) {
            this.name = name;
            experimentTypeMap.put(name, this);
        }

        public static ExperimentType find(String name) {
            ExperimentType experimentType = experimentTypeMap.get(name);
            if (experimentType == null) {
                experimentType = TWO_DIMENSIONAL;
            }
            return experimentType;
        }

        public String toString() {
            return name;
        }
    }

    public enum SanctionAction {
        FINE() {
            public void applySanctionCost(ClientData clientData) {
                // perform sanction cost logic here for fines
            }

            public void applySanctionPenalty(ClientData clientData) {
                // perform sanction penalty logic here for fines
            }
        },
        FREEZE() {
            public void applySanctionCost(ClientData clientData) {
                // perform sanction cost logic here for freezing
            }

            public void applySanctionPenalty(ClientData clientData) {
                // perform sanction penalty logic here for freezing
            }
        };

        // FIXME: do these need to be here to be able to invoke these methods on SanctionAction?
        public void applySanctionCost(ClientData clientData) {
        }

        public void applySanctionPenalty(ClientData clientData) {
        }

        public boolean isFine() {
            return this == FINE;
        }

        public boolean isFreeze() {
            return this == FREEZE;
        }
    }

    public RoundConfiguration() {
        super();
    }

    public RoundConfiguration(String resource) {
        super(resource);
    }

    public boolean shouldRandomizeGroup() {
        return (isPracticeRound() && isPrivateProperty())
                || getBooleanProperty("randomize-group", false);
    }

    /**
     * Returns the number of seconds that the flashing visualization of
     * sanctioning should occur.
     * 
     * @return
     */
    public int getSanctionFlashDuration() {
        return getIntProperty("sanction-flash-duration", DEFAULT_SANCTION_FLASH_DURATION);
    }

    public double getTokenBirthProbability() {
        return getDoubleProperty("token-birth-probability", DEFAULT_TOKEN_BIRTH_PROBABILITY);
    }

    public double getTokenMovementProbability() {
        return getDoubleProperty("token-movement-probability", DEFAULT_TOKEN_MOVEMENT_PROBABILITY);
    }

    public boolean isTokensFieldOfVisionEnabled() {
        return getBooleanProperty("tokens-field-of-vision", false);
    }

    public boolean isSubjectsFieldOfVisionEnabled() {
        return getBooleanProperty("subjects-field-of-vision", false);
    }

    public int getViewSubjectsRadius() {
        if (isSubjectsFieldOfVisionEnabled()) {
            return getIntProperty("view-subjects-radius", 6);
        }
        throw new UnsupportedOperationException("subject field of vision is not enabled.");
    }

    public double getViewTokensRadius() {
        if (isTokensFieldOfVisionEnabled()) {
            return getDoubleProperty("view-tokens-radius", 6.0d);
        }
        throw new UnsupportedOperationException("view tokens field of vision is not enabled.");
    }

    /**
     * Returns a double between [0, 1] used as a scaling factor modifying the probability
     * that a token grows in a neighboring cell.
     * 
     * @return
     */
    public double getRegrowthRate() {
        return getDoubleProperty("regrowth-rate", DEFAULT_REGROWTH_RATE);
    }

    public int getInitialNumberOfTokens() {
        return getIntProperty("starting-tokens",
                (int) (getInitialDistribution() * getResourceWidth() * getResourceDepth()));
    }

    public double getInitialDistribution() {
        return getDoubleProperty("initial-distribution", 0.25d);
    }

    public Dimension getBoardSize() {
        return new Dimension(getResourceWidth(), getResourceDepth());
    }

    public int getResourceWidth() {
        return getIntProperty("resource-width", 28);
    }

    public int getResourceDepth() {
        return getIntProperty("resource-depth", 28);
    }

    public boolean isPrivateProperty() {
        return getBooleanProperty("private-property");
    }

    public boolean isPracticeRound() {
        return getBooleanProperty("practice-round");
    }

    public int getClientsPerGroup() {
        if (isPrivateProperty()) {
            return 1;
        }
        return getIntProperty("clients-per-group", Integer.MAX_VALUE);
    }

    /**
     * Returns an int specifying how many tokens the sanctioner must pay to
     * penalize another player.
     * 
     * @return
     */
    public int getSanctionCost() {
        return getIntProperty("sanction-cost", 1);
    }

    /**
     * Returns an int specifying how much we should scale the tokens used to sanction another
     * player (for a bonus or penalty).
     * 
     * @return
     */
    public int getSanctionMultiplier() {
        return getIntProperty("sanction-multiplier", 2);
    }

    public int getSanctionPenalty() {
        return getSanctionCost() * getSanctionMultiplier();
    }

    public SanctionType getSanctionType() {
        return SanctionType.find(getProperty("sanction-type", "none"));
    }

    public boolean isPostRoundSanctioningEnabled() {
        return getSanctionType().equals(SanctionType.POST_ROUND);
    }

    public boolean isRealTimeSanctioningEnabled() {
        return getSanctionType().equals(SanctionType.REAL_TIME);
    }

    public boolean isSanctioningEnabled() {
        return isRealTimeSanctioningEnabled() || isPostRoundSanctioningEnabled();
    }

    public boolean shouldCheckOccupancy() {
        return getMaximumOccupancyPerCell() < getClientsPerGroup();
    }

    public int getMaximumOccupancyPerCell() {
        return getIntProperty("max-cell-occupancy", getClientsPerGroup());
    }

    public boolean isChatAnonymized() {
        return getBooleanProperty("anonymous-chat", false);
    }

    public double getDollarsPerToken() {
        return getDoubleProperty("dollars-per-token", getParentConfiguration().getDollarsPerToken());
    }

    /**
     * Returns the instructions for this round.  If undefined at the round level it uses default instructions at the parent ServerConfiguration level.
     */
    public String getInstructions() {
        ST template = createStringTemplate(getProperty("instructions", getParentConfiguration().getSameRoundAsPreviousInstructions()));
        // FIXME: this isn't ideal, figure out how to get any bean properties transparently accessible within a templatized instruction
        // could do it via  1. reflection 2. annotations 3. ???   
        template.add("resourceWidth", getResourceWidth());
        template.add("resourceDepth", getResourceDepth());
        template.add("duration", inMinutes(getDuration()) + " minutes");
        template.add("roundNumber", getRoundNumber());
        template.add("clientsPerGroup", getClientsPerGroup());
        template.add("dollarsPerToken", NumberFormat.getCurrencyInstance().format(getDollarsPerToken()));
        template.add("initialDistribution", NumberFormat.getPercentInstance().format(getInitialDistribution()));
        return template.render();
    }

    public boolean shouldDisplayGroupTokens() {
        return getBooleanProperty("display-group-tokens");
    }

    public boolean isQuizEnabled() {
        return getBooleanProperty("quiz");
    }

    public String getChatInstructions() {
        ST template = createStringTemplate(getProperty("chat-instructions"));
        template.add("chatDuration", inMinutes(getChatDuration()) + " minutes");
        return template.render();
    }
    
    public long inMinutes(Duration duration) {
        return inMinutes(duration.getTimeLeftInSeconds());
    }
    
    public long inMinutes(long seconds) {
        return TimeUnit.MINUTES.convert(seconds, TimeUnit.SECONDS);
    }
    
    public String getRegulationInstructions() {
        return getProperty("regulation-instructions");
    }

    public String getLastRoundDebriefing() {
        return getProperty("last-round-debriefing");
    }

    /**
     * FIXME: quiz instructions and quiz enabled should be tightly coupled..
     * 
     * @return
     */
    public String getQuizInstructions() {
        // FIXME: cache?
        ST template = createStringTemplate(getProperty("quiz-instructions"));
        template.add("quizCorrectAnswerReward", asCurrency(getQuizCorrectAnswerReward()));
        return template.render();
    }
    
    public String asCurrency(double amount) {
        return NumberFormat.getCurrencyInstance().format(amount);
    }

    public Map<String, String> getQuizAnswers() {
        Properties properties = getProperties();
        if (isQuizEnabled()) {
            Map<String, String> answers = new HashMap<String, String>();
            for (int i = 1; properties.containsKey("q" + i); i++) {
                String key = "q" + i;
                String answer = properties.getProperty(key);
                answers.put(key, answer);
            }
            return answers;
        }
        return Collections.emptyMap();
    }

    public String getQuizExplanation(String questionNumber) {
        return getProperty(questionNumber + "-explanation");
    }

    /**
     * Possible values, freeze, fine?
     * 
     * @return
     */
    public SanctionAction getSanctionAction() {
        return SanctionAction.valueOf(getProperty("sanction-action", "FINE"));
    }

    public int getNumberOfSanctionOpportunities() {
        return getIntProperty("sanction-opportunities", 30);
    }

    public int getChatDuration() {
        return getIntProperty("chat-duration", 240);
    }

    public int getSanctionVotingDuration() {
        return getIntProperty("sanction-voting-duration", 30);
    }

    public int getRegulationSubmissionDuration() {
        return getIntProperty("regulation-submission-duration", 60);
    }

    public int getRegulationDisplayDuration() {
        return getIntProperty("regulation-display-duration", 30);
    }

    public int getRegulationVotingDuration() {
        return getIntProperty("regulation-voting-duration", 60);
    }

    public int getEnforcementVotingDuration() {
        return getIntProperty("enforcement-voting-duration", 60);
    }

    public int getEnforcementDisplayDuration() {
        return getIntProperty("enforcement-display-duration", 30);
    }

    public String getSanctionInstructions() {
        return getProperty("sanction-instructions");
    }

    public boolean isAlwaysInExplicitCollectionMode() {
        return getBooleanProperty("always-explicit", true);
    }

    public boolean isExplicitCollectionEnabled() {
        return getBooleanProperty("explicit-collection", true);
    }

    public double getTopRegrowthScalingFactor() {
        return getDoubleProperty("top-rate", 0.02);
    }

    public double getBottomRegrowthScalingFactor() {
        return getDoubleProperty("bottom-rate", 0.01);
    }

    public double getTopInitialResourceDistribution() {
        return getDoubleProperty("top-initial-distribution", 0.50);
    }

    public double getBottomInitialResourceDistribution() {
        return getDoubleProperty("bottom-initial-distribution", 0.25);
    }

    public String getResourceGeneratorType() {
        return getProperty("resource-generator", "density-dependent");
    }

    public int getWorldWidth() {
        return getResourceWidth() * getResourceWorldScale();
    }

    public int getWorldDepth() {
        return getResourceDepth() * getResourceWorldScale();
    }

    public int getResourceWorldScale() {
        return getIntProperty("resource-scale", 32);
    }
    
    public boolean isChatRoundEnabled() {
        return getBooleanProperty("chat-enabled");
    }

    public boolean isChatEnabled() {
        return isChatRoundEnabled() || isInRoundChatEnabled() || isCensoredChat();
    }

    public int getMaximumResourceAge() {
        return getIntProperty("maximum-resource-age", 10);
    }

    public int getChattingRadius() {
        return getIntProperty("chat-radius", 50);
    }

    public int getResourceAgingSecondsPerYear() {
        return getIntProperty("seconds-per-year", 10);
    }

    public Point3D getTopLeftCornerCoordinate() {
        float zExtend = getWorldWidth() / 2.0f;
        float xExtend = getWorldDepth() / 2.0f;
        return new Point3D(-xExtend, 0, -zExtend);
    }

    public int ageToTokens(int resourceAge) {
        switch (resourceAge) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 6;
            case 4:
                return 9;
            case 5:
                return 15;
            case 6:
                return 20;
            case 7:
                return 23;
            case 8:
            default:
                return 25;
        }
    }

    public int getTokensPerFruits() {
        return getIntProperty("tokens-per-fruits", 4);
    }

    public int getFruitHarvestDelay() {
        return getIntProperty("fruit-harvest-delay", 20);
    }

    public ExperimentType getExperimentType() {
        return ExperimentType.find(getStringProperty("experiment-type", "2d"));
    }

    public boolean is2dExperiment() {
        return getExperimentType().equals(ExperimentType.TWO_DIMENSIONAL);
    }

    public boolean is3dExperiment() {
        return !is2dExperiment();
    }

    public String getWelcomeInstructions() {
        return getParentConfiguration().getWelcomeInstructions();
    }

    public String getGeneralInstructions() {
        ST template = createStringTemplate(getParentConfiguration().getGeneralInstructions());
        template.add("showUpPayment", getParentConfiguration().getShowUpPayment());
        return template.render();
    }

    public String getFieldOfVisionInstructions() {
        return getParentConfiguration().getFieldOfVisionInstructions();
    }

    public EnforcementMechanism[] getEnforcementMechanisms() {
        return EnforcementMechanism.values();
    }

    public boolean isRotatingMonitorEnabled() {
        return getBooleanProperty("rotating-monitor-enabled", false);
    }

    /**
     * Returns true if voting is enabled before the beginning of this round.
     * @return 
     */
    public boolean isVotingEnabled() {
        return getBooleanProperty("voting-enabled");
    }

    public String getVotingInstructions() {
        return getProperty("voting-instructions");
    }

    public String getInitialVotingInstructions() {
        ST template = createStringTemplate(getProperty("initial-voting-instructions", "<h1>Notice</h1><hr><p>You will be given the ability to vote for rules in the next screen.</p>"));
        template.add("clientsPerGroup", getClientsPerGroup());
        return template.render();
    }
    
    public List<ForagingRule> getForagingRules() {
        return Arrays.asList(ForagingRule.values());

    }
    public boolean isVotingAndRegulationEnabled() {
        return getBooleanProperty("voting-and-regulation-enabled", false);
    }

    public boolean isFieldOfVisionEnabled() {
        return isTokensFieldOfVisionEnabled() || isSubjectsFieldOfVisionEnabled();
    }

    public boolean isCensoredChat() {
        return getBooleanProperty("censored-chat-enabled", false);
    }

    /**
     * Returns true if we should run a trust game before this round actually begins.
     */
    public boolean isTrustGameEnabled() {
        return getBooleanProperty("trust-game", false);
    }

    public boolean isInRoundChatEnabled() {
        return getBooleanProperty("in-round-chat-enabled", false);
    }

    public String getCensoredChatInstructions() {
        return getProperty("censored-chat-instructions",
                "Your messages must be approved before they will be relayed to the rest of your group.");
    }

    public int getNumberOfChatsPerSecond() {
        return getIntProperty("chats-per-second", 5);
    }

    public int getDelayBetweenChats() {
        return getIntProperty("delay-between-chats", 0);
    }

    public StringBuilder getCurrentRoundInstructions() {
        return buildAllInstructions(new StringBuilder());
    }

    /**
     * Returns a StringBuilder containing all instructions for the given round.  
     * FIXME: Need to refactor this + buildInstructions variants, this logic should be simplified.
     * 
     * Given a StringBuilder, will append the various instructions conditionally relevant
     * to this {@link #RoundConfiguration()}.
     * 
     * For example, if the field of vision is enabled, will append the field of vision instructions,
     * if censored chat is enabled, then it will aadd the censored chat instructions, if the
     * chat is enabled, will append the chat instructions.
     * 
     * @param instructionsBuilder
     * @return
     */
    public StringBuilder buildAllInstructions(StringBuilder instructionsBuilder) {
        if (isFirstRound()) {
            instructionsBuilder.append(getGeneralInstructions());
        }
        if (isQuizEnabled()) {
            // first show quiz instructions only
            return instructionsBuilder.append(getQuizInstructions());
        }
        else {
            return buildInstructions(instructionsBuilder);
        }
    }
        
    public StringBuilder buildInstructions() {
        return buildInstructions(new StringBuilder());
    }
    
    public StringBuilder buildInstructions(StringBuilder instructionsBuilder) {
        return addAllSpecialInstructions(instructionsBuilder.append(getInstructions()));
    }

    public StringBuilder addAllSpecialInstructions(StringBuilder instructionsBuilder) {
        // FIXME: refactor this convoluted conditional logic, use StringTemplate
        StringBuilder builder = new StringBuilder();
        if (isFieldOfVisionEnabled()) {
            addSpecialInstructions(builder, getFieldOfVisionInstructions());
        }
        if (isInRoundChatEnabled()) {
            addSpecialInstructions(builder, getInRoundChatInstructions());
        }
        else if (isChatEnabled()) {
            addSpecialInstructions(builder,
                    "Before the beginning of this round you will be able to chat with the other members of your group for " + getChatDuration() + " seconds.");
        }
        if (isCensoredChat()) {
            addSpecialInstructions(builder, getCensoredChatInstructions());
        }
        String resourceGeneratorType = getResourceGeneratorType();
        if (resourceGeneratorType.equals("mobile")) {
            addSpecialInstructions(builder, getMobileResourceInstructions());
        }
        else if (resourceGeneratorType.equals("top-bottom-patchy")) {
            addSpecialInstructions(builder, getPatchyResourceInstructions());
        }
        if (builder.toString().isEmpty()) {
            return instructionsBuilder;
        }
        else {
            return instructionsBuilder.append("<h2>Additional instructions</h2><hr><ul>").append(builder).append("</ul>");
        }
    }

    private void addSpecialInstructions(StringBuilder builder, String instructions) {
        builder.append("<li>").append(instructions).append("</li>");
    }

    private String getMobileResourceInstructions() {
        return getProperty("mobile-resource-instructions", "<p>The resource can move around in a semblance of free will / agency.</p>");
    }

    private String getPatchyResourceInstructions() {
        return getProperty("patch-resource-instructiosn", "<p>The resource is not uniformly distributed.  There are patches of high growth and low growth.</p>");
    }

    private String getInRoundChatInstructions() {
        return getProperty("in-round-chat-instructions", "<p>You can chat during this round with all players visible on the screen.</p>");
    }

    public String getTrustGameInstructions() {
        return getProperty("trust-game-instructions");
    }

    public double getQuizCorrectAnswerReward() {
        return getDoubleProperty("quiz-correct-answer-reward", getParentConfiguration().getQuizCorrectAnswerReward());
    }

    /**
     * Returns true if we should have a survey at the beginning of this round.
     * @return
     */
    public boolean isExternalSurveyEnabled() {
        return getBooleanProperty("external-survey-enabled");
    }

    public String getSurveyInstructions() {
        return getProperty("survey-instructions");
    }

    public String getSurveyUrl() {
        return getProperty("survey-url", "https://qtrial.qualtrics.com/SE/?SID=SV_38lReBOv0Wk7wgY");
    }

    public String getSurveyInstructions(Identifier id) {
        String surveyInstructions = getSurveyInstructions();
        ST template = createStringTemplate(surveyInstructions); 
        template.add("surveyLink", getSurveyUrl());
        template.add("surveyId", id.getSurveyId());
        return template.render();
    }

    public String getSubmittedVoteInstructions() {
        return getProperty("submitted-vote-instructions", "<h1>Submitted</h1><hr><p>Thank you for submitting your vote.  Please wait while we tally the rest of the votes from the other members of your group.</p>"); 
    }
    
    public String getVotingResults(List<ForagingRule> selectedRules) {
        setSelectedRules(selectedRules);
        // FIXME: move to template style construction
        ST template = createStringTemplate(getProperty("voting-results"));
        template.add("tiebreaker", selectedRules.size() > 1);
        template.add("selectedRules", selectedRules);
        return template.render();
        /*
        StringBuilder builder = new StringBuilder("<h1>Voting Results</h1><hr>");
        if (selectedRules.size() > 1) {
            // tiebreaker
            builder.append("<p><b>NOTE:</b> There was a tie and the first rule listed here was randomly selected as the winner.</p><ul>");
            for (ForagingRule rule: selectedRules) {
                builder.append("<li>").append(rule.toString());
            }
            builder.append("</ul>");
        }
        builder.append("<h1>Selected Rule</h1><hr>");
        builder.append("<p><b>").append(selectedRules.get(0)).append("</b></p>");
        return builder.toString();
        */
    }
    
    @Override
    public String toString() {
        List<RoundConfiguration> allParameters = getParentConfiguration().getAllParameters();
        return String.format("Round %d of %d\n\t%s", allParameters.indexOf(this) + 1, allParameters.size(), getProperties());
    }
    
    public String getQuizResults(List<String> incorrectQuestionNumbers, Map<Object, Object> actualAnswers) {
        ST template = createStringTemplate(getProperty("quiz-results"));
        template.add("allCorrect", incorrectQuestionNumbers.isEmpty());
        for (String incorrectQuestionNumber : incorrectQuestionNumbers) {
            template.add("incorrect_" + incorrectQuestionNumber, String.format("Your answer, %s, was incorrect.", actualAnswers.get(incorrectQuestionNumber)));
        }
        return template.render();
    }

    public void setSelectedRules(List<ForagingRule> selectedRules) {
        this.selectedRules = selectedRules;
    }
}
