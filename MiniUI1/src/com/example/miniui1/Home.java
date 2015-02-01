package com.example.miniui1;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

// Button http://stackoverflow.com/questions/7570575/onclick-inside-fragment-called-on-activity
import android.widget.Button;
import android.view.View.OnClickListener;

public class Home extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_serversettings) {
        	// Intent for the Settings
        	Intent settingsintent = new Intent(this, SettingsActivity.class);
        	startActivity(settingsintent);
        
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements OnClickListener {
    	private final String CLASSTAG = "HOME_PLACEHOLDER_FRAGMENT";
    	
        public PlaceholderFragment() {
        
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_navigation, container, false);
            
            Button cvb = (Button) rootView.findViewById(R.id.button_checkvideo);
            Button npb = (Button) rootView.findViewById(R.id.button_newproject);
            Button mpb = (Button) rootView.findViewById(R.id.button_manageprojects);
            Button slpb = (Button) rootView.findViewById(R.id.button_startlastproject);
            
            cvb.setOnClickListener(this);
            npb.setOnClickListener(this);
            mpb.setOnClickListener(this);
            slpb.setOnClickListener(this);
            
            // Debug if true. Should perhaps be detecting a debug state
            if ( true ) {
            	Button db = (Button) rootView.findViewById(R.id.button_debug);
            	db.setOnClickListener(this);
            }
            
            return rootView;
        }

        // Add onClick
		@Override
		public void onClick(View v) {
			// Switch to right button code based on id.
			switch (v.getId()) {
	        case R.id.button_checkvideo:
	        	Log.d(CLASSTAG, "button_checkvideo pressed");
	        	Intent checkvid_intent = new Intent(getActivity(), CheckVideoActivity.class);
	        	startActivity(checkvid_intent);
	        	break;
	        case R.id.button_manageprojects:
	        	Log.d(CLASSTAG, "button_manageprojects pressed");
	        	break;
	        case R.id.button_newproject:
	        	Log.d(CLASSTAG, "button_newproject pressed");
	        	Intent newproject_intent = new Intent(getActivity(), NewProjectActivity.class);
	        	startActivity(newproject_intent);
	        	break;
	        case R.id.button_startlastproject:
	        	Log.d(CLASSTAG, "button_startlastproject pressed");
	        	break;
	        case R.id.button_debug:
	        	Log.d(CLASSTAG, "button_networktest pressed");
	        	Intent debug_intent = new Intent(getActivity(), DebugActivity.class);
	        	startActivity(debug_intent);
	        	break;
	        default:
	        	Log.e(CLASSTAG, "The impossible has happened!");
	        	break;
			}
		}
   }
}