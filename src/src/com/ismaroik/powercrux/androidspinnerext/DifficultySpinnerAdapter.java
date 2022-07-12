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

public class DifficultySpinnerAdapter extends ArrayAdapter<String>
{
	private Context mContext;
	
	private SVG mEasy;
	private SVG mMedium;
	private SVG mHard;
	
	public DifficultySpinnerAdapter(Context context, int textViewResourceId, String[] objects) 
	{
		super(context, textViewResourceId, objects);
	
		mContext = context;
		
		mEasy = SVGParser.getSVGFromResource(mContext.getResources(), R.raw.menu_difficulty_easy);
		mMedium = SVGParser.getSVGFromResource(mContext.getResources(), R.raw.menu_difficulty_medium);
		mHard = SVGParser.getSVGFromResource(mContext.getResources(), R.raw.menu_difficulty_hard);
	}
	
	public void LoadResouces()
	{
//		mEasy = SVGParser.getSVGFromResource(mContext.getResources(), R.raw.menu_difficulty_easy);
//		mMedium = SVGParser.getSVGFromResource(mContext.getResources(), R.raw.menu_difficulty_medium);
//		mHard = SVGParser.getSVGFromResource(mContext.getResources(), R.raw.menu_difficulty_hard);
	}
	
	public void FreeResources()
	{
//		mEasy = null;
//		mMedium = null;
//		mHard = null;
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
			playerImage.setImageDrawable(mEasy.createPictureDrawable());
			break;
		case 1:
			playerImage.setImageDrawable(mMedium.createPictureDrawable());
			break;
		case 2:
			playerImage.setImageDrawable(mHard.createPictureDrawable());
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
			rowImage.setImageDrawable(mEasy.createPictureDrawable());
			break;
		case 1:
			rowImage.setImageDrawable(mMedium.createPictureDrawable());
			break;
		case 2:
			rowImage.setImageDrawable(mHard.createPictureDrawable());
			break;
		}
		
		return v;
	}
}
