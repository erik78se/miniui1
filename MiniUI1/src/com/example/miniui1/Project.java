package com.example.miniui1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;

import android.util.Log;

import com.google.gson.Gson;


public class Project {
	public static final String STATUS_OPEN = "open";
	public static final String STATUS_CLOSED = "closed";
	public static final String STATUS_REOPENED = "reopened";
	
	public String name;
	public String client;
	public String operator;
	public String address;
	public Date start_time;
	public Date end_time;
	public String status;
	public String datafolder;
	public ArrayList<Observation> observations;

	
	
	public Project(String name, String client, String operator, String address) {
		this.name = name;
		this.client = client;
		this.operator = operator;
		this.address = address;
		this.start_time = new Date();
		this.end_time = null;              //use null as a indicator for a non set end time
		this.status = Project.STATUS_OPEN;
		this.datafolder = name; //TODO: change this to use a real os-path?
		this.observations = new ArrayList<Observation>();
	}
	
	public void close() {
		this.end_time = new Date();
		this.status = Project.STATUS_CLOSED;
	}
	
	public void open() {
		if ( this.status.equalsIgnoreCase( Project.STATUS_CLOSED ) ) {
			this.status = Project.STATUS_REOPENED;
		}
	}
	
	public void addObservation(Observation observation) {
			this.observations.add(observation);
			Log.d("PROJECT", "Added an observation");
	}
	
	// Saves the project to "disk"
	public boolean save(File toFile) {
		Log.d("PROJECT", String.format("About to save project at : %s)", toFile.getAbsolutePath() ));
		Log.d("PROJECT", "Saving observations: " + this.observations.size() );
		byte[] content = null;
		try {
			Gson gson = new Gson();
			String project_json = gson.toJson(this);
			
			Log.d("PROJECT", project_json);
			
			content = project_json.getBytes("utf-8");
			OutputStream fOut = null;
			File file = new File(toFile, "project.json");
			Log.d("PROJECT", "Writing to: " + file.getAbsolutePath().toString() );
			
			fOut = new FileOutputStream(file);
			fOut.write(content);
			fOut.flush();
			fOut.close();
			Log.d("PROJECT", String.format("Success creating project json file: %s)", file.getAbsolutePath() ));
			return true;
		}
		catch (UnsupportedEncodingException ue) {
			Log.e("PROJECT", "UnsupportedEncodingException", ue);
		}
		catch (IOException e) {
			Log.e("PROJECT", "IOException", e);
		}
		Log.e("PROJECT", String.format("Failed saving project to disk: %s", toFile.getAbsolutePath()));
		return false;
	}
	
	
	public String toString() {
		String obs = "";
		if ( this.observations != null ) {
			obs = this.observations.toString();
		}
		return String.format("Project[name: %s, address: %s, folder: %s, obs %s]", this.name, this.address, this.datafolder, obs);
	}
	
}
