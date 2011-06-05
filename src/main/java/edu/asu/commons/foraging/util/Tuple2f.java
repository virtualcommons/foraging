package edu.asu.commons.foraging.util;

import java.io.Serializable;

public class Tuple2f implements Serializable{
	
	private static final long serialVersionUID = -7055784802188977050L;
	
	public Tuple2f() {
		this.a = 0.0f;
		this.b = 0.0f;
	}
	
	public Tuple2f(float a, float b) {
		this.a = a;
		this.b = b;
	}
	public float a;
	public float b;
}