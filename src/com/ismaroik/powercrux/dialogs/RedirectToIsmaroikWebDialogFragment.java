package com.ismaroik.powercrux.dialogs;

import com.ismaroik.powercrux.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class RedirectToIsmaroikWebDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_ismaroik_url)
               .setPositiveButton(R.string.dialog_ismaroik_url_ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) 
                   {
                	   Intent intent = new Intent(Intent.ACTION_VIEW);
	                   intent.setData(Uri.parse("http://ismaroik.com/pcrux.htm"));
	                   startActivity(intent);
                   }
               })
               .setNegativeButton(R.string.dialog_ismaroik_url_cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
