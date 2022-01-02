package com.mahitechllc.Utility;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;

import com.mahitechllc.R;
import com.mahitechllc.activity.MapActivity;

import androidx.core.app.ActivityCompat;


public class Utils {
	public static final int locationPermissionRequestCode = 1310;
	public static boolean checkLocationPermissions(MapActivity mapActivity,
	                                               boolean requestPermission) {
		if (ActivityCompat.checkSelfPermission(mapActivity,
				Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
				mapActivity,
				Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			return true;
		}
		if (requestPermission) {
			ActivityCompat.requestPermissions(mapActivity,
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
							Manifest.permission.ACCESS_COARSE_LOCATION},
					locationPermissionRequestCode);
		}
		return false;
	}
	public static void buildAlertMessageNoGps(Context context) {
		
		new AlertDialog.Builder(context).setTitle(R.string.Location_permission)  // GPS not found
				.setMessage(R.string.Location_permission_txt) // Want to enable?
				.setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int i) {
						context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				}).setNegativeButton(R.string.cancel_text, null).show();
		
	}
}
