package edu.asu.commons.foraging.event;

import java.util.List;
import java.util.Map;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.net.Identifier;

/**
 * Persistable event usd to update the facilitator and store all the trust game results for the given round.
 */
public class TrustGameResultsFacilitatorEvent extends AbstractPersistableEvent {

	private static final long serialVersionUID = 5834548819829135618L;
	private List<String> allTrustGameResults;
	private Map<Identifier, ClientData> clientDataMap;
	public TrustGameResultsFacilitatorEvent(Identifier facilitatorId,
			Map<Identifier, ClientData> clientDataMap, List<String> allTrustGameResults) {
		super(facilitatorId);
		this.allTrustGameResults = allTrustGameResults;
		this.clientDataMap = clientDataMap;
	}
	public List<String> getAllTrustGameResults() {
		return allTrustGameResults;
	}
	public void setAllTrustGameResults(List<String> allTrustGameResults) {
		this.allTrustGameResults = allTrustGameResults;
	}
	public Map<Identifier, ClientData> getClientDataMap() {
		return clientDataMap;
	}
	public void setClientDataMap(Map<Identifier, ClientData> clientDataMap) {
		this.clientDataMap = clientDataMap;
	}
	
}
