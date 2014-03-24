# AddLive Android Tutorials

This repository contains a sample project showcasing the basics of the AddLive
Android library functionality

For more details please refer to AddLive home page: http://www.addlive.com.

Please note that tutorials require at least version v3.0.0.27 of the AddLive
SDK.

## Building the application

To use the application you need to obtain AddLive Android library. To obtain
those visit http://www.addlive.com.

Once you have all required components, copy the addlive-android-sdk.jar and the
armeabi-v7a folder into the libs folder within your project.

To use a particular tutorial, open the AndroidManifest and change the line:
android:name="com.addlive.tutorials.X" where "X" is the tutorial you want to
execute. e.g "Tutorial1"

In case of any questions or issues - feel free to reach us at
http://community.addlive.com.

## Tutorial 1 - platform init

This tutorial covers platform initialization and calling service methods.

The sample application showcasing the functionality simply loads the SDK, calls
the getVersion method and displays the version string in a TextView.

## Tutorial 2 - local preview

This tutorial covers devices handling, local preview control and rendering.

The sample application implemented, initializes the platform, sets up the local
camera using setVideoCaptureDevice and then start the local video using
startLocalVideo.

## Tutorial 3 - Basic connectivity

This tutorial covers basic connectivity features of the AddLive platform.

The sample application implemented, initializes the platform and sets up local
preview as per Tutorial 2. It allows also a user to connect to a media scope
with a hardcoded id.

## Tutorial 4 - Speakers' activity

This tutorial covers the new feature on V3 - Speech Activity.

The sample application enable the Speech Activity with monitorSpeechActivity and
implements the listener onSpeechActivity which receives the user id and activity
of each user in the session.

## Tutorial 4.1 - Two remote renders at the same time

This tutorial covers the new speech activity feature rendering two users.

The sample application will you the basics of rendering two remote feeds at the
same time along with the speech activity feature.

## Tutorial 5 - Conference App

This tutorial covers implementation of more advanced video conferencing
application and employs all of the connectivity-related features of the
AddLive SDK.

The sample application renders each new user within a HorizontalScrollView
allowing you to swipe between the video feed in the session.

## Tutorial 6 - Screen Sharing

This tutorial provides a sample implementation of screen sharing functionality

The sample application renders the screen shared within the App. Please notice
that you will only be able to receive streaming of an user within your session
you will not be able to share your screeen as it's not supported on mobiles.

## Tutorial 7 - Conference App & Speaking person video

This tutorial provides a sample implementation of one of the many uses you can
give to the speechActivity feature.

The sample application renders only the video of the speaking person and we
achieve this by using the onSpeechActivity listener and it's parameters.
It will also only allow to receive video from the speaking user by setting only
his userId using setAllowedSenders.

## License

All code examples provided within this repository are available under the
MIT-License. For more details, please refer to the LICENSE.md
