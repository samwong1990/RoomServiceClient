package com.samwong.hk.roomserviceclient.helpers;

import android.content.Context;

/**
 * Pass in context to give more power to async tasks.
 * @author wongsam
 *
 * @param <Params>
 * @param <Progress>
 * @param <Result>
 */
public abstract class AsyncTaskWithExceptionsAndContext<Params, Progress, Result> extends AsyncTaskWithExceptions<Params, Progress, Result> {
	private Context context;
	
	public AsyncTaskWithExceptionsAndContext(Context context) {
		this.context = context;
	}
	
	public Context getContext(){
		return context;
	}
}
