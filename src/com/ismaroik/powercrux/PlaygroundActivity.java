package com.ismaroik.powercrux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.larvalabs.svgandroid.SVGParser;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.PictureDrawable;

public class PlaygroundActivity extends Activity implements MediaPlayer.OnCompletionListener, IEndScreenPanelListener {
	
	private Spinner mSpinner;	
	private Point mSpinnerCenter;
	private TextView mTxtAverageFps;
	private TextView mTxtReactionTime;
	private ImageButton mBtnNextRound;
	private IGameMode mGameMode;
	final int mMaxPlayerCount = 4;
	private int mPlayerCount;
	private Animation mAnimationFadeIn;
	private Animation mAnimationCupFadeIn;
	
	private RelativeLayout mNextRoundPanel;
	private RelativeLayout mBackScreenPanel;
	private EndScreenPanel mEndScreenPanel;
	private PlayerScoreOverlayView mPlayerScoreOverlayView;
	
	private TextView mTxtBackScreenHidden;
	private TextView mTxtEndScreenHidden;
	
	private SoundPool mSoundPool;
	private int mSndGameButtonPressId;
	private int mSndGameButtonPressFullScoreId;
	private int mSndFinishId;
	private int mSndHitItPlayerFinish;
	private int mSndMissClick;
	private boolean mIsSoundEnabled;
	private boolean mIsMusicEnabled;
	
	private ImageView mCupTopLeft;
	private ImageView mCupTopRight;
	private ImageView mCupBottomLeft;
	private ImageView mCupBottomRight;
	
	RotableImageView[] mPlayerScoreImg;
	private PictureDrawable mImgPlayerScoreArrow;
	private PictureDrawable mImgPlayerScoreSplash;
	
	private boolean mIsFirstRun = true;
	
	private MediaPlayer mMediaPlayer;
	private int mMediaPlayerPosition = 0;
	private int mSelectedMusic = 0;
	private Random mRandomGenerator = new Random();
	
	Bitmap mCupImage_1 = null;
	Bitmap mCupImage_2 = null;
	Bitmap mCupImage_3 = null;
	ArrayList<Bitmap> mRotatedCupImages = new ArrayList<Bitmap>();
	
	private SpinnerResultConfig mCurrentSpinnerResultConfig;
	private SpinnerConfig mCurrentSpinnerConfig;
	
	private float mTouchMinX;
	private float mTouchMaxX;
	private float mTouchMinY;
	private float mTouchMaxY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        final Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_playground);
        
        enforceSoftwareRendering();
        
        // load sounds for game
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        mSndGameButtonPressId = mSoundPool.load(this, R.raw.gamepressbutton, 1);
        mSndGameButtonPressFullScoreId = mSoundPool.load(this, R.raw.playground_fullscore, 1);
        mSndFinishId = mSoundPool.load(this, R.raw.playground_finish, 1);
        mSndHitItPlayerFinish = mSoundPool.load(this, R.raw.playground_hitit_playerend, 1);
        mSndMissClick = mSoundPool.load(this, R.raw.playground_missclick, 1);

        mSpinner = (Spinner)findViewById(R.id.spinner);
        
        mBackScreenPanel = (RelativeLayout)findViewById(R.id.backScreenPanel);
        mBackScreenPanel.setBackgroundColor(Color.argb(150, 70, 70, 70));
        mTxtBackScreenHidden = (TextView)findViewById(R.id.txtBackScreenHidden);
        
        mEndScreenPanel = (EndScreenPanel)findViewById(R.id.endScreenPanel);
        mTxtEndScreenHidden = (TextView)findViewById(R.id.txtEndScreenHidden);
                
        mNextRoundPanel = (RelativeLayout)findViewById(R.id.nextRoundPanel);        
        mTxtAverageFps = (TextView)findViewById(R.id.txtAverageFps);
        mTxtReactionTime = (TextView)findViewById(R.id.txtReactionTime);
        mBtnNextRound = (ImageButton)findViewById(R.id.btnNextRoud);
        
        mPlayerScoreOverlayView = (PlayerScoreOverlayView)findViewById(R.id.playerScoreOverlayView);
        
        mPlayerScoreImg = new RotableImageView[4];
        mPlayerScoreImg[0] = (RotableImageView)findViewById(R.id.player1ScoreImg);
        mPlayerScoreImg[1] = (RotableImageView)findViewById(R.id.player2ScoreImg);
        mPlayerScoreImg[2] = (RotableImageView)findViewById(R.id.player3ScoreImg);
        mPlayerScoreImg[3] = (RotableImageView)findViewById(R.id.player4ScoreImg);
        
        mCupTopLeft = (ImageView)findViewById(R.id.cupTopLeft);
        mCupTopRight = (ImageView)findViewById(R.id.cupTopRight);
        mCupBottomLeft = (ImageView)findViewById(R.id.cupBottomLeft);
        mCupBottomRight = (ImageView)findViewById(R.id.cupBottmRight); 
        
        initFromSettings();
        
        mAnimationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        mAnimationCupFadeIn = AnimationUtils.loadAnimation(this, R.anim.cup_fadein);
        mEndScreenPanel.addAnimationListener(this);
        
        loadImages();
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void enforceSoftwareRendering() 
    {
    	if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
        {
        	findViewById(R.id.spinner).setLayerType(View.LAYER_TYPE_SOFTWARE, null);        	
        	
        	// next round panel
        	findViewById(R.id.txtAverageFps).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.txtReactionTime).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.btnNextRoud).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.player1ScoreImg).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.player2ScoreImg).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.player3ScoreImg).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.player4ScoreImg).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	
        	findViewById(R.id.playerScoreOverlayView).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	
        	// back screen panel
        	findViewById(R.id.txtBackScreenHidden).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.btnContinue).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.btnRestart).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.btnMenu).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	
        	// end screen panel
        	findViewById(R.id.txtEndScreenHidden).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.cupTopLeft).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.cupTopRight).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.cupBottomLeft).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.cupBottmRight).setLayerType(View.LAYER_TYPE_SOFTWARE, null);        	
        	
        	findViewById(R.id.center).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.btnEndScreenRestart).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.btnEndScreenMenu).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }
    
    private void showAndAnimate(final View target, Animation animation)
	{
    	target.setVisibility(View.VISIBLE);
		target.startAnimation(animation);
	}
    
    private void loadImages() 
    {
//    	BitmapFactory.Options options = new BitmapFactory.Options();
// 		options.inPurgeable = true;
// 		Bitmap startButton = BitmapFactory.decodeResource(getResources(), R.drawable.play, options);
// 		mBtnNextRound.setImageBitmap(startButton);
 		
    	mBtnNextRound.setImageDrawable(SVGParser.getSVGFromResource(getResources(), R.raw.playground_btnplay).createPictureDrawable());
 		
 		mImgPlayerScoreArrow = SVGParser.getSVGFromResource(getResources(), R.raw.playground_points_arrow).createPictureDrawable();
 		mImgPlayerScoreSplash = SVGParser.getSVGFromResource(getResources(), R.raw.playground_points_splash).createPictureDrawable();
 		
// 		mImgFinalCup_Gold = SVGParser.getSVGFromResource(getResources(), R.raw.playground_finalcup_gold).createPictureDrawable();
	}
    
    private void initFromSettings()
    {
    	mCurrentSpinnerConfig = (SpinnerConfig)getIntent().getSerializableExtra(MenuActivity.SPINNERCONFIG);
        GameModeConfig gameModeConfig = (GameModeConfig)getIntent().getSerializableExtra(MenuActivity.GAMEMODECONFIG);
        mIsSoundEnabled = gameModeConfig.isSoundEnabled;
        mIsMusicEnabled = gameModeConfig.isMusicEnabled;
        mPlayerCount = gameModeConfig.numberOfPlayers;
        
        // create game mode
        switch (gameModeConfig.gameMode) {
		case 0:
			mGameMode = new GameMode_HitIt(gameModeConfig, mCurrentSpinnerConfig, getResources());
			break;
		case 1:
			mGameMode = new GameMode_CoolIt(gameModeConfig, mCurrentSpinnerConfig, getResources());
		default:
			break;
		}
        
        mSpinner.setSpinnerConfig(mGameMode.GetNextRoundSpinnerConfig());
        
        setLayoutElementsDimensions(mCurrentSpinnerConfig);
    }
    
    private double convertDipToPx(double value) 
    {
		return value * getResources().getDisplayMetrics().density;
	}
    
    private void setLayoutElementsDimensions(SpinnerConfig spinnerConfig)
    {
    	Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
    	
    	// next round button size
    	int btnSize_px = (int)(convertDipToPx(spinnerConfig.innerCircleRadius * 2) + 0.5f) + (int)(convertDipToPx(3) + 0.5f);
    	
    	ImageButton btnNextRoud = (ImageButton)findViewById(R.id.btnNextRoud);
    	android.widget.RelativeLayout.LayoutParams btnNextRoudLP = new android.widget.RelativeLayout.LayoutParams(btnSize_px, btnSize_px);
    	btnNextRoudLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
    	btnNextRoudLP.addRule(RelativeLayout.CENTER_VERTICAL);
		btnNextRoud.setLayoutParams(btnNextRoudLP);
		
		// player score arrows/splash
		// black magic
		int arrowSizeW_px = (int)(outMetrics.widthPixels * 0.41f + 0.5f);
		int arrowSizeH_px = (int)(arrowSizeW_px * 1.1f + 0.5f);
		int marginTop_Bottom_px =  (int)(((outMetrics.heightPixels/2) - arrowSizeH_px) - (outMetrics.widthPixels * 0.1f) + 0.5f);
		if(marginTop_Bottom_px < 0) marginTop_Bottom_px = 0;
		int marginLeft_Right_px =  (int)((outMetrics.widthPixels * 0.04f) + 0.5f);

		mPlayerScoreOverlayView.setScoreArrowsDimensions(arrowSizeW_px, arrowSizeW_px, marginTop_Bottom_px, marginLeft_Right_px);
		
		// top left
		android.widget.RelativeLayout.LayoutParams mPlayer1ScoreImgLP = new android.widget.RelativeLayout.LayoutParams(arrowSizeW_px, arrowSizeH_px);
		mPlayer1ScoreImgLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		mPlayer1ScoreImgLP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		mPlayer1ScoreImgLP.setMargins(marginLeft_Right_px, marginTop_Bottom_px, 0, 0);
		mPlayerScoreImg[0].setLayoutParams(mPlayer1ScoreImgLP);
		mPlayerScoreImg[0].angle = 141;
    	
    	// top right
    	android.widget.RelativeLayout.LayoutParams mPlayer2ScoreImgLP = new android.widget.RelativeLayout.LayoutParams(arrowSizeW_px, arrowSizeH_px);
		mPlayer2ScoreImgLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mPlayer2ScoreImgLP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		mPlayer2ScoreImgLP.setMargins(0, marginTop_Bottom_px, marginLeft_Right_px, 0);
		mPlayerScoreImg[1].setLayoutParams(mPlayer2ScoreImgLP);
		mPlayerScoreImg[1].angle = 219;
    	
    	// bottom left
		android.widget.RelativeLayout.LayoutParams mPlayer3ScoreImgLP = new android.widget.RelativeLayout.LayoutParams(arrowSizeW_px, arrowSizeH_px);
		mPlayer3ScoreImgLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		mPlayer3ScoreImgLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mPlayer3ScoreImgLP.setMargins(marginLeft_Right_px, 0, 0, marginTop_Bottom_px);
		mPlayerScoreImg[2].setLayoutParams(mPlayer3ScoreImgLP);
		mPlayerScoreImg[2].angle = 39;
    	
    	// bottom right
		android.widget.RelativeLayout.LayoutParams mPlayer4ScoreImgLP = new android.widget.RelativeLayout.LayoutParams(arrowSizeW_px, arrowSizeH_px);
		mPlayer4ScoreImgLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mPlayer4ScoreImgLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mPlayer4ScoreImgLP.setMargins(0, 0, marginLeft_Right_px, marginTop_Bottom_px);
		mPlayerScoreImg[3].setLayoutParams(mPlayer4ScoreImgLP);
		mPlayerScoreImg[3].angle = -39;
		
		// set final screen cups dimensions
		int height = (int)(outMetrics.heightPixels * 0.30f + 0.5f);
		int width = (int)(outMetrics.widthPixels * 0.40f + 0.5f);
		int topBottomMargin = (int)(outMetrics.heightPixels * 0.06f + 0.5f);
		
		android.widget.RelativeLayout.LayoutParams mCupTopLeftLP = new android.widget.RelativeLayout.LayoutParams(width, height);
		mCupTopLeftLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mCupTopLeftLP.setMargins(0, topBottomMargin, 0, 0);
		mCupTopLeft.setLayoutParams(mCupTopLeftLP);
		
		android.widget.RelativeLayout.LayoutParams mCupTopRightLP = new android.widget.RelativeLayout.LayoutParams(width, height);
		mCupTopRightLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mCupTopRightLP.setMargins(0, topBottomMargin, 0, 0);
		mCupTopRight.setLayoutParams(mCupTopRightLP);
		
		android.widget.RelativeLayout.LayoutParams mCupBottomLeftLP = new android.widget.RelativeLayout.LayoutParams(width, height);
		mCupBottomLeftLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mCupBottomLeftLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mCupBottomLeftLP.setMargins(0, 0, 0, topBottomMargin);
		mCupBottomLeft.setLayoutParams(mCupBottomLeftLP);
		
		android.widget.RelativeLayout.LayoutParams mCupBottomRightLP = new android.widget.RelativeLayout.LayoutParams(width, height);
		mCupBottomRightLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mCupBottomRightLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mCupBottomRightLP.setMargins(0, 0, 0, topBottomMargin);
		mCupBottomRight.setLayoutParams(mCupBottomRightLP);
    }    

	@Override
	protected void onPause() {
		super.onPause();
		
		if(mMediaPlayer != null)
    	{
	    	mMediaPlayerPosition = mMediaPlayer.getCurrentPosition();
	    	mMediaPlayer.stop();
    	}
		
		if(mEndScreenPanel.getVisibility() == View.INVISIBLE && 
			mBackScreenPanel.getVisibility() == View.INVISIBLE)
		{
			if(mSpinner.isRunning())
			{
				mSpinner.stop();
			}
			mBackScreenPanel.setVisibility(View.VISIBLE);
		}
	}
	

	@Override
	protected void onStop() {
		
		if(mMediaPlayer != null)
		{
			// release media player
			mMediaPlayer.release();
		}
		super.onStop();
	}
	

	@Override
	protected void onResume() 
	{
		super.onResume();
		
		if(mMediaPlayer != null)
		{
			mMediaPlayer.release();
			mMediaPlayer = null;
			System.gc();
		}
		
		if(mSelectedMusic > 0)
		{
			mMediaPlayer = MediaPlayer.create(this, mSelectedMusic);
			mMediaPlayer.setVolume(0.7f, 0.7f);
			mMediaPlayer.setOnCompletionListener(this);
		}
		else
		{
			mSelectedMusic = getRandomMusic();
			mMediaPlayer = MediaPlayer.create(this, mSelectedMusic);
			mMediaPlayer.setVolume(0.7f, 0.7f);
			mMediaPlayer.setOnCompletionListener(this);
			
		}

    	try {
			mMediaPlayer.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		// seek, if music enabled
    	if(mIsMusicEnabled)
    	{
	    	if(mMediaPlayerPosition > 0)
	    	{
	    		mMediaPlayer.seekTo(mMediaPlayerPosition);	    		
	    	}
    	}
	}
	
	
	@Override
	public void onCompletion(MediaPlayer mp) 
	{

		if(mIsMusicEnabled)
    	{
			if(mMediaPlayer != null)
			{
				mMediaPlayer.release();
				mMediaPlayer = null;
				System.gc();
			}
			
			mSelectedMusic = getRandomMusic();
			mMediaPlayer = MediaPlayer.create(this, mSelectedMusic);
			mMediaPlayer.setVolume(0.7f, 0.7f);
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.start();
    	}
	}
	
	
	private int getRandomMusic()
	{
		int result = 0;
		int rnd = mRandomGenerator.nextInt(4);
		switch(rnd)
		{
		case 0:
			result = R.raw.playground_kickstarter;
			break;
		case 1:
			result = R.raw.playground_ontherun;
			break;
		case 2:
			result = R.raw.playground_powerjuice;
			break;
		case 3:
			result = R.raw.playground_tension;
			break;
		}
		
		return result;
	}
	

	@Override
    public void onWindowFocusChanged (boolean hasFocus)
    {
		if(hasFocus)
		{
			if(mIsFirstRun)
			{
				mIsFirstRun = false;
				mSpinnerCenter = mSpinner.getCenterCoordinates();
				
				mTouchMinX = mSpinnerCenter.x - mCurrentSpinnerConfig.innerCircleRadius;
				mTouchMaxX = mSpinnerCenter.x + mCurrentSpinnerConfig.innerCircleRadius;
				mTouchMinY = mSpinnerCenter.y - mCurrentSpinnerConfig.innerCircleRadius;
				mTouchMaxY = mSpinnerCenter.y + mCurrentSpinnerConfig.innerCircleRadius;
				
				mSpinner.start();
				
				if(mIsMusicEnabled)
		    	{
					mMediaPlayer.start();
		    	}
			}
			else
			{
				// redraw spinner
				if(mNextRoundPanel.getVisibility() == View.VISIBLE)
				{
					mSpinner.drawResult(mGameMode.getSpinnerResultConfig());
				}
				else if (mEndScreenPanel.getVisibility() == View.VISIBLE)
				{
					mSpinner.drawGameOver(mGameMode.getSpinnerResultConfig(), mGameMode.getFirstPlayers());
				}
				else
				{
					mSpinner.redraw();
				}
			}
		}
    }
	
	
	private void showAverageFps() {
		mTxtAverageFps.setText(String.valueOf(" Average FPS: " + mSpinner.getAverageFps()));
		mTxtAverageFps.setVisibility(View.VISIBLE);
	}
	
	
	private void showReactionTime()
	{
		mTxtReactionTime.setText(String.valueOf(" Reaction Time: " + mSpinner.getElapsedTime()));
		mTxtReactionTime.setVisibility(View.VISIBLE);
	}
	
	
	// gets index of player based on screen coordinates
	private int getPlayer(float x, float y)
	{
		int result = -1;
		if(x <= mSpinnerCenter.x && y <= mSpinnerCenter.y)	// top left
		{
			result = 0;
		}
		else if (x > mSpinnerCenter.x && y <= mSpinnerCenter.y)	// top right
		{
			result = 1;
		}
		else if (x <= mSpinnerCenter.x && y > mSpinnerCenter.y)	// bottom left
		{
			result = 2;
		}
		else if (x > mSpinnerCenter.x && y > mSpinnerCenter.y)	// bottom right
		{
			result = 3;
		}
		
		return result;
	}
	
	private boolean isValidTouch(float x, float y)
	{
		boolean result = false;
				
		if (x < mTouchMinX || x > mTouchMaxX || y < mTouchMinY || y > mTouchMaxY) {
		    // Definitely not within the polygon!
			result = true;
		}
		
		return result;
	}
	
	
	@Override 
	public boolean onTouchEvent(MotionEvent e) {
		
		final int action = e.getAction();
		
		if(action == MotionEvent.ACTION_DOWN)
		{
			if(mSpinner.isRunning()) 
			{
				float x = e.getX(); 
				float y = e.getY();
				
				if(!isValidTouch(x,y))
				{
					return false;
				}
				
				// get player who stopped the spinner
				int stoppingPlayer = getPlayer(x, y);
				
				// if player is disabled, skip
				if(mGameMode.isPlayerDisabled(stoppingPlayer) || mGameMode.isPlayerFinished(stoppingPlayer))
				{
					return false;
				}
				
				// stop the spinner
				mSpinner.stop();
				
				// analyze result
				int[] currentRoundScores = mSpinner.analyzeResult();
				
				// play button snd
				if(mIsSoundEnabled)
				{					
					if(currentRoundScores[stoppingPlayer] > 0)
					{
						if(currentRoundScores[stoppingPlayer] == 100)
						{
							playSound(mSndGameButtonPressFullScoreId);
						}
						else
						{
							playSound(mSndGameButtonPressId);
						}
					}
					else
					{
						playSound(mSndMissClick);
					}
				}
				
//				showAverageFps();
//				showReactionTime();
				
				mGameMode.addRound(currentRoundScores, stoppingPlayer);
				
				if(mGameMode.isGameOver())
				{
					mCurrentSpinnerResultConfig = mGameMode.getSpinnerResultConfig();
					mSpinner.drawGameOver(mCurrentSpinnerResultConfig, mGameMode.getFirstPlayers());
					
					// pause music when end game menu
					if(mIsMusicEnabled)
			    	{
						mMediaPlayer.pause();
			    	}

					if(mIsSoundEnabled)
					{
						playSound(mSndFinishId);
					}					
					
					// HACK for higher versions of android to properly show overlay panel
					mTxtEndScreenHidden.setText("");
					
					// animate end screen panel					
					showAndAnimate(mEndScreenPanel, mAnimationFadeIn);
				}
				else
				{
					// HACK for higher versions of android to properly show overlay panel
					mTxtAverageFps.setText("");
					
					mNextRoundPanel.setVisibility(View.VISIBLE);
					
					mCurrentSpinnerResultConfig = mGameMode.getSpinnerResultConfig();
					mSpinner.drawResult(mCurrentSpinnerResultConfig);
					
					// gradually show play button		
					showAndAnimate(mBtnNextRound, mAnimationFadeIn);
					
					showPlayerScoreImages(stoppingPlayer);
					
					// perform special event, if some occurred
					SpecialEventConfig specialEventConfig = mGameMode.getSpecialEventForRound();
					if(specialEventConfig != null)
					{
						switch(specialEventConfig.soundId)
						{
						case R.raw.playground_hitit_playerend:
							playSound(mSndHitItPlayerFinish);
							break;
						}
					}
				}
				
				// set spinner configuration for next round
				mCurrentSpinnerConfig = mGameMode.GetNextRoundSpinnerConfig();
				mSpinner.setSpinnerConfig(mCurrentSpinnerConfig);
			}
			else
			{
			}
		}
		
        return true;
    }
	
	
	private void showPlayerScoreImages(int stoppingPlayer)
	{
		PlayerScoreImageAnimListener listener = new PlayerScoreImageAnimListener(mSpinner);
		Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
		fadeIn.setAnimationListener(listener);
		
		for(int i = 0; i < 4; i++)
		{
			if(!mGameMode.isPlayerDisabled(i))
			{
				if(mCurrentSpinnerResultConfig.currentRoundDisplayScores[i] > 0)
				{
					mPlayerScoreImg[i].setImageDrawable(mImgPlayerScoreArrow);
					
					mPlayerScoreImg[i].setVisibility(View.VISIBLE);
					mPlayerScoreImg[i].startAnimation(fadeIn);
				}
				else
				{
					if(i == stoppingPlayer)
					{
						mPlayerScoreImg[i].setImageDrawable(mImgPlayerScoreSplash);
						
						mPlayerScoreImg[i].setVisibility(View.VISIBLE);
						mPlayerScoreImg[i].startAnimation(fadeIn);
					}										
					else
					{
						mPlayerScoreImg[i].setVisibility(View.INVISIBLE);
					}
				}
			}
			else
			{
				mPlayerScoreImg[i].setVisibility(View.INVISIBLE);
			}
		}
	}
	
	// end screen panel
	@Override
	public void onAnimationEnd() 
	{
		System.gc();
    	showWinnerCups(mGameMode);
	}
	
	
	private void showWinnerCups(IGameMode gameMode) 
	{
		ArrayList<Integer> firstPlayers = null;
		ArrayList<Integer> secondPlayers = null;
		ArrayList<Integer> thirdPlayers = null;
		
		BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inPurgeable = true;
		switch(mPlayerCount)
		{
		case 2:
			mCupImage_1 = BitmapFactory.decodeResource(getResources(), R.drawable.playground_cup_1, options);
			firstPlayers = gameMode.getFirstPlayers();
	 		showCupForPlayers(mCupImage_1, firstPlayers);
//	 		showCupForPlayers(mImgFinalCup_Gold, firstPlayers);
			break;
		case 3:
			mCupImage_1 = BitmapFactory.decodeResource(getResources(), R.drawable.playground_cup_1, options);
			mCupImage_2 = BitmapFactory.decodeResource(getResources(), R.drawable.playground_cup_2, options);
			firstPlayers = gameMode.getFirstPlayers();
			secondPlayers = gameMode.getSecondPlayers();
			showCupForPlayers(mCupImage_1, firstPlayers);
//			showCupForPlayers(mImgFinalCup_Gold, firstPlayers);
			showCupForPlayers(mCupImage_2, secondPlayers);
			break;
		case 4:
			mCupImage_1 = BitmapFactory.decodeResource(getResources(), R.drawable.playground_cup_1, options);
			mCupImage_2 = BitmapFactory.decodeResource(getResources(), R.drawable.playground_cup_2, options);
			mCupImage_3 = BitmapFactory.decodeResource(getResources(), R.drawable.playground_cup_3, options);
			firstPlayers = gameMode.getFirstPlayers();
			secondPlayers = gameMode.getSecondPlayers();
			thirdPlayers = gameMode.getThirdPlayers();
			showCupForPlayers(mCupImage_1, firstPlayers);
//			showCupForPlayers(mImgFinalCup_Gold, firstPlayers);
			showCupForPlayers(mCupImage_2, secondPlayers);
			showCupForPlayers(mCupImage_3, thirdPlayers);
			break;
		}
	}
	
	private void showCupForPlayers(PictureDrawable cupImage, ArrayList<Integer> players)
	{
		for(Integer player : players )
		{
			switch(player)
			{
			case 0:
				mCupTopLeft.setImageDrawable(cupImage);
				showAndAnimate(mCupTopLeft, mAnimationCupFadeIn);
				break;
			case 1:
				mCupTopRight.setImageDrawable(cupImage);
				showAndAnimate(mCupTopRight, mAnimationCupFadeIn);
				break;
			case 2:
				mCupBottomLeft.setImageDrawable(cupImage);
				showAndAnimate(mCupBottomLeft, mAnimationCupFadeIn);
				break;
			case 3:
				mCupBottomRight.setImageDrawable(cupImage);
				showAndAnimate(mCupBottomRight, mAnimationCupFadeIn);
				break;
			}
		}
	}
	
	private void showCupForPlayers(Bitmap cupImage, ArrayList<Integer> players)
	{
		Bitmap rotatedCupImage = null;
		
		for(Integer player : players )
		{
			switch(player)
			{
			case 0:
				if(rotatedCupImage == null)
				{
					Matrix matrixTopLeft = new Matrix();
					matrixTopLeft.postRotate(180);
					rotatedCupImage = Bitmap.createBitmap(cupImage, 0, 0, cupImage.getWidth(), cupImage.getHeight(),
							matrixTopLeft, true);
					mRotatedCupImages.add(rotatedCupImage);
				}
				
				mCupTopLeft.setImageBitmap(rotatedCupImage);
				
				showAndAnimate(mCupTopLeft, mAnimationCupFadeIn);
				break;
			case 1:
				if(rotatedCupImage == null)
				{
					Matrix matrixTopRight = new Matrix();
					matrixTopRight.postRotate(180);
					rotatedCupImage = Bitmap.createBitmap(cupImage, 0, 0, cupImage.getWidth(), cupImage.getHeight(),
							matrixTopRight, true);
					mRotatedCupImages.add(rotatedCupImage);
				}
				
				mCupTopRight.setImageBitmap(rotatedCupImage);
				
				showAndAnimate(mCupTopRight, mAnimationCupFadeIn);
				break;
			case 2:
				mCupBottomLeft.setImageBitmap(cupImage);
				
				showAndAnimate(mCupBottomLeft, mAnimationCupFadeIn);
				break;
			case 3:
				mCupBottomRight.setImageBitmap(cupImage);
				
				showAndAnimate(mCupBottomRight, mAnimationCupFadeIn);
				break;
			}
		}
	}
	
	private void hideWinnerCups()
	{
		mCupTopLeft.setVisibility(View.INVISIBLE);
		mCupTopRight.setVisibility(View.INVISIBLE);
		mCupBottomLeft.setVisibility(View.INVISIBLE);
		mCupBottomRight.setVisibility(View.INVISIBLE);
		
		mCupTopLeft.setImageResource(android.R.color.transparent);
		mCupTopRight.setImageResource(android.R.color.transparent);
		mCupBottomLeft.setImageResource(android.R.color.transparent);
		mCupBottomRight.setImageResource(android.R.color.transparent);
		
		// release images
		if(mCupImage_1 != null)
		{
			mCupImage_1.recycle();
			mCupImage_1 = null;
		}
		if(mCupImage_2 != null)
		{
			mCupImage_2.recycle();
			mCupImage_2 = null;
		}
		if(mCupImage_3 != null)
		{
			mCupImage_3.recycle();
			mCupImage_3 = null;
		}
		for(Bitmap rotatedCupImage : mRotatedCupImages)
		{
			rotatedCupImage.recycle();
		}
		mRotatedCupImages.clear();
		System.gc();
	}
	

	public void btnNextRoud(View view) {
		if(!mSpinner.isRunning())
		{
			mNextRoundPanel.setVisibility(View.INVISIBLE);
			mPlayerScoreOverlayView.setVisibility(View.INVISIBLE);
			mSpinner.start();
		}
	}
	
	
	public void btnRestart(View view) {
		if(!mSpinner.isRunning())
		{
			initFromSettings();
			
			hideWinnerCups();
			mBackScreenPanel.setVisibility(View.INVISIBLE);
			mNextRoundPanel.setVisibility(View.INVISIBLE);
			mEndScreenPanel.setVisibility(View.INVISIBLE);
			mPlayerScoreOverlayView.setVisibility(View.INVISIBLE);
			
			if(mIsMusicEnabled && !mMediaPlayer.isPlaying())
	    	{
				mMediaPlayer.start();
	    	}
			
			mSpinner.restart();
		}
	}
	
	
	public void btnMenu(View view) {
		finish();
	}
	
	
	public void btnContinue(View view) {
		mBackScreenPanel.setVisibility(View.INVISIBLE);
		
		// resume playing
		if(mIsMusicEnabled)
    	{
			mMediaPlayer.start();
    	}
				
		showAndAnimate(mNextRoundPanel, mAnimationFadeIn);
	}
	
	
	// plays specified sound using users current volume settings
	private void playSound(int soundId)
	{
		AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float actualVolume = (float) audioManager
            .getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager
            .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = actualVolume / maxVolume;
        
        mSoundPool.play(soundId, volume, volume, 1, 0, 1f);
	}
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if (keyCode == KeyEvent.KEYCODE_BACK) 
		{
			if(mEndScreenPanel.getVisibility() == View.INVISIBLE && mBackScreenPanel.getVisibility() == View.INVISIBLE)
			{
				if(mSpinner.isRunning())
				{
					mSpinner.stop();
				}
				
				// pause music when showing back menu
				if(mIsMusicEnabled)
		    	{
					mMediaPlayer.pause();
		    	}
				
				// HACK for higher versions of android to properly show overlay panel
				mTxtBackScreenHidden.setText("");
				
				showAndAnimate(mBackScreenPanel, mAnimationFadeIn);
				
				return true;
			}
	    }
		else if (keyCode == KeyEvent.KEYCODE_MENU){
	        return true;
	    } 
		else if (keyCode == KeyEvent.KEYCODE_SETTINGS){
	        return true;
	    } 
		else if (keyCode == KeyEvent.KEYCODE_SEARCH){
	        return true;
	    } 

		return super.onKeyDown(keyCode, event);
	}


	private class PlayerScoreImageAnimListener implements AnimationListener
	{
		private int currentNo;
		private Object syncLock = new Object[0]; 
		
		public PlayerScoreImageAnimListener(Spinner spinner)
		{
			mSpinner = spinner;
			currentNo = 0;
		}
		
		public void onAnimationEnd(Animation anim) 
		{
			synchronized(syncLock)
			{
				currentNo ++;
				if(currentNo == 1)	// ensure that we call this only once
				{
					mPlayerScoreOverlayView.setCurrentRoundStats(
							mCurrentSpinnerResultConfig.currentRoundDisplayScores, 
							mCurrentSpinnerConfig.disabledPlayers, 
							(int)(convertDipToPx(mCurrentSpinnerConfig.currentRoundTextSize) + 0.5f));
					mPlayerScoreOverlayView.setVisibility(View.VISIBLE);
				}
			}
        }

        public void onAnimationRepeat(Animation arg0) {}

        public void onAnimationStart(Animation arg0) {
        }
	}
	
}
