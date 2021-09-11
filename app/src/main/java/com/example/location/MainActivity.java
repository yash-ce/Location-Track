package com.example.location;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;



public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {


    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private SettingsClient mSettingsClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private ActivityRecognitionClient mActivityRecognitionClient;
    private LocationCallback mLocationCallback;
    private Button mRequestUpdatesButton;
    private Button mRemoveUpdatesButton;
    private static TextView mLocationUpdatesResultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRequestUpdatesButton = (Button) findViewById(R.id.request_updates_button);
        mRemoveUpdatesButton = (Button) findViewById(R.id.remove_updates_button);
        mLocationUpdatesResultView = (TextView) findViewById(R.id.location_updates_result);

        mSettingsClient = LocationServices.getSettingsClient(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        createLocationRequest();
        buildLocationSettingsRequest();

        mActivityRecognitionClient = new ActivityRecognitionClient(this);

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));

        ActionBar actionBar;
        actionBar = getSupportActionBar();

        // Define ColorDrawable object and parse color
        // using parseColor method
        // with color hash code as its parameter
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#00bfff"));

        // Set BackgroundDrawable
        actionBar.setBackgroundDrawable(colorDrawable);
    }


    @Override
    protected void onStart() {
        super.onStart();
//        Toast.makeText(getApplicationContext(), "On Start calles", Toast.LENGTH_SHORT).show();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        if (!checkPermissions()) {
            requestPermissions();
        } else {
        }
        updateTextField(this);
        updateButtonsState(LocationRequestHelper.getInstance(this).getBoolanValue("RequestingLocationUpdates", false));
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Toast.makeText(getApplicationContext(), "On resume called", Toast.LENGTH_SHORT).show();
        updateTextField(this);
        updateButtonsState(LocationRequestHelper.getInstance(this).getBoolanValue("RequestingLocationUpdates", false));
    }

    @Override
    protected void onStop() {
//        Toast.makeText(getApplicationContext(), "On stop called", Toast.LENGTH_SHORT).show();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getApplicationContext(), "GPS turned on", Toast.LENGTH_SHORT).show();

//                        toast("GPS turned on");
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                            toast("Please Provide Location Permission.");
                            Toast.makeText(getApplicationContext(), "Please Provide Location Permission.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        changeStatusAfterGetLastLocation("1", "Manual");

                        break;
                    case Activity.RESULT_CANCELED:
                        if (!checkPermissions()) {
                            requestPermissions();
                        }
                        Toast.makeText(getApplicationContext(), "GPS is required to Start Tracking", Toast.LENGTH_SHORT).show();
//                        toast("GPS is required to Start Tracking");
                        break;
                }
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "User interaction was cancelled.");
                requestPermissions();
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                // Permission denied.
                Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("com.example.location",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }
//    BuildConfig.APPLICATION_ID

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        updateTextField(this);
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Utils.UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(Utils.FASTEST_UPDATE_INTERVAL);

        //mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(Utils.SMALLEST_DISPLACEMENT);
        //mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        mLocationRequest.setMaxWaitTime(Utils.MAX_WAIT_TIME);
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, LocationUpdatesBroadcastReceiver.class);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Log.i(TAG, "Requesting permission");
            startLocationPermissionRequest();
        }
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }



    public void requestLocationUpdates(View view) {
        if (!checkPermissions()) {
            Toast.makeText(getApplicationContext(),"Please Allow Location Permission!",Toast.LENGTH_SHORT).show();
//            toast("Please Allow Location Permission!");
            requestPermissions();
            return;
        }
        try {
        requestPermissions();
            mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                    .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                        @Override
                        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                            //toast("All location settings are satisfied.");
                            Toast.makeText(getApplicationContext(),"All location settings are satisfied.",Toast.LENGTH_SHORT).show();
                            changeStatusAfterGetLastLocation("1","Manual");
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            int statusCode = ((ApiException) e).getStatusCode();
                            switch (statusCode) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                            "location settings ");
                                    try {
                                        ResolvableApiException rae = (ResolvableApiException) e;
                                        rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                    } catch (IntentSender.SendIntentException sie) {
                                        Log.i(TAG, "PendingIntent unable to execute request.");
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    String errorMessage = "Location settings are inadequate, and cannot be " +
                                            "fixed here. Fix in Settings.";
                                    Log.e(TAG, errorMessage);
                                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                case LocationSettingsStatusCodes.DEVELOPER_ERROR:
                                    Log.e(TAG, "DEVELOPER_ERROR");
                            }
                        }
                    });

        } catch (SecurityException e) {
            LocationRequestHelper.getInstance(getApplicationContext()).setValue("RequestingLocationUpdates",false);
            e.printStackTrace();
        }
    }

    public void removeLocationUpdates(View view) {
        changeStatusAfterGetLastLocation("0","Manual");
    }

    public static void updateTextField(Context context) {
        mLocationUpdatesResultView.setText(LocationRequestHelper.getInstance(context).getStringValue("locationTextInApp",""));
    }

    @SuppressLint("MissingPermission")
    private void changeStatusAfterGetLastLocation(final String value, final String changeby) {
        if(value == "1"){
            Toast.makeText(getApplicationContext(),"Location Updates Started!",Toast.LENGTH_SHORT).show();
//            toast("Location Updates Started!");

            mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
            LocationRequestHelper.getInstance(getApplicationContext()).setValue("RequestingLocationUpdates",true);

            Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                    Utils.UPDATE_INTERVAL,
                    getPendingIntent());

            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void result) {

                }
            });
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG, "addOnFailureListener mActivityRecognitionClient "+e);
                }
            });

        }else if(value == "0"){

            LocationRequestHelper.getInstance(getApplicationContext()).setValue("RequestingLocationUpdates",false);
            mFusedLocationClient.removeLocationUpdates(getPendingIntent());
            Utils.removeNotification(getApplicationContext());

//            toast("Location Updates Stopped!");
            Toast.makeText(getApplicationContext(),"Location Updates Stopped!",Toast.LENGTH_SHORT).show();
            Task<Void> task = mActivityRecognitionClient.removeActivityUpdates(
                    getPendingIntent());
            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void result) {
                }
            });
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG, "removeActivityUpdates addOnFailureListener "+e);

                }
            });
        }
        updateButtonsState(LocationRequestHelper.getInstance(this).getBoolanValue("RequestingLocationUpdates", false));
    }

    public void updateButtonsState(boolean requestingLocationUpdates) {

        if (requestingLocationUpdates) {
            mRequestUpdatesButton.setVisibility(View.GONE);
            mRemoveUpdatesButton.setVisibility(View.VISIBLE);
        } else {
            mRequestUpdatesButton.setVisibility(View.VISIBLE);
            mRemoveUpdatesButton.setVisibility(View.GONE);
        }
    }

}
