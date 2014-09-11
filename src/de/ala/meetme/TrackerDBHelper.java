package de.ala.meetme;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TrackerDBHelper extends SQLiteOpenHelper {

	private static final String LOGGER = TrackerDBHelper.class.getSimpleName();
	private static final String DATABASE_NAME = "TrackerDB";
	private static final int DATABASE_VERSION = 1;

	private static TrackerDBHelper dbHelper = null;

	// table name
	protected static final String TABLE_NAME = "Locations";

	// column names
	protected static final String ID = "id";
	protected static final String LATITUDE = "latitude";
	protected static final String LONGITUDE = "longitude";

	// Create table statement
	private String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + ID
			+ " INTEGER PRIMARY KEY," + LATITUDE + " REAL," + LONGITUDE + ")";

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            the application context
	 */
	private TrackerDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Log.i(LOGGER, "SightingDataHelper created");
	}

	/**
	 * Singleton for the TrackerDBHelper.
	 * 
	 * @param context
	 *            the applocation context
	 * @return an instance of the TrackerDBHelper
	 */
	public static TrackerDBHelper getInstance(Context context) {
		if (dbHelper == null) {
			dbHelper = new TrackerDBHelper(context);
		}

		return dbHelper;
	}

	// creates a new database if it does not exist
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE);
		Log.i(LOGGER, "onCreate(): New table created.");
	}

	// Called when there is a database version mismatch and the database needs
	// to be upgraded to the current version.
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
		Log.i(LOGGER, "onUpgrade(): droped the old table and created a new one");

		// XXX you can do better...
	}

}
