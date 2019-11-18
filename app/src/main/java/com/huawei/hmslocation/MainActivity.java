package com.huawei.hmslocation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.common.ResolvableApiException;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationAvailability;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.LocationSettingsRequest;
import com.huawei.hms.location.LocationSettingsResponse;
import com.huawei.hms.location.LocationSettingsStatusCodes;
import com.huawei.hms.location.SettingsClient;

import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MyLog";
    private SettingsClient settingsClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest mlocationRequest;
    private LocationCallback locationCallback;


    private TextView logView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.location_requestLocationUpdatesWithCallback).setOnClickListener(this);
        findViewById(R.id.location_removeLocationUpdatesWithCallback).setOnClickListener(this);

        logView = findViewById(R.id.ShowLocationLogLat);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);

        mlocationRequest = new LocationRequest();

        mlocationRequest.setInterval(10000);

        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                    if(locationResult != null){
                        List<Location> locations = locationResult.getLocations();
                        String text = "";
                        for(Location location : locations){
                            text = text +"Hi new Location : => \nLon:"+location.getLongitude() + "\nLat:" + location.getLatitude() + "\nAccuracy:" + location.getAccuracy();
                        }
                        logView.setText(logView.getText() + "\n\n\n" + text);
                    }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if(locationAvailability != null){
                    boolean flag = locationAvailability.isLocationAvailable();
                    logView.setText(logView.getText() + "\n\n\n" + "onLocationAvailability : "+flag);
                }
            }
        } ;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] strings =
                    {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, strings, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: apply ACCESS_FINE_LOCATION successful");
            } else {
                Log.i(TAG, "onRequestPermissionsResult: apply ACCESS_FINE_LOCATION failed");
            }

            if (grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: apply ACCESS_COARSE_LOCATION successful");
            } else {
                Log.i(TAG, "onRequestPermissionsResult: apply ACCESS_COARSE_LOCATION failed");
            }
        }
    }

    private void requestLocationUpdatesWithCallback() {
        try{
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mlocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        fusedLocationProviderClient.requestLocationUpdates(mlocationRequest, locationCallback, Looper.getMainLooper())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        System.out.println("Request locatoin updates with settings onSuccess");
                                        logView.setText(logView.getText() + "\n\n\n" + "Request locatoin updates with settings onSuccess");

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                System.out.println("Request location update with settings On failuer " + e.getMessage());
                                logView.setText(logView.getText() + "\n\n\n" + "Request location update with settings On failuer " + e.getMessage());


                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case LocationSettingsStatusCodes
                            .RESOLUTION_REQUIRED:
                        ResolvableApiException rae = (ResolvableApiException) e;
                        try {
                            rae.startResolutionForResult(MainActivity.this, 0);
                        } catch (IntentSender.SendIntentException ex) {
                            ex.printStackTrace();
                            System.out.println("PendingIntent unable to execute request.");
                            logView.setText(logView.getText() + "\n\n\n"+"PendingIntent unable to execute request.");
                        }
                        break;
                }
            }
        });

    }catch (Exception e){
            System.out.println( "requestLocationUpdatesWithCallback exception:" + e.getMessage());
            logView.setText(logView.getText() + "\n\n\n"+"requestLocationUpdatesWithCallback exception:" + e.getMessage());
        }
    }

    private void removeLocationUpdatesWithCallback(){
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            System.out.println("removeLocationUpdatesWithCallback onSuccess");
                            logView.setText(logView.getText() + "\n\n\n"+"removeLocationUpdatesWithCallback onSuccess");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    System.out.println("removeLocationUpdatesWithCallback Exception : " + e.getMessage());
                    logView.setText(logView.getText() + "\n\n\n"+"removeLocationUpdatesWithCallback Exception : " + e.getMessage());
                }
            });
        }catch (Exception e){
            System.out.println( "RemoveLocationUpdatesWithCallback exception:" + e.getMessage());
            logView.setText(logView.getText() + "\n\n\n"+"RemoveLocationUpdatesWithCallback exception:" + e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.location_requestLocationUpdatesWithCallback:
                    requestLocationUpdatesWithCallback();
                    break;
                case R.id.location_removeLocationUpdatesWithCallback:
                    removeLocationUpdatesWithCallback();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "RequestLocationUpdatesWithCallbackActivity Exception:" + e);
        }
    }

}
