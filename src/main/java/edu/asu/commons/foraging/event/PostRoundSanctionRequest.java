package edu.asu.commons.foraging.event;

import java.util.Map;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.net.Identifier;

/**
 * $Id: Exp$
 *  
 * Carries a Map tying Identifiers to integer sanction amounts (can be positive or negative). 
 * 
 * @author alllee
 * @version $Revision$
 */

public class PostRoundSanctionRequest extends AbstractPersistableEvent {
	
	private static final long serialVersionUID = 5416722808284608664L;
	
	private final Map<Identifier, Integer> sanctions;

	public PostRoundSanctionRequest(Identifier id, Map<Identifier, Integer> sanctions) {
		super(id);
		this.sanctions = sanctions;
	}
    
    public PostRoundSanctionRequest(PostRoundSanctionRequest copy) {
        super(copy.id);
        this.sanctions = copy.sanctions;
    }
	
	public Map<Identifier, Integer> getSanctions() {
		return sanctions;
	}
}
