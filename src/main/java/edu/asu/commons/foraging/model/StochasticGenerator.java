package edu.asu.commons.foraging.model;

/**
 * $Id: StochasticGenerator.java 76 2009-02-25 18:02:38Z alllee $
 * 
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 76 $
 */
public interface StochasticGenerator extends ResourceGenerator {
    public double getProbabilityForCell(GroupDataModel group, int x, int y);
    // FIXME: replace InitialFoodGenerator with initialize() method.
}