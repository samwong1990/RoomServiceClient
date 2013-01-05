package com.samwong.hk.roomserviceclient;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.samwong.hk.roomservice.api.commons.dataFormat.Report;
import com.samwong.hk.roomservice.api.commons.dataFormat.Response;
import com.samwong.hk.roomservice.api.commons.dataFormat.WifiInformation;
import com.samwong.hk.roomserviceclient.apicalls.AddNewInstance;
import com.samwong.hk.roomserviceclient.apicalls.GetListOfRooms;
import com.samwong.hk.roomserviceclient.apicalls.RoomQuery;
import com.samwong.hk.roomserviceclient.constants.LogLevel;
import com.samwong.hk.roomserviceclient.constants.LogTag;
import com.samwong.hk.roomserviceclient.helpers.Console;
import com.samwong.hk.roomserviceclient.helpers.WifiScanner;

public class MainActivity extends Activity {
	private static final int TRACKING_MODE_POLLING_FREQUENCY_IN_MILLISEC = 1000;
	final Activity thisActivity = this; // a reference for callbacks

	public void locateMe(View view) {
		final Button locateMeButton = (Button) findViewById(R.id.locateMeButton);
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				locateMeButton.setEnabled(false);
				locateMeButton.setText(getText(R.string.locateMeBtnWaiting));
			}
		});
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			new RoomQuery() {
				@Override
				protected void onPostExecute(final List<Report> results) {
					{
						((LinearLayout) findViewById(R.id.resultsArea))
								.removeAllViews();

						locateMeButton
								.setText(getText(R.string.locateMeBtnString));
						locateMeButton.setEnabled(true);
						if (results == null) {
							Console.println(thisActivity, LogLevel.ERROR,
									LogTag.APICALL, "No response from server."
											+ this.getLastException());
							return;
						}

						LinearLayout linearLayout = (LinearLayout) findViewById(R.id.resultsArea);
						final List<Button> buttonsAdded = new LinkedList<Button>();
						for (final Report report : results) {
							final String room = report.getRoom();
							Console.println(thisActivity, LogLevel.INFO,
									LogTag.APICALL,
									"Algo: " + report.getAlgorithm()
											+ " | Location Report: " + room
											+ " | Notes: " + report.getNotes());
							final Button button = new Button(getBaseContext());
							if (room == null) {
								button.setText(getString(R.string.noResult,
										report.getAlgorithm()));
								button.setEnabled(false);
							} else {
								button.setText(report.getAlgorithm() + " "
										+ room);
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

									new AddNewInstance<Report>() {
										@Override
										protected void onPostExecute(
												Response response) {
											Console.println(thisActivity,
													LogLevel.INFO,
													LogTag.APICALL,
													response.toString());
										}
									}.execute(results.get(0));
									button.setText(getString(
											R.string.reinforceBtnSuccess, room));
									button.setEnabled(false);
								}
							});
							buttonsAdded.add(button);
							TextView textView = new TextView(getBaseContext());
							textView.setText(report.getNotes());
							LinearLayout row = new LinearLayout(
									getBaseContext());
							row.addView(button);
							row.addView(textView);
							linearLayout.addView(row);
						}
					}
				}
			}.execute(WifiScanner.getWifiInformation(this));
		} else {
			// changeButtonText("No connection :(", 5000L);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		updateRoomList();
	}

	private void updateRoomList() {
		new GetListOfRooms() {

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
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

	private Timer trackingModeScheduler = null;

	public synchronized void toggleTrackingMode(final View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();

		if (on) {
			if (trackingModeScheduler != null) {
				trackingModeScheduler.cancel();
			}
			trackingModeScheduler = new Timer();
			trackingModeScheduler.scheduleAtFixedRate(new TimerTask() {

				@Override
				public void run() {
					locateMe(view);
				}
			}, 0, TRACKING_MODE_POLLING_FREQUENCY_IN_MILLISEC);
		} else {
			trackingModeScheduler.cancel();
		}
	}

	public void addRoom(View view) {
		final Button addRoomButton = (Button) findViewById(R.id.addRoomButton);
		addRoomButton.setText(getText(R.string.addRoomButtonWaiting));
		addRoomButton.setEnabled(false);
		new AddNewInstance<WifiInformation>() {

			@Override
			protected void onPostExecute(Response response) {
				Console.println(thisActivity, LogLevel.INFO, LogTag.APICALL,
						response == null ? "no response" : response.toString());
				addRoomButton.setText(getText(R.string.addRoomButtonSuccess));
				addRoomButton.setEnabled(true);
			}

		}.execute(WifiScanner.getWifiInformation(this).withRoom(
				((AutoCompleteTextView) findViewById(R.id.newRoomIdentifier)).getText()
						.toString()));
	}

}
