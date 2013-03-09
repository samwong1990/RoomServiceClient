package com.samwong.hk.roomserviceclient.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import android.app.Activity;
import android.util.Log;

import com.samwong.hk.roomservice.api.commons.dataFormat.WifiInformation;
import com.samwong.hk.roomserviceclient.constants.LogTag;

/**
 * Quick a hacky way of doing things. Looking for a better way to do this.
 * Ideally the onPreExecute bit should be in doInBackground, yet AsyncTask but be started from UI thread.
 * This limits it to either onPreExecute or onProgressUpdate or onPostExecute
 * I do want to keep onProgressUpdate for real progress update, so I've decided to put it in onPreExecute.
 *  
 * The problem with onPreExecute is that the poller would run before the user calls execute on TrainingDataAccumulator.
 * So I use the Context to execute the poller in the UI thread.
 * 
 * @author wongsam
 *
 */
public abstract class TrainingDataAccumulator extends
		AsyncTaskWithExceptions<Activity, WifiInformation, List<WifiInformation>> {

	private final String roomName;
	private final List<WifiInformation> fingerprintList = new ArrayList<WifiInformation>();;
	private final TrainingDataAccumulator trainingAsyncTask = this;
	private final CountDownLatch blocker = new CountDownLatch(1);
	private WifiScannerPoller poller;
	
	public TrainingDataAccumulator(String roomName){
		this.roomName = roomName;
	}
	
	@Override
	protected abstract void onCancelled(List<WifiInformation> results);
	
	@Override
	protected void onPreExecute() {
		poller = new WifiScannerPoller() {
			@Override
			protected void onProgressUpdate(WifiInformation... scanResult) {
				if(!trainingAsyncTask.isCancelled()){
					fingerprintList.add(scanResult[0]);
					trainingAsyncTask.publishProgress(scanResult[0]);
				}else{
					this.cancel(false);
				}
			}
			protected void onCancelled() {
				blocker.countDown();
			};
		};
	}
	
	@Override
	protected List<WifiInformation> doInBackground(Activity... params) {
		if(params.length != 1){
			addException(new IllegalArgumentException("Expects a single activity as param."));
		}
		final Activity activity = params[0];
		// execute the poller
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				poller.execute(activity);
			}
		});
		// block until polling is completed
		while(blocker.getCount() != 0){
			try {
				blocker.await();
			} catch (InterruptedException e) {
				addException(e);
				Log.e(LogTag.CLIENT.toString(), "Interupted while waiting for WifiPoller to finish.");
			}
		}
		return fingerprintList;
	}
	
	public String getRoomName() {
		return roomName;
	}	

}
