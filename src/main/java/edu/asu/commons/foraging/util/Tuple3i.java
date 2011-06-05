package edu.asu.commons.foraging.util;

import java.io.Serializable;

public class Tuple3i implements Serializable{
	
	private static final long serialVersionUID = -5063065410752605056L;
	public Tuple3i() {
		this.a = 0;
		this.b = 0;
		this.c = 0;
	}
	
	public Tuple3i(int a, int b, int c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	public void set(int a, int b, int c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	public int a;
	public int b;
	public int c;
}