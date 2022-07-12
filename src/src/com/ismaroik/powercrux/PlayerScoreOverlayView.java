package com.ismaroik.powercrux;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class PlayerScoreOverlayView extends View {
	
	private int mArrowH;
	private int mArrowW;
	private int mArrowTopMargin;
	private int mArrowLeftMargin;
	private int mParentWidth_px;
	private int mParentHeight_px;
	
	private int[] mDisplayScores;
	private boolean[] mDisabledPlayers;
	private int mCurrentRoundTextSize_px;
	
	public PlayerScoreOverlayView(Context context) {
		super(context);
		Init();
	}
	
	public PlayerScoreOverlayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Init();
	}

	public PlayerScoreOverlayView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Init();
	}
	
	private void Init()
	{
//		m_SurfaceHolder = getHolder();
		
//		this.setZOrderOnTop(true);    // necessary
//		m_SurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

	    mParentWidth_px = MeasureSpec.getSize(widthMeasureSpec);
	    mParentHeight_px = MeasureSpec.getSize(heightMeasureSpec);
	}
	
	// used for computation of current round score text position
	public void setScoreArrowsDimensions(int arrowH, int arrowW, int arrowTopMargin, int arrowLeftMargin)
	{
		mArrowH = arrowH;
		mArrowW = arrowW;
		mArrowTopMargin = arrowTopMargin;
		mArrowLeftMargin = arrowLeftMargin;
	}
	
	public void setCurrentRoundStats(int[] displayScores, boolean[] disabledPlayers, int currentRoundTextSize_px)
	{
		mDisplayScores = displayScores;
		mDisabledPlayers = disabledPlayers;
		mCurrentRoundTextSize_px = currentRoundTextSize_px;
	}
	
	protected void onDraw(Canvas canvas) {
		   super.onDraw(canvas);
		   
		   drawCurrentRoundDisplayScores(canvas, mDisplayScores, mDisabledPlayers, mCurrentRoundTextSize_px);
	}
	
	public void drawCurrentRoundDisplayScores(Canvas canvas, int[] displayScores, boolean[] disabledPlayers, int currentRoundTextSize_px)
	{		
		Paint scoreTextPaint = new Paint();
		scoreTextPaint.setAntiAlias(true);
		scoreTextPaint.setStrokeWidth(2);
		scoreTextPaint.setStrokeCap(Paint.Cap.BUTT);
		scoreTextPaint.setStyle(Paint.Style.FILL);
		scoreTextPaint.setColor(Color.rgb(57,75, 210));
		scoreTextPaint.setTextSize(currentRoundTextSize_px);
		
		for(int i = 0; i < 4; i++)
		{
			if(!disabledPlayers[i] && displayScores[i] != 0)
			{				
				final String displayScoreString = String.valueOf(displayScores[i]) + (char) 0x00B0;;
				Rect rect = new Rect();
				scoreTextPaint.getTextBounds(displayScoreString, 0, displayScoreString.length(), rect);
				int textWidth = (int) (scoreTextPaint.measureText(displayScoreString) + 0.5f);
				switch(i)
				{
					case 0:
					{
						// top left
						float arrowScoreX;
						float arrowScoreY;
						
						if(displayScores[i] > 0)
						{
							arrowScoreX = mArrowLeftMargin + 0.47f * mArrowW;
							arrowScoreY = mArrowTopMargin + 0.54f * mArrowH;
						}
						else
						{
							arrowScoreX = mArrowLeftMargin + 0.48f * mArrowW;
							arrowScoreY = mArrowTopMargin + 0.57f * mArrowH;
						}
						
						canvas.save();
						canvas.rotate(141, 
								arrowScoreX, 
								arrowScoreY);
						canvas.drawText(displayScoreString, 
								arrowScoreX - textWidth/2, 
								arrowScoreY + rect.exactCenterY(), 
								scoreTextPaint);
						canvas.restore();				
						break;
					}
					case 1:
					{
						// top right
						float arrowScoreX;
						float arrowScoreY;
						
						if(displayScores[i] > 0)
						{
							arrowScoreX = mParentWidth_px - (mArrowLeftMargin + 0.50f * mArrowW);
							arrowScoreY = mArrowTopMargin + 0.51f * mArrowH;
						}
						else
						{
							arrowScoreX = mParentWidth_px - (mArrowLeftMargin + 0.51f * mArrowW);
							arrowScoreY = mArrowTopMargin + 0.54f * mArrowH;
						}
						
						canvas.save();
						canvas.rotate(219, 
								arrowScoreX, 
								arrowScoreY);
						canvas.drawText(displayScoreString, 
								arrowScoreX - textWidth/2, 
								arrowScoreY + rect.exactCenterY(), 
								scoreTextPaint);
						canvas.restore();
						break;
					}
					case 2:
					{
						// bottom left
						float arrowScoreX;
						float arrowScoreY;
						
						if(displayScores[i] > 0)
						{
							arrowScoreX = mArrowLeftMargin + 0.50f * mArrowW;
							arrowScoreY = mParentHeight_px - (mArrowTopMargin + 0.51f * mArrowH);
						}
						else
						{
							arrowScoreX = mArrowLeftMargin + 0.51f * mArrowW;
							arrowScoreY = mParentHeight_px - (mArrowTopMargin + 0.54f * mArrowH);
						}
						
						canvas.save();
						canvas.rotate(39, 
								arrowScoreX, 
								arrowScoreY);
						canvas.drawText(displayScoreString, 
								arrowScoreX - textWidth/2, 
								arrowScoreY + rect.exactCenterY(), 
								scoreTextPaint);
						canvas.restore();
						break;
					}
					case 3:
					{
						// bottom right
						float arrowScoreX;
						float arrowScoreY;
						
						if(displayScores[i] > 0)
						{
							arrowScoreX = mParentWidth_px - (mArrowLeftMargin + 0.47f * mArrowW);
							arrowScoreY = mParentHeight_px - (mArrowTopMargin + 0.54f * mArrowH);
						}
						else
						{
							arrowScoreX = mParentWidth_px - (mArrowLeftMargin + 0.48f * mArrowW);
							arrowScoreY = mParentHeight_px - (mArrowTopMargin + 0.57f * mArrowH);
						}
						
						canvas.save();
						canvas.rotate(-39, 
								arrowScoreX, 
								arrowScoreY);
						canvas.drawText(displayScoreString, 
								arrowScoreX - textWidth/2, 
								arrowScoreY + rect.exactCenterY(), 
								scoreTextPaint);
						canvas.restore();
						break;
					}
				}
			}
		}
	}

}
