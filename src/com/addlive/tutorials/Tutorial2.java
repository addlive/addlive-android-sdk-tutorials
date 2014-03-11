package com.addlive.tutorials;

import java.util.List;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.addlive.platform.*;
import com.addlive.service.*;
import com.addlive.service.listener.*;

public class Tutorial2 extends Activity {

	/**
	*===========================================================================
	* Constants
	*===========================================================================
	*/

	private static final long ADL_APP_ID = 1; // TODO set your app ID here.
	private static final String LOG_TAG = "AddLiveDemo";

	/**
	*===========================================================================
	* Properties
	*===========================================================================
	*/
	
	// Cameras available.
    private List<Device> devices;
    
    // Current local camera id.
    private String _currentDeviceId;
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tutorial2);

		// Setting the local video view.
	    SurfaceView local = (SurfaceView) findViewById(R.id.local_video);
	    local.setZOrderMediaOverlay(true);
		
	    // Setting view's listeners.
	    initializeActions();
	    
		// Framework initialization.
		initializeAddLive();
	}

	/**
	*===========================================================================
	* UI Actions initialization and handling
	*===========================================================================
	*/

	private void initializeActions() {

		// Setting the click listener for the toggle button.
		findViewById(R.id.toggle).setOnClickListener(
	        new View.OnClickListener() {
	        	@Override
	        	public void onClick(View view) {
	        		
	        		// Looping though the device list.
	        	    for (Device device : devices) {
	        	      	if(!device.getId().equals(_currentDeviceId)){
	        	      		_currentDeviceId = device.getId();
	        	      		break;
	        	      	}
	        	    }
	        	    
	        	    // Getting the video view
	        	    SurfaceView surfaceView = 
	        	    		(SurfaceView)findViewById(R.id.local_video);
	        	    
	        		/* getService - Returns a reference to the implementation 
	        		 * of the AddLiveService interface.
	        		 * 
	        		 * setVideoCaptureDevice - Defines which video capture device
	        		 * (camera) should be used by the SDK
	        		 */
	        	    ADL.getService().setVideoCaptureDevice(
	        	    		new ResponderAdapter<Void>(),
	        	    		_currentDeviceId, surfaceView);
	        	}
	        }
		);
	}

	/**
	*===========================================================================
	* AddLive Platform initialization
	*===========================================================================
	*/

	private void initializeAddLive() {
		
		// ADL.init listener
		PlatformInitListener listener = new PlatformInitListener() {
			@Override
			public void onInitProgressChanged(InitProgressChangedEvent e) {
				// Actually not used by the platform for now. Just a placeholder.
			}

			@Override
			public void onInitStateChanged(InitStateChangedEvent e) {

				if (e.getState() == InitState.INITIALIZED) {
					
					// Successfully initialized.
					onAdlInitialized();
			    } else {
			    	
			    	// Unsuccessfully initialized.
			    	onAdlInitError(e);
			    }
			}
		};
		
		// Describes the options allowing platform initialization.
		PlatformInitOptions initOptions = new PlatformInitOptions();
	    String storageDir = 
	    		Environment.getExternalStorageDirectory().getAbsolutePath();
	    
	    // Sets the path where platform should look for the native components
	    // of the AddLive SDK.
	    initOptions.setStorageDir(storageDir);
	    
	    // Sets the value of the applicationId property.
	    initOptions.setApplicationId(ADL_APP_ID);
	    
	    Log.d(LOG_TAG, "Initializing the AddLive SDK.");
	    
	    /* Initializes the AddLive ADL. The process is asynchronous and 
	     * initialization state changes and progress updates are reported via
	     * the listener provided.
	     */
	    ADL.init(listener, initOptions, this);
	}

	//==========================================================================

	private void onAdlInitialized() {
		Log.d(LOG_TAG, "AddLive SDK initialized");

		/* getService - Returns a reference to the implementation of the 
		 * AddLiveService interface.
		 * 
		 * addServiceListener - Registers a AddLive Service listener. 
		 * Listener provided here should subclass the provided 
		 * AddLiveServiceListener stub and implement required methods.
		 */
	    ADL.getService().addServiceListener(new ResponderAdapter<Void>(),
	        getListener());

		/* 
		 * getVideoCaptureDeviceNames - Returns an id of currently selected
		 * video capture device.
		 */
	    ADL.getService().getVideoCaptureDeviceNames(
	    	new UIThreadResponder<List<Device>>(this) {
	    		@Override
	    		protected void handleResult(List<Device> devices) {
	    			
	    			// On successful process the devices.
	    			onGetVideoCaptureDeviceNames(devices);
	    		}

	    		@Override
	    		protected void handleError(int errCode, String errMessage) {
	    			Log.e(LOG_TAG, "Failed to get video capture devices.");
	          	}
	    	}
	    );
	}

	//==========================================================================

	private void onAdlInitError(InitStateChangedEvent e) {

		// Setting and showing the error label.
	    TextView status = (TextView) findViewById(R.id.error);
	    status.setTextColor(Color.RED);
	    String errMessage = "ERROR: (" + e.getErrCode() + ") " +
	        e.getErrMessage();
	    status.setText(errMessage);

	    Log.e(LOG_TAG, errMessage);
	}
	
	//==========================================================================

	private void onGetVideoCaptureDeviceNames(List<Device> devicesAvailables) {

		// Copy the devices list so we can toggle later.
	    devices = devicesAvailables;
		
	    // Setting the front camera as the local video feeding camera.
	    SurfaceView view = (SurfaceView) findViewById(R.id.local_video);
	    ADL.getService().setVideoCaptureDevice(new ResponderAdapter<Void>(),
	        VideoCaptureDevice.FRONT_CAMERA.getId(), view);
	    
	    // Setting the current feeding id camera.
	    _currentDeviceId = VideoCaptureDevice.FRONT_CAMERA.getId();
	    
		/* 
		 * startLocalVideo - Starts previewing the local video feed.
		 */
	    ADL.getService().startLocalVideo(new UIThreadResponder<String>(this) {
	    	@Override
	    	protected void handleResult(String videoSinkId) {
	    		Log.d(LOG_TAG, "Succesfully started local video.");
	    	}

	    	@Override
	    	protected void handleError(int errCode, String errMessage) {
	    		Log.e(LOG_TAG, "Failed to start local video.");
	    	}
	    }, view);
	}

	/**
	*===========================================================================
	* AddLive Service Events handling
	* Interface allowing implementation of global AddLive Service event handlers.
	*===========================================================================
	*/

	private AddLiveServiceListener getListener() {
	    return new AddLiveServiceListenerAdapter() {
	    	
			/* 
			 * onVideoFrameSizeChanged - Called whenever the video resolution of
			 * any video sink changes.
			 */
			@Override
		 	public void onVideoFrameSizeChanged(final VideoFrameSizeChangedEvent e) {
		        runOnUiThread(new Runnable() {
		        	@Override
		        	public void run() {
				      	Log.d(LOG_TAG, "onVideoFrameSizeChanged Listener triggered");
		        	}
		        });
		   	}
	    	
			/* 
			 * onConnectionLost - Called whenever local user lost a connection 
			 * to the AddLive Streaming Server.
			 * (e.g. due to Internet connectivity issues)
			 */
		  	@Override
		 	public void onConnectionLost(final ConnectionLostEvent e) {
		        runOnUiThread(new Runnable() {
		        	@Override
		        	public void run() {
				      	Log.d(LOG_TAG, "onConnectionLost Listener triggered");
		        	}
		        });
		  	}
	    	
			/* 
			 * onSessionReconnected - Called whenever the AddLive SDK 
			 * reestablishes a connection to the AddLive streaming server.
			 */
		 	@Override
		 	public void onSessionReconnected(final SessionReconnectedEvent e) {
		        runOnUiThread(new Runnable() {
		        	@Override
		        	public void run() {
				      	Log.d(LOG_TAG, "onSessionReconnected Listener triggered");
		        	}
		        });
		  	}
		};
	}
}
