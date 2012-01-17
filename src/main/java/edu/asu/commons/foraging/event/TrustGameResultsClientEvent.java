package edu.asu.commons.foraging.event;

import edu.asu.commons.event.AbstractEvent;
import edu.asu.commons.foraging.model.ClientData;

/**
 * Used to update clients with their trust game log.
 */
public class TrustGameResultsClientEvent extends AbstractEvent {
	private static final long serialVersionUID = -9129989958983083574L;
	
	private final ClientData clientData;
	public TrustGameResultsClientEvent(ClientData clientData, String log) {
		super(clientData.getId(), log);
		this.clientData = clientData;
	}
	public ClientData getClientData() {
		return clientData;
	}
}
