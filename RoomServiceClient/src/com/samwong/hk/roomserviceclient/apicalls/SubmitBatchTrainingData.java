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
import com.samwong.hk.roomservice.api.commons.dataFormat.Response;
import com.samwong.hk.roomservice.api.commons.dataFormat.TrainingData;
import com.samwong.hk.roomservice.api.commons.dataFormat.WifiInformation;
import com.samwong.hk.roomservice.api.commons.parameterEnums.Operation;
import com.samwong.hk.roomservice.api.commons.parameterEnums.ParameterKey;
import com.samwong.hk.roomservice.api.commons.parameterEnums.ReturnCode;
import com.samwong.hk.roomserviceclient.constants.LogTag;
import com.samwong.hk.roomserviceclient.helpers.AsyncTaskWithExceptionsAndContext;
import com.samwong.hk.roomserviceclient.helpers.AuthenticationDetailsPreperator;
import com.samwong.hk.roomserviceclient.helpers.URLBuilder;

public abstract class SubmitBatchTrainingData
		extends
		AsyncTaskWithExceptionsAndContext<List<WifiInformation>, Void, Response> {

	private String room;

	public SubmitBatchTrainingData(String room, Context context) {
		super(context);
		this.room = room;
	}

	protected Response doInBackground(List<WifiInformation>... param) {
		if (param.length != 1) {
			throw new IllegalArgumentException("param.length != 1");
		}

		TrainingData trainingData = new TrainingData().withRoom(room)
				.withDatapoints(param[0]);
		Log.d(LogTag.APICALL.toString(), trainingData.toString());
		
		AuthenticationDetails authenticationDetails = new AuthenticationDetailsPreperator()
		.getAuthenticationDetails(getContext());
		
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair(ParameterKey.OPERATION
				.toString(), Operation.UPLOAD_TRAINING_DATA.toString()));
		nameValuePairs.add(new BasicNameValuePair(
				ParameterKey.BATCH_TRAINING_DATA.toString(), new Gson().toJson(
						trainingData, new TypeToken<TrainingData>() {
						}.getType())));
		nameValuePairs
				.add(new BasicNameValuePair(ParameterKey.AUENTICATION_DETAILS
						.toString(), AuthenticationDetailsPreperator
						.getAuthenticationDetailsAsJson(authenticationDetails)));
		Log.d(LogTag.APICALL.toString(), nameValuePairs.toString());
		
		HttpURLConnection urlConnection = null;
		try {
			urlConnection = (HttpURLConnection) URLBuilder.build(nameValuePairs)
					.openConnection();
			urlConnection.setRequestMethod("PUT");
			urlConnection.connect();
			Scanner scanner = new Scanner(urlConnection.getInputStream(),
					"UTF-8").useDelimiter("\\A");
			if (scanner.hasNext()) {
				String result = scanner.next();
				Log.i(LogTag.APICALL.toString(), result);
				return new Gson().fromJson(result, new TypeToken<Response>() {
				}.getType());
			}
			Log.w(LogTag.APICALL.toString(),
					"no response after posting new instance.");
			return new Response().withReturnCode(ReturnCode.NO_RESPONSE)
					.withExplanation("No response");
		} catch (IOException e) {
			addException(e);
			Log.e(LogTag.APICALL.toString(),
					"caught IOException when posting new instance" + e, e);
			return new Response().withReturnCode(
					ReturnCode.UNRECOVERABLE_EXCEPTION).withExplanation(
					"Caught Exception: " + e);
		} finally {
			if (urlConnection != null)
				urlConnection.disconnect();
		}
	}

	abstract protected void onPostExecute(Response result);

}
