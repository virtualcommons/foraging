package edu.asu.commons.foraging.util;

import java.io.Serializable;

public class Tuple2i implements Serializable{
	
	private static final long serialVersionUID = 7691266601007558192L;
	public Tuple2i(int a, int b) {
		this.a = a;
		this.b = b;
	}
	
	public String toString() {
		return new String("(" + a + ", " + b + ")");
	}
	
	
	public int a;
	public int b;
}

