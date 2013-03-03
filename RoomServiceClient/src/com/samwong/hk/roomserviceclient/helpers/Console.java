package com.samwong.hk.roomserviceclient.helpers;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.samwong.hk.roomserviceclient.R;
import com.samwong.hk.roomserviceclient.constants.LogLevel;
import com.samwong.hk.roomserviceclient.constants.LogTag;

public class Console {
	private static List<Integer> logTextViewsIDs = Arrays.asList(R.id.logTextView, R.id.logTextViewInTrainingActivity);
		
	public static void println(Activity activity, LogLevel level, LogTag tag,
			String msg) {
		switch (level) {
		case DEBUG:
			Log.d(tag.toString(), msg);
			return;
		case INFO:
			Log.i(tag.toString(), msg);
			break;
		case ERROR:
			Log.e(tag.toString(), msg);
			break;
		default:
			break;
		}
		if (!level.equals(LogLevel.DEBUG)) {
			String formattedMessage = String.format("%s:%s\n",
					new SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
							.format(new Date()), msg);
			for (int id : logTextViewsIDs) {
				TextView textView = (TextView) activity.findViewById(id);
				if(textView != null){
					StringBuffer buffer = new StringBuffer(textView.getText());
					buffer.insert(0, formattedMessage);
					textView.setText(buffer);
					return;
				}
			}
		}
		return;
	}
}
