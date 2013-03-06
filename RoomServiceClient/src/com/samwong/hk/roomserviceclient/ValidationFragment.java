package com.samwong.hk.roomserviceclient;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ToggleButton;

import com.samwong.hk.roomservice.api.commons.dataFormat.Report;
import com.samwong.hk.roomservice.api.commons.dataFormat.ResponseWithReports;
import com.samwong.hk.roomserviceclient.apicalls.RoomQuery;
import com.samwong.hk.roomserviceclient.constants.LogLevel;
import com.samwong.hk.roomserviceclient.constants.LogTag;
import com.samwong.hk.roomserviceclient.helpers.Console;

public class ValidationFragment extends Fragment {
	private static final int POLLING_FREQUENCY_IN_MILLISEC = 1000;
	
	private List<String> latestRoomList = Collections.emptyList();
	private List<String> currentRoomList = null;
	private Timer validationScheduler = null;
	private ConcurrentHashMap<String, AtomicIntegerArray> counter; // 0 -> totals, 1-> hits
	
	public synchronized void toggleValidationMode(final View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();
		AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) getActivity().findViewById(R.id.roomPicker);
		if (on) {
			// Lock the room name
			autoCompleteTextView.setEnabled(false);
			final String expectedRoom = autoCompleteTextView.getText().toString();
			// reset counters
			counter = new ConcurrentHashMap<String, AtomicIntegerArray>();
			
			if (validationScheduler != null) {
				validationScheduler.cancel();
			}
			validationScheduler = new Timer();
			validationScheduler.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					validateLocation(expectedRoom);
				}
			}, 0, POLLING_FREQUENCY_IN_MILLISEC);
		} else {
			validationScheduler.cancel();
			//TODO print results and create upload button
		}
	}
	
	private void validateLocation(final String expectedRoom) {
		// Requires wifi on AND connected to internet
		new RoomQuery(getActivity()) {
			@Override
			protected void onPostExecute(final ResponseWithReports results) {
				{
					if (results == null) {
						Console.println(getActivity(), LogLevel.ERROR,
								LogTag.APICALL, "No response from server."
										+ this.getLastException());
						for (Exception e : this.getExceptions()) {
							Console.println(getActivity(), LogLevel.ERROR,
									LogTag.APICALL, e.toString());
							return;
						}
					}
					for (final Report report : results.getReports()) {
						final String room = report.getRoom();
						final String algoName = report.getAlgorithm();
						if(!counter.containsKey(algoName)){
							counter.put(algoName, new AtomicIntegerArray(2));
						}
						AtomicIntegerArray tracker = counter.get(room);
						tracker.addAndGet(0, 1);	// increment total number of queries
						if(expectedRoom.equals(room)){
							tracker.addAndGet(1, 1);
							Console.println(getActivity(), LogLevel.INFO,
									LogTag.RESULT,
									String.format("Match! increment counter. hit ratio: %d/%d", tracker.get(0), tracker.get(1))); 
						}else{
							Console.println(getActivity(), LogLevel.INFO,
									LogTag.RESULT,
									String.format("Missed. hit ratio: %d/%d", tracker.get(0), tracker.get(1)));
						}
						Console.println(getActivity(), LogLevel.INFO,
								LogTag.APICALL,
								"Algo: " + report.getAlgorithm()
										+ " | Location Report: " + room
										+ " | Notes: " + report.getNotes());
					}
				}
			}

		}.execute(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.validation_fragment, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		((ToggleButton)view.findViewById(R.id.validationToggler)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleValidationMode(v);
				return;
			}
		});
		super.onViewCreated(view, savedInstanceState);
	}

	public void updateAutoComplete(List<String> result, Activity activity) {
		latestRoomList = result;
		if (!latestRoomList.equals(currentRoomList)) {
			currentRoomList = latestRoomList;
			AutoCompleteTextView autoCompleteTextView = ((AutoCompleteTextView) activity
					.findViewById(R.id.roomPicker));
			if (autoCompleteTextView != null) {
				String[] resultArray = latestRoomList.toArray(new String[latestRoomList
						.size()]);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						activity, android.R.layout.simple_dropdown_item_1line,
						resultArray);
				((AutoCompleteTextView) activity.findViewById(R.id.roomPicker))
						.setAdapter(adapter);
			}
		}
	}
}