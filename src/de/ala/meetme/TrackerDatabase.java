package de.ala.meetme;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class TrackerDatabase implements ITrackerDatabase {

	private static final String LOGGER = TrackerDatabase.class.getSimpleName();

	private static int lastId = 0;

	private TrackerDBHelper dbhelper;
	private SQLiteDatabase trackerDatabase;

	/**
	 * Constructor. Reference the TrackerDBHelper instance.
	 * 
	 * @param context
	 *            the application context
	 */
	public TrackerDatabase(Context context) {
		dbhelper = TrackerDBHelper.getInstance(context);
		Log.i(LOGGER, "new TrackerDatabase created.");
	}

	@Override
	public void addTrackingLocation(Location location) {
		// put location data into a ContentValue-object
		ContentValues values = new ContentValues();
		values.put(TrackerDBHelper.LATITUDE, location.getLatitude());
		values.put(TrackerDBHelper.LONGITUDE, location.getLongitude());

		openWritableTrackerDatabase();

		// insert the information and get the id in return
		long insertId = trackerDatabase.insert(TrackerDBHelper.TABLE_NAME,
				null, values);

		if (insertId == -1) {
			Log.d(LOGGER, "Location could not be added.");
		} else {
			Log.d(LOGGER, "new ID: " + insertId);
			lastId++;
		}

		closeDatabase();
	}

	/**
	 * Opens the database with write access.
	 */
	private void openWritableTrackerDatabase() {
		trackerDatabase = dbhelper.getWritableDatabase();
		Log.i(LOGGER, "Open database with write access.");
	}

	@Override
	public Location getLastTrackingLocation() {
		openReadableDatabase();

		// query: select ALL where ID=lastId
		Location location = new Location((Location) null);
		String selectQuery = "SELECT * FROM " + TrackerDBHelper.TABLE_NAME
				+ " WHERE " + TrackerDBHelper.ID + "=" + lastId;

		Cursor cursor = trackerDatabase.rawQuery(selectQuery, null);

		cursor.moveToFirst();
		location.setLatitude(cursor.getDouble(1));
		location.setLongitude(cursor.getDouble(2));

		cursor.close();
		closeDatabase();

		return location;
	}

	@Override
	public List<LatLng> getAllTrackingLocations() {
		openReadableDatabase();
		List<LatLng> locations = new ArrayList<LatLng>();

		// query: Select ALL
		String selectQuery = "SELECT * FROM " + TrackerDBHelper.TABLE_NAME;
		Cursor cursor = trackerDatabase.rawQuery(selectQuery, null);

		// loop over each row
		if (cursor.moveToFirst()) {
			do {
				LatLng location = new LatLng(cursor.getDouble(1),
						cursor.getDouble(2));

				locations.add(location);
			} while (cursor.moveToNext());
		}

		cursor.close();
		closeDatabase();

		return locations;
	}

	/**
	 * Opens the database with read access.
	 */
	private void openReadableDatabase() {
		trackerDatabase = dbhelper.getReadableDatabase();
		Log.i(LOGGER, "Open database with read access.");
	}

	/**
	 * Closes the database.
	 */
	private void closeDatabase() {
		dbhelper.close();
		Log.i(LOGGER, "Database closed.");
	}

	@Override
	public void removeAllEntries() {
		openWritableTrackerDatabase();
		
		String selectQuery = "DELETE FROM " + TrackerDBHelper.TABLE_NAME;
		trackerDatabase.execSQL(selectQuery);

		closeDatabase();
	}

}
