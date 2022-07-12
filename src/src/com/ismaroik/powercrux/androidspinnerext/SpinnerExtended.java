package com.ismaroik.powercrux.androidspinnerext;

import java.lang.reflect.Method;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class SpinnerExtended extends Spinner 
{
	private int lastSelected = 0;
    private static Method s_pSelectionChangedMethod = null;

    static {        
        try {
            Class<?> noparams[] = {};

            s_pSelectionChangedMethod = AdapterView.class.getDeclaredMethod("selectionChanged", noparams);            
            if (s_pSelectionChangedMethod != null) {
                s_pSelectionChangedMethod.setAccessible(true);              
            }

        } catch( Exception e ) {
            Log.e("Custom spinner, reflection bug:", e.getMessage());
            throw new RuntimeException(e);
        }
    }

	public SpinnerExtended(Context context, AttributeSet attrs, int defStyle) { 
	    super(context, attrs, defStyle); 
	} 
	public SpinnerExtended(Context context) { 
	    super(context); 
	} 
	public SpinnerExtended(Context context, AttributeSet attrs) { 
	    super(context, attrs); 
	}
	
	public void testReflectionForSelectionChanged() {
        try {
        	Object noparams[] = {};          
            s_pSelectionChangedMethod.invoke(this, noparams);
        } catch (Exception e) {
            Log.e("Custom spinner, reflection bug: ", e.getMessage());
            e.printStackTrace();                
        }
    } 

    @Override
    public void onClick(DialogInterface dialog, int which) {    
        super.onClick(dialog, which);
            if(lastSelected == which)
            {
                testReflectionForSelectionChanged();
            }

            lastSelected = which;
    }
    
    public void setInitialSelection(int position)
    {
    	lastSelected = position;
    	super.setSelection(position);
    }
    
	@Override 
	public boolean performClick() {

	    // boolean handled = super.performClick(); => this line removed, we do not want to delegate the click to the spinner.

	    Context context = getContext();

	    final DropDownAdapter adapter = new DropDownAdapter(getAdapter());

	    CharSequence mPrompt = getPrompt();

	    AlertDialog.Builder builder = new AlertDialog.Builder(context); 
	    if (mPrompt != null) { 
	        builder.setTitle(mPrompt); 
	    } 
	    builder.setSingleChoiceItems(adapter, getSelectedItemPosition(), this).show();

	    return true; 
	}

	private static class DropDownAdapter implements ListAdapter, SpinnerAdapter { 
	    private SpinnerAdapter mAdapter;

	    public DropDownAdapter(SpinnerAdapter adapter) { 
	        mAdapter = adapter; 
	    }

	    public int getCount() { 
	        return mAdapter == null ? 0 : mAdapter.getCount(); 
	    }

	    public Object getItem(int position) { 
	        return mAdapter == null ? null : mAdapter.getItem(position); 
	    }

	    public long getItemId(int position) { 
	        return mAdapter == null ? -1 : mAdapter.getItemId(position); 
	    }

	    public View getView(int position, View convertView, ViewGroup parent) { 
	        return getDropDownView(position, convertView, parent); 
	    }

	    public View getDropDownView(int position, View convertView,ViewGroup parent) { 
	        return mAdapter == null ? null : mAdapter.getDropDownView(position, convertView, parent); 

	    }

	    public boolean hasStableIds() { 
	        return mAdapter != null && mAdapter.hasStableIds(); 
	    }

	    public void registerDataSetObserver(DataSetObserver observer){ 
	        if (mAdapter != null) { 
	            mAdapter.registerDataSetObserver(observer); 
	        } 
	    }

	    public void unregisterDataSetObserver(DataSetObserver observer) { 
	        if (mAdapter != null) { 
	            mAdapter.unregisterDataSetObserver(observer); 
	        } 
	    }

	    // PATCHED 
	    public boolean areAllItemsEnabled() { 
	        if (mAdapter instanceof BaseAdapter) { 
	            return ((BaseAdapter) mAdapter).areAllItemsEnabled(); 
	        } else { 
	            return true; 
	        } 
	    }

	    // PATCHED 
	    public boolean isEnabled(int position) { 
	        if (mAdapter instanceof BaseAdapter) { 
	            return ((BaseAdapter) mAdapter).isEnabled(position); 
	        } else { 
	            return true; 
	        } 
	    }

	    public int getItemViewType(int position) { 
	        return 0; 
	    }

	    public int getViewTypeCount() { 
	        return 1; 
	    }

	    public boolean isEmpty() { 
	        return getCount() == 0; 
	    } 
	}	
}
