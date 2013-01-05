package com.samwong.hk.roomserviceclient.apicalls;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.samwong.hk.roomservice.api.commons.dataFormat.Report;
import com.samwong.hk.roomservice.api.commons.dataFormat.Response;
import com.samwong.hk.roomservice.api.commons.dataFormat.WifiInformation;
import com.samwong.hk.roomservice.api.commons.parameterEnums.Operation;
import com.samwong.hk.roomservice.api.commons.parameterEnums.ParameterKey;
import com.samwong.hk.roomservice.api.commons.parameterEnums.ReturnCode;
import com.samwong.hk.roomserviceclient.constants.LogTag;
import com.samwong.hk.roomserviceclient.constants.URLs;
import com.samwong.hk.roomserviceclient.helpers.AsyncTaskWithExceptions;

public abstract class AddNewInstance<T> extends AsyncTaskWithExceptions<T, Void, Response>{
	
	protected Response doInBackground(T... tArray) {
		if(tArray.length != 1){
			throw new IllegalArgumentException("tArray.length != 1");
		}
		
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair(Operation.SAVEINSTANCE.toString(), ""));
		
		T t = tArray[0];
		if(t instanceof Report){
			String json = new Gson().toJson((Report) t, new TypeToken<Report>(){}.getType());
			nameValuePairs.add(new BasicNameValuePair(ParameterKey.REPORT.toString(), json));
		}else if(t instanceof WifiInformation){
			String json = new Gson().toJson((WifiInformation) t, new TypeToken<WifiInformation>(){}.getType());
			nameValuePairs.add(new BasicNameValuePair(ParameterKey.WIFIINFORMATION.toString(), json));
		}else{
			throw new IllegalArgumentException();
		}
		Log.d(LogTag.APICALL.toString(), nameValuePairs.toString());
		UrlEncodedFormEntity putData;

		HttpURLConnection urlConnection = null;
		try {
			urlConnection = (HttpURLConnection) new URL(URLs.SERVLET_URL).openConnection();
			putData = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
			urlConnection.setDoOutput(true);
			urlConnection.setRequestMethod("POST");
			urlConnection.setFixedLengthStreamingMode((int) putData.getContentLength());
			putData.writeTo(urlConnection.getOutputStream());
			urlConnection.connect();
			Scanner scanner = new Scanner(urlConnection.getInputStream(), "UTF-8").useDelimiter("\\A");
			if (scanner.hasNext()){
				String result = scanner.next();
				Log.i(LogTag.APICALL.toString(), result);
				return new Gson().fromJson(result, new TypeToken<Response>(){}.getType());
			}
			Log.w(LogTag.APICALL.toString(), "no response after posting new instance.");
			return new Response().withReturnCode(ReturnCode.NO_RESPONSE).withExplanation("No response");
		} catch(IOException e){
			addException(e);
			Log.e(LogTag.APICALL.toString(), "caught IOException when posting new instance" + e, e);
			return new Response().withReturnCode(ReturnCode.UNRECOVERABLE_EXCEPTION).withExplanation("Caught Exception: " + e);
		} finally {
			if(urlConnection != null) urlConnection.disconnect();
		}
	}

	abstract protected void onPostExecute(Response result);

}
