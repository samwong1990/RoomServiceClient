package com.samwong.hk.roomserviceclient;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ToggleButton;

import com.samwong.hk.roomservice.api.commons.dataFormat.Response;
import com.samwong.hk.roomservice.api.commons.dataFormat.WifiInformation;
import com.samwong.hk.roomservice.api.commons.parameterEnums.ReturnCode;
import com.samwong.hk.roomserviceclient.apicalls.SubmitBatchTrainingData;
import com.samwong.hk.roomserviceclient.constants.LogLevel;
import com.samwong.hk.roomserviceclient.constants.LogTag;
import com.samwong.hk.roomserviceclient.helpers.AsyncTrainingDataAccumulator;
import com.samwong.hk.roomserviceclient.helpers.Console;

public class TrainingFragment extends Fragment {
	private List<String> latestRoomList = Collections.emptyList();
	private List<String> currentRoomList = null;

	private AsyncTrainingDataAccumulator currentAccumulator;

	public void updateAutoComplete(List<String> result, Activity activity) {
		latestRoomList = result;
		if (!latestRoomList.equals(currentRoomList)) {
			currentRoomList = latestRoomList;
			AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) activity
					.findViewById(R.id.newRoomIdentifier);
			if (autoCompleteTextView != null) {
				String[] resultArray = latestRoomList
						.toArray(new String[latestRoomList.size()]);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						activity,
						android.R.layout.simple_dropdown_item_1line,
						resultArray);
				autoCompleteTextView.setAdapter(adapter);
			}
		}
	}

	public synchronized void toggleDataCollectionMode(final View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();
		Button submitButton = (Button) getActivity().findViewById(
				R.id.submitDataButton);
		if (on) {
			submitButton.setText(getText(R.string.submitDataForRoomWaiting));
			submitButton.setEnabled(false);
			AutoCompleteTextView textView = (AutoCompleteTextView) getActivity()
					.findViewById(R.id.newRoomIdentifier);
			String roomName = textView.getText().toString();
			currentAccumulator = new AsyncTrainingDataAccumulator(roomName) {
				@Override
				protected void onProgressUpdate(WifiInformation... values) {
					Console.println(getActivity(), LogLevel.INFO,
							LogTag.RESULT, "recorded a datapoint");
				}
			};
			currentAccumulator.execute(getActivity());
		} else {
			currentAccumulator.cancel(false);
			Console.println(getActivity(), LogLevel.INFO, LogTag.RESULT,
					"Done, recorded "
							+ currentAccumulator.getFingerprints().size()
							+ " datapoint");
			submitButton.setText(String.format("%s %s",
					getString(R.string.submitDataForRoom_),
					currentAccumulator.getRoomName()));
			submitButton.setEnabled(true);
			submitButton.setOnClickListener(new OnClickListener() {
				@SuppressWarnings("unchecked")
				// This is for varargs parameter. This should be type safe.
				@Override
				public void onClick(View v) {
					Log.d(LogTag.DEBUGGING.toString(),
							"Creating onclick obj for submitButton");
					new SubmitBatchTrainingData(currentAccumulator
							.getRoomName(), getActivity()) {
						@Override
						protected void onPostExecute(Response result) {
							if (!result.getReturnCode().equals(ReturnCode.OK)) {
								Console.println(getActivity(), LogLevel.ERROR,
										LogTag.APICALL, result.getExplanation());
							} else {
								Console.println(getActivity(), LogLevel.ERROR,
										LogTag.APICALL, "OK");
							}
						}
					}.execute(currentAccumulator.getFingerprints());
				}
			});
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		((ToggleButton) getActivity().findViewById(R.id.ToggleDataCollection))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						toggleDataCollectionMode(v);
					}
				});
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_training, container, false);

	}
}
