package com.samwong.hk.roomserviceclient.constants;

import com.samwong.hk.roomservice.api.commons.parameterEnums.Classifier;

public interface Defaults {
	public static final Classifier classifier = Classifier.ALL;
	public static final int MAX_RETRIES = 5;
	//public static final String SERVLET_URL = "http://project.samwong.hk:8080/RoomService/";
	public static final String SERVLET_URL = "http://192.168.0.4:8080/RoomService/api";
}
