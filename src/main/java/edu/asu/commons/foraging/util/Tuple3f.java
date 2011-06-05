package edu.asu.commons.foraging.util;

import java.io.Serializable;

public class Tuple3f implements Serializable{	
	private static final long serialVersionUID = -6746449053091929974L;
	
	public Tuple3f() {
		a = 0;
		b = 0;
		c = 0;
	}
	
	public Tuple3f(float a, float b, float c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	public float a;
	public float b;
	public float c;
	
}
