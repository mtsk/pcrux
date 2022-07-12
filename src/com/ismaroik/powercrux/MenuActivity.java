package com.ismaroik.powercrux;

import java.io.IOException;

import com.ismaroik.powercrux.androidspinnerext.DifficultySpinnerAdapter;
import com.ismaroik.powercrux.androidspinnerext.ModeSpinnerAdapter;
import com.ismaroik.powercrux.androidspinnerext.PlayersSpinnerAdapter;
import com.ismaroik.powercrux.androidspinnerext.SpinnerExtended;
import com.ismaroik.powercrux.dialogs.HelpDialogFragment;
import com.ismaroik.powercrux.dialogs.RedirectToIsmaroikWebDialogFragment;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class MenuActivity extends FragmentActivity implements OnItemSelectedListener, OnTouchListener {
	
	public final static String SPINNERCONFIG = "com.ismaroik.powercrux.SPINNERCONFIG";
	public final static String GAMEMODECONFIG = "com.ismaroik.powercrux.GAMEMODECONFIG";
	public static final String PREFS_NAME = "PowerCruxPreferences";

	private Intent mIntent;
	private float mScale;
	
	SpinnerExtended mPlayerSpinner;
	SpinnerExtended mModeSpinner;
	SpinnerExtended mDifficultySpinner;
	
	private ImageButton mbtnSound;
	private boolean mIsSoundEnabled = true;
	private ImageButton mbtnMusic;
	private boolean mIsMusicEnabled = true;
	
	private MediaPlayer mMediaPlayer;
	private int mMediaPlayerPosition = 0;
	
	private SoundPool mSoundPool;
	private int mSndGameButtonPressId;
	
	private float mScreenWidth_dp;
	private float mScreenHeight_dp;
    
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // bug when resuming application with shortcut after pressing 'home'
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) { 
            // Activity was brought to front and not created, 
            // Thus finishing this will get us to the last viewed activity 
            finish(); 
            return; 
        }
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        
        setContentView(R.layout.activity_menu);
        
        // ensure that hardware acceleration is off
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
        {
        	findViewById(R.id.btnPowercrux).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.btnHelp).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.players).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.mode).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.difficulty).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.btnMusic).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.btnSound).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        	findViewById(R.id.btnStart).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        
        mIntent = new Intent(MenuActivity.this, PlaygroundActivity.class);
        
        // load sounds for menu
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        mSndGameButtonPressId = mSoundPool.load(this, R.raw.menu_buttonpress, 1);
        
        Resources res = getResources();
        
        mPlayerSpinner = (SpinnerExtended) findViewById(R.id.players);
        PlayersSpinnerAdapter playersSpinnerAdapter = new PlayersSpinnerAdapter(MenuActivity.this, R.layout.spinner_row, res.getStringArray(R.array.players_array));
        mPlayerSpinner.setAdapter(playersSpinnerAdapter);
        mPlayerSpinner.setOnItemSelectedListener(this);
        mPlayerSpinner.setOnTouchListener(this);
        
        mModeSpinner = (SpinnerExtended) findViewById(R.id.mode);
        ModeSpinnerAdapter modeSpinnerAdapter = new ModeSpinnerAdapter(MenuActivity.this, R.layout.spinner_row, res.getStringArray(R.array.mode_array));
        mModeSpinner.setAdapter(modeSpinnerAdapter);
        mModeSpinner.setOnItemSelectedListener(this);
        mModeSpinner.setOnTouchListener(this);
        
        mDifficultySpinner = (SpinnerExtended) findViewById(R.id.difficulty);
        DifficultySpinnerAdapter difficultySpinnerAdapter = new DifficultySpinnerAdapter(MenuActivity.this, R.layout.spinner_row, res.getStringArray(R.array.difficulty_array));
        mDifficultySpinner.setAdapter(difficultySpinnerAdapter);
        mDifficultySpinner.setOnItemSelectedListener(this);
        mDifficultySpinner.setOnTouchListener(this);
        
        // load user preferences
        loadUserPreferences();
        initializeLayout();
        initializeScreenDimensions();
    }
    
    private void initializeScreenDimensions() 
    {
    	Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density  = getResources().getDisplayMetrics().density;
        mScreenHeight_dp = outMetrics.heightPixels / density;
        mScreenWidth_dp  = outMetrics.widthPixels / density;
	}
    
    private int getFraction(float percentage, float base)
    {
    	int result = (int)(((base / 100) * percentage) + 0.5f);
    	return result;
    }

	@Override
	public boolean onTouch(View v, MotionEvent motionEvent) 
    {
    	if(motionEvent.getAction() == MotionEvent.ACTION_UP)
		{
			if(mIsSoundEnabled)
        	{
        		playSound(mSndGameButtonPressId);
        	}
		}
		return false;
	}
    
    private int mSpinnerInitCall = 0;
    @Override
	public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) 
    {
    	// workaround, listener is called also during initialization 
    	if(mSpinnerInitCall == 3)
    	{
	    	if(mIsSoundEnabled)
	    	{
	    		playSound(mSndGameButtonPressId);
	    	}
    	}
    	else
    	{
    		mSpinnerInitCall++;
    	}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) 
	{
	}
    
    private void loadUserPreferences()
    {
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	
    	mIsMusicEnabled = settings.getBoolean("music", true);
    	mIsSoundEnabled = settings.getBoolean("sound", true);
        int numberOfPlayers = settings.getInt("numberOfPlayers", 2);
        
        switch(numberOfPlayers)
    	{
    	case 2:
    		mPlayerSpinner.setInitialSelection(0);
    		break;
    	case 3:
    		mPlayerSpinner.setInitialSelection(1);
    		break;
    	case 4:
    		mPlayerSpinner.setInitialSelection(2);
    		break;
    	}
        
        int gameMode = settings.getInt("gameMode", 1);
        switch(gameMode)
    	{
    	case 0:
    		mModeSpinner.setInitialSelection(0);
    		break;
    	case 1:
    		mModeSpinner.setInitialSelection(1);
    		break;
    	}
        
        int difficulty = settings.getInt("difficulty", 0);
        switch(difficulty)
    	{
    	case 0:
    		mDifficultySpinner.setInitialSelection(0);
    		break;
    	case 1:
    		mDifficultySpinner.setInitialSelection(1);
    		break;
    	case 2:
    		mDifficultySpinner.setInitialSelection(2);
    		break;
    	}
    }
    
    // organizes menu buttons according to screen dimensions 
    @SuppressWarnings("deprecation")
	private void initializeLayout()
    {   
    	mScale = getResources().getDisplayMetrics().density;
    	
    	// get screen dimensions
    	Display display = getWindowManager().getDefaultDisplay();
    	int width_px = display.getWidth();
    	int height_px = display.getHeight();
    	
    	// calculate corner button button positions
    	final float originalCornerBtnWidth = 225;
    	final float originalCornerBtnHeight = 140;
    	final int borderOffset = convertDipToPx(2);
    	final int centerOffset = convertDipToPx(2);
    	
    	float btnWidth_px =  (float)width_px / 2 - borderOffset - centerOffset;
    	float scalingFactor = btnWidth_px / originalCornerBtnWidth;
    	
    	int buttonWidth = (int)(btnWidth_px + 0.5f);
    	int buttonHeight = (int)(scalingFactor * originalCornerBtnHeight + 0.5f);
    	
		// place corner buttons
    	ImageButton btnPowercrux = (ImageButton)findViewById(R.id.btnPowercrux);
    	android.widget.AbsoluteLayout.LayoutParams btnPowercruxLP = new android.widget.AbsoluteLayout.LayoutParams(
    			buttonWidth, buttonHeight, borderOffset, borderOffset);
        btnPowercrux.setLayoutParams(btnPowercruxLP);
        
        // help
        ImageButton btnHelp = (ImageButton)findViewById(R.id.btnHelp);
        android.widget.AbsoluteLayout.LayoutParams btnHelpLP = new android.widget.AbsoluteLayout.LayoutParams(
        		buttonWidth, buttonHeight, width_px - buttonWidth - borderOffset,borderOffset);
    	btnHelp.setLayoutParams(btnHelpLP);
        
    	// sound
        mbtnSound = (ImageButton)findViewById(R.id.btnSound);        
        android.widget.AbsoluteLayout.LayoutParams btnSoundLP = new android.widget.AbsoluteLayout.LayoutParams(
    			buttonWidth, buttonHeight, borderOffset, height_px - buttonHeight - borderOffset);
        mbtnSound.setLayoutParams(btnSoundLP);        
        if(mIsSoundEnabled)
        {
        	mbtnSound.setBackgroundResource(R.drawable.btnsoundon_selector);
        }
        else
        {
        	mbtnSound.setBackgroundResource(R.drawable.btnsoundoff_selector);
        }
    	
        // music
    	mbtnMusic = (ImageButton)findViewById(R.id.btnMusic);
    	android.widget.AbsoluteLayout.LayoutParams btnIsmaroikLP = new android.widget.AbsoluteLayout.LayoutParams(
    			buttonWidth, buttonHeight, width_px - buttonWidth - borderOffset, height_px - buttonHeight - borderOffset);
    	mbtnMusic.setLayoutParams(btnIsmaroikLP);
    	if(mIsMusicEnabled)
        {
    		mbtnMusic.setBackgroundResource(R.drawable.btnmusicon_selector);
        }
        else
        {
        	mbtnMusic.setBackgroundResource(R.drawable.btnmusicoff_selector);
        }
    	
    	// place spinners
    	final float originalSpinnerWidth = 400;
    	final float originalSpinnerHeight = 90;
    	final float spinnerDistance_px = convertDipToPx(10);
    	final float spinnerWidth_px =  (float)(width_px - 2*borderOffset);
    	final float spinnerReziseFactor = spinnerWidth_px / originalSpinnerWidth;
    	final float spinnerHeight_px =  originalSpinnerHeight * spinnerReziseFactor;
    	
    	// place players spinner
    	final float originalPlayersSpinnerHeight = 118;
    	float playersSpinnerHeight_px =  originalPlayersSpinnerHeight * spinnerReziseFactor;
    	float playersSpinnerY = 47 * spinnerReziseFactor;
    	android.widget.AbsoluteLayout.LayoutParams playersLP = new android.widget.AbsoluteLayout.LayoutParams(
    			(int)(spinnerWidth_px + 0.5f), (int)(playersSpinnerHeight_px + 0.5f), borderOffset, (int)(playersSpinnerY + 0.5f));
    	mPlayerSpinner.setLayoutParams(playersLP);
    	
    	// place mode spinner
    	float modeSpinnerY = playersSpinnerY + playersSpinnerHeight_px + spinnerDistance_px;
    	android.widget.AbsoluteLayout.LayoutParams modeLP = new android.widget.AbsoluteLayout.LayoutParams(
    			(int)(spinnerWidth_px + 0.5f), (int)(spinnerHeight_px + 0.5f), borderOffset, (int)(modeSpinnerY + 0.5f));
    	mModeSpinner.setLayoutParams(modeLP);
    	
    	// place difficulty spinner
    	float difficultySpinnerY = modeSpinnerY + spinnerHeight_px + spinnerDistance_px;
    	android.widget.AbsoluteLayout.LayoutParams difficultyLP = new android.widget.AbsoluteLayout.LayoutParams(
    			(int)(spinnerWidth_px + 0.5f), (int)(spinnerHeight_px + 0.5f), borderOffset, (int)(difficultySpinnerY + 0.5f));
    	mDifficultySpinner.setLayoutParams(difficultyLP);
    	
    	// place start button
    	final float originalStartBtnHeight = 133;
    	float startBtnHeight_px =  originalStartBtnHeight * spinnerReziseFactor;
    	float btnStartY = height_px - 180 * spinnerReziseFactor;
    	ImageButton btnStart = (ImageButton)findViewById(R.id.btnStart);
    	android.widget.AbsoluteLayout.LayoutParams btnStartLP = new android.widget.AbsoluteLayout.LayoutParams(
    			(int)(spinnerWidth_px + 0.5f), (int)(startBtnHeight_px + 0.5f), borderOffset, (int)(btnStartY + 0.5f));
    	btnStart.setLayoutParams(btnStartLP);
    	
    	// if not enough space, adjust spinner buttons height
    	final float minSpinnerHeight_px = 40;
    	final float availableFreeSpace = btnStartY - playersSpinnerY - playersSpinnerHeight_px;
    	final float requiredSpace_px = 2 * spinnerHeight_px + 3 * spinnerDistance_px;
    	if(requiredSpace_px > availableFreeSpace)
    	{
    		// resize buttons
    		float scaledSpinnerHeight_px = (availableFreeSpace - (3 * spinnerDistance_px)) / 2;
    		if(scaledSpinnerHeight_px < minSpinnerHeight_px)
    		{
    			scaledSpinnerHeight_px = minSpinnerHeight_px;
    		}
    		
    		android.widget.AbsoluteLayout.LayoutParams scaledModeLP = new android.widget.AbsoluteLayout.LayoutParams(
        			(int)(spinnerWidth_px + 0.5f), (int)(scaledSpinnerHeight_px + 0.5f), borderOffset, (int)(modeSpinnerY + 0.5f));
        	mModeSpinner.setLayoutParams(scaledModeLP);
        	
        	difficultySpinnerY = modeSpinnerY + scaledSpinnerHeight_px + spinnerDistance_px;
        	android.widget.AbsoluteLayout.LayoutParams scaledDifficultyLP = new android.widget.AbsoluteLayout.LayoutParams(
        			(int)(spinnerWidth_px + 0.5f), (int)(scaledSpinnerHeight_px + 0.5f), borderOffset, (int)(difficultySpinnerY + 0.5f));
        	mDifficultySpinner.setLayoutParams(scaledDifficultyLP);
    	}
    }      
    
    private int convertDipToPx(int value) {
		return (int) ((float) value * mScale + 0.5f);
	}
	
//	private float convertDipToPx(float value) {
//		return value * mScale;
//	}
    
    @Override
    protected void onPause() {
    	
    	if(mMediaPlayer != null)
    	{
	    	mMediaPlayerPosition = mMediaPlayer.getCurrentPosition();
	    	mMediaPlayer.stop();
    	}
    	
//    	playing in between activities
//    	if (this.isFinishing())
//    	{ //basically BACK was pressed from this activity
//    		mMediaPlayer.stop();
//	        Toast.makeText(MenuActivity.this, "YOU PRESSED BACK FROM YOUR 'HOME/MAIN' ACTIVITY", Toast.LENGTH_SHORT).show();
//    	}
//    	
//    	Context context = getApplicationContext();
//    	ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//    	List<RunningTaskInfo> taskInfo = am.getRunningTasks(1);
//    	if (!taskInfo.isEmpty()) 
//    	{
//    		ComponentName topActivity = taskInfo.get(0).topActivity; 
//    		if (!topActivity.getPackageName().equals(context.getPackageName()))
//	        {
//	        	mMediaPlayer.stop();
//	        	Toast.makeText(MenuActivity.this, "YOU LEFT YOUR APP", Toast.LENGTH_SHORT).show();
//	        }
//	        else 
//	        {
//	        	Toast.makeText(MenuActivity.this, "YOU SWITCHED ACTIVITIES WITHIN YOUR APP", Toast.LENGTH_SHORT).show();
//	        }
//    	}
      
      super.onPause();
    }
    
    @Override
	protected void onResume() {
    	
    	if(mMediaPlayer != null)
		{
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
    	
    	mMediaPlayer = MediaPlayer.create(this, R.raw.dance_zone);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.setVolume(0.7f, 0.7f);
    	try {
			mMediaPlayer.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
		// start music, if enabled
    	if(mIsMusicEnabled)
    	{
	    	if(mMediaPlayerPosition > 0)
	    	{
	    		mMediaPlayer.seekTo(mMediaPlayerPosition);
	    	}
    		mMediaPlayer.start();
    	}
    	
    	((PlayersSpinnerAdapter)mPlayerSpinner.getAdapter()).LoadResouces();
    	((ModeSpinnerAdapter)mModeSpinner.getAdapter()).LoadResouces();
    	((DifficultySpinnerAdapter)mDifficultySpinner.getAdapter()).LoadResouces();
    	
		super.onResume();
	}
	
    @Override
	protected void onStop(){
       super.onStop();
       
       // save arc configuration
       SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
       SharedPreferences.Editor editor = settings.edit();
       
       editor.putBoolean("sound", mIsSoundEnabled);
       editor.putBoolean("music", mIsMusicEnabled);
       
       switch(mPlayerSpinner.getSelectedItemPosition())
   	   {
   	   case 0:
   	    	editor.putInt("numberOfPlayers", 2);
   	    	break;
   	   case 1:
   		   editor.putInt("numberOfPlayers", 3);
   		   break;
   	   case 2:
   		   editor.putInt("numberOfPlayers", 4);
   		   break;
   	   }
       
	   switch(mModeSpinner.getSelectedItemPosition()) {
	   case 0:
		   editor.putInt("gameMode", 0);
		   break;
	   case 1:
		   editor.putInt("gameMode", 1);
		   break;
   		}
	   
	   switch(mDifficultySpinner.getSelectedItemPosition())
   	   {
   	   case 0:
   		   editor.putInt("difficulty", 0);
   		   break;
   	   case 1:
   		   editor.putInt("difficulty", 1);
   		   break;
   	   case 2:
   		   editor.putInt("difficulty", 2);
   		   break;
   	   }

       // Commit the edits!
       editor.commit();
       
       if(mMediaPlayer != null)
	   {
    	   // release media player
	       mMediaPlayer.release();
	   }
    }
    
    public void startGame(View view) 
    {
    	if(mIsSoundEnabled)
    	{
    		playSound(mSndGameButtonPressId);
    	}
    	
    	SpinnerConfig spinnerConfig = new SpinnerConfig();
    	
    	// collect settings
    	spinnerConfig.borderDistance = getFraction(4, mScreenWidth_dp);;
    	spinnerConfig.innerCircleRadius = getFraction(21, mScreenWidth_dp);
    	spinnerConfig.arcsDistance = getFraction(1.8f, mScreenWidth_dp);;
    	spinnerConfig.buttonDistance = getFraction(1.5f, mScreenWidth_dp);
    	spinnerConfig.numberOfArcs = 1;
    	spinnerConfig.sweepAngleMin = 10;
    	spinnerConfig.sweepAngleMax = 20;
    	spinnerConfig.velocityIncrementStep = 20;
    	spinnerConfig.velocityScaleParameter_B = 1600;
    	spinnerConfig.velocityScaleParameter_C = 50;
    	spinnerConfig.maxArcWidth = getFraction(15, mScreenWidth_dp);
    	spinnerConfig.scoreBarHeight = getFraction(7, mScreenHeight_dp);
    	spinnerConfig.borderWidth = getFraction(0.5f, mScreenWidth_dp);
    	spinnerConfig.bigTextSize = getFraction(9.5f, mScreenWidth_dp); 		//31
    	spinnerConfig.smallTextSize = getFraction(2.8f, mScreenWidth_dp); 		// 9;
    	spinnerConfig.currentRoundTextSize = getFraction(5f, mScreenWidth_dp); 	//17;

    	GameModeConfig gameModeConfig = new GameModeConfig();
    	switch(mPlayerSpinner.getSelectedItemPosition())
    	{
    	case 0:
    		gameModeConfig.numberOfPlayers = 2;
    		break;
    	case 1:
    		gameModeConfig.numberOfPlayers = 3;
    		break;
    	case 2:
    		gameModeConfig.numberOfPlayers = 4;
    		break;
    	}
    	
    	switch(mModeSpinner.getSelectedItemPosition())
    	{
    	case 0:
    		gameModeConfig.gameMode = 1;
    		break;
    	case 1:
    		gameModeConfig.gameMode = 0;
    		break;
    	}
    	
    	switch(mDifficultySpinner.getSelectedItemPosition())
    	{
    	case 0:
    		gameModeConfig.difficulty = 0;
    		break;
    	case 1:
    		gameModeConfig.difficulty = 1;
    		break;
    	case 2:
    		gameModeConfig.difficulty = 2;
    		break;
    	}
    	
    	gameModeConfig.playerPenalty = 40;
		gameModeConfig.hitIt_TotalScore = 400;//500
		gameModeConfig.coolIt_TotalPoints = 1200;//1200
		gameModeConfig.isSoundEnabled = mIsSoundEnabled;
		gameModeConfig.isMusicEnabled = mIsMusicEnabled;
    	
    	// start playground activity
    	mIntent.putExtra(SPINNERCONFIG, spinnerConfig);
    	mIntent.putExtra(GAMEMODECONFIG, gameModeConfig);
    	
    	((PlayersSpinnerAdapter)mPlayerSpinner.getAdapter()).FreeResources();
    	((ModeSpinnerAdapter)mModeSpinner.getAdapter()).FreeResources();
    	((DifficultySpinnerAdapter)mDifficultySpinner.getAdapter()).FreeResources();
    	
    	System.gc();
    	
    	startActivity(mIntent);
    }
    
    public void btnSound(View view) 
    {        	
    	// toggle button state
    	if(mIsSoundEnabled)    		
    	{
    		mbtnSound.setBackgroundResource(R.drawable.btnsoundoff_selector);
    		mIsSoundEnabled = false;
    	}
    	else
    	{
    		playSound(mSndGameButtonPressId);
    		mbtnSound.setBackgroundResource(R.drawable.btnsoundon_selector);
    		mIsSoundEnabled = true;
    	}
    }
    
    public void btnMusic(View view) 
    {
    	if(mIsSoundEnabled)
    	{
    		playSound(mSndGameButtonPressId);
    	}
    	
    	// toggle button state
    	if(mIsMusicEnabled)
    	{
    		mbtnMusic.setBackgroundResource(R.drawable.btnmusicoff_selector);
    		mIsMusicEnabled = false;
    		
    		// pause music
    		mMediaPlayer.pause();
    	}
    	else
    	{    		
    		mbtnMusic.setBackgroundResource(R.drawable.btnmusicon_selector);
    		mIsMusicEnabled = true;
    		
    		// start music
    		mMediaPlayer.start();
    	}
    }
    
    public void btnPowercrux(View view) 
    {
    	if(mIsSoundEnabled)
    	{
    		playSound(mSndGameButtonPressId);
    	}
    	
    	FragmentManager fm = getSupportFragmentManager();
    	RedirectToIsmaroikWebDialogFragment redirectToIsmaroikWebDialogFragment = 
    			new RedirectToIsmaroikWebDialogFragment();
    	redirectToIsmaroikWebDialogFragment.show(fm, "fragment_ismaroik_url");
    }
    
    public void btnHelp(View view) 
    {
    	if(mIsSoundEnabled)
    	{
    		playSound(mSndGameButtonPressId);
    	}
    	
    	System.gc();

    	FragmentManager fm = getSupportFragmentManager();
    	HelpDialogFragment helpDialogFragment = new HelpDialogFragment();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, helpDialogFragment)
                   .addToBackStack(null).commit();
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
		if (keyCode == KeyEvent.KEYCODE_MENU){
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
}
