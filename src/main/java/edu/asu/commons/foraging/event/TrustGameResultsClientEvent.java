package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.foraging.model.GroupDataModel;
import edu.asu.commons.foraging.model.TrustGameResult;

/**
 * Used to update clients with their trust game log.
 */
public class TrustGameResultsClientEvent extends AbstractEvent {
	private static final long serialVersionUID = -9129989958983083574L;
	
	private final ClientData clientData;
	private final TrustGameResult trustGameResult;
	
	public TrustGameResultsClientEvent(ClientData clientData, TrustGameResult result) {
		super(clientData.getId(), result.getLog());
		this.clientData = clientData;
		this.trustGameResult = result;
	}
	public GroupDataModel getGroupDataModel() {
	    return clientData.getGroupDataModel();
	}
	public ClientData getClientData() {
		return clientData;
	}
	public TrustGameResult getTrustGameResult() {
		return trustGameResult;
	}
}
