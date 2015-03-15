package com.example.miniui1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

public class CheckVideoActivity extends Activity implements TextureView.SurfaceTextureListener, 
															OnBufferingUpdateListener, 
															OnCompletionListener, 
															OnPreparedListener, 
															OnVideoSizeChangedListener
															{
	private final String CLASSTAG = "VIDEO";
	
	private MediaPlayer mMediaPlayer;
	private TextureView mTextureView;
	private Button mButtonScreenShot;
	private Spinner mPipeMaterialSpinner;
	private Spinner mLocationsSpinner;
	private Project mCurrentProject;
	
	// EXAMPLE: http://www.binpress.com/tutorial/video-cropping-with-texture-view/21
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
		 	super.onCreate(savedInstanceState);
		    setContentView(R.layout.activity_check_video);
		    initView();
			mPipeMaterialSpinner = (Spinner) findViewById(R.id.spinnerPipeMaterial);
		    mLocationsSpinner = (Spinner) findViewById(R.id.spinnerLocations);
		    mCurrentProject = ((GlobalApplication) getApplicationContext()).getWorkingProject();
            if (mCurrentProject != null ) {
                setTitle(mCurrentProject.name);
            } else {
                setTitle("");
            }

            if ( mCurrentProject != null) {
                setLocationSpinnerListeners();
                populateMaterialsSpinner();
                populateLocationsSpinner();
                setupButton();
            }
            else
            {
                Log.e(CLASSTAG, "A Project is not available. This is not good");
                Toast.makeText(getApplicationContext(),
                        "No project set - start a new one first.", Toast.LENGTH_LONG).show();
            }
		}
	 
	 private void initView() {
			 Log.d(CLASSTAG, "initView() called.");
		     mTextureView = (TextureView) findViewById(R.id.textureview_video);
		        // SurfaceTexture is available only after the TextureView
		        // is attached to a window and onAttachedToWindow() has been invoked.
		        // We need to use SurfaceTextureListener to be notified when the SurfaceTexture
		        // becomes available.
		     mTextureView.setSurfaceTextureListener(this);	     
		 }

	 private void setLocationSpinnerListeners() {
		 	mLocationsSpinner.setOnTouchListener(new OnTouchListener(){
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		    	populateLocationsSpinner();
		        return false;
		    }
		 	});
		 	
		 	mLocationsSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
		 		@Override
		 	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) 
		 	    {
		 			EditText locationText = (EditText) findViewById(R.id.editTextLocation);
		 			locationText.setText( parentView.getItemAtPosition(position).toString() );
		 	    }

		 	    @Override
		 	    public void onNothingSelected(AdapterView<?> parentView) 
		 	    {
		 	        
		 	    }
			});
		 	
	 }
		 	
	 private void populateLocationsSpinner() {
		 Log.d(CLASSTAG, "populateLocationsSpinner() called.");
		 	
			mLocationsSpinner = (Spinner) findViewById(R.id.spinnerLocations);
			// Create an ArrayAdapter using the string array and a default spinner layout
			ArrayAdapter<String> spinnerArrayAdapter = 
					new ArrayAdapter<String>( this, android.R.layout.simple_spinner_item, mCurrentProject.getLocations());
			// Specify the layout to use when the list of choices appears
			spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// Apply the adapter to the spinner
			mLocationsSpinner.setAdapter(spinnerArrayAdapter);		
	}


	//Sets available materials for the spinner
	void populateMaterialsSpinner() {
		Log.d(CLASSTAG, "populateMaterialsSpinner() called.");
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.pipematerial, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		mPipeMaterialSpinner.setAdapter(adapter);
	}
	 
	 public void setupButton() {
		 Log.d(CLASSTAG, "setupButton() called.");
         // Assign click listner that takes a screenshot
		 mButtonScreenShot = (Button) findViewById(R.id.button_takescreenshot);
		 mButtonScreenShot.setOnClickListener( new OnClickListener() {
             @Override
             public void onClick(View v) 
             {
            	Log.d(CLASSTAG, "button_takescreenshot was pressed");
                String imgFileName = CheckVideoActivity.this.takeScreenShot(mTextureView);
                 //Collect input into a New Observation(Grade, New Pipe(etc, Location)), 
                Observation observationObj = newObservationFromInput();
                 //Set filename for the Observation:
                observationObj.setPictureFileName( imgFileName );
                 //Add Observation to Project
                mCurrentProject.addObservation(observationObj);
                Log.d(CLASSTAG, "Added observation.");
     		 	File projectFolder = new File(getExternalFilesDir(null), mCurrentProject.datafolder);
     		 	Log.d(CLASSTAG, "Fetched datafolder.");
     		 	
     		 	if ( mCurrentProject.save(projectFolder) ) {
     		 		Log.d(CLASSTAG, "saved Project.");
     		 	} else {
     		 		Log.d(CLASSTAG, "Failed to save Project.");
     		 	}
                 //Update Spinners
                 
             }
         });
		 Log.d(CLASSTAG, "setupButton() done.");
	 }
	 
	 public Observation newObservationFromInput() {
		 Log.d(CLASSTAG, "newObservationFromInput() called.");
		 // Get data for Pipe object
		 Spinner mySpinner=(Spinner) findViewById(R.id.spinnerPipeDimension);
		 int intDim = Integer.parseInt( mySpinner.getSelectedItem().toString());
		 mySpinner=(Spinner) findViewById(R.id.spinnerPipeMaterial);
		 String strMat = mySpinner.getSelectedItem().toString();
		 String strLoc = ((EditText) findViewById(R.id.editTextLocation)).getText().toString();
		 Location locationObj = new Location(strLoc);
		 boolean spillwater = ((CheckBox) findViewById(R.id.checkBoxSpillWater)).isChecked();
		 boolean daywater = ((CheckBox) findViewById(R.id.checkBoxDaywater)).isChecked();
		 boolean upstream = ((CheckBox) findViewById(R.id.checkBoxUpStream)).isChecked();
		 boolean clnsbfr = ((CheckBox) findViewById(R.id.checkBoxCleansedBefore)).isChecked();
		 boolean previnsp = ((CheckBox) findViewById(R.id.checkBoxCleansedBefore)).isChecked();

         //The observation might be uncertain grade.
         boolean uncertain = ((CheckBox) findViewById(R.id.checkBoxUncertain)).isChecked();

		 Pipe pipeObj = new Pipe(locationObj,intDim, strMat, spillwater, daywater, upstream,
					clnsbfr,
					previnsp);

		 // Get gata for Observation object
		 RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroupGrade);
		 String strGrade = ((RadioButton) findViewById(rg.getCheckedRadioButtonId())).getText().toString();

         Observation observation = new Observation(pipeObj, strGrade, 0, "Comment",uncertain);

         return observation;
	 }

	 // Create image (Return the filename)
	 public String takeScreenShot(TextureView vv)
	    {
		 	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-hhmmss", Locale.US);
		 	String strDate = simpleDateFormat.format( new Date() );
		 	String fileName = String.format("Observation-%s.png", strDate);
		 	
		 	// Get project dir.
		 	GlobalApplication application = ((GlobalApplication) getApplicationContext());
		 	String projName = application.getWorkingProject().name;

		 	String mPath = getExternalFilesDir(null).toString() + String.format("/%s/%s", projName, fileName );
		 	Log.d(CLASSTAG, mPath);
	        Toast.makeText(getApplicationContext(), "Capturing Screenshot: " + mPath, Toast.LENGTH_LONG).show();

	        Bitmap bm = vv.getBitmap();
	        if(bm == null)
	            Log.e(CLASSTAG,"bitmap is null");

	        OutputStream fout = null;
	        File imageFile = new File(mPath);

	        try {
	            fout = new FileOutputStream(imageFile);
	            bm.compress(Bitmap.CompressFormat.PNG, 90, fout);
	            fout.flush();
	            fout.close();
	            return fileName;
	        } catch (FileNotFoundException e) {
	            Log.e(CLASSTAG, "FileNotFoundException");
	            e.printStackTrace();
	        } catch (IOException e) {
	            Log.e(CLASSTAG, "IOException");
	            e.printStackTrace();
	        }
	        // Dont end up here or file is not really created.
	        return null;
	    }
	 
	 @Override
	    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
		 	Log.d(CLASSTAG, "onSurfaceTextureAvailable() calltestButton.setOnClickListener(testListener)");
	        Surface surface = new Surface(surfaceTexture);

	        try {
	            // AssetFileDescriptor afd = getAssets().openFd(FILE_NAME);
	            AssetFileDescriptor afd = this.getResources().openRawResourceFd(R.raw.rath264aacts);
	            mMediaPlayer = new MediaPlayer();
	            
	            // Prepare for screenshot
	            Log.d(CLASSTAG, "Preparing for handle screenshots.");
	            mMediaPlayer.setOnBufferingUpdateListener(this);
	            mMediaPlayer.setOnCompletionListener(this);
	            mMediaPlayer.setOnPreparedListener(this);
	            mMediaPlayer.setOnVideoSizeChangedListener(this);
	            // Done
	            
	            mMediaPlayer
	                    .setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
	            mMediaPlayer.setSurface(surface);
	            mMediaPlayer.setLooping(true);

	            // don't forget to call MediaPlayer.prepareAsync() method when you use constructor for
	            // creating MediaPlayer
	            mMediaPlayer.prepareAsync();

	            Log.d(CLASSTAG, String.format("Attempting mediaplayer for %s.", afd.getFileDescriptor().toString()));
	            // Play video when the media source is ready for playback.
	            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
	                @Override
	                public void onPrepared(MediaPlayer mediaPlayer) {
	                	Log.d(CLASSTAG, "onPrepared called");
	                    mediaPlayer.start();
	                }
	            });

	   
	            
	            
	        } catch (IllegalArgumentException e) {
	        	Log.d(CLASSTAG, "IllegalArgumentException" + e.getMessage());
	        	e.printStackTrace();
	        } catch (SecurityException e) {
	        	Log.d(CLASSTAG, "SecurityException" + e.getMessage());
	        	e.printStackTrace();
	        } catch (IllegalStateException e) {
	        	Log.d(CLASSTAG, "IllegalStateException" + e.getMessage());
	        	e.printStackTrace();
	        } catch (IOException e) {
	        	Log.d(CLASSTAG, "IOException:" + e.getMessage());
	            e.printStackTrace();
	        }
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		// TODO Auto-generated method stubOpened
		return false;
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		// TODO Auto-generated method stub
		
	} 
	
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            // Make sure we stop video and release resources when activity is destroyed.
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub
		
	}

}