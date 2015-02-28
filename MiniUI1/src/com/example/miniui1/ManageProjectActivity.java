package com.example.miniui1;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
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

import java.io.File;
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
        private void startUpload() {
            GlobalApplication application = ((GlobalApplication) getApplicationContext());
            String projName = application.getWorkingProject().name;

            ExistenceCheckRemoteOperation exOp = new ExistenceCheckRemoteOperation(
                    projName, getBaseContext(), true);
            RemoteOperationResult res = exOp.execute(mClient);
            // if directory doesnt exist, create it.
            if ( res.isSuccess() ) {
                CreateRemoteFolderOperation createOp = new CreateRemoteFolderOperation(projName, true);
                res = createOp.execute(mClient);
            }
            //Upload files to upFolder.
            File upFolder = new File(getExternalFilesDir(null), String.format("/%s", projName));
            File[] it = upFolder.listFiles();

            for (int i=0; i<it.length; i++) {

                String localFile = it[i].getAbsolutePath();
                String remoteFile = projName + "/" + it[i].getName();

                String mimeType = "application/octet-stream";
                if ( localFile.endsWith("png")) {
                    mimeType = "image/png";
                }
                if ( localFile.endsWith("json")) {
                    mimeType = "application/json";
                }
                Log.d(CLASSTAG, String.format("Upload:\n%s\n%s\n%s",
                        localFile, remoteFile, mimeType));
                RemoteOperationResult r = startUpload( localFile,remoteFile,mimeType);
                Log.d(CLASSTAG, "Upload result: " + r.toString() + r.getLogMessage() );
            }
        }
        private RemoteOperationResult startUpload (String fileToUpload, String remotePath, String mimeType) {
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
            startUpload();
            return 1;
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
