package org.apache.cordova.niceid;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Created by SungJoon_Kim on 2022.7.28.
 */
public class NiceId extends CordovaPlugin {

    private final String TAG = "NiceId";
    private final int REQUEST_CODE_NICEID = 6017;
    private final int REQUEST_CODE_QRSCAN = 6018;
    private final int BUILD_VERSION_CODES_M = 23;
    private final String ACTION_REQUEST_NICEID = "requestNiceId";
    private final String ACTION_DOWNLOADFILE = "downloadFile";
    private final String ACTION_START_SCAN_BEACON = "startScanBeacon";
    private final String ACTION_REQUEST_QR_CODE = "requestQRScanner";
    private final String ACTION_CHECK_PERMISSIONS = "checkPermissions";
    private final String KEY_TYPE = "type";
    private final int PERMISSION_REQUEST_COARSE_LOCATION = 100;
    private final int PERMISSION_REQUEST_CAMERA = 101;
    private CallbackContext ctxCallback;
    private BeaconManager beaconManager;


    @Override
    public boolean execute(String action, final JSONArray args,
                           final CallbackContext callbackContext) throws JSONException {

        if (ACTION_REQUEST_NICEID.equals(action)) {

            PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
            r.setKeepCallback(true);
            callbackContext.sendPluginResult(r);
            this.ctxCallback = callbackContext;
            cordova.setActivityResultCallback(this);

            cordova.getThreadPool().execute(() -> {
                Intent intent = new Intent(cordova.getActivity(), CertificationWebActivity.class);
                try {
                    intent.putExtra(KEY_TYPE, args.get(0).toString());
                    cordova.getActivity().startActivityForResult(intent, REQUEST_CODE_NICEID);
                } catch (JSONException e) {
                    e.printStackTrace();
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
                    Log.i(TAG, "The beacon I've found has minor id '"
                            + beacons.iterator().next().getId2() + "' " + beacons.iterator().next().getId3());
                    // When the id3 is 원무비콘 minor 코드 - 원무도착 API call
                }
            });
            try {
                beaconManager.startRangingBeaconsInRegion(new Region("iBeacon", null,
                        Identifier.parse("1001"), null));
            } catch (RemoteException e) {
                Log.e(TAG, "error : " + e.getMessage());
            }

        } else if (ACTION_REQUEST_QR_CODE.equals(action)) {

            Log.d(TAG, "cordova requestQRScanner called");
            PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
            r.setKeepCallback(true);
            callbackContext.sendPluginResult(r);
            this.ctxCallback = callbackContext;
            cordova.setActivityResultCallback(this);
            cordova.getThreadPool().execute(() -> {
                Intent intent = new Intent(cordova.getActivity(), BarcodeActivity.class);
                cordova.getActivity().startActivityForResult(intent, REQUEST_CODE_QRSCAN);
            });
            return true;

        } else if (ACTION_CHECK_PERMISSIONS.equals(action)) {

            if (Build.VERSION.SDK_INT < BUILD_VERSION_CODES_M) {
                Log.i(TAG, "tryToRequestMarshmallowLocationPermission() skipping because API code is " +
                        "below criteria: " + String.valueOf(Build.VERSION.SDK_INT));
                return false;
            }

            final Activity activity = cordova.getActivity();

            final Method checkSelfPermissionMethod = getCheckSelfPermissionMethod();

            if (checkSelfPermissionMethod == null) {
                Log.e(TAG, "Could not obtain the method Activity.checkSelfPermission method. Will " +
                        "not check for ACCESS_COARSE_LOCATION even though we seem to be on a " +
                        "supported version of Android.");
                return false;
            }

            try {

                List<String> permsShouldBeGranted = new ArrayList<>();

                Integer permissionCheckResult = (Integer) checkSelfPermissionMethod.invoke(
                        activity, Manifest.permission.ACCESS_COARSE_LOCATION);
                Log.i(TAG, "Permission check result for ACCESS_COARSE_LOCATION: " +
                        String.valueOf(permissionCheckResult));
                if (permissionCheckResult != PackageManager.PERMISSION_GRANTED) {
                    permsShouldBeGranted.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                }

                permissionCheckResult = (Integer) checkSelfPermissionMethod.invoke(
                        activity, Manifest.permission.CAMERA);
                Log.i(TAG, "Permission check result for CAMERA: " +
                        String.valueOf(permissionCheckResult));
                if (permissionCheckResult != PackageManager.PERMISSION_GRANTED) {
                    permsShouldBeGranted.add(Manifest.permission.CAMERA);
                }

                if (permsShouldBeGranted==null || permsShouldBeGranted.size()<1) {
                    return true;
                }

                final Method requestPermissionsMethod = getRequestPermissionsMethod();
                if (requestPermissionsMethod == null) {
                    Log.e(TAG, "Could not obtain the method Activity.requestPermissions. Will " +
                            "not ask for ACCESS_COARSE_LOCATION even though we seem to be on a " +
                            "supported version of Android.");
                    return false;
                }

                requestPermissionsMethod.invoke(activity,
                        permsShouldBeGranted.toArray(new String[0]),
                        PERMISSION_REQUEST_COARSE_LOCATION);

            } catch (final IllegalAccessException e) {
                Log.w(TAG, "IllegalAccessException while checking for ACCESS_COARSE_LOCATION:", e);
            } catch (final InvocationTargetException e) {
                Log.w(TAG, "InvocationTargetException while checking for ACCESS_COARSE_LOCATION:", e);
            }

        }

        return false;
    }

    private Method getCheckSelfPermissionMethod() {
        try {
            return Activity.class.getMethod("checkSelfPermission", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    private Method getRequestPermissionsMethod() {
        try {
            final Class[] parameterTypes = {String[].class, int.class};

            return Activity.class.getMethod("requestPermissions", parameterTypes);

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE_NICEID && intent != null) {
            Bundle extras = intent.getExtras();
            String msg = extras.getString(MobileCertification.NAME);
            Log.d(TAG, "onActivityResult " + msg);
            this.ctxCallback.sendPluginResult(new PluginResult(PluginResult.Status.OK,
                    msg));
        } else if (requestCode == REQUEST_CODE_QRSCAN && intent != null) {
            Bundle extras = intent.getExtras();
            String dataType = extras.getString(MobileCertification.DATA_TYPE);
            if ("ptNo".equals(dataType)) {
                String ptNo = extras.getString(MobileCertification.PT_NO);
                Log.d(TAG, "onQRScanResult " + ptNo);
                this.ctxCallback.sendPluginResult(new PluginResult(PluginResult.Status.OK,
                        ptNo));
            } else {

            }
        }
    }

    private class DownloadFile extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            // The file extension(URL) must be finished with ".pdf"
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
