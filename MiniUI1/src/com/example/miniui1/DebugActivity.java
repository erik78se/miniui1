package com.example.miniui1;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
// import android.net.Uri;

public class DebugActivity extends Activity {
	private final String CLASSTAG = "DEBUG_ACTIVITY";

	// Check connectivity: 
	// http://stackoverflow.com/questions/4101331/editing-or-creating-a-custom-web-page-not-available
	private Context ctxt;
	private boolean isConnected = true;
	
	// Shouts out project name from global in log
	public void logGlobalProjectName() {
		GlobalApplication state = ((GlobalApplication) getApplicationContext());
		Log.d(CLASSTAG, String.format("Project name: %s", state.getWorkingProject().name) );
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ctxt = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_network_test);
		
		addListenerOnButton();
		
		// Set isConnected, used to act on no-network-state
		setConnectedState();
	}

	// Manipulate 'isConnected' accordin to network status
	private void setConnectedState() {
		Log.d(CLASSTAG, "setConnectedState entered.");
		
		ConnectivityManager connectivityManager = (ConnectivityManager)
				ctxt.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		Log.d(CLASSTAG, "ConnectivityManager assigned in setConnectedState.");

		if (connectivityManager != null) {
			try {
				
				NetworkInfo info = connectivityManager.getActiveNetworkInfo();
				Log.d(CLASSTAG, "calling isAvailable().");
				Log.d(CLASSTAG, String.valueOf( info.isAvailable() ) );
				Log.d(CLASSTAG, "calling isConnected().");
				Log.d(CLASSTAG, String.valueOf( info.isConnected() ) );
				Log.d(CLASSTAG, "calling getState()");
				Log.d(CLASSTAG, info.getState().toString() );
				Log.d(CLASSTAG, "all States checked...");				
				String c = info.getState().toString();
				Log.d(CLASSTAG, c );
				
			   if (info.getState() != NetworkInfo.State.CONNECTED) {
				   // record the fact that there is not connection
				   Log.d(CLASSTAG, "NetworkInfo.State.CONNECTED seems CONNECTED.");
				   isConnected = false;
			   } else {
				   Log.d(CLASSTAG, String.format("The network state is: NetworkInfo.State.%s", c));
				   isConnected = true;
			   }
		    } catch (Exception e) {
		    	isConnected = false;
		    	Log.e("connectivity", e.toString());
		    }
		}
	}
	
	public void addListenerOnButton() {
		 
		Button button = (Button) findViewById(R.id.button_gettestimg);
 
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.d(CLASSTAG, "button button_gettestimg pressed");
				
				//Get default shared preferences (where our server url is)
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
				
				WebView webView = (WebView) findViewById(R.id.webView_testimg);
			    webView.getSettings().setJavaScriptEnabled(false);
			    
			    // Get settings for server
			    String url_from_settings = (String) prefs.getString("server_url", "null");
				String summary;
				
			    if (! isConnected ) {
					summary = "<html><body>You dont have a network.</body></html>";
					webView.loadData(summary, "text/html", null);
			    }
			    else {
					try { 
						URL url = new URL(url_from_settings); 
						webView.loadUrl( url.toString() );
					}
					catch ( MalformedURLException e) { 
						summary = "<html><body>You have a missconfigured server URL in your settings.</body></html>";
						webView.loadData(summary, "text/html", null);
					}
			    }
				// Dump Project Name
			    logGlobalProjectName();
			}
 
		});
	}
}
