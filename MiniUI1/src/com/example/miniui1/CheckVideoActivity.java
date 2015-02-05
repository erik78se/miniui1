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
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class CheckVideoActivity extends Activity implements TextureView.SurfaceTextureListener, 
															OnBufferingUpdateListener, 
															OnCompletionListener, 
															OnPreparedListener, 
															OnVideoSizeChangedListener  {
	private final String CLASSTAG = "DEBUG_ACTIVITY";
	
	private MediaPlayer mMediaPlayer;
	private TextureView mTextureView;
	private Button mButtonScreenShot;
	
	// EXAMPLE: http://www.binpress.com/tutorial/video-cropping-with-texture-view/21
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
		 	super.onCreate(savedInstanceState);
		    setContentView(R.layout.activity_check_video);
		    initView();
		    setupButton();
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

	 public void setupButton() {
         // Assign click listner that takes a screenshot
		 mButtonScreenShot = (Button) findViewById(R.id.button_takescreenshot);
		 mButtonScreenShot.setOnClickListener( new OnClickListener() {
             @Override
             public void onClick(View v) 
             {
            	 Log.d(CLASSTAG, "button_takescreenshot was pressed");
                 CheckVideoActivity.this.getBitmap(mTextureView);
             }
         });
	 }
	 
	 // Create image
	 public void getBitmap(TextureView vv)
	    {
		 	// Get project dir.
		 	GlobalApplication application = ((GlobalApplication) getApplicationContext());
		 	String projname = application.getProjectName();
		 	
		 	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyy-hhmmss", Locale.US);
		 	String picturefilename = simpleDateFormat.format( new Date() );
		 	
		 	String mPath = getExternalFilesDir(null).toString() + String.format("/%s/Observation-%s.%s", projname, picturefilename,"png" );
		 	
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
	        } catch (FileNotFoundException e) {
	            Log.e(CLASSTAG, "FileNotFoundException");
	            e.printStackTrace();
	        } catch (IOException e) {
	            Log.e(CLASSTAG, "IOException");
	            e.printStackTrace();
	        }
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