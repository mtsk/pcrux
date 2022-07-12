package com.ismaroik.powercrux;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

interface IEndScreenPanelListener {
    public void onAnimationEnd();
}

// workaround for android bug for animation end event
public class EndScreenPanel extends RelativeLayout 
{
	public EndScreenPanel(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
	}
	
	public EndScreenPanel(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
	}
	
	List<IEndScreenPanelListener> listeners = new ArrayList<IEndScreenPanelListener>();

    public void addAnimationListener(IEndScreenPanelListener toAdd) {
        listeners.add(toAdd);
    }
	
	@Override
	protected void onAnimationEnd() 
	{		
		super.onAnimationEnd();
		
		// Notify everybody that may be interested.
        for (IEndScreenPanelListener hl : listeners)
            hl.onAnimationEnd();
	}
}
