package com.example.miniui1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * This singleton holds application global stuff.
 *
 * The SharedPreference: working_project
 * is the preference to find the project to work with.
 *
 */

public class GlobalApplication extends Application {
	
	ArrayList<Project> gProjects = new ArrayList<Project>();
	private Project gWorkingProject;

	private static GlobalApplication singleton;

	public static GlobalApplication getInstance() {
		return singleton;
	}


	@Override
	public void onCreate() {
		super.onCreate();
		singleton = this;

        // Try to get stored working project
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String stored_wp = preferences.getString("working_project", null);

		if ( populateProjects() ) {
            Project pObj = null;
            for(Project p: gProjects){
                try {
                    if (p.name.equals(stored_wp)) {
                        pObj = p;
                        setWorkingProject(pObj);
                    }
                } catch (NullPointerException ne) {
                    setWorkingProject( getLatestProject() );
                }
            }
        }
	}

    public void setWorkingProject(Project p) {
        //Set workingProject
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("working_project", p.name);
        editor.commit();

        gWorkingProject = p;
    }

    public Project getWorkingProject() {
        return gWorkingProject;
    }

    public void addProject(Project p) {
        Log.d("GLOBAL", String.format("addProject(<%s>)", p.name ));
        this.gProjects.add(p);
    }

	// Try populate the "allProjects" with projects found on disk
	private boolean populateProjects() {
        File[] project_dirs;
		File appbasepath = getExternalFilesDir(null);
        try {
            project_dirs = appbasepath.listFiles();
        } catch (NullPointerException e) {
            return false;
        }
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
					Log.d("GLOBAL","The project: " + projectObj.toString() );
					//add it to the list of projects
					this.addProject( projectObj );
					}
				catch ( JsonSyntaxException jse ) {
					Log.e("GLOBAL", String.format("Project file has JsonSyntaxException issues: %s",
                            pdir.getName() ) );
                    jse.printStackTrace();
				}
				catch ( JsonIOException jioe ) {
					Log.e("GLOBAL", String.format("Project file has JsonIOException issues: %s",
                            pdir.getName() ) );
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


    //TODO: Perhaps return something else
	public Project getLatestProject() {
        return gProjects.get(0);
    }
}
