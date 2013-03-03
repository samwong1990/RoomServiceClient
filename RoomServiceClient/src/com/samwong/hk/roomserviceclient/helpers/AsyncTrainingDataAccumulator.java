package com.samwong.hk.roomserviceclient.helpers;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.os.SystemClock;

import com.samwong.hk.roomservice.api.commons.dataFormat.WifiInformation;

public class AsyncTrainingDataAccumulator extends
		AsyncTaskWithExceptions<Activity, WifiInformation, List<WifiInformation>> {

	private static final long SAMPLING_INTERVAL_IN_MILLISEC = 500;
	private final String roomName;
	private final List<WifiInformation> fingerprints = new LinkedList<WifiInformation>();;
	
	public AsyncTrainingDataAccumulator(String roomName){
		this.roomName = roomName;
	}
	
	@Override
	protected List<WifiInformation> doInBackground(Activity... params) {
		if(params.length != 1){
			addException(new IllegalArgumentException("Expects a single activity as param."));
		}
		Activity activity = params[0];
		while(!isCancelled()){
			WifiInformation datapoint = WifiScanner.getWifiInformation(activity);
			fingerprints.add(datapoint);
			publishProgress(datapoint);
			SystemClock.sleep(SAMPLING_INTERVAL_IN_MILLISEC);
		}
		return fingerprints;
	}
	
	public List<WifiInformation> getFingerprints(){
		return fingerprints;
	}
	
	public String getRoomName() {
		return roomName;
	}	

}
