package com.ismaroik.powercrux.androidspinnerext;

import com.ismaroik.powercrux.R;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class ModeSpinnerAdapter extends ArrayAdapter<String>
{
	private Context mContext;
	private SVG mHitIt;
	private SVG mCoolIt;
	
	public ModeSpinnerAdapter(Context context, int textViewResourceId, String[] objects) 
	{
		super(context, textViewResourceId, objects);
	
		mContext = context;
		
		mHitIt = SVGParser.getSVGFromResource(mContext.getResources(), R.raw.menu_mode_hitit);
		mCoolIt = SVGParser.getSVGFromResource(mContext.getResources(), R.raw.menu_mode_coolit);
	}
	
	public void LoadResouces()
	{
//		mHitIt = SVGParser.getSVGFromResource(mContext.getResources(), R.raw.menu_mode_hitit);
//		mCoolIt = SVGParser.getSVGFromResource(mContext.getResources(), R.raw.menu_mode_coolit);
	}
	
	public void FreeResources()
	{
//		mHitIt = null;
//		mCoolIt = null;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) 
	{
		return getCustomView(position, convertView, parent);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		return getMainView(position, convertView, parent);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public View getMainView(int position, View convertView, ViewGroup parent) 
	{
		View v = convertView;
        if (v == null) 
        {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.spinner_main, parent, false);
        }
		
		ImageView playerImage = (ImageView)v.findViewById(R.id.playerImage);
		// ensure that hardware acceleration is off
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
        {
        	playerImage.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
		
		switch(position)
		{
		case 0:
			playerImage.setImageDrawable(mCoolIt.createPictureDrawable());
			break;
		case 1:
			playerImage.setImageDrawable(mHitIt.createPictureDrawable());
			break;
		}
		
		return v;
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public View getCustomView(int position, View convertView, ViewGroup parent) 
	{
		View v = convertView;
        if (v == null) 
        {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.spinner_row, parent, false);
        }
		
		ImageView rowImage = (ImageView)v.findViewById(R.id.rowImage);
		// ensure that hardware acceleration is off
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
        {
        	rowImage.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
		
		switch(position)
		{
		case 0:
			rowImage.setImageDrawable(mCoolIt.createPictureDrawable());
			break;
		case 1:
			rowImage.setImageDrawable(mHitIt.createPictureDrawable());
			break;
		}
		
		return v;
	}
}
