package com.example.miniui1;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

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


	void populateMaterialsSpinner() {
		mMaterialSpinner = (Spinner) findViewById(R.id.spinnerMaterial); 
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.pipematerial, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		mMaterialSpinner.setAdapter(adapter);
	}
	
	
	public void addListenerOnButton() {
		 
		Button button = (Button) findViewById(R.id.button_projectcreate);
 
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.d(CLASSTAG, "button buttonProjectCreate pressed");
				checkDiskSpace();
				
				String pname = (String)((EditText)findViewById(R.id.editTextProjectName)).getText().toString();
				File pdir = createProjectDir(pname);
				if ( createProject(pdir) ) {
					Log.d(CLASSTAG, "Created project");
				} else {
					Log.d(CLASSTAG, "Didnt create Project.");
				}
					// TODO Auto-generated catch block
				
				// What more?
				// startActivity(someIntent);
			}
 
		});
	}

	// Create a json file (metadata)
	/** { project:
	 * 		{ start-time: <Date:Time>,
	 * 		  close-time: <Date:Time>,
			  status: <open|closed|reopened>,
			  name: <String>,
			  client-name: <String>,
			  creator: <String>,
			  tag: <String>Pipematerial
		}
	 * @throws JSONException 
	 * @throws IOException 
**/
	boolean createProject(File project_dir) {
		String operator = (String)((EditText)findViewById(R.id.editTextProjectOperator)).getText().toString();
		String client = (String)((EditText)findViewById(R.id.EditTextProjectClient)).getText().toString();
		String address = (String)((EditText)findViewById(R.id.EditTextProjectAddress)).getText().toString();
		// Create a Project with name from the File
		String name = project_dir.getName();
		Project mProject = new Project(name, client, operator, address);
		Gson gson = new Gson();
		String json = gson.toJson(mProject);  
		
		Log.d(CLASSTAG, json);
		
		byte[] content;
		try {
			content = json.getBytes("utf-8");
		} catch (UnsupportedEncodingException ue) {
			// TODO Auto-generated catch block
			Log.e("UnsupportedEncodingException in createMetaFile()", ue.getMessage());
			return false;
		}
				
		Log.d(CLASSTAG, String.format("About to create file in path : %s)", project_dir.getAbsolutePath() ));

		try {
			OutputStream fOut = null;

			File file = new File(project_dir, "metadata.json");
	
			if ( file.createNewFile() ) {
				fOut = new FileOutputStream(file);
				Log.d(CLASSTAG, String.format("Success creating file: %s)", file.getAbsolutePath() ));
				fOut.write(content);
				fOut.flush();
				fOut.close();
				// All has gone well, so sage to add it to global application
				((GlobalApplication) getApplicationContext()).addProject(mProject);
			}
		}
		catch (IOException e) {
			Log.e("IOException in createMetaFile()", e.getMessage());
			return false;
		}

		return true;
	}
	
	
	// Check disk space, for now always OK
	void checkDiskSpace() {
		if ( mCurrentDir != null) {
			Log.d(CLASSTAG, "external storage can be used.");
			Log.d(CLASSTAG, "Total space on device: " + String.valueOf( mCurrentDir.getTotalSpace() ) );
		} else {
			Log.d(CLASSTAG, String.format(("external storage can NOT be used, available. Avail: %s, Write: %s"), 
					mExternalStorageAvailable, mExternalStorageWriteable));
		}
	}
	
	// Create a new project dir.
	File createProjectDir(String name) {
		if ( ! name.isEmpty() ) {
			Log.d(CLASSTAG, String.format("Creating a project with name = %s", name));
			// Do create
		} else {
			Log.d(CLASSTAG, "Empty project name, will not create project."); 
			}
		
		File folder = new File(getExternalFilesDir(null), name);
		// Create here local dir with name of project
		// File sdCard = Environment.getExternalStorageDirectory();
		// File folder = new File(sdCard.getAbsolutePath() + "/" + name);
		
		if ( folder.mkdir() ) {
			Log.d(CLASSTAG, String.format("mkdir = true (Project directory %s was created.)", folder.getName() ));
		} else {
			Log.d(CLASSTAG, String.format("mkdir = false (Project directory %s existed already?)", folder.getName() ));
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
	

