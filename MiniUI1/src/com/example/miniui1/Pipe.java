package com.example.miniui1;

public class Pipe {

	public Location location;
	public int pipe_dimension;
	public String pipe_material;
	public boolean spillwater = false;
	public boolean daywater = false;
	public boolean upstream = false;
	public boolean cleansed_before = false;
	public boolean previously_inspected = false;
	
	
	public Pipe(Location location, 
			int pipe_dimension, 
			String pipe_material,
			boolean spillwater, 
			boolean daywater, 
			boolean upstream, 
			boolean cleansed_before,
			boolean previously_inspected) {

		this.location = location;
		this.pipe_dimension = pipe_dimension;
		this.pipe_material = pipe_material;
		this.spillwater = spillwater;
		this.daywater = daywater;
		this.upstream = upstream;
		this.cleansed_before = cleansed_before;
		this.previously_inspected = previously_inspected;
	}

}
