package com.ismaroik.powercrux;

import java.util.ArrayList;

import android.content.res.Resources;

//adds up scores up to 360
public class GameMode_HitIt implements IGameMode {
	private float mTotalScore;			// max score
	private int mNumberOfPlayers;
	private int mMaxPlayerCount;
	private int mPlayerPenalty;			// if player who stopped the spinner didn't hit anything
	private int[] mPlayerScores;				// player scores
	private int[] mPlayerScoresCurrentRound;	// player scores for the last round
	private SpinnerConfig mNextRoundSpinnerConfig;
	private int mLastRoundStoppingPlayer;
	private int mDifficulty;
	
	private ArrayList<Integer> mFirstPlayers = new ArrayList<Integer>();
	private ArrayList<Integer> mSecondPlayers = new ArrayList<Integer>();
	private ArrayList<Integer> mThirdPlayers = new ArrayList<Integer>();
	private boolean mFirstPlayersComplete = false;
	private boolean mSecondPlayersComplete = false;
	private boolean mThirdPlayersComplete = false;
	
	private ArrayList<Integer> mFinishedPlayers = new ArrayList<Integer>();
	private boolean mPlayerFinished = false;
	
	public GameMode_HitIt(GameModeConfig gameModeConfig, SpinnerConfig initialSpinnerConfig, Resources resources)
	{
		mTotalScore = gameModeConfig.hitIt_TotalScore;
		mMaxPlayerCount = gameModeConfig.maxPlayerCount;
		mNumberOfPlayers = gameModeConfig.numberOfPlayers;
		mPlayerPenalty = gameModeConfig.playerPenalty;
		mPlayerScores = new int[mMaxPlayerCount];
		mPlayerScoresCurrentRound = new int[mMaxPlayerCount];
		mDifficulty = gameModeConfig.difficulty;
		
		mNextRoundSpinnerConfig = initialSpinnerConfig;
		
		mNextRoundSpinnerConfig.centerNumber = (int)mTotalScore;
		mNextRoundSpinnerConfig.centerText = resources.getString(R.string.hititCenterText);
		
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
	
	private float getAveragePlayerScore_percent()
	{
		float result = 0;
		
		for(int i = 0; i < mMaxPlayerCount; i++)
		{
			result += mPlayerScores[i];
		}
		
		result = result / (float)mNumberOfPlayers;
		
		// percentage from total score
		result = result / (mTotalScore / 100);
		
		return result;
	}
	
	// based on game mode rules calculates spinner configuration for next round
	private void calculateNextRoundSpinnerConfig()
	{
		float averagePlayerScore_percent = getAveragePlayerScore_percent();
		
		if(averagePlayerScore_percent <= 20)
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
		else if(averagePlayerScore_percent > 20 && averagePlayerScore_percent <= 40)
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
		else if(averagePlayerScore_percent > 40 && averagePlayerScore_percent <= 60)
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
		else if(averagePlayerScore_percent > 60 && averagePlayerScore_percent <= 80)
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
		else if(averagePlayerScore_percent > 80)
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
		mPlayerFinished = false;
		mLastRoundStoppingPlayer = stoppingPlayer;
		
		// calculate player scores
		for(int i = 0; i < mMaxPlayerCount; i++)
		{
			// is player disabled?
			if(!mNextRoundSpinnerConfig.disabledPlayers[i])
			{
				if(mPlayerScores[i] < mTotalScore)
				{
					// calculate angle
					int angle = (int)(currentRoundScores[i] * 0.9f + 0.5f);
					
					// add to current
					mPlayerScores[i] += angle;
					
					// check if it is not over
					if(mPlayerScores[i] >= mTotalScore)
					{
						mPlayerScoresCurrentRound[i] = angle - (mPlayerScores[i] - (int)mTotalScore);
						mPlayerScores[i] = (int)mTotalScore;
						
						// add player to appropriate winner list
						if(!mFirstPlayersComplete)
						{
							mFirstPlayers.add(i);
						}
						else if(!mSecondPlayersComplete)
						{
							mSecondPlayers.add(i);
						}
						else if(!mThirdPlayersComplete)
						{
							mThirdPlayers.add(i);
						}
						
						mFinishedPlayers.add(i);
						mPlayerFinished = true;
					}
					else
					{
						// save scores from current round
						mPlayerScoresCurrentRound[i] = angle;
					}
				}
				else
				{
					mPlayerScoresCurrentRound[i] = 0;
				}
			}
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
				
		// were there some players exceeding target score?, if yes set flag on appropriate list 
		if(!mFirstPlayersComplete && mFirstPlayers.size() > 0)
		{
			mFirstPlayersComplete = true;
		}
		else if(!mSecondPlayersComplete && mSecondPlayers.size() > 0)
		{
			mSecondPlayersComplete = true;
		}
		else if(!mThirdPlayersComplete && mThirdPlayers.size() > 0)
		{
			mThirdPlayersComplete = true;
		}
		
		calculateNextRoundSpinnerConfig();
	}

	private float[] getTotalPlayerScores()
	{
		float[] totalScores = new float[mMaxPlayerCount];
		
		for(int i = 0; i < mMaxPlayerCount; i++)
		{
			// calculate angle
			float percentageFromFullCircle = (float)mPlayerScores[i] / (mTotalScore / 100);
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
		result.centerCircleRadius = 100;
		result.stoppingPlayer = mLastRoundStoppingPlayer;
		result.centerNumber = (int)mTotalScore;
		result.centerText = "TARGET";
		
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
		return mFinishedPlayers.contains(playerIndex);
	}
	
	public boolean isGameOver()
	{
		// if there are less than two players, it's game over 
		int numberOfPlayersWithFullScore = 0;
		for(int i = 0; i < mMaxPlayerCount; i++)
		{
			if(mPlayerScores[i] >= mTotalScore)
			{
				numberOfPlayersWithFullScore++;
			}
		}
		
		if((mNumberOfPlayers - numberOfPlayersWithFullScore) < 2)
		{
			return true;
		}
		else
		{
			return false;
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
		if(mPlayerFinished)
		{
			SpecialEventConfig specialEventConfig = new SpecialEventConfig();
			specialEventConfig.soundId = R.raw.playground_hitit_playerend;
			return specialEventConfig;
		}
		else
		{
			return null;
		}
	}
}
