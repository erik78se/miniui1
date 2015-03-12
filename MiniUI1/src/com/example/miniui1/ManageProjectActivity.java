package com.example.miniui1;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.ChunkedUploadRemoteFileOperation;
import com.owncloud.android.lib.resources.files.CreateRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.DownloadRemoteFileOperation;
import com.owncloud.android.lib.resources.files.ExistenceCheckRemoteOperation;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.RemoveRemoteFileOperation;
import com.owncloud.android.lib.resources.files.UploadRemoteFileOperation;
import com.owncloud.android.lib.resources.status.GetRemoteStatusOperation;

import java.io.File;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.owncloud.android.lib.common.OwnCloudClientFactory.createOwnCloudClient;
import static com.owncloud.android.lib.common.OwnCloudCredentialsFactory.*;

// EXAMPLE FROM: http://www.vogella.com/tutorials/AndroidListView/article.html
public class ManageProjectActivity extends ListActivity implements  View.OnClickListener {

    //Use for project content string format.
    private String nameFormat = "Name: %s";
    protected String CLASSTAG = "MAIN_ACTIVITY";
    private OwnCloudClient mClient;


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

        /** Setup OwncloudClient **/
        SharedPreferences prefs = PreferenceManager.
                getDefaultSharedPreferences(getApplicationContext());
        // Get settings for server
        String url_from_settings = (String) prefs.getString("server_url", "null");
        String username_from_settings = (String) prefs.getString("server_username", "null");
        String password_from_settings = (String) prefs.getString("server_password", "null");

        Uri uri = Uri.parse( url_from_settings );

        mClient = OwnCloudClientFactory.createOwnCloudClient(uri,getApplicationContext(),true);
        mClient.setCredentials(
                OwnCloudCredentialsFactory.newBasicCredentials(
                        username_from_settings,
                        password_from_settings
                )
        );

    }

    /* Test connection here:
     * (http://server/owncloud/status.php)
     */
    public boolean testNetworkConnection() {
        Log.d(CLASSTAG, "testNetworkConnection called");
        ConnectivityManager connectivityManager = (ConnectivityManager)
                ManageProjectActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean returnValue = false;
        try {
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info.isConnected()) {
                returnValue = true;
            } else {
                returnValue = false;
            }
        } catch (Exception e) {
            returnValue = false;
            Log.e(CLASSTAG, "Exception in testOwnCloudConnections");
            e.printStackTrace();
        }
        return returnValue;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Project project = (Project) getListAdapter().getItem(position);
        Toast.makeText(this, project.name+ " selected", Toast.LENGTH_LONG).show();
    }

    // Handle syncswitch clicks
    @Override
    public void onClick(View v) {
        //TODO: This test should not be here. Only during devel.
        testNetworkConnection();

        // Switch to right button code based on id.
        switch ( v.getId() ) {
            case R.id.syncswitch:
                OwnCloudSyncTask task = new OwnCloudSyncTask(mClient);
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

    private class OwnCloudSyncTask extends AsyncTask<String, Integer, Integer> implements
            OnRemoteOperationListener {

        ProgressBar bar;
        Switch switchbutton;
        OwnCloudClient owncloud_client;
        // Handler mHandler = new Handler();

        public OwnCloudSyncTask(OwnCloudClient oc) {
            this.owncloud_client = oc;
        }

        public void setProgressBar(ProgressBar bar) {
            this.bar = bar;
        }

        public void setSwitchButton(Switch s) {
            this.switchbutton = s;
        }

        /** Access to the library method to Upload a File
         * @param storagePath
         * @param remotePath
         * @param mimeType
         * @param client			Client instance configured to access the target OC server.
         *
         * @return
         */
        public RemoteOperationResult uploadFile(
                String storagePath, String remotePath, String mimeType, OwnCloudClient client
        ) {
            UploadRemoteFileOperation uploadOperation;
            if ((new File(storagePath)).length() > ChunkedUploadRemoteFileOperation.CHUNK_SIZE ) {
                uploadOperation = new ChunkedUploadRemoteFileOperation(
                        storagePath, remotePath, mimeType
                );
            } else {
                uploadOperation = new UploadRemoteFileOperation(
                        storagePath, remotePath, mimeType
                );
            }

            RemoteOperationResult result = uploadOperation.execute(client);
            return result;
        }

        // Example code:
        // https://doc.owncloud.org/server/7.0/developer_manual/android_library/examples.html
        //TODO: If the files are already there, dont sync them.
        //TODO: Perhaps all this needs to go into the doInBackground() since it might be executed
        // in the UI-thread... not sure.
        private boolean uploadAllFilesFromProject() {
            GlobalApplication application = ((GlobalApplication) getApplicationContext());
            String projName = application.getWorkingProject().name;
            boolean retValue = false;
            RemoteOperationResult res;
            ExistenceCheckRemoteOperation exOp = new ExistenceCheckRemoteOperation(
                    projName, getBaseContext(), true);
            try {
                res = exOp.execute(mClient);
                //TODO: Replace with proper test of connectivity
                if ( res.isException() ) {
                    Log.e(CLASSTAG, "ERROR, replace me with a correct test for connectivity" );
                    return false;
                }

            } catch ( Exception ce ) {
                Log.d(CLASSTAG, "Couldnt connect.");
                ce.printStackTrace();
                return false;
            }
            // if directory doesnt exist, create it.
            if ( res.isSuccess() ) {
                CreateRemoteFolderOperation createOp = new CreateRemoteFolderOperation(projName, true);
                res = createOp.execute(mClient);
            }

            //Upload files to upFolder.
            File upFolder = new File(getExternalFilesDir(null), String.format("/%s", projName));
            File[] allFiles = upFolder.listFiles();
            int count_files = allFiles.length;
            for (int i=0; i<count_files; i++) {
                String localFile = allFiles[i].getAbsolutePath();
                String remoteFile = projName + "/" + allFiles[i].getName();
                String mimeType = "application/octet-stream";
                if ( localFile.endsWith("png")) {
                    mimeType = "image/png";
                }
                if ( localFile.endsWith("json")) {
                    mimeType = "application/json";
                }
                RemoteOperationResult r = uploadFile( localFile,remoteFile,mimeType);
                bar.setProgress((int) ((i+1 / (float) count_files) * 100));
            }
            retValue = true;
            return retValue;
        }


        private RemoteOperationResult uploadFile(String fileToUpload,
                                                  String remotePath,
                                                  String mimeType) {
            UploadRemoteFileOperation uploadOperation = new UploadRemoteFileOperation( fileToUpload,
                    remotePath, mimeType);
            // uploadOperation.addDatatransferProgressListener();
            return uploadOperation.execute(mClient);
        }

        /**
         * The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute()
         */
        protected Integer doInBackground(String... files) {
            boolean success = uploadAllFilesFromProject();
            if ( success ) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (this.bar != null) {
                bar.setProgress(values[0]);
            }
        }

        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */
        protected void onPostExecute(Integer result) {
            Log.d("POSTEXE", result.toString());
            //TODO: To something intuitive with UI based on result.
            Toast.makeText(ManageProjectActivity.this,
                           String.format("Complete: %s",
                           result.toString()), Toast.LENGTH_LONG).show();
            switchbutton.setClickable(true);
        }

        @Override
        public void onRemoteOperationFinish(RemoteOperation remoteOperation,
                                            RemoteOperationResult remoteOperationResult) {
            if (!remoteOperationResult.isSuccess()) {
                Toast.makeText(getBaseContext(), "FAILED", Toast.LENGTH_SHORT).show();
                Log.e(CLASSTAG, remoteOperationResult.getLogMessage(), remoteOperationResult.getException());

            } else if (remoteOperation instanceof ReadRemoteFolderOperation) {
                // onSuccessfulRefresh((ReadRemoteFolderOperation) remoteOperation, remoteOperationResult);
                Toast.makeText(getBaseContext(), "do onSuccessfulRefresh", Toast.LENGTH_SHORT).show();
            } else if (remoteOperation instanceof UploadRemoteFileOperation ) {
                // onSuccessfulUpload((UploadRemoteFileOperation) remoteOperation, remoteOperationResult);
                Toast.makeText(getBaseContext(), "do onSuccessfulUpload", Toast.LENGTH_SHORT).show();
            } else if (remoteOperation instanceof RemoveRemoteFileOperation) {
                // onSuccessfulRemoteDeletion((RemoveRemoteFileOperation) remoteOperation, remoteOperationResult);
                Toast.makeText(getBaseContext(), "do onSuccessfulRemoteDeletion", Toast.LENGTH_SHORT).show();
            } else if (remoteOperation instanceof DownloadRemoteFileOperation) {
                // onSuccessfulDownload((DownloadRemoteFileOperation)remoteOperation, remoteOperationResult);
                Toast.makeText(getBaseContext(), "do onSuccessfulDownload", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getBaseContext(), "SUCCESS", Toast.LENGTH_SHORT).show();
            }


        }
    }

}
