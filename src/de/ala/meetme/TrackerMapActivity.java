package de.ala.meetme;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class TrackerMapActivity extends Activity {

	private static final String LOGGER = TrackerMapActivity.class
			.getSimpleName();

	private GoogleMap mGoogleMap;
	private Marker mMarker;
	private Polyline mPolyline;
	private ITrackerDatabase database = new TrackerDatabase(this);

	private boolean isTracking;

	private BroadcastReceiver locationReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Location location = (Location) intent.getExtras().get("location");

			drawLineOnMap();
			// saveLocation(location);
			// uploadLocation(location);
		}
	};

	// Called when the activity is created the first time
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tracker_map);

		isTracking = isMyServiceRunning();

		// Disable title in the action bar
		ActionBar mActionBar = getActionBar();
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayShowHomeEnabled(false);

		// try to initialize the map
		try {
			initMap();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_tracker_map_activity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// enable or disable tracking
		case R.id.tracking:
			if (isTracking) {
				disableTracking();
				item.setTitle("Tracking OFF");
			} else {
				enableTracking();
				item.setTitle("Tracking ON");
			}
			break;
		// clear the database
		case R.id.clear_db:
			database.removeAllEntries();
			mGoogleMap.clear();
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * Disbale tracking
	 */
	private void disableTracking() {
		isTracking = false;
		stopTrackingService();

		Toast.makeText(this, "Disable Tracking", Toast.LENGTH_SHORT).show();
		Log.d(LOGGER, "Disbale LocationService");
	}

	/**
	 * Enable tracking
	 */
	private void enableTracking() {
		isTracking = true;
		startTrackingService();

		Toast.makeText(this, "Start Tracking", Toast.LENGTH_SHORT).show();
		Log.d(LOGGER, "Enable LocationService");
	}

	/**
	 * Stop TrackingService
	 */
	private void stopTrackingService() {
		Intent stopIntent = new Intent(this, TrackerService.class);
		stopService(stopIntent);
	}

	/**
	 * Start TrackingService
	 */
	private void startTrackingService() {
		Intent startIntent = new Intent(this, TrackerService.class);
		startService(startIntent);
	}

	/**
	 * Sets up the Map if needed.
	 */
	private void initMap() {
		if (mGoogleMap == null) {
			mGoogleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.tracker_map)).getMap();

			// check if map is created successfully or not
			if (mGoogleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Unable to create the map", Toast.LENGTH_SHORT).show();
			} else {
				setMapToMyPosition();
			}
		}
	}

	/**
	 * Set the start position
	 */
	private void setMapToMyPosition() {
		mGoogleMap.setMyLocationEnabled(true);
		Location mLocation = mGoogleMap.getMyLocation();

		if (mLocation != null) {
			LatLng mLatLng = new LatLng(mLocation.getLatitude(),
					mLocation.getLongitude());

			mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng,
					13));
			Log.d(LOGGER, "Set map to: " + mLocation.getLatitude() + ", "
					+ mLocation.getLongitude());
		} else {
			Log.d(LOGGER, "Can not find a startposition");
		}
	}

	/**
	 * Saves the received location into the database.
	 * 
	 * @param location
	 *            location-object
	 */
	protected void saveLocation(Location location) {
		ITrackerDatabase database = new TrackerDatabase(this);
		database.addTrackingLocation(location);
	}

	/**
	 * Start the upload of a received location.
	 * 
	 * @param location
	 *            location-object
	 */
	protected void uploadLocation(Location location) {
		Intent uploadIntent = new Intent(this, UploadService.class);
		uploadIntent.putExtra("latitude", location.getLatitude());
		uploadIntent.putExtra("longitude", location.getLongitude());
		startService(uploadIntent);
	}

	@Override
	public void onResume() {
		super.onResume();

		isTracking = isMyServiceRunning();

		// register locationReceiver
		IntentFilter locationFilter = new IntentFilter();
		locationFilter.addAction(TrackerService.BROADCAST_ACTION);
		registerReceiver(locationReceiver, locationFilter);

		initMap();
	}

	protected void drawLineOnMap() {
		if(mPolyline != null) {
			mPolyline.remove();
		}
		
		PolylineOptions options = new PolylineOptions();
		options.width(4);
		options.color(Color.RED);
		
		for (LatLng location : database.getAllTrackingLocations()) {
			options.add(location);
		}
		
		mPolyline = mGoogleMap.addPolyline(options);
	}

	@Override
	public void onPause() {
		super.onPause();

		unregisterReceiver(locationReceiver);
	}

	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (TrackerService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
}
