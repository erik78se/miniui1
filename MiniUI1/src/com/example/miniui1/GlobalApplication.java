package com.example.miniui1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class GlobalApplication extends Application {
	
	List<Project> gProjects = new ArrayList<Project>();
	private Project gWorkingProject;

	private static GlobalApplication singleton;

	public static GlobalApplication getInstance() {
		return singleton;
	}
	

	public void setWorkingProject(Project p) {
		gWorkingProject = p;
	}
	public Project getWorkingProject() {
		return gWorkingProject;
	}
	
	public void addProject(Project p) {
		Log.d("GLOBAL", String.format("addProject(<%s>)", p.name ));
		this.gProjects.add(p);
	}
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		singleton = this;
		// Now prepare the Application singleton.
		if (! populateProjects() ) {
			Toast.makeText(getApplicationContext(), "No project found, start a new one.", Toast.LENGTH_LONG).show();
		} else {
			setWorkingProject((Project) getLatestProject());
		}
	}
	
	
	// Try populate the "allProjects" with projects found on disk
	private boolean populateProjects() {
		File appbasepath = getExternalFilesDir(null);
		File[] project_dirs = appbasepath.listFiles();
		for ( File pdir : project_dirs ) {
			String pfn = pdir.toString() + "/project.json";
			java.io.File projectfile = new java.io.File(pfn);
			if ( projectfile.exists() ) {
				Log.d("GLOBAL", String.format("Project file found: %s", pdir.getName() ) );
				Gson gson = new Gson();
				try {
					BufferedReader br = new BufferedReader( new FileReader(pfn));  
					//convert the json string back to object
					Project projectObj = gson.fromJson(br, Project.class);
					//add it to the list of projects
					this.addProject( projectObj );
					} 
				catch ( JsonSyntaxException jse ) {
					Log.e("GLOBAL", String.format("Project file has JsonSyntaxException issues: %s", pdir.getName() ) );
					jse.printStackTrace();
				}
				catch ( JsonIOException jioe ) {
					Log.e("GLOBAL", String.format("Project file has JsonIOException issues: %s", pdir.getName() ) );
					jioe.printStackTrace();
				}
				catch ( FileNotFoundException e ) {
					e.printStackTrace();
				}
			} else {
				Log.d("GLOBAL", String.format("No project in: %s", pfn  ) );
			}
		}
		// Tell caller if we managed to read some projects or not
		return (! gProjects.isEmpty());
	}
	
	
	// Looks in the "getExternalFilesDir" folder
	// picks out the file/folder with the last creation date.
	// uses that for latest project.
	// TODO: read the json metadata files to properly create a list of project
	public boolean setLatestWorkingProject() {
		File appbasepath = getExternalFilesDir(null);
		File[] project_dirs = appbasepath.listFiles();
		long lastMod = Long.MIN_VALUE;
		File choice = null;
		for (File projdir : project_dirs) {
			String pfn = appbasepath.toString() + "/" + projdir.toString() + "/project.json";
			java.io.File projectfile = new java.io.File(pfn);
			Log.d("GLOBAL", String.format("Project File found with age: %s,  %s", projdir.getName(), String.valueOf( projdir.lastModified() )) );
		    if (projdir.lastModified() > lastMod && projectfile.exists() ) {
		    	choice = projdir;
		        lastMod = projdir.lastModified();
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
	
	public Project getLatestProject() {
		// For now, just return any
		return gProjects.get(0);
	}
	
}
