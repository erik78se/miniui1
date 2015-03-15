package com.example.miniui1;

public class Observation {
	
	public String grade;
	public int distance = 0;
	public String comment = "Start";
	public Pipe pipe;
	public String picture;
    public boolean uncertainty = false; //Indicate if the observation has uncertainty.

	public static final String GRADE_1 = "1";
	public static final String GRADE_2 = "2";
	public static final String GRADE_3 = "3";
	public static final String GRADE_4 = "4";

	
	
	public Observation(Pipe pipe, String grade, int distance, String comment) {
		this.grade = grade;
		this.pipe = pipe;
		this.distance = distance;
		this.comment = comment;
	}

    public Observation(Pipe pipe, String grade, int distance, String comment, boolean uncertainty) {
        this(pipe,grade,distance,comment);
        this.uncertainty = uncertainty;
    }

    public void setUncertain(boolean u) {
        this.uncertainty = u;
    }

	public void setPictureFileName(String picture) {
		this.picture = picture;
	}
	
	public String getPictureFileName() {
		return picture;
	}
	
}
