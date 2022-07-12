package com.ismaroik.powercrux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.graphics.Path;
import android.graphics.Point;

public class ArcsIntersectAnalyzer {
	
	// represents arc by one or two intervals in range from 0 to 360 degrees
	private class Arc
	{
		public Interval[] mAngleIntervals;
	}
	private boolean mIsIntersect;			// indicates whether there is intersect in current configuration
	private Interval[] mIntersects;			// array of intersects - intervals in range 0 - 720 degrees
	
	public ArcsIntersectAnalyzer(){
		
	}
	
	// gets score for given player (0 - 3), from top-left to bottom-right, from 0 to 100 % of coverage
	public int getScore(int player)	
	{
		int result = 0;
		
		if(mIsIntersect && mIntersects != null && mIntersects.length > 0)
		{
			ArrayList<Interval> playerIntersects = new ArrayList<Interval>();
			Interval[] playerAreas = new Interval[2];
			switch(player)
			{
				case 0:
				{
					playerAreas[0] = new Interval(180,270);
					playerAreas[1] = new Interval(540,630);
					break;
				}
				case 1:
				{
					playerAreas[0] = new Interval(270,360);
					playerAreas[1] = new Interval(630,720);
					break;
				}
				case 2:
				{
					playerAreas[0] = new Interval(90,180);
					playerAreas[1] = new Interval(450,540);
					break;
				}
				case 3:
				{
					playerAreas[0] = new Interval(0,90);
					playerAreas[1] = new Interval(360,450);
					break;
				}
			}
						
			// calculate intersects with player area
			for(int i = 0; i < mIntersects.length; i++)
			{
				for(int j = 0; j < playerAreas.length; j++)
				{
					if(mIntersects[i].intersects(playerAreas[j]))
					{
						Interval playerIntersect = mIntersects[i].intersect(playerAreas[j]);
						playerIntersects.add(playerIntersect);
					}
				}
			}
			
			// calculate how much area is covered with intersect (degrees)
			for(int i = 0; i < playerIntersects.size(); i++)
			{
				Interval current = playerIntersects.get(i);
				result += current.getRight() - current.getLeft();
			}
			
			// percents 90degrees = 100%
			if(result > 0)
			{
				result = (int)((float)result / 0.9f + 0.5f);
			}
		}
		
		return result;
	}
	
	public boolean getIsIntersect()
	{
		return mIsIntersect;
	}
	
	private float normalizeAngle(float angle)
	{
		float normalizedAngle = angle % 360;
		if(normalizedAngle < 0)
		{
			normalizedAngle += 360;
		}
		
		return normalizedAngle;
	}
	
	// performs calculations required to analyze given arc configuration - calculates all intersects, if any
	public void analyze(int[] sweepAngles, float[] startAngles)
	{
		final int numberOfArcs = sweepAngles.length;
		
		// normalize angles
		for(int i = 0; i < numberOfArcs; i++)
		{
			startAngles[i] = normalizeAngle(startAngles[i]);
			sweepAngles[i] = (int)normalizeAngle(sweepAngles[i]);
		}
		
		// create list of arc objects representing opened part of arcs in range 0 to 360 degrees
		ArrayList<Arc> openedArcs = new ArrayList<Arc>();
		for(int i = 0; i < numberOfArcs; i++)
		{
			final float endAngle = startAngles[i] - (360 - sweepAngles[i]);
			
			if(endAngle < 0)
			{
				Interval openArcPortion1 = new Interval(0, startAngles[i]);
				Interval openArcPortion2 = new Interval(360 + endAngle, 360);
				Arc arc = new Arc();
				arc.mAngleIntervals = new Interval[2];
				arc.mAngleIntervals[0] = openArcPortion1;
				arc.mAngleIntervals[1] = openArcPortion2;
				openedArcs.add(arc);
			}
			else
			{
				Interval openArc;
				if(startAngles[i] < endAngle)
				{
					openArc = new Interval(startAngles[i], endAngle);
				}
				else
				{
					openArc = new Interval(endAngle, startAngles[i]);
				}
				Arc arc = new Arc();
				arc.mAngleIntervals = new Interval[1];
				arc.mAngleIntervals[0] = openArc;
				openedArcs.add(arc);
			}
		}
		
		
		// will contain final intersects
		ArrayList<Interval> result = new ArrayList<Interval>();
		
		// initialize with first arc
		Arc firstArc = openedArcs.get(0);
		for(int i = 0; i < firstArc.mAngleIntervals.length; i++)
		{
			result.add(openedArcs.get(0).mAngleIntervals[i]);
		}
		
		// calculate intersects with each subsequent arc
		for(int i = 1; i < numberOfArcs; i++)
		{
			Interval[] currentArcInts = openedArcs.get(i).mAngleIntervals;
			ArrayList<Interval> intersectsWithCurrent = new ArrayList<Interval>();
			
			// calculate intersects each with each, for resulting intervals and current arc intervals
			for(int j = 0; j < currentArcInts.length; j++)
			{
				Interval currentArcInt = currentArcInts[j];
				for(int k = 0; k < result.size(); k++)
				{
					if(currentArcInt.intersects(result.get(k)))
					{
						intersectsWithCurrent.add(currentArcInt.intersect(result.get(k)));
					}
				}
			}
			
			result = intersectsWithCurrent;
		}
		
		// set member variables according to results
		if(result.size() > 0)
		{
			mIsIntersect = true;
			
			// join intervals around 0 degrees, if there are some to join
			Interval firstPart = null;
			Interval secondPart = null;
			for(int i = 0; i < result.size(); i++)
			{
				if(result.get(i).getLeft() == 0)
				{
					secondPart = result.get(i);
				}
				if(result.get(i).getRight() == 360)
				{
					firstPart = result.get(i);
				}
			}
			
			if(firstPart != null && secondPart != null)
			{
				Interval joined = new Interval(firstPart.getLeft(), secondPart.getRight() + 360);
				
				result.remove(firstPart);
				result.remove(secondPart);
				result.add(joined);
			}
		}
		else
		{
			mIsIntersect = false;
		}
		
		mIntersects = result.toArray(new Interval[0]);
	}
	
	// expects line angle from 0 to 360
	private Point getLineCords(int rectWidth, int rectHeight, float lineAngle)
	{
		final int halfRectWidth = (int) (rectWidth / 2 + 0.5f);
		final int halfRectHeight = (int) (rectHeight / 2 + 0.5f);
		final float diagonalAngle = (float) Math.toDegrees(Math.atan((float)halfRectHeight / (float)halfRectWidth));
		
		int x = 0;
		int y = 0;
		
		// check corner cases
		if (lineAngle == 0 || lineAngle == 360)
		{
			x = rectWidth;
			y = halfRectHeight;
		}
		else if (lineAngle == 90)
		{
			x = halfRectWidth;
			y = rectHeight;
		}
		else if (lineAngle == 180)
		{
			x = 0;
			y = halfRectHeight;
		}
		else if (lineAngle == 270)
		{
			x = halfRectWidth;
			y = 0;
		}
		else
		{
			// project into first quadrant (bottom left) and calculate coordinates there
			float projectedLineAngle = lineAngle % 180;
			if(projectedLineAngle > 90)
			{
				projectedLineAngle = 180 - projectedLineAngle;
			}
			
			if(0 < projectedLineAngle && projectedLineAngle < diagonalAngle)
			{
				x = rectWidth;
				y = halfRectHeight + (int) (Math.tan(Math.toRadians(projectedLineAngle))*halfRectWidth + 0.5f);
			}
			else
			{
				x = halfRectWidth + (int) (Math.tan(Math.toRadians(90-projectedLineAngle))*halfRectHeight + 0.5f);
				y = rectHeight;
			}
			
			// adjust coordinates according to actual quadrant position
			if (90 < lineAngle && lineAngle < 180)
			{
				x = rectWidth - x;
			}
			else if (180 < lineAngle && lineAngle < 270)
			{
				x = rectWidth - x;
				y = rectHeight - y;
			}
			else if (270 < lineAngle && lineAngle < 360)
			{
				y = rectHeight - y;
			}
		}
		
		// no values below zero are expected
		if(x < 0)
		{
			x = 0;
		}
		if(y < 0)
		{
			y = 0;
		}
		
		return new Point(x,y);
	}
	
	public Path[] calculateIntersectAreas(final int rectWidth, final int rectHeight)
	{
		ArrayList<Point> lefts = new ArrayList<Point>();
		ArrayList<Point> rights = new ArrayList<Point>();
		
		// calculate border points & assign into correct list
		for(int i = 0; i < mIntersects.length; i++)
		{
			Point leftBorderPoint = getLineCords(rectWidth, rectHeight, mIntersects[i].getLeft());
			Point rightBorderPoint = getLineCords(rectWidth, rectHeight, mIntersects[i].getRight() % 360);
			
			lefts.add(leftBorderPoint);
			rights.add(rightBorderPoint);
		}

		// sort border points
		Collections.sort(lefts, new Comparator<Point>(){
			  public int compare(Point p1, Point p2) {
				  	return ArcsIntersectAnalyzer.this.compareBorderPoints(p1, p2, rectWidth, rectHeight);
				    }});
		Collections.sort(rights, new Comparator<Point>(){
			  public int compare(Point p1, Point p2) {
				  	return ArcsIntersectAnalyzer.this.compareBorderPoints(p1, p2, rectWidth, rectHeight);
				    }});
		
		assert (lefts.size() == rights.size());
		
		// check if first right point is after first left, if not shuffle right array by 1 to satisfy input condition
		if(lefts.size() > 0)
		{
			if(compareBorderPoints(lefts.get(0), rights.get(0), rectWidth, rectHeight) > 0)
			{
				Point firstItem = rights.get(0);
				rights.remove(0);
				rights.add(firstItem);
			}
		}
		
		// create intersect areas by traversing lists
		final int centerX = (int) (rectWidth / 2 + 0.5f);
		final int centerY = (int) (rectHeight / 2 + 0.5f);
		
		Path intersectPath = new Path();
		intersectPath.moveTo(centerX, centerY);
		
		for(int i = 0; i < lefts.size(); i++)
		{
			// from center to left point, across border to closest right point, back to center
			Point left = lefts.get(i);
			Point right = rights.get(i);
			intersectPath.lineTo(left.x, left.y);
			drawLineAccrosBorderToPoint(intersectPath, rectWidth, rectHeight, left, right);
			intersectPath.lineTo(centerX, centerY);
		}
		
		intersectPath.close();
		
		Path[] result = new Path[1];
		result[0] = intersectPath;
		return result;
	}
	
	// compares two points on border of rectangle from min-max as: top-right-bottom-left 
	private int compareBorderPoints(Point a, Point b, int rectWidth, int rectHeight)
	{
		int compareSizeA = calculateCompareSize(a, rectWidth, rectHeight);
		int compareSizeB = calculateCompareSize(b, rectWidth, rectHeight);
		
		return compareSizeA < compareSizeB ? -1 : compareSizeA > compareSizeB ? 1 : 0;
	}
	
	private int calculateCompareSize(Point borderPoint, int rectWidth, int rectHeight)
	{
		int result = 0;
		
		if(borderPoint.y == 0) 					// top
		{
			result = borderPoint.x;
		}
		else if (borderPoint.x == rectWidth)	// right
		{
			result = rectWidth + borderPoint.y;
		}
		else if (borderPoint.y == rectHeight)	// bottom
		{
			result = rectWidth + rectHeight  + (rectWidth - borderPoint.x);
		}
		else if (borderPoint.x == 0)			// left
		{
			result = 2*rectWidth + rectHeight  + (rectHeight - borderPoint.y);
		}
		
		return result;
	}
	
	private void drawLineAccrosBorderToPoint(
			Path intersectPath,
			int rectWidth, 
			int rectHeight,
			Point leftBorderPoint,
			Point rightBorderPoint)
	{
		// are border points on the same border side in clockwise direction?
		if( (compareBorderPoints(leftBorderPoint, rightBorderPoint, rectWidth, rectHeight) <= 0) &&
			(leftBorderPoint.x == rightBorderPoint.x || leftBorderPoint.y == rightBorderPoint.y))
		{
			intersectPath.lineTo(rightBorderPoint.x, rightBorderPoint.y);
		}
		else
		{
			// draws lines onto intersectPath across boundary rectangle in clockwise direction 
			// from leftBorderPoint towards rightBorderPoint
			Point currentPosition = new Point(leftBorderPoint.x, leftBorderPoint.y);
			while(true)
			{
				// left border line
				if(currentPosition.x == 0 && currentPosition.y > 0)
				{
					intersectPath.lineTo(0, 0);
					currentPosition.y = 0;
					
					if(currentPosition.y == rightBorderPoint.y)
					{
						break;
					}
				}
				
				// top border line
				if(currentPosition.x >= 0 && currentPosition.y == 0)
				{
					intersectPath.lineTo(rectWidth, 0);
					currentPosition.x = rectWidth;
					
					if(currentPosition.x == rightBorderPoint.x)
					{
						break;
					}
				}
				
				// right border line
				if(currentPosition.x == rectWidth && currentPosition.y >= 0)
				{
					intersectPath.lineTo(rectWidth, rectHeight);
					currentPosition.y = rectHeight;
					
					if(currentPosition.y == rightBorderPoint.y)
					{
						break;
					}
				}
				
				// bottom border line
				if(currentPosition.x >= 0 && currentPosition.y == rectHeight)
				{
					intersectPath.lineTo(0, rectHeight);
					currentPosition.x = 0;
					
					if(currentPosition.x == rightBorderPoint.x)
					{
						break;
					}
				}
			}
			
			intersectPath.lineTo(rightBorderPoint.x, rightBorderPoint.y);
		}
	}
}
