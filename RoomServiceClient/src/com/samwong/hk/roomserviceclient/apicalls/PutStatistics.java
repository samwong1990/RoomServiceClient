package com.samwong.hk.roomserviceclient.apicalls;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.samwong.hk.roomservice.api.commons.dataFormat.AuthenticationDetails;
import com.samwong.hk.roomservice.api.commons.dataFormat.Response;
import com.samwong.hk.roomservice.api.commons.dataFormat.RoomStatistic;
import com.samwong.hk.roomservice.api.commons.parameterEnums.Operation;
import com.samwong.hk.roomservice.api.commons.parameterEnums.ParameterKey;
import com.samwong.hk.roomservice.api.commons.parameterEnums.ReturnCode;
import com.samwong.hk.roomserviceclient.constants.HttpVerb;
import com.samwong.hk.roomserviceclient.constants.LogTag;
import com.samwong.hk.roomserviceclient.helpers.APICaller;
import com.samwong.hk.roomserviceclient.helpers.AuthenticationDetailsPreperator;

/**
 * For my testing purpose only, assume this is not supported in the long run.
 * @author wongsam
 *
 */
public abstract class PutStatistics extends APICaller<List<RoomStatistic>, Void, Response> {

	public PutStatistics(Context context) {
		super(context);
	}

	abstract protected void onPostExecute(Response result);

	@Override
	protected Response doInBackground(List<RoomStatistic>... stats) {
		if(stats.length != 1){
			throw new IllegalArgumentException("Expects a single list of RoomStatistic"); 
		}

		// prepare parameters
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs
				.add(new BasicNameValuePair(ParameterKey.OPERATION.toString(),
						Operation.UPLOAD_STATISTICS.toString()));
		nameValuePairs.add(new BasicNameValuePair(ParameterKey.VALIDATION_STATISTICS.toString(),
				new Gson().toJson(stats[0], new TypeToken<List<RoomStatistic>>() {
				}.getType())));
		AuthenticationDetails authenticationDetails = new AuthenticationDetailsPreperator().getAuthenticationDetails(getContext());
		nameValuePairs.add(new BasicNameValuePair(
				ParameterKey.AUENTICATION_DETAILS.toString(),
				AuthenticationDetailsPreperator.getAuthenticationDetailsAsJson(authenticationDetails)));
		Log.d(LogTag.APICALL.toString(), nameValuePairs.toString());

		try {
			String result = getJsonResponseFromAPICall(HttpVerb.PUT, nameValuePairs);
			return new Gson().fromJson(result, new TypeToken<Response>() {
			}.getType());
		} catch (Exception e) {
			addException(e);
		}
		return new Response().withExplanation("Failed to complete API call").withReturnCode(ReturnCode.UNRECOVERABLE_EXCEPTION);
		
	}


}
