package edu.asu.commons.foraging.model;

import java.awt.Color;
import java.io.Serializable;

public class AnimationData implements Serializable {

    private static final long serialVersionUID = 7460099576173626488L;
    private float heading = 0;
    private int animationState = 0;
    private boolean animationActive = false;
    private boolean male = true;
    private Color skinColor = new Color(1.0f, 0.77f, 0.75f, 1.0f);
    private Color hairColor = new Color(0.0f, 0.0f, 0.0f, 1.0f);
    private Color shirtColor = new Color(1.0f, 0.0f, 0.0f, 1.0f);
    private Color trouserColor = new Color(0.0f, 0.0f, 1.0f, 1.0f);
    private Color shoesColor = new Color(0.5f, 0.0f, 0.0f, 1.0f);
    
    public float getHeading() {
        return heading;
    }

    public void setHeading(float heading) {
        this.heading = heading;
    }

    public int getAnimationState() {
        return animationState;
    }

    public void setAnimationState(int animationState) {
        this.animationState = animationState;
    }
    
    public void setAnimationActiveFlag(boolean animationActive) {
        this.animationActive = animationActive;
    }
    
    public boolean isAnimationActive() {
        return animationActive;
    }

    public void setMale(boolean male) {
        this.male = male;
    }

    public void setHairColor(Color hairColor) {
        this.hairColor = hairColor;
    }

    public void setSkinColor(Color skinColor) {
        this.skinColor = skinColor;     
    }

    public void setShirtColor(Color shirtColor) {
        this.shirtColor = shirtColor;
    }

    public void setTrouserColor(Color trouserColor) {
        this.trouserColor = trouserColor;       
    }

    public void setShoesColor(Color shoesColor) {
        this.shoesColor = shoesColor;
    }

    public Color getHairColor() {
        return hairColor;
    }

    public boolean isMale() {
        return male;
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
}
