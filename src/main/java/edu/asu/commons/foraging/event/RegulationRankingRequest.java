package edu.asu.commons.foraging.event;


import java.util.SortedSet;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.foraging.model.RegulationData;
import edu.asu.commons.net.Identifier;

/**
 * $Id: RegulationRankingRequest.java 522 2010-06-30 19:17:48Z alllee $
 * 
 * 
 * 
 * @author <a href='dbarge@asu.edu'>Deepak Barge</a>
 * @version $Revision: 522 $
 */

public class RegulationRankingRequest extends AbstractPersistableEvent {

    private static final long serialVersionUID = 475300882222383637L;
    
    private SortedSet<RegulationData> rankedRegulationData;
    
    private int[] rankings;
    
    /**
     * Constructs a ranking request with the appropriate ranking.
     * @param id
     * @param rankings
     */
    public RegulationRankingRequest(Identifier id, int[] rankings) {
        super(id);
        this.rankings = rankings;
    }
    
    public RegulationRankingRequest(Identifier id, SortedSet<RegulationData> rankedRegulationData) {
        super(id);
        this.rankedRegulationData = rankedRegulationData;
    }
    
    public SortedSet<RegulationData> getRankedRegulationData() {
        return rankedRegulationData;
    }

    public int[] getRankings() {
        return rankings;
    }
    
}
