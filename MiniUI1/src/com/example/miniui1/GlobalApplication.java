package com.example.miniui1;

import java.io.File;

import android.app.Application;
import android.util.Log;

public class GlobalApplication extends Application {
	
	private String gProjectName;

	private static GlobalApplication singleton;

	public static GlobalApplication getInstance() {
	return singleton;
	}

	public String getProjectName() {
		return gProjectName;
	}
	
	public void setProjectName(String n) {
		Log.d("GLOBAL", String.format("setProjectName(%s)", n ));
		this.gProjectName = n;
	}
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		singleton = this;
		setLatestProject();
	}
	
	// Looks in the "getExternalFilesDir" folder
	// picks out the file/folder with the last creation date.
	// uses that for latest project.
	private void setLatestProject() {
		File folder = getExternalFilesDir(null);
		File[] files = folder.listFiles();
		    long lastMod = Long.MIN_VALUE;
		    File choice = null;
		    for (File file : files) {
		    	Log.d("GLOBAL", String.format("File age: %s,  %s", file.getName(), String.valueOf( file.lastModified() )) );
		        if (file.lastModified() > lastMod) {
		            choice = file;
		            lastMod = file.lastModified();
		        }
		    }
		 setProjectName( choice.getName() );	
	}
	
}
