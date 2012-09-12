package edu.asu.commons.foraging.model;

/**
 * $Id$
 * 
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public interface StochasticGenerator extends ResourceGenerator {
    public double getProbabilityForCell(GroupDataModel group, int x, int y);
    // FIXME: replace InitialFoodGenerator with initialize() method.
}