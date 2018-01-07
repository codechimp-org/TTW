package org.codechimp.ttw;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SendToWearTask extends AsyncTask<String, Void, Void> {

    private static final String TAG = "SendToWearTask";
    private static final String NOTIFICATION_PATH = "/notification";

    private Context context;

    public SendToWearTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        String pattern = params[0];

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

        List<Node> nodes = null;
        try {
            nodes = Tasks.await(Wearable.getNodeClient(context).getConnectedNodes());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        if (nodes != null) {
            for (Node node : nodes) {
                Wearable.getMessageClient(context).sendMessage(
                        node.getId(), NOTIFICATION_PATH, pattern.getBytes());
            }
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