package org.codechimp.ttw;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WearListenerService extends WearableListenerService {

    private static final String TAG = "WearListenerService";
    private GoogleApiClient googleApiClient;

    private static final String NOTIFICATION_PATH = "/notification";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        byte[] data = messageEvent.getData();

        if (path.equals(NOTIFICATION_PATH)) {
            Log.d(TAG, "Notification received - " + new String(data));

            vibratePattern(new String(data));
        }
    }

    private void vibratePattern(String pattern) {
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //Break string into an array of longs
        String[] split = pattern.split(",");
        long[] patternLongs = new long[split.length];
        for (int i=0; i < split.length; i++) {
            patternLongs[i] = Long.parseLong(split[i]);
        }

        v.vibrate(patternLongs, -1);
    }
}