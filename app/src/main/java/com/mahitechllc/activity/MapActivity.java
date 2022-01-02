package com.mahitechllc.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mahitechllc.R;
import com.mahitechllc.Utility.Utils;
import com.mahitechllc.firebase.FirebaseDB;
import com.mahitechllc.locations.LocationUpdateService;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
                                                              GoogleApiClient.ConnectionCallbacks
		, GoogleApiClient.OnConnectionFailedListener {
	
	private static final int REQUEST_CODE_PERMISSION = 2;
	
	private final float cameraZoom = 12f;
	
	String SerialId = "";
	
	GoogleApiClient googleApiClient;
	
	double lat, lon;
	
	LatLng loc;
	
	private GoogleMap mMap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		SupportMapFragment mapFragment =
				(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}
	
	@SuppressLint ("HardwareIds")
	public String getDeviceIMEI() {
		String deviceUniqueIdentifier = null;
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			deviceUniqueIdentifier =
					Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
		} else {
			final TelephonyManager mTelephony =
					(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			if (mTelephony.getDeviceId() != null) {
				deviceUniqueIdentifier = mTelephony.getDeviceId();
			} else {
				deviceUniqueIdentifier =
						Settings.Secure.getString(getContentResolver(),
								Settings.Secure.ANDROID_ID);
			}
		}
		return deviceUniqueIdentifier;
	}
	
	public String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}
	
	private void startLocationService() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForegroundService(new Intent(this, LocationUpdateService.class));
		} else {
			startService(new Intent(this, LocationUpdateService.class));
		}
	}
	
	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		final LocationManager manager =
				(LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean gps_enabled = false;
		boolean network_enabled = false;
		try {
			gps_enabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
		}
		try {
			network_enabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception ex) {
		}
		if (!gps_enabled && !network_enabled) {
			Utils.buildAlertMessageNoGps(this);
		} else {
			checkPermissions();
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions,
	                                       @NonNull @NotNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == Utils.locationPermissionRequestCode) {
			if (Utils.checkLocationPermissions(this, false)) {
				startLocationService();
			}
		} else {
			Toast.makeText(MapActivity.this, R.string.location_permission_required,
					Toast.LENGTH_LONG).show();
		}
	}
	
	private void checkPermissions() {
		if (!(ActivityCompat.checkSelfPermission(this,
				Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) || !(ActivityCompat.checkSelfPermission(
				this,
				Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) || !(ActivityCompat.checkSelfPermission(
				this,
				Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.READ_PHONE_STATE,
							Manifest.permission.ACCESS_FINE_LOCATION,
							Manifest.permission.ACCESS_COARSE_LOCATION},
					REQUEST_CODE_PERMISSION);
		} else {
			buildGoogleApiClient();
			
			SerialId = getDeviceIMEI();
			FirebaseDB.addUser(SerialId, getDeviceName());
			FusedLocationProviderClient fusedLocationClient =
					LocationServices.getFusedLocationProviderClient(this);
			if (ContextCompat.checkSelfPermission(this,
					Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
				fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
					if (location != null) {
						LocationUpdateService.currentLocation = location;
						FirebaseDB.addLocation(SerialId);
					}
				});
			}
			if (Utils.checkLocationPermissions(this, true)) {
				startLocationService();
			}
		}
	}
	
	private void buildGoogleApiClient() {
		googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(
				this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
		googleApiClient.connect();
	}
	
	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {
		mMap = googleMap;
			if (ActivityCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
				this,
				Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		mMap.setMyLocationEnabled(true);
	}
	
	@Override
	public void onPointerCaptureChanged(boolean hasCapture) {
	}
	
	@Override
	public void onConnected(@Nullable Bundle bundle) {
		if (ActivityCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
			if (ActivityCompat.checkSelfPermission(this,
					Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				// TODO: Consider calling
				//    ActivityCompat#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for ActivityCompat#requestPermissions for more details.
				return;
			}
		LocationUpdateService.currentLocation =
				LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
		if (LocationUpdateService.currentLocation != null) {
			lat = LocationUpdateService.currentLocation.getLatitude();
			lon = LocationUpdateService.currentLocation.getLongitude();
			mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
			// Add a marker in Sydney and move the camera
			LatLng currentLocation = new LatLng(lat, lon);
			mMap.addMarker(new MarkerOptions().position(currentLocation).title("Your Location"));
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, cameraZoom));
			
		}
	}
	
	@Override
	public void onConnectionSuspended(int i) {
	}
	
	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
	}
	
}