package com.samwong.hk.roomserviceclient.apicalls;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.samwong.hk.roomservice.api.commons.parameterEnums.Operation;
import com.samwong.hk.roomservice.api.commons.parameterEnums.ParameterKey;
import com.samwong.hk.roomserviceclient.constants.LogTag;
import com.samwong.hk.roomserviceclient.constants.URLs;
import com.samwong.hk.roomserviceclient.helpers.AsyncTaskWithExceptionsAndContext;
import com.samwong.hk.roomserviceclient.helpers.AuthenticationDetailsPreperator;

public abstract class GetListOfRooms extends AsyncTaskWithExceptionsAndContext<Void, Void, List<String>>{

	public GetListOfRooms(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected List<String> doInBackground(Void... params) {
		HttpURLConnection urlConnection = null;
		try {
			String url = URLs.SERVLET_URL + "?" + 
		ParameterKey.OPERATION.toString() + "=" + Operation.GET_LIST_OF_ROOMS.toString() + "&" +
		ParameterKey.AUENTICATION_DETAILS.toString() + "=" + AuthenticationDetailsPreperator.getAuthenticationDetailsAsJson(getContext());
			Log.i(LogTag.APICALL.toString(), "Getting list of rooms with url:" + url);
			urlConnection = (HttpURLConnection) new URL(url).openConnection();
			urlConnection.setConnectTimeout(1000);
			urlConnection.setChunkedStreamingMode(0);
			urlConnection.connect();
			Scanner scanner = new Scanner(urlConnection.getInputStream(), "UTF-8").useDelimiter("\\A");
			if (scanner.hasNext()){
				String result = scanner.next();
				Log.i(LogTag.APICALL.toString(), result);
				return new Gson().fromJson(result, new TypeToken<List<String>>(){}.getType());
			}
			Log.w(LogTag.APICALL.toString(), "No response for the list of room query");
		} catch (IOException e) {
			addException(e);
			Log.e(LogTag.APICALL.toString(), "Caught IOException: " + e);
		} finally {
				if(urlConnection != null) urlConnection.disconnect();
		}
		return null;
	}
	
	abstract protected void onPostExecute(List<String> result);


}
