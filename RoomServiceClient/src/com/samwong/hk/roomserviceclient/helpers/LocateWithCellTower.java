package com.samwong.hk.roomserviceclient.helpers;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

/**
 * A hacky way of asynchronisely getting location
 * Blocks getLocation until it is ready.
 * @author wongsam
 *
 */

public class LocateWithCellTower{
	public void requestSingleUpdate(Context context, final LocationReceiver receiver){
		// Acquire a reference to the system Location Manager
		final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		      // Called when a new location is found by the network location provider.
		      receiver.setLocation(location);
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {}

		    public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {}
		  };

		// Register the listener with the Location Manager to receive location updates
		locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, Looper.getMainLooper());
	}
}