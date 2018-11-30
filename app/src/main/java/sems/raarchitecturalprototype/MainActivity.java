package sems.raarchitecturalprototype;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import sems.raarchitecturalprototype.interfaces.ILocationCalculator;
import sems.raarchitecturalprototype.interfaces.IThreshold;

public class MainActivity extends AppCompatActivity {

    public static final int INIT_PERMISSION_REQUEST_CODE = 6124;
    public static final String[] PERMISSION_REQUESTS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private IThreshold threshold; // holds thresholds
    private ILocationCalculator locationCalculator; // calculates new location based on current location, speed, and direction
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastLocation;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLocation();
    }

    private void initLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) { // checks if permissions are granted (https://developer.android.com/training/permissions/requesting)
            Log.d(getString(R.string.app_name), "Requesting permissions");
            ActivityCompat.requestPermissions(this, PERMISSION_REQUESTS, INIT_PERMISSION_REQUEST_CODE);
            return;
        }

        Log.d(getString(R.string.app_name), "Starting location client");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        startLocationLoop();
    }

    @SuppressLint("MissingPermission")
    private void startLocationLoop() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        if (location.getAccuracy() > threshold.getAccuracyThreshold()
                                && location.getExtras().getInt("satellites") < threshold.getSatelliteThreshold()) { // bad signal
                            currentLocation = locationCalculator.calculate(lastLocation.getLatitude(), lastLocation.getLongitude(),
                                    lastLocation.getSpeed(), lastLocation.getBearing());
                        } else {
                            currentLocation = location;
                            lastLocation = location;
                        }
                    }
                    updateGuiWithLocation(currentLocation);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void updateGuiWithLocation(Location location) {
        Log.d(getString(R.string.app_name), String.valueOf(location.getLatitude()));
        Log.d(getString(R.string.app_name), String.valueOf(location.getLongitude()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == INIT_PERMISSION_REQUEST_CODE) { // requestCode used to link permission requests together
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // if the request is cancelled, the result arrays are empty
                Log.d(getString(R.string.app_name), "Permission was granted");
                initLocation();
            } else {
                Log.d(getString(R.string.app_name), "Permission was denied");
            }
        }
    }

}
