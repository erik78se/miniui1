package com.example.miniui1;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;

public class NewProjectActivity extends Activity {
	private final String CLASSTAG = "NEW_PROJECT_ACTIVITY";
	
	// Handle storage with this
	BroadcastReceiver mExternalStorageReceiver;
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	
	private File mCurrentDir; //Will be a non null value if storage can be used.
	
	Spinner mMaterialSpinner; // materialSpinner
	
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
				checkDisk();
				String pname = (String)((EditText)findViewById(R.id.editTextProjectName)).getText().toString();
				File pdir = createNewProjectDir(pname);

                if ( pdir != null ) {
                    // Make project
                    String operator = (String) ((EditText) findViewById(R.id.editTextProjectOperator)).getText().toString();
                    String client = (String) ((EditText) findViewById(R.id.EditTextProjectClient)).getText().toString();
                    String address = (String) ((EditText) findViewById(R.id.EditTextProjectAddress)).getText().toString();
                    Project p = new Project(pdir.getName(), client, operator, address);
                    if (p.save(pdir)) {
                        ((GlobalApplication) getApplicationContext()).addProject(p);
                        ((GlobalApplication) getApplicationContext()).setWorkingProject(p);
                        Log.d(CLASSTAG, String.format("Created project: %s", p.name));
                        //Jump to CheckVideoActivity
                        Intent i = new Intent(getBaseContext(), CheckVideoActivity.class);
                        startActivity(i);
                    } else {
                        Log.e(CLASSTAG, "Didnt create Project, couldnt save it.");
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(),"Couldnt create new project.",
                            Toast.LENGTH_LONG).show();
                }
			}
		});
	}
	
	
	// Check disk space, for now always OK
	void checkDisk() {
		if ( mCurrentDir != null) {
			Log.d(CLASSTAG, "external storage can be used.");
			Log.d(CLASSTAG, "Total space on device: " + String.valueOf( mCurrentDir.getTotalSpace() ) );
		} else {
            Log.e(CLASSTAG, String.format(("external storage can NOT be used, available. Avail: %s, Write: %s"),
					mExternalStorageAvailable, mExternalStorageWriteable));
            Toast.makeText(getApplicationContext(),"External Storage not available. Will not procede.",
                           Toast.LENGTH_LONG).show();
		}
	}
	
	// Create a new project dir.
		File createNewProjectDir(String name) {
			if ( ! name.isEmpty() ) {
				Log.d(CLASSTAG, String.format("Creating a project with name = %s", name));
				// Do create
			} else {
				Log.d(CLASSTAG, "Empty project name, will not create project."); 
			}
			
			File folder = new File(getExternalFilesDir(null), name);
			
			if ( folder.mkdir() ) {
				Log.d(CLASSTAG, String.format("mkdir = true (Project directory %s was created.)", folder.getName() ));
			} else {
				Log.d(CLASSTAG, String.format("mkdir = false (Project directory %s existed already?)", folder.getName() ));
                folder = null;
			}
			return folder;
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
	

