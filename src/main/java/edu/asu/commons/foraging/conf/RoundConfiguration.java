package edu.asu.commons.foraging.conf;

import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.stringtemplate.v4.ST;

import edu.asu.commons.conf.ExperimentRoundParameters;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.EnforcementMechanism;
import edu.asu.commons.foraging.model.ResourceDispenser;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.foraging.rules.Strategy;
import edu.asu.commons.foraging.rules.iu.ForagingStrategy;
import edu.asu.commons.foraging.rules.iu.ForagingStrategyNomination;
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
public class RoundConfiguration extends ExperimentRoundParameters.Base<ServerConfiguration, RoundConfiguration> {

    private static final long serialVersionUID = 8575239803733029326L;

    public final static String[] CHAT_HANDLES = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S" };
    public final static double DEFAULT_REGROWTH_RATE = 0.01;
    public final static int DEFAULT_ROUND_TIME = 5 * 60;
    
    private static final double DEFAULT_PATCHY_BOTTOM_INITIAL_DISTRIBUTION = 0.25;
    private static final double DEFAULT_PATCHY_TOP_INITIAL_DISTRIBUTION = 0.50;
    private static final double DEFAULT_TOP_REGROWTH_RATE = 0.02;
    private static final int DEFAULT_SANCTION_FLASH_DURATION = 3;
    private static final double DEFAULT_TOKEN_MOVEMENT_PROBABILITY = 0.2d;
    private static final double DEFAULT_TOKEN_BIRTH_PROBABILITY = 0.01d;

    private List<Strategy> selectedRules;

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

    /**
     * FIXME: rename for consistency, getSubjectsFieldOfVisionRadius()
     * @return
     */
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
        ST template = createStringTemplate(getProperty("instructions", getParentConfiguration().getSameAsPreviousRoundInstructions()));
        // FIXME: probably should just lift these out into methods on RoundConfiguration
        // and refer to them as self.durationInMinutes or self.dollarsPerTokenCurrencyString, etc.
        template.add("duration", inMinutes(getDuration()) + " minutes");
        template.add("dollarsPerToken", toCurrencyString(getDollarsPerToken()));
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
        return createStringTemplate(getProperty("chat-instructions")).render();
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

    /**
     * FIXME: quiz instructions and quiz enabled should be tightly coupled..
     * 
     * @return
     */
    public String getQuizInstructions() {
        // FIXME: cache?
        ST template = createStringTemplate(getProperty("quiz-instructions"));
        template.add("quizCorrectAnswerReward", toCurrencyString(getQuizCorrectAnswerReward()));
        return template.render();
    }
    
    public String toCurrencyString(double amount) {
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
        return getDoubleProperty("top-rate", DEFAULT_TOP_REGROWTH_RATE);
    }

    public double getBottomRegrowthScalingFactor() {
        return getDoubleProperty("bottom-rate", DEFAULT_REGROWTH_RATE);
    }

    public double getTopInitialResourceDistribution() {
        return getDoubleProperty("top-initial-distribution", DEFAULT_PATCHY_TOP_INITIAL_DISTRIBUTION);
    }

    public double getBottomInitialResourceDistribution() {
        return getDoubleProperty("bottom-initial-distribution", DEFAULT_PATCHY_BOTTOM_INITIAL_DISTRIBUTION);
    }

    public String getResourceGeneratorType() {
        return getProperty("resource-generator", ResourceDispenser.Type.DENSITY_DEPENDENT.toString());
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
    
    public boolean isImposedStrategyEnabled() {
    	return getBooleanProperty("imposed-strategy-enabled");
    }

    public String getVotingInstructions() {
        return render(getProperty("voting-instructions"));
    }

    public String getInitialVotingInstructions() {
        return createStringTemplate(getProperty("initial-voting-instructions")).render();
    }
    
    public boolean isVotingAndRegulationEnabled() {
        return getBooleanProperty("voting-and-regulation-enabled", false);
    }

    public boolean isFieldOfVisionEnabled() {
        return isTokensFieldOfVisionEnabled() || isSubjectsFieldOfVisionEnabled();
    }

    public boolean isCensoredChat() {
        return getBooleanProperty("censored-chat-enabled");
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
        if (builder.length() == 0) {
            return instructionsBuilder;
        }
        else {
            // FIXME: localize via ResourceBundle
            return instructionsBuilder.append("<hr><ul>").append(builder).append("</ul>");
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
        return getProperty("in-round-chat-instructions", getParentConfiguration().getInRoundChatInstructions());
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
        return getProperty("survey-instructions", getParentConfiguration().getSurveyInstructions());
    }

    public String getSurveyUrl(Identifier id) {
        ST template = createStringTemplate(getProperty("survey-url", "https://qtrial.qualtrics.com/SE/?SID=SV_38lReBOv0Wk7wgY"));
        template.add("surveyId", id.getSurveyId());
        return template.render();
    }

    public String getSurveyInstructions(Identifier id) {
        String surveyInstructions = getSurveyInstructions();
        ST template = createStringTemplate(surveyInstructions); 
        template.add("surveyUrl", getSurveyUrl(id));
        return template.render();
    }

    public String getSubmittedVoteInstructions() {
        return render(getProperty("submitted-vote-instructions")); 
    }
    
    public String generateVotingResults(List<Strategy> selectedRules, Map<Strategy, Integer> nominations) {
    	List<ForagingStrategyNomination> sortedNominations = new ArrayList<ForagingStrategyNomination>();
    	for (Map.Entry<Strategy, Integer> entry: new TreeMap<Strategy, Integer>(nominations).entrySet()) {
    		Strategy strategy = entry.getKey();
    		sortedNominations.add(new ForagingStrategyNomination(strategy, entry.getValue(), strategy.equals(selectedRules.get(0))));
    	}
        setSelectedRules(selectedRules);
        ST template = createStringTemplate(getVotingResultsTemplate());
        template.add("nominations", sortedNominations);
        template.add("tiebreaker", selectedRules.size() > 1);
        return template.render();
    }

    public List<ForagingStrategy> getForagingStrategies() {
        return Arrays.asList(ForagingStrategy.values());
    }
    
    public String getVotingResultsTemplate() {
        return getProperty("voting-results", getParentConfiguration().getVotingResults());
    }
    
    public String getFacilitatorDebriefingTemplate() {
    	return getProperty("facilitator-debriefing", getParentConfiguration().getFacilitatorDebriefing());
    }
    
    @Override
    public String toString() {
        List<RoundConfiguration> allParameters = getParentConfiguration().getAllParameters();
        return String.format("Round %d of %d", allParameters.indexOf(this) + 1, allParameters.size());
    }
    
    public String fullStatus() {
    	return toString() + "\n\t" + getProperties();
    }
    
    public String getQuizResults(List<String> incorrectQuestionNumbers, Map<Object, Object> actualAnswers) {
        ST template = createStringTemplate(getProperty("quiz-results"));
        // FIXME: actual answers includes the submit button, so there's an off-by-one that we need to deal with.
        int totalQuestions = actualAnswers.size() - 1;
        int numberCorrect = totalQuestions - incorrectQuestionNumbers.size();
        template.add("allCorrect", incorrectQuestionNumbers.isEmpty());
        template.add("numberCorrect", numberCorrect);
        template.add("totalQuestions", totalQuestions);
        template.add("totalQuizEarnings", toCurrencyString(getQuizCorrectAnswerReward() * numberCorrect));
        for (String incorrectQuestionNumber : incorrectQuestionNumbers) {
            template.add("incorrect_" + incorrectQuestionNumber, String.format("Your answer, %s, was incorrect.", actualAnswers.get(incorrectQuestionNumber)));
        }
        return template.render();
    }
    
    public List<Strategy> getSelectedRules() {
        return selectedRules;
    }

    public void setSelectedRules(List<Strategy> selectedRules) {
        this.selectedRules = selectedRules;
    }
    
    public double tokensToDollars(int tokens) {
    	return isPracticeRound() ? 0.0d : tokens * getDollarsPerToken();
    }
    
    public String getClientDebriefing() {
        return getProperty("client-debriefing", getParentConfiguration().getClientDebriefing());
    }
    
    public String generateClientDebriefing(ClientData data, boolean showExitInstructions) {
        ST st = createStringTemplate(getClientDebriefing());
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        populateClientEarnings(data, getParentConfiguration(), formatter, showExitInstructions);
        st.add("clientData", data);
        // FIXME: replace showExitInstructions within client debriefing with a ExitInstructions template?
        st.add("showExitInstructions", showExitInstructions);
        st.add("showUpPayment", formatter.format(getParentConfiguration().getShowUpPayment()));
        return st.render();
    }

    private void populateClientEarnings(ClientData data, ServerConfiguration serverConfiguration, NumberFormat formatter, boolean includeTrustGame) {
        data.setGrandTotalIncome(formatter.format(serverConfiguration.getTotalIncome(data, includeTrustGame)));
        data.setCurrentIncome(formatter.format(tokensToDollars(data.getCurrentTokens())));
        data.setQuizEarnings(formatter.format(serverConfiguration.getQuizEarnings(data)));
        data.setTrustGameEarnings(formatter.format(data.getTrustGameIncome()));
    }

	public String generateFacilitatorDebriefing(ServerDataModel serverDataModel) {
		ST template = createStringTemplate(getFacilitatorDebriefingTemplate());
		template.add("lastRound", serverDataModel.isLastRound());
		ServerConfiguration serverConfiguration = getParentConfiguration();
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		for (ClientData data: serverDataModel.getClientDataMap().values()) {
		    populateClientEarnings(data, serverConfiguration, formatter, true);
		}
		template.add("clientDataList", serverDataModel.getClientDataMap().values());
		return template.render();
	}
	
	// returns the next round configuration without advancing the pointer in ServerConfiguration
	public RoundConfiguration nextRound() {
		return getParentConfiguration().getNextRoundConfiguration();
	}

	public boolean shouldWaitForFacilitatorSignal() {
		return isPostRoundSanctioningEnabled() || (isTrustGameEnabled() && isLastRound());
	}
	
	public String getLastChatHandle() {
	    return CHAT_HANDLES[getClientsPerGroup() - 1];
	}
	
	public String getChatDurationInMinutes() {
	    return inMinutes(getChatDuration()) + " minutes";
	}
	
	public String getDurationInMinutes() {
	    return inMinutes(getDuration()) + " minutes";
	}
	
	public boolean showTokenAnimation() {
		return getBooleanProperty("show-token-animation", true);
	}

    public String getSurveyConfirmationMessage() {
        return getProperty("survey-confirmation-message", "Please make sure you have completed the survey before continuing.  Have you completed the survey?");

    }

    public String[] getTrustGamePlayerTwoColumnNames() {
        return getParentConfiguration().getTrustGamePlayerTwoColumnNames();
    }

    public String getTrustGamePlayerOneAllocationLabel() {
        return getParentConfiguration().getTrustGamePlayerOneAllocationLabel();
    }

    public String getTrustGamePlayerTwoAllocationLabel() {
        return getParentConfiguration().getTrustGamePlayerTwoAllocationLabel();
    }

    public String getTrustGamePlayerTwoInstructionLabel() {
        return getParentConfiguration().getTrustGamePlayerTwoInstructionLabel();
    }

    public String getPlayerOneAmountToKeepValidation() {
        return getParentConfiguration().getPlayerOneAmountToKeepValidation();
    }

    public String getPlayerTwoAmountToKeepValidation() {
        return getParentConfiguration().getPlayerTwoAmountToKeepValidation();
    }

}
