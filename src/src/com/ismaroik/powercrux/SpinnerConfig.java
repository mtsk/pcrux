package com.ismaroik.powercrux;

import java.io.Serializable;

// configuration variables for spinner layout & function, all measures are in dip
public class SpinnerConfig implements Serializable  {
	private static final long serialVersionUID = -3071789982186597123L;

	public int borderDistance;
	public int innerCircleRadius;
	public int arcsDistance;
	public int buttonDistance;
	public int numberOfArcs;
	public int sweepAngleMin;
	public int sweepAngleMax;
	public int velocityIncrementStep;
	public int velocityScaleParameter_B;
	public int velocityScaleParameter_C;
	public boolean[] disabledPlayers;	// true if player on specified index should be disabled
	public int maxArcWidth;
	public int scoreBarHeight;
	public float borderWidth;
	public int bigTextSize;
	public int smallTextSize;
	public int currentRoundTextSize;
	
	// number shown in center circle
	public int centerNumber;
	
	// text shown in center circle
	public String centerText;
}
