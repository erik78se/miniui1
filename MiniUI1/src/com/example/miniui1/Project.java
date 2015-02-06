package com.example.miniui1;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

public class Project {
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
	public boolean pipe_cleansed = false;
	public boolean previously_inspected = false;
	public String datafolder;
	

	public Project(String name, String client, String operator, String address) {
		this.name = name;
		this.client = client;
		this.operator = operator;
		this.address = address;
		this.start_time =  new Date(DateFormat.LONG);
		this.status = "open";
		this.datafolder = name; //TODO: change this to use a real os-path?
	}
	
}
