package com.ismaroik.powercrux;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

// renders spinning circles based on supplied parameters 
public class Spinner extends SurfaceView implements Runnable
{    
	// =========INPUT PARAMETERS START===========
	// rendering parameters
	private int mArcBorderDistance_dip = 25; 	// distance of largest spin from border of device screen in (dip) -> must be at least 2*mButtonDistance_dip
	private int mInnerCircleRadius_dip = 50;			// radius of inner circle around which arcs are stacked (dip)
	private int mArcsDistance_dip = 10;			// distance between arcs in (dip)
	private int mNumberOfArcs = 3;						// number of rendered arcs, available space is equally divided between arcs
	private int mSweepAngleMin = 270;					// sweep angle for arcs is random, between min. and max. values (degrees)	
	private int mSweepAngleMax = 320;
	private int mVelocityIncrementStep = 20;		// step by which velocities of arcs are incremented as they are closer to inner circle (degrees/second)
		
	// angular velocity = (B/elapsedTime) + C
	private int mB = 2000;
	private int mC = 50;
	
	private SpinnerConfig mSpinnerConfiguration;
	
	private int mButtonDistance_dip = 10;			// distance of button from border, same distance is from center of screen
	private int mScoreBarHeight_dip = 30;					// height of score bar drawn on buttons
	private int mMaxArcWidth_dip = 25;
	private float mBordersWidth_px;	//width of all borders (buttons, circles, etc.)
	private int mBigTextSize_dip = 31;
	private int mSmallTextSize_dip = 9;
	
	// =========INPUT PARAMETERS END=============
	final private int mMaxPlayerCount = 4;	// max number of players, each of the players can be disabled or enabled per round
	
	private float[] mPlayerScores;			// current scores for all players in percents, values above 100 are shown as %100
											// remainder is no. of circles above score bar
	private int[] mDisplayScores;			// score figures which are displayed for each player
	private int mCenterCircleRadius;		// relative radius of center circle from 0 to 100%
	
	SpinnerResultConfig mCurrentSpinnerResultConfig;
	Path[] mCurrentItersects;
	
	private int mParentWidth_px;
	private int mParentHeight_px;
	private int mScreenCenterX_px;
	private int mScreenCenterY_px;
	private float mScale;					// scale according to devices screen density
	private int mOuterRadius_dip;			// radius of outer border of biggest arc
//	private float mArcWidth_px;	// width of each arc calculated by initializeArcDimensions method

	private RectF[] mArcRects;				// rectangle objects for each arc - from smallest to biggest arc
	private int[] mSweepAngles;				// sweep angle for each arc - from smallest to biggest arc (degree)
	private float[] mAngularVelocities; 	// current angular velocities for each arc (degrees/second)
	private float[] mStartAngles;				// starting angle for each arc - from smallest to biggest arc (degree)

	private int mSuffixFrames = 1;		// number of frames which are rendered after stop have been requested (without further moving spinner) 
										// there are two buffers for rendering canvas, if not both of them are same after stopping, flickering effect might apear
										// and state variables will not correspond with screen status
	
	private long mTimeStart_ms;	 		// time stamp when spinning started (milliseconds)
	private long mTimeLastUpdate_ms;   	// time stamp from last update of arcs position
	
	private Path mBtnTopRight;
	private Path mBtnTopLeft;
	private Path mBtnBottomLeft;
	private Path mBtnBottomRight;
	
	private Paint mArcPaint;
	private Bitmap mBackground;
	private Bitmap mBackgroundImage;
	private SurfaceHolder m_SurfaceHolder;
	
	private Thread mWorkerThread = null;
	private boolean mIsRunning = false;
	private long mNumberOfRenderdFrames;			// total number of rendered frames
	
	private boolean mOneTimeInitDone = false;
	
	private float mPlayerDscWidth_px;	
	
	private ArcsIntersectAnalyzer arcsIntersectAnalyzer = new ArcsIntersectAnalyzer(); // contains math to analyze results
	
	public Spinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initConstructor();
	}

	public Spinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initConstructor();
	}

	public Spinner(Context context) 
	{
		super(context);
		
		initConstructor();
	}
	
	private void initConstructor()
	{
		mCurrentSpinnerResultConfig = null;
		mPlayerScores = new float[mMaxPlayerCount];
		mDisplayScores = new int[mMaxPlayerCount];
		
		for(int i = 0; i < mPlayerScores.length; i++)
		{
			mPlayerScores[i] = 0;
			mDisplayScores[i] = 0;
		}
		mCenterCircleRadius = 100;
		
		mScale = getResources().getDisplayMetrics().density;
	}
	
	public void setSpinnerConfig(SpinnerConfig spinnerConfig) {
		
		mSpinnerConfiguration = spinnerConfig;
		
		// set member variables according to configuration
		mArcBorderDistance_dip = spinnerConfig.borderDistance;
		mInnerCircleRadius_dip = spinnerConfig.innerCircleRadius;
		mArcsDistance_dip = spinnerConfig.arcsDistance;
		mButtonDistance_dip = spinnerConfig.buttonDistance;
		mNumberOfArcs = spinnerConfig.numberOfArcs;
		mSweepAngleMin = spinnerConfig.sweepAngleMin;
		mSweepAngleMax = spinnerConfig.sweepAngleMax;
		mVelocityIncrementStep = spinnerConfig.velocityIncrementStep;
		mB = spinnerConfig.velocityScaleParameter_B;
		mC = spinnerConfig.velocityScaleParameter_C;
		mMaxArcWidth_dip = spinnerConfig.maxArcWidth;
		mScoreBarHeight_dip = spinnerConfig.scoreBarHeight;
		mBordersWidth_px = convertDipToPx(spinnerConfig.borderWidth);
		mBigTextSize_dip = spinnerConfig.bigTextSize;
		mSmallTextSize_dip = spinnerConfig.smallTextSize;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

	    mParentWidth_px = MeasureSpec.getSize(widthMeasureSpec);
	    mParentHeight_px = MeasureSpec.getSize(heightMeasureSpec);
	    
	    mScreenCenterX_px = (int)((float)mParentWidth_px / 2 + 0.5f);
		mScreenCenterY_px = (int)((float)mParentHeight_px / 2 + 0.5f);
		
	    this.setMeasuredDimension(
	    		mParentWidth_px, mParentHeight_px);
	}
	
	public Point getCenterCoordinates()
	{
		return new Point(mScreenCenterX_px, mScreenCenterY_px);
	}
	
	// initializes spinner based on current member variables, has to be called after mScreenCenterX_px, mScreenCenterY_px have been initialized
	private void initialize() {
		
		// check if we have center of the screen
		if(mScreenCenterX_px == 0 || mScreenCenterY_px == 0){
			throw new RuntimeException("Screen dimensions have not been initialized!");
		}
		
		if(!mOneTimeInitDone)
		{
			// load & scale background image
	 		BitmapFactory.Options options = new BitmapFactory.Options();
	 		options.inPurgeable = true;
	 		mBackgroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.background, options);
	 		mBackgroundImage = Bitmap.createScaledBitmap(
	 				mBackgroundImage,
	 				mParentWidth_px, 
	 				mParentHeight_px, 
	 				true);
	 		
	 		m_SurfaceHolder = getHolder();
	 		
			calculateOuterRadius_dip();
			createButtonPaths();
	 		
	 		mOneTimeInitDone = true;
		}
		
		// initialize based on render parameters
		mArcRects = new RectF[mNumberOfArcs];
		mSweepAngles = new int[mNumberOfArcs];
		mAngularVelocities = new float[mNumberOfArcs];
		mStartAngles = new float[mNumberOfArcs];
		
		initializeArcDimensions();
		drawUnderlyingCanvas(true);
	}
	
	// creates button path objects and stores them into member variables
	private void createButtonPaths()
	{
		final float buttonFromBorderDistance_px = this.convertDipToPx(mButtonDistance_dip);
		final float outerArcRadius_px = this.convertDipToPx(mOuterRadius_dip);
		final float btnDistanceFromCenterAxis_px = buttonFromBorderDistance_px;
		final float cornerArcRadius = buttonFromBorderDistance_px * 1.5f;
		
		// button arc sweep angle
		double btnBaseArcStartAngle_rad = Math.asin((double)btnDistanceFromCenterAxis_px / (double)outerArcRadius_px);
		float btnBaseArcStartAngle = (float)Math.toDegrees(btnBaseArcStartAngle_rad);
		float btnArcSweepAngle  = 90-2*btnBaseArcStartAngle;
		RectF btnArcRectF = new RectF(mScreenCenterX_px - outerArcRadius_px, mScreenCenterY_px - outerArcRadius_px, mScreenCenterX_px + outerArcRadius_px, mScreenCenterY_px + outerArcRadius_px);
				
		float arcCoordinateOffset = outerArcRadius_px - (float)(outerArcRadius_px*(Math.cos(btnBaseArcStartAngle_rad)));
		float btnTopOfArc_Y_px = mScreenCenterY_px - outerArcRadius_px + arcCoordinateOffset;
		float btnBottomOfArc_Y_px = mScreenCenterY_px + outerArcRadius_px + arcCoordinateOffset;
		float btnRightOfArc_X_px = mScreenCenterX_px + outerArcRadius_px + arcCoordinateOffset;
		float btnLeftOfArc_X_px = mScreenCenterX_px - outerArcRadius_px + arcCoordinateOffset;
		
		float centerArcRadius = cornerArcRadius;
		if(cornerArcRadius > (outerArcRadius_px - buttonFromBorderDistance_px + arcCoordinateOffset))
		{
			centerArcRadius = outerArcRadius_px - buttonFromBorderDistance_px + arcCoordinateOffset;
		}
				
		// top right
		RectF topRightCornerRect = new RectF(mScreenCenterX_px + btnDistanceFromCenterAxis_px, buttonFromBorderDistance_px, 
				mScreenCenterX_px + btnDistanceFromCenterAxis_px + cornerArcRadius*2, buttonFromBorderDistance_px + cornerArcRadius*2);
		RectF topRightCenterRect = new RectF(mParentWidth_px - buttonFromBorderDistance_px - centerArcRadius*2, mScreenCenterY_px - buttonFromBorderDistance_px - centerArcRadius*2, 
				mParentWidth_px - buttonFromBorderDistance_px, mScreenCenterY_px - buttonFromBorderDistance_px);
		
		mBtnTopRight = new Path();
		mBtnTopRight.moveTo(mParentWidth_px - buttonFromBorderDistance_px, mScreenCenterY_px - btnDistanceFromCenterAxis_px - centerArcRadius);
		mBtnTopRight.lineTo(mParentWidth_px - buttonFromBorderDistance_px, buttonFromBorderDistance_px);
		mBtnTopRight.lineTo(mScreenCenterX_px + btnDistanceFromCenterAxis_px + cornerArcRadius, buttonFromBorderDistance_px);
		mBtnTopRight.arcTo(topRightCornerRect,270,-90);
		mBtnTopRight.lineTo(mScreenCenterX_px + btnDistanceFromCenterAxis_px, btnTopOfArc_Y_px);
		mBtnTopRight.arcTo(
				btnArcRectF,
				270 + btnBaseArcStartAngle, 
				btnArcSweepAngle);					
		mBtnTopRight.lineTo(mParentWidth_px - buttonFromBorderDistance_px - centerArcRadius, mScreenCenterY_px - btnDistanceFromCenterAxis_px);
		mBtnTopRight.arcTo(topRightCenterRect, 90, -90);
		mBtnTopRight.close();
		
		// top left
		RectF topLeftCornerRect = new RectF(mScreenCenterX_px - btnDistanceFromCenterAxis_px - cornerArcRadius*2, buttonFromBorderDistance_px, 
				mScreenCenterX_px - btnDistanceFromCenterAxis_px, buttonFromBorderDistance_px + cornerArcRadius*2);
		RectF topLeftCenterRect = new RectF(buttonFromBorderDistance_px, mScreenCenterY_px - buttonFromBorderDistance_px - centerArcRadius*2, 
				buttonFromBorderDistance_px + centerArcRadius*2, mScreenCenterY_px - buttonFromBorderDistance_px);
		
		mBtnTopLeft = new Path();
		mBtnTopLeft.moveTo(mScreenCenterX_px - btnDistanceFromCenterAxis_px, btnTopOfArc_Y_px);
		mBtnTopLeft.lineTo(mScreenCenterX_px - btnDistanceFromCenterAxis_px, buttonFromBorderDistance_px + cornerArcRadius);
		mBtnTopLeft.arcTo(topLeftCornerRect,0,-90);
		mBtnTopLeft.lineTo(buttonFromBorderDistance_px, buttonFromBorderDistance_px);
		mBtnTopLeft.lineTo(buttonFromBorderDistance_px, mScreenCenterY_px - btnDistanceFromCenterAxis_px - cornerArcRadius);
		mBtnTopLeft.arcTo(topLeftCenterRect,180,-90);
		mBtnTopLeft.lineTo(btnLeftOfArc_X_px, mScreenCenterY_px - btnDistanceFromCenterAxis_px);
		mBtnTopLeft.arcTo(
				btnArcRectF,
				180 + btnBaseArcStartAngle, 
				btnArcSweepAngle);
		mBtnTopLeft.lineTo(mScreenCenterX_px - btnDistanceFromCenterAxis_px, btnTopOfArc_Y_px);
		mBtnTopLeft.close();
		
		// bottom left
		RectF bottomLeftCornerRect = new RectF(mScreenCenterX_px - btnDistanceFromCenterAxis_px - cornerArcRadius*2, mParentHeight_px - buttonFromBorderDistance_px - cornerArcRadius*2, 
				mScreenCenterX_px - btnDistanceFromCenterAxis_px, mParentHeight_px - buttonFromBorderDistance_px);
		RectF bottomLeftCenterRect = new RectF(buttonFromBorderDistance_px, mScreenCenterY_px + buttonFromBorderDistance_px, 
				buttonFromBorderDistance_px + centerArcRadius*2, mScreenCenterY_px + buttonFromBorderDistance_px + centerArcRadius*2);
		
		mBtnBottomLeft = new Path();
		mBtnBottomLeft.moveTo(btnLeftOfArc_X_px, mScreenCenterY_px + btnDistanceFromCenterAxis_px);
		mBtnBottomLeft.lineTo(buttonFromBorderDistance_px + centerArcRadius, mScreenCenterY_px + btnDistanceFromCenterAxis_px);
		mBtnBottomLeft.arcTo(bottomLeftCenterRect,270,-90);
		mBtnBottomLeft.lineTo(buttonFromBorderDistance_px, mParentHeight_px - buttonFromBorderDistance_px);
		mBtnBottomLeft.lineTo(mScreenCenterX_px - btnDistanceFromCenterAxis_px - cornerArcRadius, mParentHeight_px - buttonFromBorderDistance_px);
		mBtnBottomLeft.arcTo(bottomLeftCornerRect,90,-90);
		mBtnBottomLeft.lineTo(mScreenCenterX_px - btnDistanceFromCenterAxis_px, btnBottomOfArc_Y_px);
		mBtnBottomLeft.arcTo(
				btnArcRectF,
				90 + btnBaseArcStartAngle, 
				btnArcSweepAngle);
		mBtnBottomLeft.lineTo(btnLeftOfArc_X_px, mScreenCenterY_px + btnDistanceFromCenterAxis_px);
		mBtnBottomLeft.close();
		
		// bottom right
		RectF bottomRightCornerRect = new RectF(mScreenCenterX_px + btnDistanceFromCenterAxis_px, mParentHeight_px - buttonFromBorderDistance_px - cornerArcRadius*2, 
				mScreenCenterX_px + btnDistanceFromCenterAxis_px + cornerArcRadius*2, mParentHeight_px - buttonFromBorderDistance_px);
		RectF bottomRightCenterRect = new RectF(mParentWidth_px - buttonFromBorderDistance_px - centerArcRadius*2, mScreenCenterY_px + buttonFromBorderDistance_px, 
				mParentWidth_px - buttonFromBorderDistance_px, mScreenCenterY_px + buttonFromBorderDistance_px + centerArcRadius*2);
		
		mBtnBottomRight = new Path();
		mBtnBottomRight.moveTo(mScreenCenterX_px + btnDistanceFromCenterAxis_px, mParentHeight_px - buttonFromBorderDistance_px - cornerArcRadius);
		mBtnBottomRight.arcTo(bottomRightCornerRect,180,-90);
		mBtnBottomRight.lineTo(mParentWidth_px - buttonFromBorderDistance_px, mParentHeight_px - buttonFromBorderDistance_px);
		mBtnBottomRight.lineTo(mParentWidth_px - buttonFromBorderDistance_px, mScreenCenterY_px + btnDistanceFromCenterAxis_px + centerArcRadius);
		mBtnBottomRight.arcTo(bottomRightCenterRect,0,-90);
		mBtnBottomRight.lineTo(btnRightOfArc_X_px, mScreenCenterY_px + btnDistanceFromCenterAxis_px);
		mBtnBottomRight.arcTo(
				btnArcRectF,
				btnBaseArcStartAngle, 
				btnArcSweepAngle);
		mBtnBottomRight.lineTo(mScreenCenterX_px + btnDistanceFromCenterAxis_px, mParentHeight_px - buttonFromBorderDistance_px - cornerArcRadius);	
		mBtnBottomRight.close();
	}
	
	// draws onto background image everything except arcs
	private void drawUnderlyingCanvas(boolean isInitializig)
	{
		// on the bottom put background image
		if(mBackground != null)
		{
			mBackground.recycle();
		}
		
		System.gc();
		
		mBackground = 
				Bitmap.createBitmap(mBackgroundImage);
		
		Canvas canvas = new Canvas(mBackground);
		
		drawPlayerAreaMarkers(canvas);
		drawCenterCircle(canvas);
		drawButtonBodies(canvas);

		if(isInitializig)
		{
			// draw current scores 
			for(int i = 0; i < mMaxPlayerCount; i++)
			{
				if(!mSpinnerConfiguration.disabledPlayers[i])
				{
					drawScore(i, mPlayerScores[i], mDisplayScores[i], canvas);
				}
			}
			drawButtonBorders(canvas);
//			drawDisabledPlayers(canvas);
		}
	}
	
	private void drawPlayerAreaMarkers(Canvas canvas) 
	{
		final float triangleBorderWidth_px = mBordersWidth_px/2;
		Paint trianglePaint = new Paint();
		trianglePaint.setAntiAlias(true);	
		trianglePaint.setDither(true); 
		trianglePaint.setColor(Color.RED);
		trianglePaint.setStyle(Paint.Style.FILL);
		trianglePaint.setStrokeWidth(triangleBorderWidth_px);
		trianglePaint.setStrokeJoin(Paint.Join.ROUND);
		trianglePaint.setStrokeCap(Paint.Cap.ROUND);
		
		final float triangleBaseHalf = this.convertDipToPx(mButtonDistance_dip) * 1.3f;
		final float triangleHeight = (float)(triangleBaseHalf * Math.tan(Math.toRadians(50)));
		
		// bottom
		final float triangleBottomBaseY = mParentHeight_px - triangleBorderWidth_px/2;
		Path triangleBottom = new Path();
		triangleBottom.moveTo(mScreenCenterX_px - triangleBaseHalf, triangleBottomBaseY);
		triangleBottom.lineTo(mScreenCenterX_px + triangleBaseHalf, triangleBottomBaseY);
		triangleBottom.lineTo(mScreenCenterX_px, triangleBottomBaseY - triangleHeight);
		triangleBottom.lineTo(mScreenCenterX_px - triangleBaseHalf, triangleBottomBaseY);
		
		// top
		final float triangleTopBaseY = triangleBorderWidth_px/2;
		Path triangleTop = new Path();
		triangleTop.moveTo(mScreenCenterX_px - triangleBaseHalf, triangleTopBaseY);
		triangleTop.lineTo(mScreenCenterX_px + triangleBaseHalf, triangleTopBaseY);
		triangleTop.lineTo(mScreenCenterX_px, triangleHeight);
		triangleTop.lineTo(mScreenCenterX_px - triangleBaseHalf, triangleTopBaseY);
		
		// left
		final float triangleLeftBaseX = triangleBorderWidth_px/2;
		Path triangleLeft = new Path();
		triangleTop.moveTo(triangleLeftBaseX, mScreenCenterY_px - triangleBaseHalf);
		triangleTop.lineTo(triangleLeftBaseX, mScreenCenterY_px + triangleBaseHalf);
		triangleTop.lineTo(triangleHeight, mScreenCenterY_px);
		triangleTop.lineTo(triangleLeftBaseX, mScreenCenterY_px - triangleBaseHalf);
		
		// right
		final float triangleRightBaseX = mParentWidth_px - triangleBorderWidth_px/2;
		Path triangleRight = new Path();
		triangleTop.moveTo(triangleRightBaseX, mScreenCenterY_px - triangleBaseHalf);
		triangleTop.lineTo(triangleRightBaseX, mScreenCenterY_px + triangleBaseHalf);
		triangleTop.lineTo(mParentWidth_px - triangleHeight, mScreenCenterY_px);
		triangleTop.lineTo(triangleRightBaseX, mScreenCenterY_px - triangleBaseHalf);
		
		// fill
		canvas.drawPath(triangleBottom, trianglePaint);
		canvas.drawPath(triangleTop, trianglePaint);
		canvas.drawPath(triangleLeft, trianglePaint);
		canvas.drawPath(triangleRight, trianglePaint);
		
		// border
		trianglePaint.setColor(Color.rgb(255, 255, 255));
		trianglePaint.setStyle(Paint.Style.STROKE);
		canvas.drawPath(triangleBottom, trianglePaint);
		canvas.drawPath(triangleTop, trianglePaint);
		canvas.drawPath(triangleLeft, trianglePaint);
		canvas.drawPath(triangleRight, trianglePaint);
		
		// marker line
		float dashLenght = 0.7f * triangleBaseHalf;
		Paint markerLinePaint = new Paint();
		markerLinePaint.setAntiAlias(true);		
		markerLinePaint.setColor(Color.rgb(255, 255, 255));
		markerLinePaint.setStrokeWidth(triangleBorderWidth_px);
		markerLinePaint.setStrokeCap(Paint.Cap.BUTT);
		markerLinePaint.setStyle(Paint.Style.STROKE);
		markerLinePaint.setPathEffect(new DashPathEffect(new float[] {dashLenght,dashLenght*0.6f}, 0));
		
		canvas.drawLine(
				mScreenCenterX_px, triangleHeight, 
				mScreenCenterX_px, mParentHeight_px - triangleHeight, 
				markerLinePaint);
		
		canvas.drawLine(
				triangleHeight, mScreenCenterY_px, 
				mParentWidth_px - triangleHeight, mScreenCenterY_px, 
				markerLinePaint);
	}

	private void drawCenterCircle(Canvas canvas)
	{
		float circleRadius_px = this.convertDipToPx(mInnerCircleRadius_dip) - mBordersWidth_px/2;
		
		// draw fill
		Paint circleBorderPaint = new Paint();
		circleBorderPaint.setAntiAlias(true);
		circleBorderPaint.setColor(Color.rgb(125, 125, 113));
		circleBorderPaint.setAlpha(230);
		circleBorderPaint.setStrokeWidth(4);
		circleBorderPaint.setStrokeCap(Paint.Cap.BUTT);
		circleBorderPaint.setStyle(Paint.Style.FILL);
		canvas.drawCircle(mScreenCenterX_px, mScreenCenterY_px, circleRadius_px, circleBorderPaint);
		
		// draw inner fill
		// calculate radius of fill based on member variable
		final float actualRadius = (int)((circleRadius_px * ((float)mCenterCircleRadius / 100)) + 0.5f);
		
		circleBorderPaint.setAlpha(255);
		circleBorderPaint.setStyle(Paint.Style.FILL);
		circleBorderPaint.setColor(Color.rgb(57, 75, 210));
		canvas.drawCircle(mScreenCenterX_px, mScreenCenterY_px, actualRadius, circleBorderPaint);
		
		// draw border
		circleBorderPaint.setAlpha(255);
		circleBorderPaint.setColor(Color.BLACK);
		circleBorderPaint.setStrokeWidth(mBordersWidth_px);
		circleBorderPaint.setStrokeCap(Paint.Cap.BUTT);
		circleBorderPaint.setStyle(Paint.Style.STROKE);
		canvas.drawCircle(mScreenCenterX_px, mScreenCenterY_px, circleRadius_px, circleBorderPaint);
		
		drawCenterCircleText(canvas, circleRadius_px);
	}
	
	private void drawCenterCircleText(Canvas canvas, float boundaryCircleRadius_px) 
	{	
		Paint centerTextPaint = new Paint();
		centerTextPaint.setAntiAlias(true);
		centerTextPaint.setStrokeWidth(2);
		centerTextPaint.setStrokeCap(Paint.Cap.BUTT);
		centerTextPaint.setStyle(Paint.Style.FILL);
		centerTextPaint.setColor(Color.WHITE);
		centerTextPaint.setTextSize(convertDipToPx(mSmallTextSize_dip));

		// draw center text
		final float offsetFromCenter_px = mBordersWidth_px * 2;
		Rect centerTextRect = new Rect();
		centerTextPaint.getTextBounds(mSpinnerConfiguration.centerText, 0, mSpinnerConfiguration.centerText.length(), centerTextRect);
		canvas.drawText(mSpinnerConfiguration.centerText, 
				mScreenCenterX_px - centerTextRect.exactCenterX(), 
				mScreenCenterY_px + offsetFromCenter_px + centerTextRect.height(), 
				centerTextPaint);
		canvas.save();
		canvas.rotate(-180, 
				mScreenCenterX_px, 
				mScreenCenterY_px);
		canvas.drawText(mSpinnerConfiguration.centerText, 
				mScreenCenterX_px - centerTextRect.exactCenterX(), 
				mScreenCenterY_px + offsetFromCenter_px + centerTextRect.height(),
				centerTextPaint);
		canvas.restore();
		
		// draw center number (in degrees)
		final float offsetBetweenTexts = convertDipToPx(7.5f);
		centerTextPaint.setTextSize(convertDipToPx(mBigTextSize_dip));
		
		String numberString = String.valueOf(mSpinnerConfiguration.centerNumber);
		numberString += (char) 0x00B0;	// degrees symbol
		
		Rect centerNumberRect = new Rect();
		centerTextPaint.getTextBounds(numberString, 0, numberString.length(), centerNumberRect);
		canvas.drawText(numberString, 
				mScreenCenterX_px - centerNumberRect.exactCenterX(), 
				mScreenCenterY_px + offsetFromCenter_px + centerTextRect.height() + offsetBetweenTexts + centerNumberRect.height(), 
				centerTextPaint);
		canvas.save();
		canvas.rotate(-180, 
				mScreenCenterX_px, 
				mScreenCenterY_px);
		canvas.drawText(numberString, 
				mScreenCenterX_px - centerNumberRect.exactCenterX(), 
				mScreenCenterY_px + offsetFromCenter_px + centerTextRect.height() + offsetBetweenTexts + centerNumberRect.height(),
				centerTextPaint);
		canvas.restore();
		
		// draw center line
		centerTextPaint.setStrokeWidth(mBordersWidth_px);
		final int borderCircleOffset_px = (int)(boundaryCircleRadius_px * 0.1f + 0.5f);
		canvas.drawLine(mScreenCenterX_px - boundaryCircleRadius_px + borderCircleOffset_px, mScreenCenterY_px, 
				mScreenCenterX_px + boundaryCircleRadius_px - borderCircleOffset_px, mScreenCenterY_px, centerTextPaint);
		
	}

	// draws buttons onto background image
	private void drawButtonBodies(Canvas canvas) {
				
		// button paint
		Paint buttonPaint = new Paint();
		buttonPaint.setAntiAlias(true);		
		
		buttonPaint.setStrokeWidth(2);
		buttonPaint.setStrokeCap(Paint.Cap.BUTT);
		buttonPaint.setStyle(Paint.Style.FILL);
		
		// top left
		if(mSpinnerConfiguration.disabledPlayers[0])
		{
			buttonPaint.setColor(Color.argb(170, 40, 40, 40));
		}
		else if(mCurrentSpinnerResultConfig != null && mCurrentSpinnerResultConfig.totalPlayerScores[0] >= 100)
		{
			buttonPaint.setColor(Color.argb(255, 0,252,240));
		}
		else
		{
			buttonPaint.setColor(Color.argb(100, 0,252,240));
		}				
		canvas.drawPath(mBtnTopLeft, buttonPaint);
		
		// top right
		if(mSpinnerConfiguration.disabledPlayers[1])
		{
			buttonPaint.setColor(Color.argb(170, 40, 40, 40));
		}
		else if(mCurrentSpinnerResultConfig != null && mCurrentSpinnerResultConfig.totalPlayerScores[1] >= 100)
		{
			buttonPaint.setColor(Color.argb(255, 255, 30, 0));
		}
		else
		{
			buttonPaint.setColor(Color.argb(100, 255, 30, 0));
		}
		canvas.drawPath(mBtnTopRight, buttonPaint);
		
		// bottom left
		if(mSpinnerConfiguration.disabledPlayers[2])
		{
			buttonPaint.setColor(Color.argb(170, 40, 40, 40));
		}
		else if(mCurrentSpinnerResultConfig != null && mCurrentSpinnerResultConfig.totalPlayerScores[2] >= 100)
		{
			buttonPaint.setColor(Color.argb(255, 185,133,27));
		}
		else
		{
			buttonPaint.setColor(Color.argb(100, 185,133,27));
		}
		canvas.drawPath(mBtnBottomLeft, buttonPaint);
		
		// bottom right
		if(mSpinnerConfiguration.disabledPlayers[3])
		{
			buttonPaint.setColor(Color.argb(170, 40, 40, 40));
		}
		else if(mCurrentSpinnerResultConfig != null && mCurrentSpinnerResultConfig.totalPlayerScores[3] >= 100)
		{
			buttonPaint.setColor(Color.argb(255, 255,0,255));
		}
		else
		{
			buttonPaint.setColor(Color.argb(100, 255,0,255));
		}
		canvas.drawPath(mBtnBottomRight, buttonPaint);
	}
	
	private void drawButtonBorders(Canvas canvas)
	{
		mPlayerDscWidth_px = (mScreenCenterX_px - 2 * this.convertDipToPx(mButtonDistance_dip)) * 0.10f;
		drawScoreBarGuidlines(canvas, mPlayerDscWidth_px);
		
		// border
		Paint buttonPaint = new Paint();
		buttonPaint.setColor(Color.BLACK);
		buttonPaint.setStrokeWidth(mBordersWidth_px);
		buttonPaint.setAlpha(255);
		buttonPaint.setStrokeCap(Paint.Cap.ROUND);
		buttonPaint.setStrokeJoin(Paint.Join.ROUND);
		buttonPaint.setStyle(Paint.Style.STROKE);
		
		drawButtonSegments(canvas, buttonPaint, mPlayerDscWidth_px);
		
		canvas.drawPath(mBtnTopLeft, buttonPaint);
		canvas.drawPath(mBtnTopRight, buttonPaint);		
		canvas.drawPath(mBtnBottomLeft, buttonPaint);		
		canvas.drawPath(mBtnBottomRight, buttonPaint);
	}
	
	private void drawButtonSegments(Canvas canvas, Paint linePaint, float playerDscWidth_px) 
	{
		final int buttonFromArcDistance_px = this.convertDipToPx(mButtonDistance_dip);
		final int scoreBarHeight_px = convertDipToPx(mScoreBarHeight_dip);
		final int btnDistanceFromCenterAxis_px = buttonFromArcDistance_px;
		
		final float playerDstFromCenterAxis_px = btnDistanceFromCenterAxis_px + playerDscWidth_px;
		final float outerRadius_px = this.convertDipToPx(mOuterRadius_dip);
		final float topOfOuterCircleY_px = mScreenCenterY_px - outerRadius_px;		
		final float lineLenght = topOfOuterCircleY_px + outerRadius_px - (float)Math.sqrt(outerRadius_px * outerRadius_px - playerDstFromCenterAxis_px * playerDstFromCenterAxis_px);
		
		final float textRectCenterY = (topOfOuterCircleY_px - buttonFromArcDistance_px)/2 + buttonFromArcDistance_px;
		final float textLeftRectCenterX = mScreenCenterX_px - playerDscWidth_px/2 - btnDistanceFromCenterAxis_px;
		final float textRightRectCenterX = mScreenCenterX_px + playerDscWidth_px/2 + btnDistanceFromCenterAxis_px;
		
		Paint descriptionTextPaint = new Paint();
		descriptionTextPaint.setAntiAlias(true);
		descriptionTextPaint.setStrokeWidth(2);
		descriptionTextPaint.setStrokeCap(Paint.Cap.BUTT);
		descriptionTextPaint.setStyle(Paint.Style.FILL);
		descriptionTextPaint.setColor(Color.WHITE);
		descriptionTextPaint.setTextSize(convertDipToPx(mSmallTextSize_dip));
		
//		Rect descriptionRect = new Rect();
//		descriptionTextPaint.getTextBounds(descriptionText, 0, descriptionText.length(), descriptionRect);
//		canvas.save();
//		canvas.rotate(-180, 
//				mScreenCenterX_px - buttonFromArcDistance_px - rightDescTextOffset_px - descriptionRect.exactCenterX(), 
//				buttonFromArcDistance_px + scoreBarHeight_px + bottomTextOffset + descriptionRect.exactCenterY());
//		canvas.drawText(descriptionText, 
//				mScreenCenterX_px - buttonFromArcDistance_px - rightDescTextOffset_px - descriptionRect.width(), 
//				buttonFromArcDistance_px + scoreBarHeight_px + bottomTextOffset - descriptionRect.height(), 
//				descriptionTextPaint);
//		canvas.restore();
		
		// score upper line & player desc area
		if(!mSpinnerConfiguration.disabledPlayers[0])
		{
			// player line
			canvas.drawLine(mScreenCenterX_px - btnDistanceFromCenterAxis_px - playerDscWidth_px, 
					buttonFromArcDistance_px, 
					mScreenCenterX_px - btnDistanceFromCenterAxis_px - playerDscWidth_px, 
					lineLenght, linePaint);
			
			// score line
			canvas.drawLine(buttonFromArcDistance_px, 
					buttonFromArcDistance_px + scoreBarHeight_px, 
					mScreenCenterX_px - btnDistanceFromCenterAxis_px - playerDscWidth_px, 
					buttonFromArcDistance_px + scoreBarHeight_px, linePaint);
			
			// text
			String player1 = getResources().getString(R.string.player1);
			Rect player1Rect = new Rect();
			descriptionTextPaint.getTextBounds(player1, 0, player1.length(), player1Rect);
			
			canvas.save();
			canvas.rotate(90, 
					textLeftRectCenterX, 
					textRectCenterY);
			canvas.drawText(player1, 
					textLeftRectCenterX - player1Rect.width() / 2, 
					textRectCenterY + player1Rect.height() / 2, 
					descriptionTextPaint);
			canvas.restore();
		}
		
		if(!mSpinnerConfiguration.disabledPlayers[1])
		{
			// player line
			canvas.drawLine(mScreenCenterX_px + btnDistanceFromCenterAxis_px + playerDscWidth_px, 
					buttonFromArcDistance_px, 
					mScreenCenterX_px + btnDistanceFromCenterAxis_px + playerDscWidth_px, 
					lineLenght, linePaint);
			
			// score line
			canvas.drawLine(mScreenCenterX_px * 2 - buttonFromArcDistance_px, 
					buttonFromArcDistance_px + scoreBarHeight_px, 
					mScreenCenterX_px + btnDistanceFromCenterAxis_px + playerDscWidth_px, 
					buttonFromArcDistance_px + scoreBarHeight_px, linePaint);
			
			// text
			String player4 = getResources().getString(R.string.player4);
			Rect player4Rect = new Rect();
			descriptionTextPaint.getTextBounds(player4, 0, player4.length(), player4Rect);
			
			canvas.save();
			canvas.rotate(-90, 
					textRightRectCenterX, 
					textRectCenterY);
			canvas.drawText(player4, 
					textRightRectCenterX - player4Rect.width() / 2, 
					textRectCenterY + player4Rect.height() / 2, 
					descriptionTextPaint);
			canvas.restore();
		}
		
		if(!mSpinnerConfiguration.disabledPlayers[2])
		{
			// player line
			canvas.drawLine(mScreenCenterX_px - btnDistanceFromCenterAxis_px - playerDscWidth_px, 
					mParentHeight_px - buttonFromArcDistance_px, 
					mScreenCenterX_px - btnDistanceFromCenterAxis_px - playerDscWidth_px, 
					mParentHeight_px - lineLenght, linePaint);
			
			// score line
			canvas.drawLine(buttonFromArcDistance_px, 
					 mScreenCenterY_px * 2 - buttonFromArcDistance_px - scoreBarHeight_px, 
					 mScreenCenterX_px - btnDistanceFromCenterAxis_px - playerDscWidth_px, 
					 mScreenCenterY_px * 2 - buttonFromArcDistance_px - scoreBarHeight_px, linePaint);
			
			// text
			String player3 = getResources().getString(R.string.player3);
			Rect player3Rect = new Rect();
			descriptionTextPaint.getTextBounds(player3, 0, player3.length(), player3Rect);
			
			canvas.save();
			canvas.rotate(90, 
					textLeftRectCenterX, 
					mParentHeight_px - textRectCenterY);
			canvas.drawText(player3, 
					textLeftRectCenterX - player3Rect.width() / 2, 
					mParentHeight_px - textRectCenterY + player3Rect.height() / 2, 
					descriptionTextPaint);
			canvas.restore();
		}
		
		if(!mSpinnerConfiguration.disabledPlayers[3])
		{
			// player line
			canvas.drawLine(mScreenCenterX_px + btnDistanceFromCenterAxis_px + playerDscWidth_px, 
					mParentHeight_px - buttonFromArcDistance_px, 
					mScreenCenterX_px + btnDistanceFromCenterAxis_px + playerDscWidth_px, 
					mParentHeight_px - lineLenght, linePaint);
			
			// score line
			canvas.drawLine(mScreenCenterX_px + btnDistanceFromCenterAxis_px + playerDscWidth_px, 
					mScreenCenterY_px * 2 - buttonFromArcDistance_px - scoreBarHeight_px, 
					mScreenCenterX_px * 2 - buttonFromArcDistance_px, 
					mScreenCenterY_px * 2 - buttonFromArcDistance_px - scoreBarHeight_px, linePaint);
			
			// text
			String player2 = getResources().getString(R.string.player2);
			Rect player2Rect = new Rect();
			descriptionTextPaint.getTextBounds(player2, 0, player2.length(), player2Rect);
			
			canvas.save();
			canvas.rotate(-90, 
					textRightRectCenterX, 
					mParentHeight_px - textRectCenterY);
			canvas.drawText(player2, 
					textRightRectCenterX - player2Rect.width() / 2, 
					mParentHeight_px - textRectCenterY + player2Rect.height() / 2, 
					descriptionTextPaint);
			canvas.restore();
		}
	}
	
	private void drawScoreBarGuidlines(Canvas canvas, float playerDscWidth_px)
	{
		final int buttonFromArcDistance_px = this.convertDipToPx(mButtonDistance_dip);
		final int scoreBarHeight_px = convertDipToPx(mScoreBarHeight_dip);
		final int btnDistanceFromCenterAxis_px = buttonFromArcDistance_px;
		final int offset = (int)(mBordersWidth_px / 2 + 0.5f);
		
		// score guide lines
		Paint guideLinePaint = new Paint();
		guideLinePaint.setAntiAlias(true);		
		guideLinePaint.setColor(Color.BLACK);
		guideLinePaint.setStrokeWidth(convertDipToPx(1.5f));
		guideLinePaint.setStrokeCap(Paint.Cap.BUTT);
		guideLinePaint.setStyle(Paint.Style.STROKE);
		guideLinePaint.setPathEffect(new DashPathEffect(new float[] {2,4}, 0));
		final float fullScoreBarLenght_px = mScreenCenterX_px - btnDistanceFromCenterAxis_px - buttonFromArcDistance_px - playerDscWidth_px;
		float guidlinesSpacing = (float)fullScoreBarLenght_px / 10;
		float currentOffsetX = 0;
		for(int i = 0; i < 9; i++)
		{
			currentOffsetX += guidlinesSpacing;
			
			// top left
			if(!mSpinnerConfiguration.disabledPlayers[0])
			{
				canvas.drawLine(
						currentOffsetX + buttonFromArcDistance_px, 
						scoreBarHeight_px + buttonFromArcDistance_px - offset, 
						currentOffsetX + buttonFromArcDistance_px, 
						buttonFromArcDistance_px + offset, 
						guideLinePaint);
			}
			
			// top right
			if(!mSpinnerConfiguration.disabledPlayers[1])
			{
				canvas.drawLine(
						currentOffsetX + mScreenCenterX_px + btnDistanceFromCenterAxis_px + playerDscWidth_px, 
						scoreBarHeight_px + buttonFromArcDistance_px - offset, 
						currentOffsetX + mScreenCenterX_px + btnDistanceFromCenterAxis_px + playerDscWidth_px, 
						buttonFromArcDistance_px + offset, 
						guideLinePaint);
			}
			
			// bottom left
			if(!mSpinnerConfiguration.disabledPlayers[2])
			{
				canvas.drawLine(
						currentOffsetX + buttonFromArcDistance_px, 
						2*mScreenCenterY_px - scoreBarHeight_px - buttonFromArcDistance_px + offset, 
						currentOffsetX + buttonFromArcDistance_px, 
						2*mScreenCenterY_px - buttonFromArcDistance_px - offset, 
						guideLinePaint);
			}
			
			// bottom right
			if(!mSpinnerConfiguration.disabledPlayers[3])
			{
				canvas.drawLine(
						currentOffsetX + mScreenCenterX_px + btnDistanceFromCenterAxis_px + playerDscWidth_px, 
						2*mScreenCenterY_px - scoreBarHeight_px - buttonFromArcDistance_px + offset, 
						currentOffsetX + mScreenCenterX_px + btnDistanceFromCenterAxis_px + playerDscWidth_px, 
						2*mScreenCenterY_px - buttonFromArcDistance_px - offset, 
						guideLinePaint);
			}
		}
	}
	
	// based on render parameters initializes draw info list
	private void initializeArcDimensions() {
		
		// space from inner circle to outer arc border - space between arcs = total free space available (5 dip for additional space between inner circle and arcs)
		final float availableFreeSpace = (mOuterRadius_dip - mInnerCircleRadius_dip) - (mArcsDistance_dip * (mNumberOfArcs + 1));
		
		// width of arcs
		float arcsWidth_dip = availableFreeSpace / mNumberOfArcs;				

		// fill list of arc info
		final float distanceBetweenArcRadius_dip = arcsWidth_dip + mArcsDistance_dip;
		float currentArcRadius_dip = mOuterRadius_dip - arcsWidth_dip/2 - mArcsDistance_dip;
		for (int i = mNumberOfArcs - 1; i >= 0; i--)
		{
			// calculate radius for current arc & create rect
			if(i < mNumberOfArcs - 1)
			{	
				currentArcRadius_dip = currentArcRadius_dip - distanceBetweenArcRadius_dip;
			}
			final float currentArcRadius_px = this.convertDipToPx(currentArcRadius_dip);
			mArcRects[i] = new RectF(
					mScreenCenterX_px - currentArcRadius_px,
					mScreenCenterY_px - currentArcRadius_px, 
					mScreenCenterX_px + currentArcRadius_px, 
					mScreenCenterY_px + currentArcRadius_px);
						
			// generate random sweep angle within boundaries
			Random r = new Random();
			mSweepAngles[i] = r.nextInt(this.mSweepAngleMax - this.mSweepAngleMin) + this.mSweepAngleMin;
			
			// generate random start angle
			mStartAngles[i] = r.nextInt(360);
		}
		
		// create arc paint based on calculated width
		mArcPaint = new Paint();
		mArcPaint.setAntiAlias(true);		
		mArcPaint.setColor(Color.YELLOW);
		mArcPaint.setStrokeCap(Paint.Cap.BUTT);
		mArcPaint.setStyle(Paint.Style.STROKE);
		
		if(arcsWidth_dip > mMaxArcWidth_dip)
		{
			mArcPaint.setStrokeWidth(convertDipToPx(mMaxArcWidth_dip));
		}
		else
		{
			mArcPaint.setStrokeWidth(convertDipToPx(arcsWidth_dip));
		}
	}
	
	// converts given dip value to pixels
	private int convertDipToPx(int value) {
		return (int) ((float) value * mScale + 0.5f);
	}
	
	private float convertDipToPx(float value) {
		return value * mScale;
	}

	// calculates radius of outer arc (to the border of arc) according devices screen parameters
	private void calculateOuterRadius_dip() {
		
		// get minimal screen dimension
		final int minWidth_px = Math.min(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
		
		// convert to dip
		final int minWidth_dip = (int) ((minWidth_px / mScale) + 0.5f);
		
		// subtract distance from border in dip 
		mOuterRadius_dip = (minWidth_dip - (mArcBorderDistance_dip * 2)) / 2;
	}
	
	// updates arcs location
	private void updateArcLocations() {
		
		// get delta from last update & elapsed time from start
		final float timeDelta = (float) (System.currentTimeMillis() - this.mTimeLastUpdate_ms);
		final float elapsedTime_s = (float) (System.currentTimeMillis() - this.mTimeStart_ms) / 1000;
		
		// update time stamp
		this.mTimeLastUpdate_ms = System.currentTimeMillis();
				
		// calculate current angular velocity (1/x with some scaling)
		final float currentAngularVelocity = (mB / elapsedTime_s) + mC;
		
		// update current velocities respecting increasing velocity towards center
		float currentArcInitialVelocity = currentAngularVelocity - this.mVelocityIncrementStep;
		for (int i = mNumberOfArcs - 1; i >=0; i--)
		{
			currentArcInitialVelocity += mVelocityIncrementStep;
			mAngularVelocities[i] = currentArcInitialVelocity;
		}
		
		// calculate position of each arc
		boolean isClockWise = true;
		for (int i = mNumberOfArcs - 1; i >=0; i--)
		{
			// calculate movement in degrees from previous render
			final float deltaDegrees = mAngularVelocities[i] * (timeDelta / 1000);
			
			// alternate direction in which circles spin
			if(isClockWise) {
				mStartAngles[i] += deltaDegrees;
				isClockWise = false;
			}
			else {
				mStartAngles[i] -= deltaDegrees;
				isClockWise = true;
			}
		}
	}

	@Override
	public void run() 
	{
		int sufixFrames = mSuffixFrames;
		
		// offset start for environment to get ready (on some devices sometimes doesn't draw otherwise)
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		
		while(sufixFrames >= 0)
		{
			if(!m_SurfaceHolder.getSurface().isValid())
			{
				continue;
			}
			
			Canvas canvas = null;
			try
			{
				canvas = m_SurfaceHolder.lockCanvas();
				if(canvas != null)
				{
					synchronized(m_SurfaceHolder) 
					{
						if(isRunning())
						{
							this.updateArcLocations();
						}
						else
						{
							sufixFrames--;
						}
						
						// draw background image - clear canvas
						canvas.drawBitmap(mBackground, 0, 0, null);
						
						// draw arcs
						for (int i = mNumberOfArcs - 1; i >=0; i--)
						{
							canvas.drawArc(mArcRects[i], mStartAngles[i], mSweepAngles[i], false, mArcPaint);
						}
					}
				}
			}
			finally
			{
				if(canvas != null)
				{
					m_SurfaceHolder.unlockCanvasAndPost(canvas);
				}
			}

			this.mNumberOfRenderdFrames++;
		}
	}
	
	public int getAverageFps() {
		float elapsedTime_s = (float) (System.currentTimeMillis() - this.mTimeStart_ms) / 1000;
		return (int)(this.mNumberOfRenderdFrames / elapsedTime_s);
	}
	
	
	public float getElapsedTime() {
		final float elapsedTime_s = (float) (System.currentTimeMillis() - this.mTimeStart_ms) / 1000;
		return elapsedTime_s;
	}
	
	public boolean isRunning(){
		synchronized(this)
		{
			return this.mIsRunning;
		}
	}
	
	public void setIsRunning(boolean isRunning)
	{
		synchronized(this)
		{
			mIsRunning = isRunning;
		}
	}
	
	// returns scores of players in %
	public int[] analyzeResult()
	{
		// set all scores to 0 by default
		int[] result = new int[mMaxPlayerCount];
		for(int i = 0; i < result.length; i++)
		{
			result[i] = 0;
		}
		
		if(!mIsRunning)
		{
			// analyze arc positions
			arcsIntersectAnalyzer.analyze(mSweepAngles, mStartAngles);
			
			if(arcsIntersectAnalyzer.getIsIntersect())
			{
				// calculate score
				for(int i = 0; i < mMaxPlayerCount; i++)
				{
					result[i] = arcsIntersectAnalyzer.getScore(i);
				}
			}
		}
		
		return result;
	}
	
	// gets index of corresponding player based on coordinates (from top-left to bottom-right)
	public int getPlayer(float touchX, float touchY)
	{
		int result = -1;
		
		if(touchX <= mScreenCenterX_px && touchY <= mScreenCenterY_px)
		{
			result = 0;
		}
		else if (touchX > mScreenCenterX_px && touchY <= mScreenCenterY_px)
		{
			result = 1;
		}
		else if (touchX <= mScreenCenterX_px && touchY > mScreenCenterY_px)
		{
			result = 3;
		}
		else if (touchX > mScreenCenterX_px && touchY > mScreenCenterY_px)
		{
			result = 4;
		}
		
		return result;
	}
	
	private void highLightButton(Canvas canvas, int playerIndex)
	{
		Path buttonToHighlight = null;
		
		Paint buttonPaint = new Paint();
		// determine which button was pressed
		switch(playerIndex)
		{
		case 0:	// top left
			buttonPaint.setColor(Color.rgb(0,252,240));
			buttonToHighlight = mBtnTopLeft;
			break;
		case 1:	// top right
			buttonPaint.setColor(Color.rgb(255,30,0));
			buttonToHighlight = mBtnTopRight;
			break;
		case 2:	// bottom left
			buttonPaint.setColor(Color.rgb(185,133,27));
			buttonToHighlight = mBtnBottomLeft;
			break;
		case 3:	// bottom right
			buttonPaint.setColor(Color.rgb(255,0,255));
			buttonToHighlight = mBtnBottomRight;
			break;
		}
		
		// highlight pressed button
		buttonPaint.setAntiAlias(true);	
		
		buttonPaint.setStyle(Paint.Style.FILL);
		buttonPaint.setAlpha(255);
		
		canvas.drawPath(buttonToHighlight, buttonPaint);
	}
	
	private void drawIntersectArea(Canvas canvas, Path[] intersects)
	{
		if(arcsIntersectAnalyzer.getIsIntersect())
		{
			Paint intersectPaint = new Paint();
			intersectPaint.setAntiAlias(true);		
			intersectPaint.setXfermode(new PorterDuffXfermode(Mode.SCREEN));
			intersectPaint.setColor(Color.argb(120, 255, 255, 0));
			intersectPaint.setStrokeWidth(2);
			intersectPaint.setStrokeCap(Paint.Cap.BUTT);
			intersectPaint.setStyle(Paint.Style.FILL);
			
			// draw intersects
			for(int i = 0; i < intersects.length; i++)
			{
				canvas.drawPath(intersects[i], intersectPaint);
			}
		}
	}
	
	// draws results of current round on screen, expects that analyzeResult method was called before
	public void drawResult(SpinnerResultConfig spinnerResultConfig) 
	{
		if(!mIsRunning)
		{
			mCurrentSpinnerResultConfig = spinnerResultConfig;
			
			// update member variables 
			for(int i = 0; i < mMaxPlayerCount; i++)
			{
				mPlayerScores[i] = spinnerResultConfig.totalPlayerScores[i];
				mDisplayScores[i] = spinnerResultConfig.displayScores[i];
			}
			mCenterCircleRadius = spinnerResultConfig.centerCircleRadius;
			mSpinnerConfiguration.centerText = spinnerResultConfig.centerText;
			mSpinnerConfiguration.centerNumber = spinnerResultConfig.centerNumber;
			
			// draw the results
			Canvas canvas = m_SurfaceHolder.lockCanvas();
			
			// redraw the background on the image with updated scores
			drawUnderlyingCanvas(false);
			
			// draw background image - clear canvas
			canvas.drawBitmap(mBackground, 0, 0, null);
			
			// draw arcs in final position
			for (int i = mArcRects.length - 1; i >=0; i--)
			{
				canvas.drawArc(mArcRects[i], mStartAngles[i], mSweepAngles[i], false, mArcPaint);
			}

			highLightButton(canvas, spinnerResultConfig.stoppingPlayer);
			
			// draw intersects
			mCurrentItersects = arcsIntersectAnalyzer.calculateIntersectAreas(mParentWidth_px, mParentHeight_px);
			
			// player scores
			for(int i = 0; i < mMaxPlayerCount; i++)
			{
				if(!mSpinnerConfiguration.disabledPlayers[i])
				{
					drawScore(i,mPlayerScores[i], mDisplayScores[i], canvas);
				}
			}
//			drawCurrentRoundDisplayScores(spinnerResultConfig.currentRoundDisplayScores, canvas);
			drawButtonBorders(canvas);
//			drawDisabledPlayers(canvas);
			
			drawIntersectArea(canvas, mCurrentItersects);
			
			m_SurfaceHolder.unlockCanvasAndPost(canvas);
		}
	}
	
	public void drawGameOver(
			SpinnerResultConfig spinnerResultConfig,
			ArrayList<Integer> highestScorePlayers)
	{
		if(!mIsRunning)
		{
			// update player scores
			for(int i = 0; i < mMaxPlayerCount; i++)
			{
				mPlayerScores[i] = spinnerResultConfig.totalPlayerScores[i];
				mDisplayScores[i] = spinnerResultConfig.displayScores[i];
			}
			mCenterCircleRadius = spinnerResultConfig.centerCircleRadius;
			
			// draw the results
			Canvas canvas = m_SurfaceHolder.lockCanvas();
			
			// redraw the background on the image with updated scores
			drawUnderlyingCanvas(false);
			
			// draw background image - clear canvas
			canvas.drawBitmap(mBackground, 0, 0, null);
			
			// draw arcs in final position
			for (int i = mArcRects.length - 1; i >=0; i--)
			{
				canvas.drawArc(mArcRects[i], mStartAngles[i], mSweepAngles[i], false, mArcPaint);
			}

			highLightButton(canvas, spinnerResultConfig.stoppingPlayer);
			
			// draw intersects
			Path[] intersects = arcsIntersectAnalyzer.calculateIntersectAreas(mParentWidth_px, mParentHeight_px);
			drawIntersectArea(canvas, intersects);
			
			// player scores
			for(int i = 0; i < mMaxPlayerCount; i++)
			{
				if(!mSpinnerConfiguration.disabledPlayers[i])
				{
					drawScore(i,mPlayerScores[i], mDisplayScores[i], canvas);
				}
			}
//			drawCurrentRoundDisplayScores(spinnerResultConfig.currentRoundDisplayScores, canvas);
			drawButtonBorders(canvas);
//			drawDisabledPlayers(canvas);
			
			m_SurfaceHolder.unlockCanvasAndPost(canvas);
		}
	}
	
	public void restart()
	{		
		initConstructor();
		start();
	}
	
	public void start()
	{
		setIsRunning(true);
		this.mWorkerThread = new Thread(this);
		
		// initialize
		this.initialize();
		
		// collect any left over objects before start
		System.gc();
		
		// mark when the spinning started
		this.mNumberOfRenderdFrames = 0;
		this.mTimeStart_ms = this.mTimeLastUpdate_ms = System.currentTimeMillis();
		
		// start the spinner
		this.mWorkerThread.start();
	}
	
	public void stop()
	{
		setIsRunning(false);
		
		while(true)
		{
			try
			{
				mWorkerThread.join();
			}
			catch( InterruptedException e)
			{
				e.printStackTrace();
			}
			break;
		}
	}
	
	// redraws the spinner based on it's current state 
	public void redraw()
	{
		drawUnderlyingCanvas(true);
		
		Canvas canvas = m_SurfaceHolder.lockCanvas();
		
		// draw background image - clear canvas
		canvas.drawBitmap(mBackground, 0, 0, null);
		
		// draw arcs
		for (int i = mArcRects.length - 1; i >=0; i--)
		{
			canvas.drawArc(mArcRects[i], mStartAngles[i], mSweepAngles[i], false, mArcPaint);
		}
		
		m_SurfaceHolder.unlockCanvasAndPost(canvas);
	}

	// draws score for given player player numbers from 0 to 3, from top-left to bottom-right.
	// score from 0 to n %, score%100 is shown, parts above 100 are shown as small circles above the score 
	private void drawScore(int player, float score, int displayScore, Canvas canvas)
	{	
		boolean isOver = false;
		float scoreToDraw = 0;
		if(score >= 100)
		{
			isOver = true;
			scoreToDraw  = score % 100;
		}
		else
		{
			scoreToDraw = score;
		}
		
		final int padding_px = 0;//convertDipToPx(2);	// inner padding of score bar
		final int leftTextOffset = convertDipToPx(5); // offset from left border of score bar
		final int bottomTextOffset = convertDipToPx(5);
		final int descriptionScoreTextOffset = convertDipToPx(5);
		String displayScoreString = String.valueOf(displayScore);
		displayScoreString += (char) 0x00B0;
		String descriptionText = getResources().getString(R.string.scoreDescTotal);				
		
		final int scoreBarHeight_px = convertDipToPx(mScoreBarHeight_dip);
		final int buttonFromArcDistance_px = this.convertDipToPx(mButtonDistance_dip);
		final int btnDistanceFromCenterAxis_px = buttonFromArcDistance_px;
		
		final float fullScoreBarLenght_px = mScreenCenterX_px - btnDistanceFromCenterAxis_px - buttonFromArcDistance_px - 2*padding_px - mPlayerDscWidth_px;
		
		float scoreLenght_px = 0;
		if(!isOver)
		{
			scoreLenght_px = (int) (scoreToDraw * (fullScoreBarLenght_px / 100) + 0.5f);
		}
		else
		{
			scoreLenght_px = fullScoreBarLenght_px;
		}
		
		Paint scoreTextPaint = new Paint();
		scoreTextPaint.setAntiAlias(true);
		scoreTextPaint.setStrokeWidth(2);
		scoreTextPaint.setStrokeCap(Paint.Cap.BUTT);
		scoreTextPaint.setStyle(Paint.Style.FILL);
		scoreTextPaint.setColor(Color.WHITE);
		scoreTextPaint.setTextSize(convertDipToPx(mBigTextSize_dip));
		
		Paint descriptionTextPaint = new Paint();
		descriptionTextPaint.setAntiAlias(true);
		descriptionTextPaint.setStrokeWidth(2);
		descriptionTextPaint.setStrokeCap(Paint.Cap.BUTT);
		descriptionTextPaint.setStyle(Paint.Style.FILL);
		descriptionTextPaint.setColor(Color.WHITE);
		descriptionTextPaint.setTextSize(convertDipToPx(mSmallTextSize_dip));
		Rect descriptionRect = new Rect();
		descriptionTextPaint.getTextBounds(descriptionText, 0, descriptionText.length(), descriptionRect);			
		
		Path scorePathToDraw = new Path();
		switch(player)
		{
			case 0:
			{
				// top left
				scorePathToDraw.moveTo(buttonFromArcDistance_px + padding_px, buttonFromArcDistance_px + scoreBarHeight_px - padding_px);	
				scorePathToDraw.lineTo(buttonFromArcDistance_px + padding_px, buttonFromArcDistance_px + padding_px);
				scorePathToDraw.lineTo(buttonFromArcDistance_px + padding_px + scoreLenght_px, buttonFromArcDistance_px + padding_px);
				scorePathToDraw.lineTo(buttonFromArcDistance_px + padding_px + scoreLenght_px, buttonFromArcDistance_px + scoreBarHeight_px - padding_px);
				scorePathToDraw.close();
				
				canvas.save();
				canvas.rotate(-180, 
						buttonFromArcDistance_px + leftTextOffset + descriptionRect.exactCenterX(), 
						buttonFromArcDistance_px + scoreBarHeight_px + bottomTextOffset + descriptionRect.exactCenterY());
				canvas.drawText(descriptionText, 
						buttonFromArcDistance_px + leftTextOffset, 
						buttonFromArcDistance_px + scoreBarHeight_px + bottomTextOffset - descriptionRect.height(), 
						descriptionTextPaint);
				canvas.restore();
				
				Rect scoreRect = new Rect();
				scoreTextPaint.getTextBounds(displayScoreString, 0, displayScoreString.length(), scoreRect);
				canvas.save();
				canvas.rotate(-180, 
						buttonFromArcDistance_px + leftTextOffset + scoreRect.exactCenterX(), 
						buttonFromArcDistance_px + scoreBarHeight_px + bottomTextOffset + descriptionRect.height() + descriptionScoreTextOffset + scoreRect.exactCenterY());
				canvas.drawText(displayScoreString, 
						buttonFromArcDistance_px + leftTextOffset, 
						buttonFromArcDistance_px + scoreBarHeight_px + bottomTextOffset + descriptionRect.height() + descriptionScoreTextOffset - scoreRect.height(), 
						scoreTextPaint);
				canvas.restore();
				break;
			}
			case 1:
			{
				// top right
				scorePathToDraw.moveTo(mScreenCenterX_px * 2 - buttonFromArcDistance_px - padding_px, buttonFromArcDistance_px + scoreBarHeight_px - padding_px);
				scorePathToDraw.lineTo(mScreenCenterX_px * 2 - buttonFromArcDistance_px - padding_px, buttonFromArcDistance_px + padding_px);
				scorePathToDraw.lineTo(mScreenCenterX_px * 2 - buttonFromArcDistance_px - padding_px - scoreLenght_px, buttonFromArcDistance_px + padding_px);
				scorePathToDraw.lineTo(mScreenCenterX_px * 2 - buttonFromArcDistance_px - padding_px - scoreLenght_px, buttonFromArcDistance_px + scoreBarHeight_px - padding_px);
				scorePathToDraw.close();

				int decsriptionTextWidth = (int) (descriptionTextPaint.measureText(descriptionText) + 0.5f);
				canvas.save();
				canvas.rotate(-180, 
						mScreenCenterX_px * 2 - buttonFromArcDistance_px - leftTextOffset - descriptionRect.exactCenterX(), 
						buttonFromArcDistance_px + scoreBarHeight_px + bottomTextOffset + descriptionRect.exactCenterY());
				canvas.drawText(descriptionText, 
						mScreenCenterX_px * 2 - buttonFromArcDistance_px - leftTextOffset - decsriptionTextWidth, 
						buttonFromArcDistance_px + scoreBarHeight_px + bottomTextOffset - descriptionRect.height(), 
						descriptionTextPaint);
				canvas.restore();
				
				Rect scoreRect = new Rect();
				scoreTextPaint.getTextBounds(displayScoreString, 0, displayScoreString.length(), scoreRect);
				int scoreTextWidth = (int) (scoreTextPaint.measureText(displayScoreString) + 0.5f);
				canvas.save();
				canvas.rotate(-180, 
						mScreenCenterX_px * 2 - buttonFromArcDistance_px - leftTextOffset - scoreRect.exactCenterX(), 
						buttonFromArcDistance_px + scoreBarHeight_px + bottomTextOffset + + descriptionRect.height() + descriptionScoreTextOffset + scoreRect.exactCenterY());
				canvas.drawText(displayScoreString, 
						mScreenCenterX_px * 2 - buttonFromArcDistance_px - leftTextOffset - scoreTextWidth, 
						buttonFromArcDistance_px + scoreBarHeight_px + bottomTextOffset + descriptionRect.height() + descriptionScoreTextOffset - scoreRect.height(), 
						scoreTextPaint);
				canvas.restore();
				break;
			}
			case 2:
			{
				// bottom left
				scorePathToDraw.moveTo(buttonFromArcDistance_px + padding_px, mScreenCenterY_px * 2 - buttonFromArcDistance_px - scoreBarHeight_px + padding_px);
				scorePathToDraw.lineTo(buttonFromArcDistance_px + padding_px, mScreenCenterY_px * 2 - buttonFromArcDistance_px - padding_px);
				scorePathToDraw.lineTo(buttonFromArcDistance_px + padding_px + scoreLenght_px, mScreenCenterY_px * 2 - buttonFromArcDistance_px - padding_px);
				scorePathToDraw.lineTo(buttonFromArcDistance_px + padding_px + scoreLenght_px, mScreenCenterY_px * 2 - buttonFromArcDistance_px - scoreBarHeight_px + padding_px);
				scorePathToDraw.close();

				canvas.drawText(descriptionText, 
						buttonFromArcDistance_px + leftTextOffset, 
						mScreenCenterY_px * 2 - buttonFromArcDistance_px - scoreBarHeight_px - bottomTextOffset, 
						descriptionTextPaint);
				
				canvas.drawText(displayScoreString, 
						buttonFromArcDistance_px + leftTextOffset, 
						mScreenCenterY_px * 2 - buttonFromArcDistance_px - scoreBarHeight_px - bottomTextOffset - descriptionRect.height() - descriptionScoreTextOffset, 
						scoreTextPaint);
				break;
			}
			case 3:
			{
				// bottom right
				scorePathToDraw.moveTo(mScreenCenterX_px * 2 - buttonFromArcDistance_px - padding_px, mScreenCenterY_px * 2 - buttonFromArcDistance_px - scoreBarHeight_px + padding_px);
				scorePathToDraw.lineTo(mScreenCenterX_px * 2 - buttonFromArcDistance_px - padding_px, mScreenCenterY_px * 2 - buttonFromArcDistance_px - padding_px);
				scorePathToDraw.lineTo(mScreenCenterX_px * 2 - buttonFromArcDistance_px - padding_px - scoreLenght_px, mScreenCenterY_px * 2 - buttonFromArcDistance_px - padding_px);
				scorePathToDraw.lineTo(mScreenCenterX_px * 2 - buttonFromArcDistance_px - padding_px - scoreLenght_px, mScreenCenterY_px * 2 - buttonFromArcDistance_px - scoreBarHeight_px + padding_px);
				scorePathToDraw.close();

				int decsriptionTextWidth = (int) (descriptionTextPaint.measureText(descriptionText) + 0.5f);
				canvas.drawText(descriptionText, 
						mScreenCenterX_px * 2 - buttonFromArcDistance_px - leftTextOffset - decsriptionTextWidth, 
						mScreenCenterY_px * 2 - buttonFromArcDistance_px - scoreBarHeight_px - bottomTextOffset, 
						descriptionTextPaint);
				
				int scoreTextWidth = (int) (scoreTextPaint.measureText(displayScoreString) + 0.5f);
				canvas.drawText(displayScoreString, 
						mScreenCenterX_px * 2 - buttonFromArcDistance_px - leftTextOffset - scoreTextWidth, 
						mScreenCenterY_px * 2 - buttonFromArcDistance_px - scoreBarHeight_px - bottomTextOffset - descriptionRect.height() - descriptionScoreTextOffset, 
						scoreTextPaint);
				break;
			}
		}
		
		// draw score
		Paint scorePaint = new Paint();
		scorePaint.setAntiAlias(true);
		scorePaint.setStrokeWidth(2);
		scorePaint.setStrokeCap(Paint.Cap.BUTT);
		scorePaint.setStyle(Paint.Style.FILL);
		scorePaint.setColor(Color.rgb(255, 255, 0));
		
		// just change color for now
		if(isOver)
		{
			scorePaint.setColor(Color.GREEN);
		}
		
		canvas.drawPath(scorePathToDraw, scorePaint);
	}
}
