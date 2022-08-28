package org.apache.cordova.niceid;

import android.app.DownloadManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Collection;

/**
 * Created by SungJoon_Kim on 2022/7/28.
 */
public class NiceId extends CordovaPlugin {

    private final String TAG = "NiceId";
    private final int REQUEST_CODE = 6018;
    private final String ACTION_REQUEST_NICEID = "requestNiceId";
    private final String ACTION_DOWNLOADFILE = "downloadFile";
    private final String ACTION_START_SCAN_BEACON = "startScanBeacon";
    private final String KEY_TYPE = "type";
    private CallbackContext ctxCallback;
    private BeaconManager beaconManager;

//    private ActivityResultLauncher<Intent> startActivityResult;

//    public NiceId() {
//        startActivityResult = cordova.getActivity().registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    if (result.getResultCode() == Activity.RESULT_OK) {
//                        Intent intent = result.getData();
//                        String msg = intent.getStringExtra(MobileCertification.NAME);
//                        Log.d(TAG, "onActivityResult " + msg);
//                        ctxCallback.sendPluginResult(
//                                new PluginResult(PluginResult.Status.OK, msg));
//                    }
//                }
//            });
//    }

    @Override
    public boolean execute(String action, final JSONArray args,
                           final CallbackContext callbackContext) throws JSONException {

        if (ACTION_REQUEST_NICEID.equals(action)) {

            PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
            r.setKeepCallback(true);
            callbackContext.sendPluginResult(r);
            this.ctxCallback = callbackContext;
            cordova.setActivityResultCallback(this);

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    Intent intent = new Intent(cordova.getActivity(), CertificationWebActivity.class);
                    try {
                        intent.putExtra(KEY_TYPE, args.get(0).toString());
                        cordova.getActivity().startActivityForResult(intent, REQUEST_CODE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            return true;

        } else if (ACTION_DOWNLOADFILE.equals(action)) {
            new DownloadFile().execute((String)args.get(0));
        } else if (ACTION_START_SCAN_BEACON.equals(action)) {
            beaconManager = BeaconManager.getInstanceForApplication(cordova.getContext());

            // iBeacon 추가
            beaconManager.getBeaconParsers().add(new BeaconParser().
                    setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
            beaconManager.addRangeNotifier((Collection<Beacon> beacons, Region region) -> {
                if (beacons.size() > 0) {
                    Log.i(TAG, "The first beacon I see has minor id "
                            + beacons.iterator().next().getId2() + " " + beacons.iterator().next().getId3());
                    // When the id3 is 원무비콘 minor 코드 - 원무도착 API call
                }
            });
        /*
        1001	엘리베이터 앞
        1002	로비 왼쪽
        1003	로비 오른쪽
        1004	원무 대기존
        3010    자곡동 테스트 minor id
        */
            try {
                beaconManager.startRangingBeaconsInRegion(new Region("iBeacon", null,
                        Identifier.parse("1001"), null));
            } catch (RemoteException e) {
                Log.e(TAG, "error : " + e.getMessage());
            }

        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE && intent != null) {
            Bundle extras = intent.getExtras();
            String msg = extras.getString(MobileCertification.NAME);
            Log.d(TAG, "onActivityResult " + msg);
            this.ctxCallback.sendPluginResult(new PluginResult(PluginResult.Status.OK,
                    msg));
        }
    }

    private class DownloadFile extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            // e.g.) http://maven.apache.org/maven-1.x/maven.pdf
            String fileUrl = strings[0];
            openPdf(fileUrl);
            return null;
        }
    }

    private void openPdf(String uri) {
        Log.d(TAG, "uri " + uri);
        Intent intent1 = new Intent(Intent.ACTION_VIEW);
        intent1.setDataAndType(Uri.parse(uri), "application/pdf");
        cordova.getContext().startActivity(intent1);
    }

}
