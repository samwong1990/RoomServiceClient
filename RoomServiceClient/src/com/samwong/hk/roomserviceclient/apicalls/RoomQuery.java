package com.samwong.hk.roomserviceclient.apicalls;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.samwong.hk.roomservice.api.commons.dataFormat.Report;
import com.samwong.hk.roomservice.api.commons.dataFormat.WifiInformation;
import com.samwong.hk.roomservice.api.commons.helper.InstanceFriendlyGson;
import com.samwong.hk.roomservice.api.commons.parameterEnums.ParameterKey;
import com.samwong.hk.roomserviceclient.constants.Defaults;
import com.samwong.hk.roomserviceclient.constants.LogTag;
import com.samwong.hk.roomserviceclient.constants.URLs;
import com.samwong.hk.roomserviceclient.helpers.AsyncTaskWithExceptions;

public abstract class RoomQuery extends
		AsyncTaskWithExceptions<WifiInformation, Integer, List<Report>> {
	protected List<Report> doInBackground(WifiInformation... wifiInformation) {
		if (wifiInformation.length != 1) {
			Log.e(LogTag.APICALL.toString(),
					"Expects a single wifiInformation, given "
							+ wifiInformation.length);
		}
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair(ParameterKey.CLASSIFIER
				.toString(), Defaults.classifier.toString()));
		nameValuePairs.add(new BasicNameValuePair(ParameterKey.OBSERVATION
				.toString(), new Gson().toJson(wifiInformation[0]))); // Straight
																		// to
																		// zero,
																		// as
																		// this
																		// is a
																		// controlled
																		// environment.
		UrlEncodedFormEntity postData;

		HttpURLConnection urlConnection = null;
		try {
			urlConnection = (HttpURLConnection) new URL(URLs.SERVLET_URL)
					.openConnection();
			urlConnection.setConnectTimeout(1000);
			urlConnection.setRequestMethod("POST");
			postData = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
			urlConnection.setDoOutput(true);
			urlConnection.setFixedLengthStreamingMode((int) postData
					.getContentLength());
			postData.writeTo(urlConnection.getOutputStream());
			urlConnection.connect();
			Scanner scanner = new Scanner(urlConnection.getInputStream(),
					"UTF-8").useDelimiter("\\A");
			if (scanner.hasNext()) {
				String result = scanner.next();
				Log.i(LogTag.APICALL.toString(), result);
				return InstanceFriendlyGson.gson.fromJson(result,
						new TypeToken<List<Report>>() {
						}.getType());
			}
			Log.w(LogTag.APICALL.toString(), "No response for the room query");
		} catch (IOException e) {
			addException(e);
			Log.e(LogTag.APICALL.toString(), "Caught IOException: " + e);
		} finally {
			if (urlConnection != null)
				urlConnection.disconnect();
		}
		return null;
	}

	abstract protected void onPostExecute(List<Report> result);
}