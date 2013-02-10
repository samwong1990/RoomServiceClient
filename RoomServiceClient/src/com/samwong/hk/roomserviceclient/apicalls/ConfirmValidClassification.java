package com.samwong.hk.roomserviceclient.apicalls;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import net.sf.javaml.core.Instance;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.samwong.hk.roomservice.api.commons.dataFormat.Report;
import com.samwong.hk.roomservice.api.commons.dataFormat.Response;
import com.samwong.hk.roomservice.api.commons.parameterEnums.Operation;
import com.samwong.hk.roomservice.api.commons.parameterEnums.ParameterKey;
import com.samwong.hk.roomservice.api.commons.parameterEnums.ReturnCode;
import com.samwong.hk.roomserviceclient.constants.LogTag;
import com.samwong.hk.roomserviceclient.constants.URLs;
import com.samwong.hk.roomserviceclient.helpers.AsyncTaskWithExceptionsAndContext;
import com.samwong.hk.roomserviceclient.helpers.AuthenticationDetailsPreperator;

/**
 * Classifier returned the right room, so the data used in query can be saved as
 * a new datapoint
 * 
 * @author wongsam
 * 
 */
public abstract class ConfirmValidClassification extends
		AsyncTaskWithExceptionsAndContext<Report, Void, Response> {

	public ConfirmValidClassification(Context context) {
		super(context);
	}

	protected Response doInBackground(Report... param) {

		if (param.length != 1) {
			throw new IllegalArgumentException("param.length != 1");
		}

		String instanceAsJson = new Gson().toJson(param[0].getInstance(),
				new TypeToken<Instance>() {
				}.getType());
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs
				.add(new BasicNameValuePair(ParameterKey.OPERATION.toString(),
						Operation.CONFIRM_VALID_CLASSIFICATION.toString()));
		nameValuePairs.add(new BasicNameValuePair(ParameterKey.ROOM.toString(),
				param[0].getRoom()));
		nameValuePairs.add(new BasicNameValuePair(ParameterKey.INSTANCE
				.toString(), instanceAsJson));
		nameValuePairs.add(new BasicNameValuePair(
				ParameterKey.AUENTICATION_DETAILS.toString(),
				AuthenticationDetailsPreperator.getAuthenticationDetailsAsJson(getContext())));
		Log.d(LogTag.APICALL.toString(), nameValuePairs.toString());

		UrlEncodedFormEntity putData;
		HttpURLConnection urlConnection = null;
		try {
			urlConnection = (HttpURLConnection) new URL(URLs.SERVLET_URL)
					.openConnection();
			putData = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
			urlConnection.setDoOutput(true);
			urlConnection.setRequestMethod("PUT");
			urlConnection.setFixedLengthStreamingMode((int) putData
					.getContentLength());
			putData.writeTo(urlConnection.getOutputStream());
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
