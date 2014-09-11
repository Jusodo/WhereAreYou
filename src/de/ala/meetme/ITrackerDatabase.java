package de.ala.meetme;

import java.util.List;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public interface ITrackerDatabase {

	/**
	 * Adds a new location to the database.
	 * 
	 * @param location
	 *            the location
	 */
	public void addTrackingLocation(Location location);

	/**
	 * Returns the last location, that were added to the databse.
	 * 
	 * @return the last location
	 */
	public Location getLastTrackingLocation();

	/**
	 * Returns a list with all locations.
	 * 
	 * @return list with location
	 */
	public List<LatLng> getAllTrackingLocations();

	/**
	 * Removes all entries from the database.
	 * 
	 */
	public void removeAllEntries();
}
