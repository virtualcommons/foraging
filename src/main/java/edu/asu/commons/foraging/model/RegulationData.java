package edu.asu.commons.foraging.model;

import java.io.Serializable;

import edu.asu.commons.net.Identifier;

/**
 * $Id: RegulationData.java 416 2009-12-25 05:17:14Z alllee $
 *
 * Prototype object used to carry information about a regulation proposed
 * by a participant.   
 * 
 * @author <a href='mailto:dbarge@asu.edu'>dbarge</a>
 * @version $Revision: 416 $
 */

public class RegulationData implements Serializable, Comparable<RegulationData> {

    private static final long serialVersionUID = 5281922601551921005L;
    
    private Identifier id;
    private String text;
    private double rank;
    private int index;
    
    public RegulationData(Identifier id, String text) {
        this(id, text, -1);
    }
    
    public RegulationData(Identifier id, String text, int index) {
        this.id = id;
        this.text = text;
        this.index = index;
    }
    
    /**
     * Copy constructor to be used when setting the ranks on these regulation data.
     * 
     * @param regulationData
     */
    public RegulationData(RegulationData regulationData) {
        this(regulationData.id, regulationData.text);
    }
    
    /**
     * Returns the identifier of the participant that generated this regulation.
     * @return
     */
    public Identifier getId() {
        return id;
    }

    public String getText(){
    	return text;
	}

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }
    
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof RegulationData) {
            RegulationData regulationData = (RegulationData) o;
            return regulationData.id.equals(id) && regulationData.text.equals(text) && regulationData.rank == rank;
        }
        return false;
    }
    
    public int hashCode() {
        return text.hashCode() ^ id.hashCode() * ((int) rank + 34);
    }
    
    public String toString() {
    	return String.format("regulation from %s and index %d : %s", id, index, text);
    }
    
    public int compareTo(RegulationData regulationData) {
        int comparison = Double.valueOf(rank).compareTo(regulationData.rank);
        if (comparison == 0) {
            if (equals(regulationData)) {
                return comparison;
            }
            else {
                return text.compareTo(regulationData.text);
            }
        }
        return comparison;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
    
}
