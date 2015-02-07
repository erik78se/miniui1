package com.example.miniui1;

import java.util.Date;


public class Project {
	public static final String STATUS_OPEN = "open";
	public static final String STATUS_CLOSED = "closed";
	public static final String STATUS_REOPENED = "reopened";
	
	public String name;
	public String client;
	public String operator;
	public String address;
	public String pipe_material;
	public Date start_time;
	public Date end_time;
	public String status;
	public boolean spillwater = false;
	public boolean daywater = false;
	public boolean upstream = false;
	public boolean downstream = false;
	public boolean cleansed_before = false;
	public boolean previously_inspected = false;
	public String datafolder;
	

	public Project(String name, String client, String operator, String address) {
		this.name = name;
		this.client = client;
		this.operator = operator;
		this.address = address;
		this.start_time = new Date();
		this.end_time = null;              //use null as a indicator for a non set end time
		this.status = Project.STATUS_OPEN;
		this.datafolder = name; //TODO: change this to use a real os-path?
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
	
}
