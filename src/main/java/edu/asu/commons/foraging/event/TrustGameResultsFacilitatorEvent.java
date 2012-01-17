package edu.asu.commons.foraging.event;

import java.util.List;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.net.Identifier;

/**
 * Persistable event usd to update the facilitator and store all the trust game results for the given round.
 */
public class TrustGameResultsFacilitatorEvent extends AbstractPersistableEvent {

	private static final long serialVersionUID = 5834548819829135618L;
	private List<String> allTrustGameResults;
	private ServerDataModel serverDataModel;
	public TrustGameResultsFacilitatorEvent(Identifier facilitatorId,
			ServerDataModel serverDataModel, List<String> allTrustGameResults) {
		super(facilitatorId);
		this.allTrustGameResults = allTrustGameResults;
		this.serverDataModel = serverDataModel;
	}
	public List<String> getAllTrustGameResults() {
		return allTrustGameResults;
	}
	public void setAllTrustGameResults(List<String> allTrustGameResults) {
		this.allTrustGameResults = allTrustGameResults;
	}
	public ServerDataModel getServerDataModel() {
		return serverDataModel;
	}
	public void setServerDataModel(ServerDataModel serverDataModel) {
		this.serverDataModel = serverDataModel;
	}

	
}
