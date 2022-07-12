package com.ismaroik.powercrux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import android.content.res.Resources;
import android.util.FloatMath;

public class GameMode_CoolIt implements IGameMode  {
	private float mTotalPoints;					// total points which are in play (one point correspond to one degree)
	private int mCurrentPoints;					// current points in bank - area of center circle
	private int mNumberOfPlayers;
	private int mPlayerPenalty;			// if player who stopped the spinner didn't hit anything
	private int mMaxPlayerCount;
	private int[] mPlayerScores;				// player scores
	private int[] mPlayerScoresCurrentRound;	// player scores for the last round
	private SpinnerConfig mNextRoundSpinnerConfig;
	private int mLastRoundStoppingPlayer;
	private int mDifficulty;
	private ArrayList<Integer> mFirstPlayers = new ArrayList<Integer>();
	private ArrayList<Integer> mSecondPlayers = new ArrayList<Integer>();
	private ArrayList<Integer> mThirdPlayers = new ArrayList<Integer>();
	
	public GameMode_CoolIt(GameModeConfig gameModeConfig, SpinnerConfig initialSpinnerConfig, Resources resources)
	{
		mTotalPoints = gameModeConfig.coolIt_TotalPoints;
		mCurrentPoints = gameModeConfig.coolIt_TotalPoints;
		mMaxPlayerCount = gameModeConfig.maxPlayerCount;
		mNumberOfPlayers = gameModeConfig.numberOfPlayers;
		mPlayerPenalty = gameModeConfig.playerPenalty;
		mPlayerScores = new int[mMaxPlayerCount];		// internal arrays are always for max player count
		mPlayerScoresCurrentRound = new int[mMaxPlayerCount];
		mDifficulty = gameModeConfig.difficulty;
		
		mNextRoundSpinnerConfig = initialSpinnerConfig;
		mNextRoundSpinnerConfig.centerNumber = (int)mTotalPoints;
		mNextRoundSpinnerConfig.centerText = resources.getString(R.string.coolitCenterText);
		
		// create disabled players array
		initialSpinnerConfig.disabledPlayers = new boolean[mMaxPlayerCount];
		for(int i = 0; i < mMaxPlayerCount; i++)
		{
			initialSpinnerConfig.disabledPlayers[i] = false;
		}
		
		// initialize disabled players array based on number of players
		switch(mNumberOfPlayers)
		{
		case 2:
			initialSpinnerConfig.disabledPlayers[1] = true;
			initialSpinnerConfig.disabledPlayers[2] = true;
			break;
		case 3:
			initialSpinnerConfig.disabledPlayers[1] = true;
			break;
		}
		
		calculateNextRoundSpinnerConfig();
	}
	
	// based on game mode rules calculates spinner configuration for next round
	private void calculateNextRoundSpinnerConfig()
	{
		float arcInflationPercentage = 100 - (mCurrentPoints / (mTotalPoints / 100));
		
		if(arcInflationPercentage <= 20)
		{
			switch(mDifficulty)
			{
			case 0:
				mNextRoundSpinnerConfig.numberOfArcs = 1;
				mNextRoundSpinnerConfig.sweepAngleMin = 170;
				mNextRoundSpinnerConfig.sweepAngleMax = 200;
				break;
			case 1:
				mNextRoundSpinnerConfig.numberOfArcs = 1;
				mNextRoundSpinnerConfig.sweepAngleMin = 200;
				mNextRoundSpinnerConfig.sweepAngleMax = 230;
				break;
			case 2:
				mNextRoundSpinnerConfig.numberOfArcs = 2;
				mNextRoundSpinnerConfig.sweepAngleMin = 180;
				mNextRoundSpinnerConfig.sweepAngleMax = 210;
				break;
			}
		}
		else if(arcInflationPercentage > 20 && arcInflationPercentage <= 40)
		{
			switch(mDifficulty)
			{
			case 0:
				mNextRoundSpinnerConfig.numberOfArcs = 1;
				mNextRoundSpinnerConfig.sweepAngleMin = 200;
				mNextRoundSpinnerConfig.sweepAngleMax = 230;
				break;
			case 1:
				mNextRoundSpinnerConfig.numberOfArcs = 2;
				mNextRoundSpinnerConfig.sweepAngleMin = 170;
				mNextRoundSpinnerConfig.sweepAngleMax = 200;
				break;
			case 2:
				mNextRoundSpinnerConfig.numberOfArcs = 3;
				mNextRoundSpinnerConfig.sweepAngleMin = 150;
				mNextRoundSpinnerConfig.sweepAngleMax = 180;
				break;
			}
		}
		else if(arcInflationPercentage > 40 && arcInflationPercentage <= 60)
		{
			switch(mDifficulty)
			{
			case 0:
				mNextRoundSpinnerConfig.numberOfArcs = 1;
				mNextRoundSpinnerConfig.sweepAngleMin = 230;
				mNextRoundSpinnerConfig.sweepAngleMax = 260;
				break;
			case 1:
				mNextRoundSpinnerConfig.numberOfArcs = 2;
				mNextRoundSpinnerConfig.sweepAngleMin = 200;
				mNextRoundSpinnerConfig.sweepAngleMax = 230;
				break;
			case 2:
				mNextRoundSpinnerConfig.numberOfArcs = 4;
				mNextRoundSpinnerConfig.sweepAngleMin = 120;
				mNextRoundSpinnerConfig.sweepAngleMax = 150;
				break;
			}
		}
		else if(arcInflationPercentage > 60 && arcInflationPercentage <= 80)
		{
			switch(mDifficulty)
			{
			case 0:
				mNextRoundSpinnerConfig.numberOfArcs = 2;
				mNextRoundSpinnerConfig.sweepAngleMin = 140;
				mNextRoundSpinnerConfig.sweepAngleMax = 170;
				break;
			case 1:
				mNextRoundSpinnerConfig.numberOfArcs = 3;
				mNextRoundSpinnerConfig.sweepAngleMin = 140;
				mNextRoundSpinnerConfig.sweepAngleMax = 170;
				break;
			case 2:
				mNextRoundSpinnerConfig.numberOfArcs = 5;
				mNextRoundSpinnerConfig.sweepAngleMin = 90;
				mNextRoundSpinnerConfig.sweepAngleMax = 120;
				break;
			}
		}
		else if(arcInflationPercentage > 80)
		{
			switch(mDifficulty)
			{
			case 0:
				mNextRoundSpinnerConfig.numberOfArcs = 2;
				mNextRoundSpinnerConfig.sweepAngleMin = 170;
				mNextRoundSpinnerConfig.sweepAngleMax = 200;
				break;
			case 1:
				mNextRoundSpinnerConfig.numberOfArcs = 4;
				mNextRoundSpinnerConfig.sweepAngleMin = 110;
				mNextRoundSpinnerConfig.sweepAngleMax = 140;
				break;
			case 2:
				mNextRoundSpinnerConfig.numberOfArcs = 6;
				mNextRoundSpinnerConfig.sweepAngleMin = 60;
				mNextRoundSpinnerConfig.sweepAngleMax = 90;
				break;
			}
		}
	}
	
	public void addRound(int[] currentRoundScores, int stoppingPlayer)
	{
		mLastRoundStoppingPlayer = stoppingPlayer;
		
		int currentRoundDistributedPoints = 0;
		
		for(int i = 0; i < mMaxPlayerCount; i++)
		{
			// calculate angle
			int angle = (int)((float)currentRoundScores[i] * 0.9f + 0.5f);

			// is player disabled?
			if(!mNextRoundSpinnerConfig.disabledPlayers[i])
			{
				// save scores from current round
				mPlayerScoresCurrentRound[i] = angle;
			}
			else
			{
				mPlayerScoresCurrentRound[i] = 0;
			}
			
			currentRoundDistributedPoints += angle;
		}
		
		// check whether total distributed points is still in bank
		if(currentRoundDistributedPoints > mCurrentPoints)
		{
			// divide remaining points proportionally among the players according to ratio between their scores
			for(int i = 0; i < mMaxPlayerCount; i++)
			{
				final float percentFromRound = ((float)mPlayerScoresCurrentRound[i] / (float)currentRoundDistributedPoints);
				final int assignedPoints = (int)((percentFromRound * mCurrentPoints) + 0.5f);
				mPlayerScores[i] += assignedPoints;
				mPlayerScoresCurrentRound[i] = assignedPoints;
			}
			
			currentRoundDistributedPoints = mCurrentPoints;
		}
		else
		{
			for(int i = 0; i < mMaxPlayerCount; i++)
			{
				mPlayerScores[i] += mPlayerScoresCurrentRound[i];
			}
		}
		
		// update points in bank - subtract points distributed in this round among players
		if(mCurrentPoints > 0)
		{
			mCurrentPoints = mCurrentPoints - currentRoundDistributedPoints;
		}
		
		// did player who stopped spinner scored?
		if(currentRoundScores[stoppingPlayer] == 0)
		{
			// apply penalty
			mPlayerScores[stoppingPlayer] -= mPlayerPenalty;
			if(mPlayerScores[stoppingPlayer] < 0)
			{
				mPlayerScores[stoppingPlayer] = 0;
			}
			
			// save scores from current round
			mPlayerScoresCurrentRound[stoppingPlayer] = -mPlayerPenalty;
		}
		
		calculateNextRoundSpinnerConfig();
	}

	private float[] getTotalPlayerScores()
	{
		float[] totalScores = new float[mMaxPlayerCount];
		
		for(int i = 0; i < mMaxPlayerCount; i++)
		{
			// calculate angle
			float percentageFromFullCircle = (float)mPlayerScores[i] / (mTotalPoints / 100);
			totalScores[i] = percentageFromFullCircle;
		}
		
		return totalScores;
	}
	
	public SpinnerResultConfig getSpinnerResultConfig()
	{
		SpinnerResultConfig result = new SpinnerResultConfig();
		result.totalPlayerScores = getTotalPlayerScores();
		result.displayScores = mPlayerScores;
		result.currentRoundDisplayScores = mPlayerScoresCurrentRound;
		result.stoppingPlayer = mLastRoundStoppingPlayer;
		
		result.centerNumber = mCurrentPoints;
		result.centerText = "DEGREES LEFT";
		
		// circle area is proportional to points left in bank
		double relativeRadius = (FloatMath.sqrt(mCurrentPoints / mTotalPoints)) * 100;
		result.centerCircleRadius = (int)(relativeRadius + 0.5f);	// rounding
		
		return result;
	}
	
	public SpinnerConfig GetNextRoundSpinnerConfig()
	{
		return mNextRoundSpinnerConfig;
	}
	
	public boolean isPlayerDisabled(int playerIndex)
	{
		return mNextRoundSpinnerConfig.disabledPlayers[playerIndex];
	}
	
	public boolean isPlayerFinished(int playerIndex)
	{
		return false;
	}
	
	public boolean isGameOver()
	{
		// if there are no more points in bank, it's game over
		if(mCurrentPoints <= 0)
		{
			resolvePlayerPositions();
			return true;
		}
		
		return false;
	}
	
	private void resolvePlayerPositions()
	{
		// get unique scores
		HashSet<Integer> uniquePlayerScoresHashSet = new HashSet<Integer>();
		for(int i = mPlayerScores.length - 1; i >= 0; i--)
		{
			uniquePlayerScoresHashSet.add(mPlayerScores[i]);
		}		
		
		// order scores
		Integer[] sortedPlayerScores = uniquePlayerScoresHashSet.toArray(new Integer[0]);
		Arrays.sort(sortedPlayerScores);
		
		// find score per player position
		int firstPlayerScores = sortedPlayerScores[sortedPlayerScores.length - 1];
		int secondPlayerScores = sortedPlayerScores.length > 1 ? sortedPlayerScores[sortedPlayerScores.length - 2] : -1;
		int thirdPlayerScores = sortedPlayerScores.length > 2 ? sortedPlayerScores[sortedPlayerScores.length - 3] : -1;
		
		// add players to appropriate list based on score
		mFirstPlayers.clear();
		mSecondPlayers.clear();
		mThirdPlayers.clear();
		for(int i = 0; i < mMaxPlayerCount; i++)
		{
			if (mPlayerScores[i] == firstPlayerScores)
			{
				mFirstPlayers.add(i);
			}
			else if (mPlayerScores[i] == secondPlayerScores)
			{
				mSecondPlayers.add(i);
			}
			else if (mPlayerScores[i] == thirdPlayerScores)
			{
				mThirdPlayers.add(i);
			}
		}
	}
	
	public ArrayList<Integer> getFirstPlayers()
	{
		return mFirstPlayers;
	}

	@Override
	public ArrayList<Integer> getSecondPlayers() 
	{
		return mSecondPlayers;
	}

	@Override
	public ArrayList<Integer> getThirdPlayers() 
	{
		return mThirdPlayers;
	}

	@Override
	public SpecialEventConfig getSpecialEventForRound() 
	{
		return null;
	}
}
