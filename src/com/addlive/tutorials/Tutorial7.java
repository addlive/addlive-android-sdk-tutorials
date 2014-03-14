package com.addlive.tutorials;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.addlive.platform.*;
import com.addlive.service.*;
import com.addlive.service.listener.*;

public class Tutorial7 extends Activity {

	/**
	*===========================================================================
	* Constants
	*===========================================================================
	*/

	private static final long ADL_APP_ID = 1; // TODO set your app ID here.
	private static final String ADL_API_KEY = "AddLiveSuperSecret"; // TODO set you API key here.
	private static final String ADL_SCOPE_ID = "ADL_iOS"; // TODO set you scope ID here.

	private static final String LOG_TAG = "AddLiveTutorial";

	/**
	* ===========================================================================
	* Nested classes
	* ===========================================================================
	*/

	class User {
		long userId; 
	    String videoSinkId = "";
	    int activity;

	    User(long userId, String videoSinkId, int activity) {
	    	this.userId = userId;
	    	this.videoSinkId = videoSinkId;
	    	this.activity = activity;
	    }
	}

	/**
	*===========================================================================
	* Properties
	*===========================================================================
	*/

	// key is user ID
	private Map<Long, User> userMap = new HashMap<Long, User>();
	
	// Local userId.
	private long _userId;
	
	// Remote videoSinkId feeding.
	private String _remoteSinkId = "";
	
	// Remote userId feeding.
	long _remoteUserId;
	
	// Remote VideoView object.
    com.addlive.view.VideoView _view;
    
    // Remote VideoView width after calculating.
    float _videoWidth;
    
    // Remote VideoView height after calculating.
    float _videoHeight;
    
    // Remote VideoView max width.
    float _videoMaxWidth;
    
    // Remote VideoView max height.
    float _videoMaxHeight;
    
    // Timer to check and set the speaking feeder
    int _checkTimer;
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tutorial7);

		// Hiding keyboard.
	    getWindow().setSoftInputMode(
	        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// Setting the local video view.
	    SurfaceView local = (SurfaceView) findViewById(R.id.local_video);
	    local.setZOrderMediaOverlay(true);
	    
	    // Setting the local userId.
	    Random rand = new Random();
	    _userId = (1 + rand.nextInt(9999));
	    
	    // Setting the maximum constants according to the device screen.
	    Display display = getWindowManager().getDefaultDisplay();
	    Point size = new Point();
	    display.getSize(size);
	    _videoMaxWidth = (float)(size.x * 0.95);
	    _videoMaxHeight = (float)(size.y * 0.55);
	    
	    // Setting the reference to VideoView object.
	    _view = (com.addlive.view.VideoView) findViewById(R.id.remote_video);
		
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

		// Setting the click listener for the connect button.
		findViewById(R.id.connect).setOnClickListener(
	        new View.OnClickListener() {
	        	@Override
	        	public void onClick(View view) {
	        		
	        		// Calling method to handle the connection.
	        		onConnect();
	        	}
	        }
		);
		
		// Setting the click listener for the disconnect button.
		findViewById(R.id.disconnect).setOnClickListener(
			new View.OnClickListener() {
				@Override
		      	public void onClick(View view) {
					
					// Calling method to handle the disconnection.
		        	onDisconnect();
		    	}
		 	}
		);
	}

	//==========================================================================

	private void onConnect() {
		
		// Showing a message when connecting.
	    TextView status = (TextView) findViewById(R.id.status);
	    status.setText("Connecting...");

	    // Disabling the connect button.
	    Button connect = (Button) findViewById(R.id.connect);
	    connect.setEnabled(false);

	    Log.d(LOG_TAG, "Connecting to scope: '" + ADL_SCOPE_ID + "'");
	    
	    // Creating the descriptor to send with the connect call.
	    ConnectionDescriptor desc = genConnDescriptor(); 

	    // Responder to handle the connect response.
	    UIThreadResponder<MediaConnection> connectResponder =
	    	new UIThreadResponder<MediaConnection>(this) {
	          	@Override
	          	protected void handleResult(MediaConnection result) {

	        	    Log.d(LOG_TAG, "Successfully connected to the scope");
	        	    
	        	    // Removing the message when connecting.
	        	    TextView status = (TextView) findViewById(R.id.status);
	        	    status.setText("");

	        	    // Hiding the connect button.
	        	    Button connect = (Button) findViewById(R.id.connect);
	        	    connect.setVisibility(View.GONE);

	        	    // Showing and enabling the disconnect button.
	        	    Button disconnect = (Button) findViewById(R.id.disconnect);
	        	    disconnect.setVisibility(View.VISIBLE);
	        	    disconnect.setEnabled(true);
	        	    
	        	    /* getService - Returns a reference to the implementation 
	        	     * of the AddLiveService interface.
	        		 * 
	        		 * monitorSpeechActivity - Controls monitoring of speech 
	        		 * activity within given scope.
	        		 */
	        	    ADL.getService().monitorSpeechActivity(
	        	    		new ResponderAdapter<Void>(), ADL_SCOPE_ID, true);
	        	    
	        	    // Starting the thread in charge of setting the speaking
	        	    // video feed.
	        	    checkActivity();
	          	}

	          	@Override
	          	protected void handleError(int errCode, String errMessage) {
	          		
	          		// Setting and showing the error label.
	          		Log.e(LOG_TAG, "ERROR: (" + errCode + ") " + errMessage);

	        	    TextView status = (TextView) findViewById(R.id.status);
	        	    status.setTextColor(Color.RED);
	        	    status.setText("ERROR: (" + errCode + ") " + errMessage);

	        	    // Enabling the connect button again.
	        	    Button connect = (Button) findViewById(R.id.connect);
	        	    connect.setEnabled(true);
	          	}
	        };
	        
		/* getService - Returns a reference to the implementation of the 
		 * AddLiveService interface.
		 * 
		 * connect - Establishes connection to the streaming server using given
		 * description. This is the most important method of all provided by 
		 * the AddLive Service API.
		 */
	    ADL.getService().connect(connectResponder, desc);
	}

	//==========================================================================

	private void onDisconnect() {
	
		// Disabling the disconnect button.
		Button disconnect = (Button) findViewById(R.id.disconnect);
	    disconnect.setEnabled(false);
	
	    // Showing a message when disconnecting.
	    TextView status = (TextView) findViewById(R.id.status);
	    status.setText("Disconnecting ...");
	    
	    // Responder to handle the disconnect response.
	    UIThreadResponder<Void> disconnectResponder =
	        new UIThreadResponder<Void>(this) {
	          	@Override
	          	protected void handleResult(Void result) {
	        	    
	        	    // Removing the message when disconnecting.
	        	    TextView status = (TextView) findViewById(R.id.status);
	          		status.setText("");

	        	    // Showing the connect button.
	          	    Button connect = (Button) findViewById(R.id.connect);
	          	    connect.setVisibility(View.VISIBLE);
	          	    connect.setEnabled(true);

	        	    // Hiding the disconnect button.
	          	    Button disconnect = (Button) findViewById(R.id.disconnect);
	          	    disconnect.setVisibility(View.GONE);

	          	    // Clearing the remote VideoView.
	          	    _view.stop();
	          	    _view.setSinkId("");
	          	    _remoteSinkId = "";
	          	    _remoteUserId = 0L;
		         	
		         	// Clearing the local record.
		            userMap.clear();
	          	}
	
	          	@Override
	          	protected void handleError(int errCode, String errMessage) {

	          		// Setting and showing the error label.
	          		Log.e(LOG_TAG, "ERROR: (" + errCode + ") " + errMessage);

	        	    TextView status = (TextView) findViewById(R.id.status);
	        	    status.setTextColor(Color.RED);
	        	    status.setText("ERROR: (" + errCode + ") " + errMessage);

	        	    // Enabling the disconnect button again.
	        	    Button disconnect = (Button) findViewById(R.id.disconnect);
	        	    disconnect.setEnabled(true);
	          	}
	    };
        
		/* getService - Returns a reference to the implementation of the 
		 * AddLiveService interface.
		 * 
		 * disconnect - Disconnects previously established connection to the 
		 * streaming server.
		 */
	    ADL.getService().disconnect(disconnectResponder, ADL_SCOPE_ID);
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
	    TextView status = (TextView) findViewById(R.id.status);
	    status.setTextColor(Color.RED);

	    String errMessage = "ERROR: (" + e.getErrCode() + ") " +
	        e.getErrMessage();
	    status.setText(errMessage);

	    Log.e(LOG_TAG, errMessage);
	}
	
	//==========================================================================

	private void onGetVideoCaptureDeviceNames(List<Device> devicesAvailables) {

	    // Setting the front camera as the local video feeding camera.
	    SurfaceView view = (SurfaceView) findViewById(R.id.local_video);
	    ADL.getService().setVideoCaptureDevice(new ResponderAdapter<Void>(),
	        VideoCaptureDevice.FRONT_CAMERA.getId(), view);
	    
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
		        		Log.v(LOG_TAG, "videoFrameSizeChanged: " + e.getSinkId()
		        				+ " -> " + e.getWidth() + "x" + e.getHeight());
		        		
		        		// if it's not the local renderer
		        		if(!e.getSinkId().equals("AddLiveRenderer1") && 
		        				_remoteSinkId.equals(e.getSinkId()) ){
		        			
			        		// Getting the new values to scale the VideoView
			        		fitDimensions(e.getWidth(), e.getHeight(), 
			        						_videoMaxWidth, _videoMaxHeight);
			        		
			        		// Get the layout.
			        		RelativeLayout.LayoutParams _rootLayoutParams = 
			        		(RelativeLayout.LayoutParams) _view.getLayoutParams();
			        		
			        		// Setting new dimensions.
			        		_rootLayoutParams.width = (int)_videoWidth;
			        		_rootLayoutParams.height = (int)_videoHeight;
			        		_view.setLayoutParams(_rootLayoutParams);
			        		
			        		// Setting the new resolution to the remote VideoView.
			        		_view.resolutionChanged(e.getWidth(), e.getHeight());
		        		}
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
				      	
		          	    // Clearing the remote VideoView.
		          	    _view.stop();
		          	    _view.setSinkId("");
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

			/* 
			 * onSpeechActivity - Reports speech activity within given scope.
			 */
		 	@Override
		 	public void onSpeechActivity(final SpeechActivityEvent e) {
		        runOnUiThread(new Runnable() {
		        	@Override
		        	public void run() {
		        		
		        		// Loop through all the activity list
		        		for (long userActive : e.getActiveSpeakers()) {
		        			if(userActive != -1){
		        				User updateUser = userMap.get(userActive);
		        				updateUser.activity++;
		        			}
		        		}
		        	    
		        	    _checkTimer++;
		        	    
		        	    // If there is some activity.
		        	    if(_checkTimer >= 15 && !userMap.isEmpty())
		        	    {
					      	Log.d(LOG_TAG, "checkActivity");
		        	    	checkActivity();
		        	    }
		        	}
		        });
		  	}

			/* 
			 * onMediaStreamEvent - Called whenever remote user publishes or 
			 * stops publishing media of particular type, inside a scope to 
			 * which local user is also connected.
			 */
		 	@Override
		  	public void onMediaStreamEvent(final UserStateChangedEvent e) {
		        super.onMediaStreamEvent(e);
		        runOnUiThread(new Runnable() {
		        	@Override
		        	public void run() {
		        		
		        		if(e.getMediaType() == MediaType.VIDEO) {

		    				// Setting the new remoteVideoSink value.
		    			   	_remoteSinkId = e.getVideoSinkId();
		    			   	
			        		// Update record
			        	    User remoteUser = userMap.get(e.getUserId());
			        	    remoteUser.videoSinkId = _remoteSinkId;
		        			
			        	    // If it's the current user feeding video.
			        	    if(e.getUserId() == _remoteUserId) {
			        	    	
			        			if(e.isVideoPublished()) {

			        			   	// Stopping the previous VideoView.
			        			    _view.stop();

			        				// Setting the remoteVideoSink to the VideoView
			        			    _view.setSinkId(_remoteSinkId);
			        			    _view.start();
			        			} else {
			        				
			        				// Stopping the feeding.
			        			    _view.stop(); 
			        			    _view.setSinkId(_remoteSinkId);
			        			}
			        	    }
		        		}
		        	}
		        });
			}

			/* 
			 * onUserEvent - Called whenever new remote user joined or 
			 * existing participant left a scope to which local user is also 
			 * connected.
			 */
			@Override
		 	public void onUserEvent(final UserStateChangedEvent e) {
		        super.onUserEvent(e);
		    	runOnUiThread(new Runnable() {
		    		@Override
		    		public void run() {
		    			Log.d(LOG_TAG, "onAdlUserEvent: " + e.toString());
		    			
		    			// If nobody is feeding.
		    		    if (e.isConnected() && _remoteSinkId.equals("")) {
		    				Log.i(LOG_TAG, "New user connected: " + 
		    						e.getUserId());

		    	            // Getting the videoId.
		    				if(e.isVideoPublished()){
		    					
			    				// Setting the new screenSink value.
			    			   	_remoteSinkId = e.getVideoSinkId();
		    				}
		    				
		    				// Setting the userId feeding.
		    				_remoteUserId = e.getUserId();
		    				
		    				// Saving the videoSinkId with it's corresponding
		    				// userId.
		    			    userMap.put(e.getUserId(), new User(e.getUserId(), 
		    			    							_remoteSinkId, 
		    			    							0));
		    			   	
		    			    // Start video feed.
		    			    startVideoWithTheCurrentSpeaker();
		    			    
		    		    } else if(e.isConnected()) {
		    		    	
		    				// Saving the videoSinkId with it's corresponding
		    				// userId.
		    			    userMap.put(e.getUserId(), new User(
		    			    						e.getUserId(), 
		    			    						e.getVideoSinkId(), 
		    			    						0));
		    		    	
		    		    } else {
		    		    	
		    		    	// Removing the userId record.
		    		        userMap.remove(e.getUserId());
		    		        
		    		        // If the user disconnected was the one feeding.
		    		        if(e.getUserId() == _remoteUserId) {
		    		        	
		    		        	_remoteSinkId = "";
		    		        	
		        				// Stopping the feeding.
		        			    _view.stop(); 
		        			    _view.setSinkId(_remoteSinkId);
		    		        }
		    		    }
		    		}
		    	});
		 	}
		};
	}

	//==========================================================================
	
	private void startVideoWithTheCurrentSpeaker(){
	   	// Stopping the previous VideoView if any.
	    _view.stop();

		// Setting the remoteVideoSink to the VideoView
	    _view.setSinkId(_remoteSinkId);
	    _view.start();
	}

	/**
	*===========================================================================
	* AddLive Platform Allowed Senders
	*===========================================================================
	*/

	private void updateAllowedSenders(){

		// Set the speaking user as the only one in the List to send.
		List<Long> userList = java.util.Arrays.asList(_remoteUserId);
		
		/* getService - Returns a reference to the implementation of the 
		 * AddLiveService interface.
		 * 
		 * setAllowedSenders - Allows application to specify from which remote 
		 * users, the local user will receive media streams.
		 */
		ADL.getService().setAllowedSenders(new ResponderAdapter<Void>(), 
											ADL_SCOPE_ID, 
											MediaType.VIDEO, 
											userList);
	}
	
	/**
	*===========================================================================
	* Private helpers
	*===========================================================================
	*/
	
	private void checkActivity() {

	    // Restart the timer.
		_checkTimer = 0;
        
        // Checking activity.
        boolean activity = false;
    	for (User user : userMap.values()) {
	    	if(user.activity > 0){
	    		activity = true;
	    	}
		}
    	
        // If there is some activity.
        if(activity) {
        	
            // Getting the max activity during those 2 seconds.
        	int max = 0;
        	long userId = 0;
        	for (User user : userMap.values()) {
        		if(user.activity > max){
        			max = user.activity;
        			userId = user.userId;
        		}
  			}

            // If it's a different user as the one feeding video right now.
            if(userId != _remoteUserId && max > 0) {
                // Get his userId.
            	_remoteUserId = userId;
                
                // Get his sinkerId.
        	    User remoteUser = userMap.get(userId);
        	    _remoteSinkId = remoteUser.videoSinkId;
                
                // Change video feed calling the method in the main thread.
                updateAllowedSenders();
                startVideoWithTheCurrentSpeaker();
            }

         	// Clearing the speech activity record.
        	for (User user : userMap.values()) {
		    	user.activity = 0;
  			}
        }
	}

	// Generates the ConnectionDescriptor (authentication + video description)
	private ConnectionDescriptor genConnDescriptor() {

		ConnectionDescriptor desc = new ConnectionDescriptor();
		desc.setAutopublishAudio(true);
		desc.setAutopublishVideo(true);
	  	desc.setScopeId(ADL_SCOPE_ID);

	    // Authentication
	    String salt = "Some random string salt";
	    long timeNow = System.currentTimeMillis() / 1000;
	    long expires = timeNow + (5 * 60);

	    AuthDetails authDetails = new AuthDetails();
	    authDetails.setUserId(_userId);
	    authDetails.setSalt(salt);
	    authDetails.setExpires(expires);
	    String signatureBody = String.valueOf(ADL_APP_ID) + ADL_SCOPE_ID +
	        _userId + salt + expires + ADL_API_KEY;
	    MessageDigest digest;
	    String signature = "";
	    try {
	    	digest = MessageDigest.getInstance("SHA-256");
	      	digest.update(signatureBody.getBytes());
	      	signature = bytesToHexString(digest.digest());
	    } catch (NoSuchAlgorithmException e1) {
	    	Log.e(LOG_TAG, "Failed to calculate authentication signature due to "
	    			+ "missing SHA-256 algorithm.");
	    }
	    authDetails.setSignature(signature);
	    desc.setAuthDetails(authDetails);

	    return desc;
	}

	private static String bytesToHexString(byte[] bytes) {
	    
	    StringBuilder sb = new StringBuilder();
	    for (byte aByte : bytes) {
	    	String hex = Integer.toHexString(0xFF & aByte);
	    	if (hex.length() == 1) {
	    		sb.append('0');
	    	}
	    	sb.append(hex);
	    }
	    return sb.toString();
	}
	
	private void fitDimensions(float srcW, float srcH, float targetW, float targetH){
	    float srcAR = srcW / srcH;
	    float targetAR = targetW / targetH;
	    
	    if (srcW < targetW && srcH < targetH) {
	        _videoWidth = srcW;
	        _videoHeight = srcH;
	    }
	    if (srcAR < targetAR) {
	        // Match height
	        _videoWidth = srcW * targetH / srcH;
	        _videoHeight = targetH;
	    } else {
	        // Match width
	        _videoWidth = targetW;
	        _videoHeight = targetW * srcH / srcW;
	    }
	}
}
