package edu.asu.commons.foraging.event;

import java.awt.Color;

import edu.asu.commons.event.AbstractPersistableEvent;
import edu.asu.commons.net.Identifier;

public class AgentInfoRequest extends AbstractPersistableEvent {

	private static final long serialVersionUID = 6583504160936665158L;
	private boolean male = true;
	private Color hairColor = null;
	private Color skinColor = null;
	private Color shirtColor = null;
	private Color trouserColor = null;
	private Color shoesColor = null;
	
	//Used by the abstract visualization
	public AgentInfoRequest(Identifier id, Color skinColor) {
		super(id);
		this.skinColor = skinColor;
	}

	//Used by forestry visualization
	public AgentInfoRequest(Identifier id, boolean male, Color hairColor, Color skinColor, Color shirtColor, Color trouserColor, Color shoesColor) {
        super(id);
        this.male = male;
        this.hairColor = hairColor;
        this.skinColor = skinColor;
        this.shirtColor = shirtColor;
        this.trouserColor = trouserColor;
        this.shoesColor = shoesColor;
    }
	
	public boolean isMale() {
		return male;
	}
	
	public Color getHairColor() {
		return hairColor;
	}

	public Color getShirtColor() {
		return shirtColor;
	}

	public Color getShoesColor() {
		return shoesColor;
	}

	public Color getSkinColor() {
		return skinColor;
	}

	public Color getTrouserColor() {
		return trouserColor;
	}
	
	public String toString() {
        return "Avatar info request sent by: " + id;
    }

}
