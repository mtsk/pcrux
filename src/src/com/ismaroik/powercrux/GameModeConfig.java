package com.ismaroik.powercrux;

import java.io.Serializable;

// contains configuration variables for all possible game modes
public class GameModeConfig implements Serializable {
		private static final long serialVersionUID = 5614140074591108531L;

		public final int maxPlayerCount = 4;	// total number of players (currently 4)
	
		public int numberOfPlayers; // currently 2 ~ 4
		
		public int playerPenalty;
		
		public int difficulty;	// 0 - easy; 1 - medium; 2 - hard
		
		public int gameMode;	// HitIt = 0; CoolIt = 1

		public int hitIt_TotalScore;
		
		public int coolIt_TotalPoints;
		
		public boolean isSoundEnabled;
		
		public boolean isMusicEnabled;
}
