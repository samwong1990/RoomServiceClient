package com.samwong.hk.roomserviceclient;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ToggleButton;

import com.samwong.hk.roomservice.api.commons.dataFormat.Response;
import com.samwong.hk.roomservice.api.commons.dataFormat.WifiInformation;
import com.samwong.hk.roomservice.api.commons.parameterEnums.ReturnCode;
import com.samwong.hk.roomserviceclient.apicalls.GetListOfRooms;
import com.samwong.hk.roomserviceclient.apicalls.SubmitBatchTrainingData;
import com.samwong.hk.roomserviceclient.constants.LogLevel;
import com.samwong.hk.roomserviceclient.constants.LogTag;
import com.samwong.hk.roomserviceclient.helpers.AsyncTrainingDataAccumulator;
import com.samwong.hk.roomserviceclient.helpers.Console;

public class TrainingActivity extends Activity {
	final Activity thisActivity = this; // a reference for callbacks
	
	private AsyncTrainingDataAccumulator currentAccumulator;
	
	private void updateRoomList() {
		new GetListOfRooms(thisActivity) {
			@Override
			protected void onPostExecute(List<String> result) {
				Console.println(thisActivity, LogLevel.INFO, LogTag.APICALL,
						"Received roomList:" + result);
				if (result == null) {
					Console.println(
							thisActivity,
							LogLevel.ERROR,
							LogTag.APICALL,
							"No response for the roomList query."
									+ this.getLastException());
					return;
				}
				String[] resultArray = result
						.toArray(new String[result.size()]);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						thisActivity,
						android.R.layout.simple_dropdown_item_1line,
						resultArray);
				((AutoCompleteTextView) findViewById(R.id.newRoomIdentifier))
						.setAdapter(adapter);
			}
		}.execute();
	}


	public synchronized void toggleDataCollectionMode(final View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();
		Button submitButton = (Button) findViewById(R.id.submitDataButton);
		if (on) {
			submitButton.setText(getText(R.string.submitDataForRoomWaiting));
			submitButton.setEnabled(false);
			
			AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.newRoomIdentifier);
			String roomName = textView.getText().toString();

			currentAccumulator = new AsyncTrainingDataAccumulator(roomName) {
				@Override
				protected void onProgressUpdate(WifiInformation... values) {
					Console.println(thisActivity, LogLevel.INFO, LogTag.RESULT, "recorded a datapoint");
				}
			};
			currentAccumulator.execute(this);
		} else {
			currentAccumulator.stop();
			Console.println(thisActivity, LogLevel.INFO, LogTag.RESULT, "Done, recorded " + currentAccumulator.getFingerprints().size() + " datapoint");
			submitButton.setText(String.format("%s %s", getString(R.string.submitDataForRoom_), currentAccumulator.getRoomName()));
			submitButton.setEnabled(true);
			submitButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new SubmitBatchTrainingData(currentAccumulator.getRoomName(), thisActivity) {
						@Override
						protected void onPostExecute(Response result) {
							if(!result.getReturnCode().equals(ReturnCode.OK)){
								Console.println(thisActivity, LogLevel.ERROR, LogTag.APICALL, result.getExplanation());
							}
						}
					};
				}
			});
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_reloadRoomList:
			updateRoomList();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_training);
		updateRoomList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_training, menu);
		return true;
	}

}
