package com.samwong.hk.roomserviceclient.apicalls;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.samwong.hk.roomservice.api.commons.dataFormat.AuthenticationDetails;
import com.samwong.hk.roomservice.api.commons.parameterEnums.Operation;
import com.samwong.hk.roomservice.api.commons.parameterEnums.ParameterKey;
import com.samwong.hk.roomserviceclient.constants.LogTag;
import com.samwong.hk.roomserviceclient.helpers.AsyncTaskWithExceptionsAndContext;
import com.samwong.hk.roomserviceclient.helpers.AuthenticationDetailsPreperator;
import com.samwong.hk.roomserviceclient.helpers.URLBuilder;

public abstract class GetListOfRooms extends
		AsyncTaskWithExceptionsAndContext<Void, Void, List<String>> {

	public GetListOfRooms(Context context) {
		super(context);
	}

	@Override
	protected List<String> doInBackground(Void... params) {
		HttpURLConnection urlConnection = null;
		try {
			AuthenticationDetails authenticationDetails = new AuthenticationDetailsPreperator().getAuthenticationDetails(getContext());
			
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair(ParameterKey.OPERATION.toString(), Operation.GET_LIST_OF_ROOMS.toString()));
			nvps.add(new BasicNameValuePair(ParameterKey.AUENTICATION_DETAILS.toString(), AuthenticationDetailsPreperator.getAuthenticationDetailsAsJson(authenticationDetails)));
			
			urlConnection = (HttpURLConnection) URLBuilder.build(nvps).openConnection();
			urlConnection.setConnectTimeout(1000);
			urlConnection.setChunkedStreamingMode(0);
			urlConnection.connect();
			Scanner scanner = new Scanner(urlConnection.getInputStream(),
					"UTF-8").useDelimiter("\\A");
			if (scanner.hasNext()) {
				String result = scanner.next();
				Log.i(LogTag.APICALL.toString(), result);
				return new Gson().fromJson(result,
						new TypeToken<List<String>>() {
						}.getType());
			}
			Log.w(LogTag.APICALL.toString(),
					"No response for the list of room query");
		} catch (IOException e) {
			addException(e);
			Log.e(LogTag.APICALL.toString(), "Caught IOException: " + e);
		} finally {
			if (urlConnection != null)
				urlConnection.disconnect();
		}
		return null;
	}

	abstract protected void onPostExecute(List<String> result);

}
