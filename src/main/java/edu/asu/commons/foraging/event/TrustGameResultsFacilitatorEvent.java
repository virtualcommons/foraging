package edu.asu.commons.foraging.event;

import java.util.ArrayList;
import java.util.List;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.foraging.model.ServerDataModel;
import edu.asu.commons.foraging.model.TrustGameResult;
import edu.asu.commons.net.Identifier;

/**
 * Persistable event used to update the facilitator and persist all the trust game results for the given round.
 */
public class TrustGameResultsFacilitatorEvent extends AbstractPersistableEvent {

	private static final long serialVersionUID = 5834548819829135618L;
	private List<TrustGameResult> trustGameResults;
	private ServerDataModel serverDataModel;
	public TrustGameResultsFacilitatorEvent(Identifier facilitatorId, ServerDataModel serverDataModel, List<TrustGameResult> trustGameResults) {
		super(facilitatorId);
		this.serverDataModel = serverDataModel;
		this.trustGameResults = trustGameResults;
	}
	public List<String> getTrustGameLog() {
		ArrayList<String> log = new ArrayList<String>();
		for (TrustGameResult result: trustGameResults) {
			log.add(result.getLog());
		}
		return log;
	}
	public ServerDataModel getServerDataModel() {
		return serverDataModel;
	}
	public void setServerDataModel(ServerDataModel serverDataModel) {
		this.serverDataModel = serverDataModel;
	}
	public List<TrustGameResult> getTrustGameResults() {
		return trustGameResults;
	}
	public void setTrustGameResults(List<TrustGameResult> trustGameResults) {
		this.trustGameResults = trustGameResults;
	}


	
}
