package com.ismaroik.powercrux;

import java.util.ArrayList;

// each game mode implements this interface to be able to control game engine
// game mode contains all info about current game state - number of players, scores, etc..
public interface IGameMode {

	// when round ends this method is called, game mode is responsible to update its state for later querying
	public void addRound(int[] currentRoundScores, int stoppingPlayer);
	
	// gets configuration of spinner to be used in the next round
	public SpinnerConfig GetNextRoundSpinnerConfig();
	
	// true if player on specified index is disabled
	public boolean isPlayerDisabled(int playerIndex);
	
	// true if player reached final score
	public boolean isPlayerFinished(int playerIndex);
	
	// gets spinner result configuration for current round
	public SpinnerResultConfig getSpinnerResultConfig();
	
	// true if game has ended
	public boolean isGameOver();
	
	// gets index of player who has highest score (if there are more players with same score, returns all of them)
	public ArrayList<Integer> getFirstPlayers();
	public ArrayList<Integer> getSecondPlayers();
	public ArrayList<Integer> getThirdPlayers();
	
	// returns config if special event occurred during lastly added round, otherwise null
	public SpecialEventConfig getSpecialEventForRound();
}
