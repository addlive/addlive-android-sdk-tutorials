package com.addlive.tutorials;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import com.addlive.platform.ADL;
import com.addlive.platform.InitProgressChangedEvent;
import com.addlive.platform.InitState;
import com.addlive.platform.InitStateChangedEvent;
import com.addlive.platform.PlatformInitListener;
import com.addlive.platform.PlatformInitOptions;
import com.addlive.service.UIThreadResponder;

public class Tutorial1 extends Activity {

    /**
     * ===========================================================================
     * Constants
     * ===========================================================================
     */

    private static final long ADL_APP_ID = 1; // TODO set your app ID here.
    private static final String LOG_TAG = "AddLiveTutorial";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial1);

        // Framework initialization.
        initializeAddLive();
    }


    /**
     * ===========================================================================
     * AddLive Platform initialization
     * ===========================================================================
     */

    private void initializeAddLive() {

        // ADL.init listener.
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
        String storageDir = Environment.getExternalStorageDirectory().getAbsolutePath();

        // Sets the path where platform should look for the native components of the AddLive SDK.
        initOptions.setStorageDir(storageDir);

        // Sets the value of the applicationId property.
        initOptions.setApplicationId(ADL_APP_ID);

        Log.d(LOG_TAG, "Initializing the AddLive SDK.");

	    /* Initializes the AddLive ADL. The process is asynchronous and 
	     * initialization state changes and progress updates are reported via the
	     * listener provided.
	     */
        ADL.init(listener, initOptions, this);
    }

    // ===========================================================================

    private void onAdlInitialized() {
        Log.d(LOG_TAG, "AddLive SDK initialized");

		/* getService - Returns a reference to the implementation of the 
		 * AddLiveService interface.
		 * 
		 * getVersion - Returns a version of currently used Service.
		 */
        ADL.getService().getVersion(new UIThreadResponder<String>(this) {
            @Override
            protected void handleResult(String version) {
                Log.d(LOG_TAG, "AddLive SDK version: " + version);

                // Setting the SDK version on it's label.
                TextView versionLabel = (TextView) findViewById(R.id.sdkVersion);
                versionLabel.setTextColor(Color.GREEN);
                versionLabel.setText(version);
            }

            @Override
            protected void handleError(int errCode, String errMessage) {
                Log.e(LOG_TAG, "Failed to get version string.");
            }
        });
    }

    // ===========================================================================

    private void onAdlInitError(InitStateChangedEvent e) {

        // Setting the error on it's label.
        TextView status = (TextView) findViewById(R.id.error);
        status.setTextColor(Color.RED);
        String errMessage = "ERROR: (" + e.getErrCode() + ") " +
                e.getErrMessage();
        status.setText(errMessage);

        Log.e(LOG_TAG, errMessage);
    }
}
