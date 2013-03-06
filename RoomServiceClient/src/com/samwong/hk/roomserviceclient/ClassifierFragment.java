package com.samwong.hk.roomserviceclient;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.samwong.hk.roomservice.api.commons.dataFormat.Report;
import com.samwong.hk.roomservice.api.commons.dataFormat.Response;
import com.samwong.hk.roomservice.api.commons.dataFormat.ResponseWithReports;
import com.samwong.hk.roomservice.api.commons.parameterEnums.ReturnCode;
import com.samwong.hk.roomserviceclient.apicalls.ConfirmValidClassification;
import com.samwong.hk.roomserviceclient.apicalls.RoomQuery;
import com.samwong.hk.roomserviceclient.constants.LogLevel;
import com.samwong.hk.roomserviceclient.constants.LogTag;
import com.samwong.hk.roomserviceclient.helpers.Console;

public class ClassifierFragment extends Fragment {
	private static final int TRACKING_MODE_POLLING_FREQUENCY_IN_MILLISEC = 2000;
	private Activity activity;

	/**
	 * Uses AsyncTask and sleep to do the polling.
	 * executeOnExecutor is used because the default executor is a serial executor. 
	 * This prevents the AsyncTask in locateMe from running.
	 * @param view
	 */
	public synchronized void toggleTrackingMode(final View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();
		// If it is on, spawn a thread to keep fetching location. 
		if (on) {
			new AsyncTask<Void, Void, Void>(){

				@Override
				protected Void doInBackground(Void... params) {
					while(((ToggleButton) view).isChecked()){
						publishProgress((Void) null);
						SystemClock.sleep(TRACKING_MODE_POLLING_FREQUENCY_IN_MILLISEC);
					}
					return null;
				}
				
				protected void onProgressUpdate(Void... values) {
					locateMe(view);
				};
				
			}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,(Void)null);
		}
	}

	/**
	 * onPostExecute, it will clear the result area, and add a button in the form of [$Algo $room].
	 * A click listener is attched to it such that when clicked, it will submit a validation to the server.
	 * This showcases how one can continuously improve the prediction quality over use.
	 * @param view
	 */
	public void locateMe(View view) {
		final Button locateMeButton = (Button) getActivity().findViewById(R.id.locateMeButton);
		locateMeButton.setEnabled(false);
		locateMeButton.setText(getText(R.string.locateMeBtnWaiting));
		
		// Requires wifi on AND connected to internet
		new RoomQuery(getActivity()) {
			@Override
			protected void onPostExecute(final ResponseWithReports results) {
				{
					((LinearLayout) activity.findViewById(R.id.resultsArea))
							.removeAllViews();

					locateMeButton.setText(getText(R.string.locateMeBtnString));
					locateMeButton.setEnabled(true);
					if (!results.getReturnCode().equals(ReturnCode.OK)) {
						Console.println(getActivity(), LogLevel.ERROR,
								LogTag.APICALL, "No response from server."
										+ this.getLastException());
						for (Exception e : this.getExceptions()) {
							Console.println(getActivity(), LogLevel.ERROR,
									LogTag.APICALL, e.toString());
							return;
						}
					}
					LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.resultsArea);
					final List<Button> buttonsAdded = new LinkedList<Button>();
					for (final Report report : results.getReports()) {
						final String room = report.getRoom();
						Console.println(getActivity(), LogLevel.INFO,
								LogTag.APICALL,
								"Algo: " + report.getAlgorithm()
										+ " | Location Report: " + room
										+ " | Notes: " + report.getNotes());
						final Button button = new Button(getActivity().getBaseContext());
						if (room == null) {
							button.setText(getString(R.string.noResult,
									report.getAlgorithm()));
							button.setEnabled(false);
						} else {
							button.setText(report.getAlgorithm() + " " + room);
						}

						button.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								button.setEnabled(false);
								button.setText(getString(
										R.string.addRoomButtonWaiting, room));
								for (Button buttonAdded : buttonsAdded) {
									buttonAdded.setEnabled(false);
								}
								// Just need one report, because the wifi
								// signature will be the same for all.

								new ConfirmValidClassification(getActivity()) {
									@Override
									protected void onPostExecute(
											Response response) {
										Console.println(getActivity(),
												LogLevel.INFO, LogTag.APICALL,
												response.getReturnCode() + ": " + response.getExplanation());
										if(response.getReturnCode().equals(ReturnCode.OK)){
											button.setText(getString(
													R.string.reinforceBtnSuccess, room));
										}else{
											button.setText(report.getRoom() + " " + getString(
													R.string.reinforceBtnFailure, room));
											for (Button buttonAdded : buttonsAdded) {
												buttonAdded.setEnabled(true);
											}
										}
										
									}
								}.execute(results.getReports().get(0));
							}
						});
						buttonsAdded.add(button);
						TextView textView = new TextView(getActivity().getBaseContext());
						textView.setText(report.getNotes());
						LinearLayout row = new LinearLayout(getActivity().getBaseContext());
						row.addView(button);
						row.addView(textView);
						linearLayout.addView(row);
					}
				}
			}

		}.execute(getActivity());
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		((ToggleButton) getActivity().findViewById(R.id.trackingToggler)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleTrackingMode(v);
			}
		});
		((Button) getActivity().findViewById(R.id.locateMeButton)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				locateMe(v);
			}
		});
		this.activity = getActivity();
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_main, container, false);
	}
}