package com.example.miniui1;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

// EXAMPLE FROM: http://www.vogella.com/tutorials/AndroidListView/article.html
public class ManageProjectActivity extends ListActivity implements  View.OnClickListener {

    //Use for project content string format.
    private String nameFormat = "Name: %s";
    protected String CLASSTAG = "MAIN_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //All projects goes here.
        ArrayList<Project> projects = ((GlobalApplication) getApplicationContext()).gProjects;

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getListView().setSelector(android.R.color.darker_gray);

        ProjectArrayAdapter adapter = new ProjectArrayAdapter(this, projects) {
            //Set tag on syncswitches
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                // Make it possible to find in onClick.
                row.findViewById(R.id.syncswitch).setTag(position);
                row.findViewById(R.id.syncswitch).setOnClickListener(ManageProjectActivity.this);
                
                // This setOnTouchListener disables switching for the swtichbutton to allow only clicks.
                row.findViewById(R.id.syncswitch).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_MOVE) {
                            return true;
                        }
                        return false;
                    }
                });

                row.findViewById(R.id.progressBar).setTag("progressbar-"+position);

                return row;
            }
        };
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Project project = (Project) getListAdapter().getItem(position);
        Toast.makeText(this, project.name+ " selected", Toast.LENGTH_LONG).show();
    }

    // Handle syncswitch clicks
    @Override
    public void onClick(View v) {
        // Switch to right button code based on id.
        switch ( v.getId() ) {
            case R.id.syncswitch:
                Log.d(CLASSTAG, v.getTag().toString());
                SyncProjectTask task = new SyncProjectTask();
                task.setSwitchButton((Switch) v.findViewWithTag(v.getTag()));
                task.setProgressBar((ProgressBar) v.getRootView().
                        findViewWithTag("progressbar-" + v.getTag()));
                task.bar.setProgress(0);
                if ( task.switchbutton.isChecked() ) {
                    //disable -> until task is done.
                    task.switchbutton.setClickable(false);
                    task.execute("rubbish");
                } else {
                    Toast.makeText(this, "Sync off for: " + v.getTag(), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                Log.e(CLASSTAG, "The impossible has happened!");
                break;
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     *
     * Private class to handle threads doing progress things.
     *
     */

    private class SyncProjectTask extends AsyncTask<String, Integer, Integer> {

        ProgressBar bar;
        Switch switchbutton;

        public void setProgressBar(ProgressBar bar) {
            this.bar = bar;
        }

        public void setSwitchButton(Switch s) {
            this.switchbutton = s;
        }

        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
        protected Integer doInBackground(String... files) {
            int count = 4;
            Integer result = 0;
            for (int i = 0; i <= count; i++) {
                result++;
                SystemClock.sleep(2000);
                bar.setProgress((int) ((i / (float) count) * 100));
                // Escape early if cancel() is called
                if (isCancelled()) break;
            }
            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (this.bar != null) {
                bar.setProgress(values[0]);
            }
        }

        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
        protected void onPostExecute(Integer result) {
            Log.d("POSTEXE", result.toString());
            switchbutton.setClickable(true);

        }
    }


}
