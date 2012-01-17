package edu.asu.commons.foraging.model;

import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.conf.RoundConfiguration.SanctionAction;
import edu.asu.commons.foraging.event.RealTimeSanctionRequest;
import edu.asu.commons.foraging.graphics.Point3D;
import edu.asu.commons.foraging.rules.ForagingRule;
import edu.asu.commons.foraging.ui.Circle;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.util.Duration;

/**
 * 
 * $Id$
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>, Deepali Bhagvat
 * @version $Revision$
 */
public class ClientData implements Serializable {

    private static final long serialVersionUID = 5281922601551921005L;
    
    private final Identifier id;
    private GroupDataModel groupDataModel;

    private RegulationData regulationData;

    private int[] regulationRankings;
    private int[] enforcementRankings;
    
    private ForagingRole foragingRole = ForagingRole.HARVEST;
    
    private int totalTokens;
    private int currentTokens;
    private int tokensCollectedLastRound;
    private int sanctionBonuses;
    private int sanctionPenalties;
    // stores the accumulated cost of sanctioning that this user has paid this round.
    private int sanctionCosts;
    private int assignedNumber;
    private Point3D position;
    
    private volatile boolean collecting;
    private volatile boolean explicitCollectionMode;
    private Duration freezeDuration;
    private LinkedList<RealTimeSanctionRequest> latestSanctions = new LinkedList<RealTimeSanctionRequest>();
    private AnimationData animationData;
    
    private boolean subjectsFieldOfVisionEnabled;
    private boolean tokensFieldOfVisionEnabled;
    private double viewSubjectsRadius;
    private double viewTokensRadius;
    
    // only needed if this client has already received a tax.
    private boolean taxReceived = false;
    
    private double trustGamePlayerOneAmountToKeep;
    private double[] trustGamePlayerTwoAmountsToKeep;
    
    private ForagingRule votedRule;
    private ArrayList<String> trustGameLog = new ArrayList<String>();

    // String fields to be set and formatted for use in templates.
    private String grandTotalIncome;
    private String currentIncome;
    private String quizEarnings;
    private String trustGameEarnings;
    
    private double totalIncome = 0.0d;
    private double trustGameIncome = 0.0d;
    private int correctQuizAnswers = 0;

    public void setTrustGamePlayerOneAmountToKeep(double trustGamePlayerOneAmountToKeep) {
        this.trustGamePlayerOneAmountToKeep = trustGamePlayerOneAmountToKeep;
    }

    public void setTrustGamePlayerTwoAmountsToKeep(double[] trustGamePlayerTwoAmountsToKeep) {
        this.trustGamePlayerTwoAmountsToKeep = trustGamePlayerTwoAmountsToKeep;
    }

    public double getTrustGamePlayerOneAmountToKeep() {
        return trustGamePlayerOneAmountToKeep;
    }

    public double[] getTrustGamePlayerTwoAmountsToKeep() {
        return trustGamePlayerTwoAmountsToKeep;
    }
    

	// this is only used in the Rotating Monitor enforcement mechanism.
    private int tokensCollectedDuringInterval = 0;

    public ClientData(Identifier id) {
        this.id = id;
    }
    
    public Point getPoint() {
        return new Point( Math.round(position.x), Math.round(position.y) );
    }
    
    public Point3D getPoint3D() {
        return position;
    }

    public Point getPosition() {
        return getPoint();
    }
        
    public void setPosition(Point position) {
        setPosition(new Point3D((float) position.x, (float) position.y, 0.0f));
    }

    public void setPosition(Point3D position) {
        this.position = position;
    }
    
    
    /**
     * Returns the current number of tokens this participant has collected.
     * Sanctions from or against this participant are reflected in the total.
     */
    public int getCurrentTokens() {
        return currentTokens;
    }
    
    public void addTokens(int tokens) {
        currentTokens += tokens;
        // this can only be invoked on the server side
        RoundConfiguration configuration = getGroupDataModel().getRoundConfiguration();
        if ( ! configuration.isPracticeRound() ) {
            totalTokens += tokens;
            totalIncome += (tokens * configuration.getDollarsPerToken());
        }
        tokensCollectedDuringInterval += tokens;
    }

    public Circle getSubjectsFieldOfVision() {
    	if (isSubjectsFieldOfVisionEnabled()) {
    		return new Circle(getPoint(), viewSubjectsRadius);
    	}
    	else {
    		return null;
    	}
    }
    
    public boolean isSanctioningAllowed() {
    	return foragingRole != null && foragingRole.isSanctioningAllowed();
    }
    
    public boolean isHarvestingAllowed() {
    	return foragingRole != null && foragingRole.isHarvestingAllowed();
    }
    
    public boolean isSubjectInFieldOfVision(Point subjectPosition) { 
    	Circle circle = getSubjectsFieldOfVision();
    	if (circle == null) {
    		// if the field of vision is null that means that there is no
    		// field of vision enabled.
    		return true;
    	}
    	return circle.contains(subjectPosition);
    	// could also do return circle == null || circle.contains(point);
    }
    
    public Circle getTokensFieldOfVision() {
    	if (isTokensFieldOfVisionEnabled()) {
    		return new Circle(getPoint(), viewTokensRadius);
    	}
    	else {
    		return null;
    	}
    }
    
    public void addToken() {
        addTokens(1);
    }
    
    public int applyMonitorTax() {
        int monitorTax = tokensCollectedDuringInterval / 4;
        subtractTokens(monitorTax);
        return monitorTax;
    }
    
    // used for post round sanctioning
    public synchronized void postRoundSanctionCost(int cost) {
        sanctionCosts += Math.abs(cost);
    }
    
    // used for real time sanctioning
    public void sanctionCost() {
        RoundConfiguration roundConfiguration = getGroupDataModel().getRoundConfiguration();
        SanctionAction sanctionAction = roundConfiguration.getSanctionAction();
        if (sanctionAction.isFine()) {
            final int sanctionCost = roundConfiguration.getSanctionCost();
            sanctionCosts += sanctionCost;
            subtractTokens(sanctionCost);
        }
        else if (sanctionAction.isFreeze()) {
            freeze( roundConfiguration.getSanctionCost() );
        }
    }
    
    private int subtractTokens(int amount) {
        int tokensToSubtract = Math.min(currentTokens, amount);
        currentTokens = currentTokens - tokensToSubtract;
        totalTokens = totalTokens - tokensToSubtract;
        return tokensToSubtract;
    }
    
    public int sanctionPenalty() {
        RoundConfiguration roundConfiguration = getGroupDataModel().getRoundConfiguration();
        SanctionAction sanctionAction = roundConfiguration.getSanctionAction();
        // FIXME: add logic to SanctionAction instead, i.e.,
        // sanctionAction.apply(clientData); 
        // that performs this conditional logic based on the actual action in place.
        if (sanctionAction.isFine()) {
            final int sanctionPenalty = roundConfiguration.getSanctionPenalty();
            sanctionPenalties += sanctionPenalty;
            return subtractTokens(sanctionPenalty);
        }
        else if (sanctionAction.isFreeze()) {
            freeze( roundConfiguration.getSanctionPenalty() );
        }
        // in the case of freeze, the return value is meaningless.
        return -1;

    }

    /**
     * If this ClientData is already frozen, this method returns false and does nothing.
     * If not, sets or resets the freeze duration for this ClientData and returns true.
     *        
     * @param seconds
     * @return false if this ClientData is already frozen, true otherwise.
     */
    private synchronized boolean freeze(int seconds) {
        if (freezeDuration == null || freezeDuration.hasExpired()) {
            // always recreate the freeze duration since we may be switching
            // from sanction cost to sanction penalty.
            freezeDuration = Duration.create( seconds, TimeUnit.SECONDS );
            return true;
        }
        return false;
        // otherwise we are already frozen and there is nothing more to do -
        // cannot freeze an already frozen player.
    }

    public boolean isFrozen() {
        return freezeDuration != null && ! freezeDuration.hasExpired();
    }
   
    public synchronized void postRoundSanctionPenalty(final int tokens) {
        if (tokens < 0) {
            sanctionPenalties += Math.abs(tokens);
        }
        else {
            sanctionBonuses += tokens;
        }
    }
    
    public void applyPostRoundSanctioning() {
    	System.out.println("Apply post round sanctioning");
        tokensCollectedLastRound = currentTokens;
        int netGain = sanctionBonuses - sanctionPenalties - sanctionCosts;
        if (netGain < 0 && Math.abs(netGain) > currentTokens) {
            // lose everything this round.
            totalTokens -= currentTokens;
            currentTokens = 0;
        }
        else {
            currentTokens += netGain;
            totalTokens += netGain;
        }
    }
    
    /**
     * Returns a queue of sanction requests that have been most recently applied to this client.
     * @return
     */
    public Queue<RealTimeSanctionRequest> getLatestSanctions() {
        return latestSanctions;
    }

    public Identifier getId() {
        return id;
    }
    public int getTotalTokens() {
        return totalTokens;
    }
    
    public void setCurrentTokens(int currentTokens) {
        this.currentTokens = currentTokens;
    }
    
    public void setTotalTokens(int totalTokens) {
        this.totalTokens = totalTokens;
    }


    /**
     * Returns the number of tokens used to sanction others in this round.
     */
    public int getSanctionBonuses() {
        return sanctionBonuses;
    }

    /**
     * Returns the number of tokens taken away from this client by other
     * players.
     */
    public int getSanctionPenalties() {
        return sanctionPenalties;
    }
    
    public int getSanctionCosts() {
        return sanctionCosts;
    }

    /**
     * Prepares this client for the next round.
     *
     */
    public void reset() {
        resetCurrentTokens();
        resetLatestSanctions();
        foragingRole = ForagingRole.HARVEST;
        taxReceived = false;
    }
    
    public void resetLatestSanctions() {
    	if (latestSanctions != null) {
    		latestSanctions.clear();
    	}
    }

    private void resetCurrentTokens() {
        currentTokens = 0;
        sanctionBonuses = 0;
        sanctionPenalties = 0;
        sanctionCosts = 0;
        tokensCollectedDuringInterval = 0;
    }
    
    public void resetTokensCollectedDuringInterval() {
        tokensCollectedDuringInterval = 0;
    }

    public int getAssignedNumber() {
        return assignedNumber;
    }

    public void setAssignedNumber(int assignedNumber) {
        this.assignedNumber = assignedNumber;
    }

    public int getTokensCollectedLastRound() {
        return tokensCollectedLastRound;
    }

    public GroupDataModel getGroupDataModel() {
        return groupDataModel;
    }

    public void setGroupDataModel(GroupDataModel groupDataModel) {
        this.groupDataModel = groupDataModel;
    }
    
    public RegulationData getRegulationData() {
        return regulationData;
    }

    public void setRegulationData(RegulationData regulationData) {
		this.regulationData = regulationData;
    }
    
    // FIXME: generalize for arbitrary clients per group.
    public Point3D generate3DPosition() {
//        float x = assignedNumber * getGroupDataModel().getCurrentConfiguration().getWorldWidth() / getGroupDataModel().getNumberOfClients() + 1;
        RoundConfiguration currentConfiguration = getGroupDataModel().getRoundConfiguration();
        Point3D topLeftCorner = currentConfiguration.getTopLeftCornerCoordinate();
        float worldDepth = currentConfiguration.getWorldDepth() / 4.0f;
        float worldWidth = currentConfiguration.getWorldWidth() / 4.0f;
        switch (assignedNumber) {
        case 1:
            return ( topLeftCorner.add(new Point3D(worldWidth, 0, worldDepth)) );
        case 2:
            return ( topLeftCorner.add(new Point3D(worldWidth * 3, 0, worldDepth)) );
        case 3:
            return ( topLeftCorner.add(new Point3D(worldWidth, 0, worldDepth * 3)) );
        case 4:
            return ( topLeftCorner.add(new Point3D(worldWidth * 3, 0, worldDepth * 3)) );
        default:
            throw new IllegalArgumentException("generate3DPosition is hardcoded to only support up to 4 clients");
        }
    }
    
    public void setCollecting() {
        collecting = true;
    }

    public void collectToken() {
//        if (collecting) {
        getGroupDataModel().collectToken(this);
//        }
//        collecting = false;
    }

    public boolean isExplicitCollectionMode() {
        return explicitCollectionMode;
    }

    public void setExplicitCollectionMode(boolean explicitCollectionMode) {
        this.explicitCollectionMode = explicitCollectionMode;
    }
    
    public void initializePosition() {
        RoundConfiguration roundConfiguration = getGroupDataModel().getRoundConfiguration();
        setExplicitCollectionMode(roundConfiguration.isAlwaysInExplicitCollectionMode());
        subjectsFieldOfVisionEnabled = roundConfiguration.isSubjectsFieldOfVisionEnabled();
        if (subjectsFieldOfVisionEnabled) {
        	viewSubjectsRadius = roundConfiguration.getViewSubjectsRadius();
        }
        tokensFieldOfVisionEnabled = roundConfiguration.isTokensFieldOfVisionEnabled(); 
        if (tokensFieldOfVisionEnabled) {
        	viewTokensRadius = roundConfiguration.getViewTokensRadius();
        }
        if (roundConfiguration.isPrivateProperty()) {
            setPosition (new Point(roundConfiguration.getResourceWidth() / 2, roundConfiguration.getResourceDepth() / 2));
        }
        else if (roundConfiguration.is2dExperiment()) {
            int clientsPerGroup = roundConfiguration.getClientsPerGroup();
            double cellWidth = roundConfiguration.getResourceWidth() / (double) clientsPerGroup;
            int x = (int) ((cellWidth / 2) + (cellWidth * (getAssignedNumber() - 1)));
            int y = roundConfiguration.getResourceDepth() / 2;
            setPosition(new Point(x, y));
        }
        else {
            // 3d initialize position
            setPosition( generate3DPosition() );
        }
    }

    public int getAnimationState() {
        return getAnimationData().getAnimationState();
    }

    public Color getHairColor() {
        return getAnimationData().getHairColor();
    }

    public float getHeading() {
        return getAnimationData().getHeading();
    }

    public Color getShirtColor() {
        return getAnimationData().getShirtColor();
    }

    public Color getShoesColor() {
        return getAnimationData().getShoesColor();
    }

    public Color getSkinColor() {
        return getAnimationData().getSkinColor();
    }

    public Color getTrouserColor() {
        return getAnimationData().getTrouserColor();
    }

    public boolean isAnimationActive() {
        return getAnimationData().isAnimationActive();
    }

    public boolean isMale() {
        return getAnimationData().isMale();
    }

    public void setAnimationActiveFlag(boolean animationActive) {
        getAnimationData().setAnimationActiveFlag(animationActive);
    }

    public void setAnimationState(int animationState) {
        getAnimationData().setAnimationState(animationState);
    }

    public void setHairColor(Color hairColor) {
        getAnimationData().setHairColor(hairColor);
    }

    public void setHeading(float heading) {
        getAnimationData().setHeading(heading);
    }

    public void setMale(boolean male) {
        getAnimationData().setMale(male);
    }

    public void setShirtColor(Color shirtColor) {
        getAnimationData().setShirtColor(shirtColor);
    }

    public void setShoesColor(Color shoesColor) {
        getAnimationData().setShoesColor(shoesColor);
    }

    public void setSkinColor(Color skinColor) {
        getAnimationData().setSkinColor(skinColor);
    }

    public void setTrouserColor(Color trouserColor) {
        getAnimationData().setTrouserColor(trouserColor);
    }

    private AnimationData getAnimationData() {
        if (animationData == null) {
            animationData = new AnimationData();
        }
        return animationData;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

	public boolean isSubjectsFieldOfVisionEnabled() {
		return subjectsFieldOfVisionEnabled;
	}

	public boolean isTokensFieldOfVisionEnabled() {
		return tokensFieldOfVisionEnabled;
	}

	public double getViewSubjectsRadius() {
		return viewSubjectsRadius;
	}

	public double getViewTokensRadius() {
		return viewTokensRadius;
	}
	
	public ForagingRole getForagingRole() {
	    return foragingRole;
	}

    public void setForagingRole(ForagingRole foragingRole) {
        this.foragingRole = foragingRole;
    }
    
    public boolean isMonitor() {
        return foragingRole != null && foragingRole.isMonitor();
    }

    public int[] getRegulationRankings() {
        return regulationRankings;
    }

    public void setRegulationRankings(int[] regulationRankings) {
        this.regulationRankings = regulationRankings;
    }

    public int[] getEnforcementRankings() {
        return enforcementRankings;
    }

    public void setEnforcementRankings(int[] enforcementRankings) {
        this.enforcementRankings = enforcementRankings;
    }

    public boolean isTaxReceived() {
        return taxReceived;
    }

    public void setTaxReceived() {
        this.taxReceived = true;
    }

    public String toString() {
        return String.format("[%s #%d]", id, assignedNumber);
    }

    public void addTrustGameEarnings(double trustGameEarnings) {
        this.trustGameIncome += trustGameEarnings;
    }
    
    public double getTrustGameIncome() {
        return trustGameIncome;
    }

    public void logTrustGame(String log) {
        trustGameLog.add(log);
    }

    public List<String> getTrustGameLog() {
        return trustGameLog;
    }

    public int getCorrectQuizAnswers() {
        return correctQuizAnswers;
    }

    public void addCorrectQuizAnswers(int numberOfCorrectAnswers) {
        correctQuizAnswers += numberOfCorrectAnswers;
    }

    public ForagingRule getVotedRule() {
        return votedRule;
    }

    public void setVotedRule(ForagingRule votedRule) {
        this.votedRule = votedRule;
    }
    
    public String getSurveyId() {
        return getId().getSurveyId();
    }

	public String getGrandTotalIncome() {
		return grandTotalIncome;
	}

	public void setGrandTotalIncome(String grandTotalIncome) {
		this.grandTotalIncome = grandTotalIncome;
	}

	public String getCurrentIncome() {
		return currentIncome;
	}

	public void setCurrentIncome(String currentIncome) {
		this.currentIncome = currentIncome;
	}

	public String getQuizEarnings() {
		return quizEarnings;
	}

	public void setQuizEarnings(String quizEarnings) {
		this.quizEarnings = quizEarnings;
	}

	public void setTrustGameEarnings(String trustGameEarnings) {
		this.trustGameEarnings = trustGameEarnings;
	}

	public String getTrustGameEarnings() {
		return trustGameEarnings;
	}


}
