package com.example.location;

import android.content.BroadcastReceiver;
//package com.freakyjolly.demobackgroundlocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.text.DateFormat;
import java.util.Date;

//com.freakyjolly.demobackgroundlocation

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "LUBroadcastReceiver";
    public static final String ACTION_PROCESS_UPDATES =
            "com.example.location.action" + ".PROCESS_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {

            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                Utils.setLocationUpdatesResult(context, DateFormat.getDateTimeInstance().format(new Date()));
                Utils.getLocationUpdates(context,intent,"PROCESS_UPDATES");

            }
        }
    }
}



//public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
//}
