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

public class PlayersSpinnerAdapter extends ArrayAdapter<String>
{
	private Context mContext;
	
	private SVG m2Players;
	private SVG m3Players;
	private SVG m4Players;
	
	public PlayersSpinnerAdapter(Context context, int textViewResourceId, String[] objects) 
	{
		super(context, textViewResourceId, objects);
	
		mContext = context;
		
		m2Players = SVGParser.getSVGFromResource(mContext.getResources(), R.raw.menu_players_2);
		m3Players = SVGParser.getSVGFromResource(mContext.getResources(), R.raw.menu_players_3);
		m4Players = SVGParser.getSVGFromResource(mContext.getResources(), R.raw.menu_players_4);
	}
	
	public void LoadResouces()
	{
//		m2Players = SVGParser.getSVGFromResource(mContext.getResources(), R.raw.menu_players_2);
//		m3Players = SVGParser.getSVGFromResource(mContext.getResources(), R.raw.menu_players_3);
//		m4Players = SVGParser.getSVGFromResource(mContext.getResources(), R.raw.menu_players_4);
	}
	
	public void FreeResources()
	{
//		m2Players = null;
//		m3Players = null;
//		m4Players = null;
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

	public boolean areAllItemsEnabled() 
	{
//        return false;
        return true;
    }	
	
	public boolean isEnabled(int position) 
	{
//		if(position == 0)
//		{
//        	return false;
//		}
//		else
//		{
//			return true;
//		}
		return true;
    }
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public View getMainView(int position, View convertView, ViewGroup parent) 
	{
		View v = convertView;
        if (v == null) 
        {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.spinnerplayer_main, parent, false);
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
			playerImage.setImageDrawable(m2Players.createPictureDrawable());
			break;
		case 1:
			playerImage.setImageDrawable(m3Players.createPictureDrawable());
			break;
		case 2:
			playerImage.setImageDrawable(m4Players.createPictureDrawable());
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
			rowImage.setImageDrawable(m2Players.createPictureDrawable());
			break;
		case 1:
			rowImage.setImageDrawable(m3Players.createPictureDrawable());
			break;
		case 2:
			rowImage.setImageDrawable(m4Players.createPictureDrawable());
			break;
		}
		
		return v;
	}
}
