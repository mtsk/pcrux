package com.ismaroik.powercrux;

public class Interval {
	private float mLeft;
    private float mRight;

    public Interval(float left, float right) {
        assert (left <= right);
        this.mLeft  = left;
        this.mRight = right;
    }

    public float getLeft()  { return mLeft;  }
    public float getRight() { return mRight; }

    // does this interval a intersect b?
    public boolean intersects(Interval b) {
        
        if(this.mRight < b.getLeft() || this.mLeft > b.getRight())
        {
        	return false;
        }
        else
        {
        	return true;
        }
    }
    
    public Interval Clone()
    {
    	return new Interval(this.mLeft, this.mRight);
    }
    
    // returns new interval which is union of the two, null if intervals do not intersect
    public Interval union(Interval b)
    {
    	Interval result = null;
    	
    	if(this.intersects(b))
    	{
    		//result of union is Min(left), Max(right)
    		result = new Interval(
    				Math.min(this.mLeft, b.getLeft()),
    				Math.max(this.mRight, b.getRight())
    				);
    	}
    	
    	return result;
    }
    
    public Interval intersect(Interval b)
    {
    	Interval result = null;
    	
    	if(this.intersects(b))
    	{
    		//result of union is Max(left), Min(right)
    		result = new Interval(
    				Math.max(this.mLeft, b.getLeft()),
    				Math.min(this.mRight, b.getRight())
    				);
    	}
    	
    	return result;
    }
}

