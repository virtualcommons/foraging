package edu.asu.commons.foraging.event;


import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.foraging.model.SanctionMechanism;
import edu.asu.commons.net.Identifier;

/**
 * $Id: EnforcementRankingRequest.java 522 2010-06-30 19:17:48Z alllee $
 * 
 * Sent from a client to the server signaling that the client 
 * has updated the votes to the given options
 * 
 * @author <a href='allen.lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 522 $
 */

public class EnforcementRankingRequest extends AbstractPersistableEvent {

    private static final long serialVersionUID = 475300882222383637L;

    private int[] rankings;
    
    public EnforcementRankingRequest(Identifier id, int[] rankings) {
        super(id);
        this.rankings = rankings;
    }
    
    public int[] getRankings() {
    	return rankings;
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s", id, "Enforcement Mechanism Ranking", convertRankings());
    }

    private String convertRankings() {
        StringBuilder builder = new StringBuilder();
        // FIXME: this will need to be changed after we fix voting which
        // needs refactoring
        switch (rankings[0]) {
            case 0:
                // voted for no sanctioning
                builder.append("1: no sanctioning, 2: sanctioning");
                break;
            case 1:
                // voted for sanctioning
                builder.append("1: sanctioning, 2: no sanctioning");
                break;
            default:
                System.err.println("Invalid ranking: " + rankings[0]);
        }
        return builder.toString();
    }

}
