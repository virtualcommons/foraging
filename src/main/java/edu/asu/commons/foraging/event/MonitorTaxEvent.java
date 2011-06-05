package edu.asu.commons.foraging.event;

import java.util.Map;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.net.Identifier;

public class MonitorTaxEvent extends AbstractPersistableEvent {
	
	private static final long serialVersionUID = -3191310440517697158L;
	
	private Map<Identifier, Integer> monitorTaxes;
	private int totalMonitorTax;
	
	public MonitorTaxEvent(Identifier id, Map<Identifier, Integer> monitorTaxes, int totalMonitorTax) {
		super(id);
		this.monitorTaxes = monitorTaxes;
		this.totalMonitorTax = totalMonitorTax;
	}

	public Map<Identifier, Integer> getMonitorTaxes() {
		return monitorTaxes;
	}

	public int getTotalMonitorTax() {
		return totalMonitorTax;
	}
	
}
