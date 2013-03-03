package com.samwong.hk.roomserviceclient.apicalls;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.samwong.hk.roomservice.api.commons.dataFormat.AuthenticationDetails;
import com.samwong.hk.roomservice.api.commons.dataFormat.ResponseWithReports;
import com.samwong.hk.roomservice.api.commons.dataFormat.WifiInformation;
import com.samwong.hk.roomservice.api.commons.helper.InstanceFriendlyGson;
import com.samwong.hk.roomservice.api.commons.parameterEnums.Operation;
import com.samwong.hk.roomservice.api.commons.parameterEnums.ParameterKey;
import com.samwong.hk.roomserviceclient.constants.Defaults;
import com.samwong.hk.roomserviceclient.constants.LogTag;
import com.samwong.hk.roomserviceclient.constants.URLs;
import com.samwong.hk.roomserviceclient.helpers.AsyncTaskWithExceptionsAndContext;
import com.samwong.hk.roomserviceclient.helpers.AuthenticationDetailsPreperator;
import com.samwong.hk.roomserviceclient.helpers.WifiScanner;

public abstract class RoomQuery extends
AsyncTaskWithExceptionsAndContext<Activity, Integer, ResponseWithReports> {

	public RoomQuery(Context context) {
		super(context);
	}

	private static final int MAX_RETRIES = 5;

	protected ResponseWithReports doInBackground(Activity... param) {
		if (param.length == 0) {
			throw new IllegalArgumentException(
					"RoomQuery requires an activity to perform wifi scanning.");
		}
		Activity activity = param[0];
		
		WifiInformation wifiInformation = WifiScanner
				.getWifiInformation(activity);

		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair(ParameterKey.OPERATION
				.toString(), Operation.CLASSIFY.toString()));
		nameValuePairs.add(new BasicNameValuePair(ParameterKey.CLASSIFIER
				.toString(), Defaults.classifier.toString()));
		nameValuePairs.add(new BasicNameValuePair(ParameterKey.OBSERVATION
				.toString(), new Gson().toJson(wifiInformation)));
		AuthenticationDetails authenticationDetails = new AuthenticationDetailsPreperator().getAuthenticationDetails(getContext());
		nameValuePairs.add(new BasicNameValuePair(
				ParameterKey.AUENTICATION_DETAILS.toString(),
				AuthenticationDetailsPreperator.getAuthenticationDetailsAsJson(authenticationDetails)));
		
		UrlEncodedFormEntity postData;

		HttpURLConnection urlConnection = null;
		try {
			postData = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");

			urlConnection = (HttpURLConnection) new URL(URLs.SERVLET_URL)
			.openConnection();
			urlConnection.setConnectTimeout(1000);
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoOutput(true);
			urlConnection.setFixedLengthStreamingMode((int) postData
					.getContentLength());
			postData.writeTo(urlConnection.getOutputStream());
			// Wait for internet connection
			ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			int timeout = 50;
			for(int i=1; i<=MAX_RETRIES; i++){
				if(networkInfo != null && networkInfo.isConnected()) {
					urlConnection.connect();
					Scanner scanner = new Scanner(urlConnection.getInputStream(),
							"UTF-8").useDelimiter("\\A");
					if (scanner.hasNext()) {
						String result = scanner.next();
						Log.i(LogTag.APICALL.toString(), "RESPONSE:" + result);
						return InstanceFriendlyGson.gson.fromJson(result,
								new TypeToken<ResponseWithReports>() {
						}.getType());
					}
					Log.w(LogTag.APICALL.toString(), "No response for the room query");
				}else if(i != MAX_RETRIES){
					long wakeTime = System.currentTimeMillis() + timeout;
					while(wakeTime - System.currentTimeMillis() > 0){
						try {
							Thread.sleep(wakeTime - System.currentTimeMillis());
						} catch (InterruptedException e) {
							addException(e);
							e.printStackTrace();
						}
					}
					timeout *= 2;
				}else{
					addException(new ConnectException(String.format("Can't get internet connection after %d tries", i)));
				}
			}
		} catch (IOException e) {
			addException(e);
			Log.e(LogTag.APICALL.toString(), "Caught IOException: " + e);
		} finally {
			if (urlConnection != null)
				urlConnection.disconnect();
		}
		return null;
	}

	abstract protected void onPostExecute(ResponseWithReports result);
}