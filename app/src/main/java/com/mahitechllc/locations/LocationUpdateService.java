package com.mahitechllc.locations;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.mahitechllc.R;
import com.mahitechllc.activity.MapActivity;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


public class LocationUpdateService extends Service {
	
	//region data
	private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 3000;
	
	public static Location currentLocation = null;
	
	private FusedLocationProviderClient mFusedLocationClient;
	
	//Location Callback
	private final LocationCallback locationCallback = new LocationCallback() {
		@Override
		public void onLocationResult(LocationResult locationResult) {
			super.onLocationResult(locationResult);
			currentLocation = locationResult.getLastLocation();
			Log.e("Locations " , currentLocation.getLatitude() + "," + currentLocation.getLongitude());
		}
	};
	

	
	private LocationRequest locationRequest;
	
	//endregion
	
	//onCreate
	@Override
	public void onCreate() {
		super.onCreate();
		initData();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		prepareForegroundNotification();
		startLocationUpdates();
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mFusedLocationClient.removeLocationUpdates(locationCallback);
	}
	
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@SuppressLint ("MissingPermission")
	private void startLocationUpdates() {
		mFusedLocationClient.requestLocationUpdates(this.locationRequest, this.locationCallback,
				Looper.myLooper());
	}
	
	private void prepareForegroundNotification() {
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel serviceChannel =
					new NotificationChannel("Location-Notification", "Location Service Channel",
							NotificationManager.IMPORTANCE_DEFAULT);
			NotificationManager manager = getSystemService(NotificationManager.class);
			manager.createNotificationChannel(serviceChannel);
		}
		Intent notificationIntent = new Intent(this, MapActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 1234, notificationIntent, 0);
		
		Notification notification =
				new NotificationCompat.Builder(this, "Location-Notification").setContentTitle(
						getString(R.string.app_name)).setContentTitle(
						getString(R.string.location_notification_desc)).setSmallIcon(
						R.mipmap.ic_launcher).setContentIntent(pendingIntent).build();
		startForeground(111, notification);
	}
	
	private void initData() {
		locationRequest = LocationRequest.create();
		locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
		locationRequest.setSmallestDisplacement(50);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		
		mFusedLocationClient =
				LocationServices.getFusedLocationProviderClient(getApplicationContext());
	}
	
}
