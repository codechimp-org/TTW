package org.codechimp.ttw;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class SendToWearTask extends AsyncTask<String, Void, Void> {

    private static final String TAG = "SendToWearTask";
    private static final String NOTIFICATION_PATH = "/notification";

    Context context;

    public SendToWearTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        String pattern = params[0];

        // Off, On
        //pattern = "0,500,110,500,110,450,110,200,110,170,40,450,110,200,110,170,40,500";  // Star Wars Imperial
        //pattern = "0,150,50,75,50,75,50,150,50,75,50,75,50,300";
        pattern = "0,75,50,75,50,75";

        // Connect to wear
        final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);
                        // Now you can use the data layer API
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();
        googleApiClient.blockingConnect();

        // Send the item to wear
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
        if (nodes != null) {
            for (Node node : nodes.getNodes()) {
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                        googleApiClient, node.getId(), NOTIFICATION_PATH, pattern.getBytes()).await();

                if (!result.getStatus().isSuccess()) {
                    Log.e(TAG, "ERROR: failed to send Message: " + result.getStatus());
                }
            }
        } else {
            Log.e(TAG, "ERROR: No nodes found");
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }
}