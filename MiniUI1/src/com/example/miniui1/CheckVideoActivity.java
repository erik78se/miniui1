package com.example.miniui1;

import java.io.IOException;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

public class CheckVideoActivity extends Activity implements TextureView.SurfaceTextureListener {
	private final String CLASSTAG = "DEBUG_ACTIVITY";
	private static final String FILE_NAME = "rath264aacts.mp4";
	
	private MediaPlayer mMediaPlayer;
	private TextureView mTextureView;

	
	// EXAMPLE: http://www.binpress.com/tutorial/video-cropping-with-texture-view/21
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
		 	super.onCreate(savedInstanceState);
		    setContentView(R.layout.activity_check_video);
		    initView();
		}
	
	 private void initView() {
		 Log.d(CLASSTAG, "initView() called.");
	     mTextureView = (TextureView) findViewById(R.id.textureview_video);
	     Log.d(CLASSTAG, "1.");
	     if ( mTextureView != null ) {
			Log.d(CLASSTAG, "mTextureView is not null");
	     } else {
			Log.d(CLASSTAG, "mTextureView is null");
	     }
	        // SurfaceTexture is available only after the TextureView
	        // is attached to a window and onAttachedToWindow() has been invoked.
	        // We need to use SurfaceTextureListener to be notified when the SurfaceTexture
	        // becomes available.
	     mTextureView.setSurfaceTextureListener(this);
	 }

	 @Override
	    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
		 	Log.d(CLASSTAG, "onSurfaceTextureAvailable() called.");
	        Surface surface = new Surface(surfaceTexture);

	        try {
	            // AssetFileDescriptor afd = getAssets().openFd(FILE_NAME);
	            AssetFileDescriptor afd = this.getResources().openRawResourceFd(R.raw.rath264aacts);
	            mMediaPlayer = new MediaPlayer();
	            mMediaPlayer
	                    .setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
	            mMediaPlayer.setSurface(surface);
	            mMediaPlayer.setLooping(true);

	            // don't forget to call MediaPlayer.prepareAsync() method when you use constructor for
	            // creating MediaPlayer
	            mMediaPlayer.prepareAsync();

	            Log.d(CLASSTAG, String.format("setOnPreparedListener for the mediaplayer for %s.", afd.getFileDescriptor().toString()));
	            // Play video when the media source is ready for playback.
	            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
	                @Override
	                public void onPrepared(MediaPlayer mediaPlayer) {
	                	Log.d(CLASSTAG, "mediaPlayer.start().");
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
		// TODO Auto-generated method stub
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
}