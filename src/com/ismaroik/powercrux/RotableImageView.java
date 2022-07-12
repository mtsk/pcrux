package com.ismaroik.powercrux;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RotableImageView extends ImageView {
	
	public int angle = 0;
	
	public RotableImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public RotableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
	
	public RotableImageView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
	}
	
	@Override
	public void draw(Canvas canvas) {
	    canvas.save();
	    canvas.rotate(angle,getWidth()/2,getHeight()/2);
	    super.draw(canvas);
	    canvas.restore();
	}
}
