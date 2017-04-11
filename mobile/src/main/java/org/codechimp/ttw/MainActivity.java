package org.codechimp.ttw;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.wearable.intent.RemoteIntent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        CapabilityApi.CapabilityListener {

    private static final String TAG = "TTW";

    private GoogleApiClient googleApiClient;

    // Name of capability listed in Wear app's wear.xml.
    // IMPORTANT NOTE: This should be named differently than your Phone app's capability.
    private static final String CAPABILITY_WEAR_APP = "verify_remote_ttw_wear_app";

    private Set<Node> wearNodesWithApp;
    private List<Node> allConnectedNodes;

    // Result from sending RemoteIntent to wear device(s) to open app in play/app store.
    private final ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.d(TAG, "onReceiveResult: " + resultCode);

            if (resultCode == RemoteIntent.RESULT_OK) {
                // Do nothing
            } else if (resultCode == RemoteIntent.RESULT_FAILED) {
                Toast toast = Toast.makeText(
                        getApplicationContext(),
                        R.string.no_watch_app_remote_error,
                        Toast.LENGTH_LONG);
                toast.show();

            } else {
                throw new IllegalStateException("Unexpected result " + resultCode);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        // Get references to UI widgets
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        if (toolbar != null) {
//            try {
//                setSupportActionBar(toolbar);
//            } catch (Throwable t) {
//                // WTF SAMSUNG 4.2.2!
//            }
//            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        }

        Button hideLauncherButton = (Button) findViewById(R.id.hideLauncherButton);
        hideLauncherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PackageManager p = getPackageManager();
                p.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                finish();
            }
        });

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if ((googleApiClient != null) && googleApiClient.isConnected()) {

            Wearable.CapabilityApi.removeCapabilityListener(
                    googleApiClient,
                    this,
                    CAPABILITY_WEAR_APP);

            googleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected()");

        // Set up listeners for capability changes (install/uninstall of remote app).
        Wearable.CapabilityApi.addCapabilityListener(
                googleApiClient,
                this,
                CAPABILITY_WEAR_APP);

        // Initial request for devices with our capability, aka, our Wear app installed.
        findWearDevicesWithApp();

        // Initial request for all Wear devices connected (with or without our capability).
        // Additional Note: Because there isn't a listener for ALL Nodes added/removed from network
        // that isn't deprecated, we simply update the full list when the Google API Client is
        // connected and when capability changes come through in the onCapabilityChanged() method.
        findAllWearDevices();
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): connection to location client suspended: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed(): " + connectionResult);
    }

    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        Log.d(TAG, "onCapabilityChanged(): " + capabilityInfo);

        wearNodesWithApp = capabilityInfo.getNodes();

        // Because we have an updated list of devices with/without our app, we need to also update
        // our list of active Wear devices.
        findAllWearDevices();

        verifyNodeAndUpdateUI();
    }

    private void findWearDevicesWithApp() {
        Log.d(TAG, "findWearDevicesWithApp()");

        // You can filter this by FILTER_REACHABLE if you only want to open Nodes (Wear Devices)
        // directly connect to your phone.
        PendingResult<CapabilityApi.GetCapabilityResult> pendingResult =
                Wearable.CapabilityApi.getCapability(
                        googleApiClient,
                        CAPABILITY_WEAR_APP,
                        CapabilityApi.FILTER_ALL);

        pendingResult.setResultCallback(new ResultCallback<CapabilityApi.GetCapabilityResult>() {
            @Override
            public void onResult(@NonNull CapabilityApi.GetCapabilityResult getCapabilityResult) {
                Log.d(TAG, "onResult(): " + getCapabilityResult);

                if (getCapabilityResult.getStatus().isSuccess()) {
                    CapabilityInfo capabilityInfo = getCapabilityResult.getCapability();
                    wearNodesWithApp = capabilityInfo.getNodes();
                    verifyNodeAndUpdateUI();

                } else {
                    Log.d(TAG, "Failed CapabilityApi: " + getCapabilityResult.getStatus());
                }
            }
        });
    }

    private void findAllWearDevices() {
        Log.d(TAG, "findAllWearDevices()");

        PendingResult<NodeApi.GetConnectedNodesResult> pendingResult =
                Wearable.NodeApi.getConnectedNodes(googleApiClient);

        pendingResult.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetConnectedNodesResult getConnectedNodesResult) {

                if (getConnectedNodesResult.getStatus().isSuccess()) {
                    allConnectedNodes = getConnectedNodesResult.getNodes();
                    verifyNodeAndUpdateUI();

                } else {
                    Log.d(TAG, "Failed CapabilityApi: " + getConnectedNodesResult.getStatus());
                }
            }
        });
    }

    private void verifyNodeAndUpdateUI() {
        Log.d(TAG, "verifyNodeAndUpdateUI()");

        if ((wearNodesWithApp == null) || (allConnectedNodes == null)) {
            Log.d(TAG, "Waiting on Results for both connected nodes and nodes with app");

        } else if (allConnectedNodes.isEmpty()) {
            Log.d(TAG, "No wear devices");

        } else if (wearNodesWithApp.isEmpty()) {
            Log.d(TAG, "No devices with app");
            showRemoteInstallDialog();

        } else if (wearNodesWithApp.size() < allConnectedNodes.size()) {
            Log.d(TAG, String.format("Installed on some devices (%s)", wearNodesWithApp));
        } else {
            Log.d(TAG, String.format("Installed on all devices (%s)", wearNodesWithApp));
        }
    }

    protected void showRemoteInstallDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.no_watch_app_title)
                .setMessage(R.string.no_watch_app_description)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.no_watch_app_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openPlayStoreOnWearDevicesWithoutApp();
                    }
                })
                .show();
    }

    private void openPlayStoreOnWearDevicesWithoutApp() {
        Log.d(TAG, "openPlayStoreOnWearDevicesWithoutApp()");

        // Create a List of Nodes (Wear devices) without your app.
        ArrayList<Node> nodesWithoutApp = new ArrayList<>();

        for (Node node : allConnectedNodes) {
            if (!wearNodesWithApp.contains(node)) {
                nodesWithoutApp.add(node);
            }
        }

        if (!nodesWithoutApp.isEmpty()) {
            Log.d(TAG, "Number of nodes without app: " + nodesWithoutApp.size());

            Intent intent =
                    new Intent(Intent.ACTION_VIEW)
                            .addCategory(Intent.CATEGORY_BROWSABLE)
                            .setData(Uri.parse("market://details?id=" + getPackageName()));

            for (Node node : nodesWithoutApp) {
                RemoteIntent.startRemoteActivity(
                        getApplicationContext(),
                        intent,
                        resultReceiver,
                        node.getId());
            }
        }
    }
}