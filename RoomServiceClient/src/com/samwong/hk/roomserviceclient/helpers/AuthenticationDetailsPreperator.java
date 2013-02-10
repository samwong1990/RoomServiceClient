package com.samwong.hk.roomserviceclient.helpers;

import android.content.Context;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.samwong.hk.roomservice.api.commons.dataFormat.AuthenticationDetails;

public class AuthenticationDetailsPreperator {

	public static AuthenticationDetails getAuthenticationDetails(Context context) {
		// Start the localization thread, then deal with other things
		LocateWithCellTower locateWithCellTower = new LocateWithCellTower();
		locateWithCellTower.execute(context);

		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		String wifiMacAddress = wm.getConnectionInfo().getMacAddress();
		Location location = locateWithCellTower.getLocation();
		return new AuthenticationDetails()
				.withDeviceWifiMacAddress(wifiMacAddress)
				.withDeviceModel(Build.MODEL)
				.withDeviceInstallID(Installation.id(context))
				.withDeviceBrand(Build.BRAND)
				.withDeviceManaufacturer(Build.MANUFACTURER)
				.withDeviceProduct(Build.PRODUCT)
				.withDeviceLatitude(location.getLatitude())
				.withDeviceLongitude(location.getLongitude());
	}

	public static String getAuthenticationDetailsAsJson(Context context) {
		return new Gson().toJson(getAuthenticationDetails(context),
				new TypeToken<AuthenticationDetails>() {
				}.getType());
	}
}
