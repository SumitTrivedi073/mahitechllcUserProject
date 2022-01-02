package com.mahitechllc.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mahitechllc.locations.LocationUpdateService;

import java.util.HashMap;


public class FirebaseDB {
	private static final String currentLocation = "current_locations";
	
	private static final String users_data = "users_data";
	
	public static void addUser(String serialId, String deviceName) {
		
		DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("name",deviceName);
		firebaseDatabase.child(users_data).child(serialId).setValue(hashMap);
	}
	
	public static void addLocation(String userId) {
		DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
		firebaseDatabase.child(currentLocation).child(userId).setValue(
				new FirebaseLocationModel(LocationUpdateService.currentLocation.getLatitude(),
						LocationUpdateService.currentLocation.getLongitude()));
	}
	
}
