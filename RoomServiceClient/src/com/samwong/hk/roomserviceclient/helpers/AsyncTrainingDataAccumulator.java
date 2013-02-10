package com.samwong.hk.roomserviceclient.helpers;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;

import com.samwong.hk.roomservice.api.commons.dataFormat.WifiInformation;

public class AsyncTrainingDataAccumulator extends
		AsyncTaskWithExceptions<Activity, WifiInformation, List<WifiInformation>> {

	private boolean isCollectingData = true;
	private final String roomName;
	private List<WifiInformation> fingerprints = null;
	
	public AsyncTrainingDataAccumulator(String roomName){
		this.roomName = roomName;
	}
	
	@Override
	protected List<WifiInformation> doInBackground(Activity... params) {
		if(params.length != 1){
			addException(new IllegalArgumentException("Expects a single activity as param."));
		}
		Activity activity = params[0];
		List<WifiInformation> accumulator = new LinkedList<WifiInformation>();
		while(isCollectingData){
			WifiInformation datapoint = WifiScanner.getWifiInformation(activity);
			accumulator.add(datapoint);
			publishProgress(datapoint);
		}
		fingerprints = accumulator;
		return accumulator;
	}
	
	public List<WifiInformation> getFingerprints(){
		return fingerprints;
	}
	
	public void stop(){
		isCollectingData = false;
	}
	
	public String getRoomName() {
		return roomName;
	}	

}
