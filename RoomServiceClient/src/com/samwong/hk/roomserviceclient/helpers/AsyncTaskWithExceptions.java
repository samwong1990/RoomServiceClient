package com.samwong.hk.roomserviceclient.helpers;

import java.util.LinkedList;
import java.util.List;

import android.os.AsyncTask;

public abstract class AsyncTaskWithExceptions<Params, Progress, Result> extends
		AsyncTask<Params, Progress, Result> {
	protected List<Exception> exceptions = new LinkedList<Exception>();

	public List<Exception> getExceptions() {
		return exceptions;
	}

	public Exception getLastException() {
		return exceptions.size() > 0 ? exceptions.get(exceptions.size() - 1)
				: null;
	}

	protected void addException(Exception e) {
		exceptions.add(e);
	}

}
