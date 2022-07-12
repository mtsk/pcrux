package com.ismaroik.powercrux;

// contains all data required to draw result of round
public class SpinnerResultConfig {
	
	// player who stopped the spinner
	public int stoppingPlayer;
	
	// total scores of players in %
	public float[] totalPlayerScores;
	
	// figures to be displayed by game as total player scores
	public int[] displayScores;
	
	// figures to be displayed as current round score
	public int[] currentRoundDisplayScores;
	
	// center circle radius in %
	public int centerCircleRadius;
	
	// number shown in center circle
	public int centerNumber;
	
	// text shown in center circle
	public String centerText;
}
