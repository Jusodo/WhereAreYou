package de.ala.meetme;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class TrackerService extends Service implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

	// logger
	private static final String LOGGER = TrackerService.class.getSimpleName();
	
	public static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	public static final int UPDATE_INTERVALL_MILLISECONDS = 5000;
	public static final int FAST_INTERVALL_MILLISECONDS = 1000;
	
	public static final String BROADCAST_ACTION = "TrackerService";

	// IBinder mBinder = new LocalBinder();

	private LocationClient mLocationClient;
	private LocationRequest mLocationRequest;

	private boolean mUpdatesRequested;

	private Boolean serviceAvailable = false;
	
	private Intent intent;

	@Override
	public void onCreate() {
		super.onCreate();

		mUpdatesRequested = false;

		// create and configure the LocationRequest
		mLocationRequest = LocationRequest.create()
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
				.setInterval(UPDATE_INTERVALL_MILLISECONDS)
				.setFastestInterval(FAST_INTERVALL_MILLISECONDS);

		// check if service is available
		serviceAvailable = serviceIsConnected();

		// create the LocationClient
		mLocationClient = new LocationClient(this, this, this);

		intent = new Intent(BROADCAST_ACTION);
	}

	private boolean serviceIsConnected() {
		// check if Google Play is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);

		if (ConnectionResult.SUCCESS == resultCode) {
			Log.d(LOGGER, "Google Play Service is available");

			return true;
		} else {
			Log.d(LOGGER, "Google Play Serivce is not available");
			return false;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		if (!serviceAvailable || mLocationClient.isConnected()
				|| mUpdatesRequested) {
			return START_STICKY;
		}

		setUpLocationClientIfNeeded();

		if (!mLocationClient.isConnected() || !mLocationClient.isConnecting()
				|| !mUpdatesRequested) {
			
			Log.d(LOGGER, "TrackerService stated");
			mUpdatesRequested = true;			
			mLocationClient.connect();
		}

		return START_STICKY;
	}

	/**
	 * Set up a new LocationClient.
	 */
	private void setUpLocationClientIfNeeded() {
		if (mLocationClient == null) {
			mLocationClient = new LocationClient(this, this, this);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		String msg = Double.toString(location.getLatitude())
				+ Double.toString(location.getLongitude());

		Log.d(LOGGER, msg);
		
		// send broadcast intent
		intent.putExtra("location", location);
		intent.setAction(BROADCAST_ACTION);
		sendBroadcast(intent);
		
		ITrackerDatabase database = new TrackerDatabase(this);
		database.addTrackingLocation(location);
		
		Intent serviceIntent = new Intent(this, UploadService.class);
		serviceIntent.putExtra("latitude", location.getLatitude());
		serviceIntent.putExtra("longitude", location.getLongitude());
		startService(serviceIntent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		mUpdatesRequested = false;

		if (serviceAvailable && mLocationClient != null) {
			mLocationClient.removeLocationUpdates(this);
			mLocationClient = null;
		}

		Log.d(LOGGER, "TrackerService stopped");

		super.onDestroy();
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		if (mUpdatesRequested) {
			mLocationClient.requestLocationUpdates(mLocationRequest, this);
			Log.d(LOGGER, "Location requested");
		}
	}

	@Override
	public void onDisconnected() {
		Log.d(LOGGER, "TrackerService disconnected");
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		mUpdatesRequested = false;
		
		// try to resolve the error with the Google Play service
		if (connectionResult.hasResolution()) {
			// TODO try to resolve the problem
		} else {
			// TODO if no resolution is available
		}
	}
}
