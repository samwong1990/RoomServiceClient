package com.samwong.hk.roomserviceclient.helpers;

import java.util.concurrent.CountDownLatch;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import com.samwong.hk.roomserviceclient.constants.LogLevel;
import com.samwong.hk.roomserviceclient.constants.LogTag;

/**
 * A hacky way of asynchronisely getting location
 * Blocks getLocation until it is ready.
 * @author wongsam
 *
 */
public class LocateWithCellTower extends
		AsyncTaskWithExceptions<Context, Void, Void> {
	private Location result = null;
	private CountDownLatch countDownLatch = new CountDownLatch(1);

	@Override
	protected Void doInBackground(Context... param) {
		if (param.length != 1) {
			throw new IllegalArgumentException("Excepts a single context");
		}
		Context context = param[0];

		// Begin getting a network fix. Used to limit search space
		LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				result = location;
				countDownLatch.countDown();
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};
		// Register the listener with the Location Manager to receive location
		// updates
		locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,
				locationListener, Looper.getMainLooper());
		return null;
	}


	public Location getLocation(){
		while (countDownLatch.getCount() > 0) {
			try {
				countDownLatch.await();
			} catch (InterruptedException e) {
				Console.println(null, LogLevel.ERROR, LogTag.APICALL, "Interrupted while waiting for location");
			}
		}
		return result;
	}
}
