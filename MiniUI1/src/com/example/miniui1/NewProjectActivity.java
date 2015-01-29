package com.example.miniui1;


import java.io.File;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NewProjectActivity extends Activity {
	private final String CLASSTAG = "NEW_PROJECT_ACTIVITY";
	
	// Handle storage with this
	BroadcastReceiver mExternalStorageReceiver;
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	
	//Identify the current Project we are working on
	//Perhaps model projects as a Class?
	private String workingProject;
	private File mCurrentDir; //Will be a non null value if storage can be used.
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_project);
		updateExternalStorageState();
        startWatchingExternalStorage();
		addListenerOnButton();
	}

	public void addListenerOnButton() {
		 
		Button button = (Button) findViewById(R.id.button_projectcreate);
 
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.d(CLASSTAG, "button buttonProjectCreate pressed");
				if ( mCurrentDir != null) {
					Log.d(CLASSTAG, "external storage can be used.");
					Log.d(CLASSTAG, "Total space on device: " + String.valueOf( mCurrentDir.getTotalSpace() ) );
				} else {
					Log.d(CLASSTAG, String.format(("external storage can NOT be used, available. Avail: %s, Write: %s"), 
							mExternalStorageAvailable, mExternalStorageWriteable));
				}
				// Start working on the new project
				// Allocate space: http://stackoverflow.com/questions/2130932/how-to-create-directory-automatically-on-sd-card
				// Check for already existing project w same name
				// Create directory for the project
				// Create metadata for project
				// What more?
				// startActivity(someIntent);
			}
 
		});
	}

	
	// Update the storage states (mExternalStorageAvailable, mExternalStorageWriteable).
	void updateExternalStorageState() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        mExternalStorageAvailable = mExternalStorageWriteable = true;
	    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        mExternalStorageAvailable = true;
	        mExternalStorageWriteable = false;
	    } else {
	        mExternalStorageAvailable = mExternalStorageWriteable = false;
	    }
	    
	    handleExternalStorageState(mExternalStorageAvailable, mExternalStorageWriteable);
	}

	void startWatchingExternalStorage() {
	    mExternalStorageReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            Log.i("test", "Storage: " + intent.getData());
	            updateExternalStorageState();
	        }
	    };
	    IntentFilter filter = new IntentFilter();
	    filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
	    filter.addAction(Intent.ACTION_MEDIA_REMOVED);
	    registerReceiver(mExternalStorageReceiver, filter);
	    updateExternalStorageState();
	}

	void stopWatchingExternalStorage() {
	    unregisterReceiver(mExternalStorageReceiver);
	}
	
	void handleExternalStorageState(boolean isAvailable, boolean isWritable) {
		if ( isAvailable && isWritable ) {
			mCurrentDir = Environment.getExternalStorageDirectory();
		} else {
			Log.w(CLASSTAG, "getExternalStorageDirectory is not ready.");
			mCurrentDir = null;
			//TODO: Inform the user here.
		}
	}
}
	

