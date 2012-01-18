package edu.asu.commons.foraging.model;

import java.io.Serializable;

public class TrustGameResult implements Serializable {

	private static final long serialVersionUID = -1886062761191495771L;
	
	private ClientData playerOne;
	private ClientData playerTwo;
	private double playerOneAmountToKeep;
	private double playerOneEarnings;
	private double playerTwoEarnings;
	private double[] playerTwoAmountsToKeep;
	private String log;
	
	public TrustGameResult(ClientData playerOne, ClientData playerTwo) {
		this.playerOne = playerOne;
		this.playerTwo = playerTwo;
	}
	
	public ClientData getPlayerOne() {
		return playerOne;
	}
	public void setPlayerOne(ClientData playerOne) {
		this.playerOne = playerOne;
	}
	public ClientData getPlayerTwo() {
		return playerTwo;
	}
	public void setPlayerTwo(ClientData playerTwo) {
		this.playerTwo = playerTwo;
	}
	public double getPlayerOneAmountToKeep() {
		return playerOneAmountToKeep;
	}
	public void setPlayerOneAmountToKeep(double playerOneAmountToKeep) {
		this.playerOneAmountToKeep = playerOneAmountToKeep;
	}
	public double getPlayerOneEarnings() {
		return playerOneEarnings;
	}
	public void setPlayerOneEarnings(double playerOneEarnings) {
		this.playerOneEarnings = playerOneEarnings;
	}
	public double getPlayerTwoEarnings() {
		return playerTwoEarnings;
	}
	public void setPlayerTwoEarnings(double playerTwoEarnings) {
		this.playerTwoEarnings = playerTwoEarnings;
	}
	public String getLog() {
		return log;
	}
	public void setLog(String log) {
		this.log = log;
	}
	
	public String toString() {
		return log;
	}

	public double[] getPlayerTwoAmountsToKeep() {
		return playerTwoAmountsToKeep;
	}

	public void setPlayerTwoAmountsToKeep(double[] playerTwoAmountsToKeep) {
		this.playerTwoAmountsToKeep = playerTwoAmountsToKeep;
	}

}
