package com.example.miniui1;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NewProjectActivity extends Activity {
	private final String CLASSTAG = "NEW_PROJECT_ACTIVITY";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_project);
		addListenerOnButton();
	}

	public void addListenerOnButton() {
		 
		Button button = (Button) findViewById(R.id.button_projectcreate);
 
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.d(CLASSTAG, "button buttonProjectCreate pressed");
				// Start working on the new project
				// Check for already existing project w same name
				// Create directory for the project
				// Create metadata for project
				// What more?
				// startActivity(someIntent);
			}
 
		});
	}
}
