package com.ismaroik.powercrux.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ismaroik.powercrux.R;

public class HelpDialogFragment extends DialogFragment {
	
	View mView;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		// Inflate the layout to use as dialog or embedded fragment
		mView = inflater.inflate(R.layout.dialog_help, container, false);
        return mView;
    }
	



	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) 
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        LayoutInflater inflater = getActivity().getLayoutInflater();
        
        builder.setView(inflater.inflate(R.layout.dialog_help, null))        
               .setPositiveButton(R.string.dialog_help_ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) 
                   {
                	   
                   }
               })
               .setNeutralButton(R.string.dialog_help_video, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) 
                   {
                	   Intent intent = new Intent(Intent.ACTION_VIEW);
	                   intent.setData(Uri.parse("http://ismaroik.com/"));
	                   startActivity(intent);
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		
		// reclaim memory used up by help screen
		System.gc();
	}
}
