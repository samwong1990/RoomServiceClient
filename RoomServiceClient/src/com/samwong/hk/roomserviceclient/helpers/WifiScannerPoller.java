package com.samwong.hk.roomserviceclient.helpers;

import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;

import com.samwong.hk.roomservice.api.commons.dataFormat.WifiInformation;
import com.samwong.hk.roomserviceclient.constants.Defaults;
import com.samwong.hk.roomserviceclient.constants.LogTag;

/**
 * Due to android reporting old wifi scans, we use this poller to publish new wifi fingerprints.
 * Typical usage would be to 'execute' this task, and then do your logic with the new wifi fingerprints in the onProgressUpdate method.
 * Call cancel when you're done.
 * Don't forget to use onCancelled instead of onPostExecute to clean up/whatever afterwards.
 * @author wongsam
 *
 */
public abstract class WifiScannerPoller extends
		AsyncTaskWithExceptions<Activity, WifiInformation, Void> {
	
	WifiInformation prevScan;
	
	@Override
	abstract protected void onProgressUpdate(WifiInformation... values);
	
	@Override
	protected Void doInBackground(Activity... params) {
		if(params.length != 1){
			addException(new IllegalArgumentException("Expects a single activity as param."));
		}
		Activity activity = params[0];
		WifiInformation datapoint = null;
		long lastUpdate = System.currentTimeMillis();
		while(!isCancelled()){
			datapoint = WifiScanner.getWifiInformation(activity);
			
			if(!datapoint.equals(prevScan)){
				Log.i(LogTag.CLIENT.toString(), "Obtained new fingerprint after " + (lastUpdate - System.currentTimeMillis()) + "ms");
				prevScan = datapoint;
				lastUpdate = System.currentTimeMillis();
				publishProgress(datapoint);
			}else{
				Log.i(LogTag.CLIENT.toString(), "Same old fingerprint");
			}
			SystemClock.sleep(Defaults.SAMPLING_INTERVAL_IN_MILLISEC);
		}
		return null;
	}
}
