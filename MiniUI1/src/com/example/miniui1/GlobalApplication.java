package com.example.miniui1;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.util.Log;

public class GlobalApplication extends Application {
	
	List<Project> allProjects = new ArrayList<Project>();
	private Project gWorkingProject;

	private static GlobalApplication singleton;

	public static GlobalApplication getInstance() {
	return singleton;
	}

	public String getProjectName() {
		return gWorkingProject.name;
	}
	
	//TODO: Once moved all project into a database,
	// change this to work on that instead
	public void setWorkingProject(Project p) {
		Log.d("GLOBAL", String.format("setWorkingProjectName(%s)", p.name ));
		this.gWorkingProject = p;
	}
	
	public void addProject(Project p) {
		Log.d("GLOBAL", String.format("addProject(<%s>)", p.name ));
		this.allProjects.add(p);
	}
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		singleton = this;
		setLatestWorkingProject();
	}
	
	// Looks in the "getExternalFilesDir" folder
	// picks out the file/folder with the last creation date.
	// uses that for latest project.
	// TODO: read the json metadata files to properly create a list of project
	public boolean setLatestWorkingProject() {
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
		if ( choice != null ) {
			//TODO: use real project data here.
			Project p = new Project(choice.getName(), "dummy-a", "dummy-b", "dummy-c");
			setWorkingProject( p );
			return true;
		} else {
			return false;
		}
	}
	
}
