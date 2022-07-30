package org.apache.cordova.niceid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.snuh.healthcare.healthpilot.dto.MobileCertification;

/**
 * Created by SungJoon_Kim on 2022/7/28.
 */
public class NiceId extends CordovaPlugin {

    private final String TAG = "NiceId";
    private final int REQUEST_CODE = 6018;
    private final String ACTION_REQUEST_NICEID = "requestNiceId";
    private final String MOBILE = "mobile";
    private final String I_PIN = "i_pin";
    private final String KEY_TYPE = "type";
    private CallbackContext ctxCallback;

    private final ActivityResultLauncher<Intent> startActivityResult = cordova.getActivity().registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        String msg = intent.getStringExtra(MobileCertification.NAME);
                        Log.d(TAG, "onActivityResult " + msg);
                        ctxCallback.sendPluginResult(
                                new PluginResult(PluginResult.Status.OK, msg));
                    }
                }
            });



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
                    startActivityResult.launch(intent);
//                    Intent intent = new Intent(cordova.getActivity(), CertificationWebActivity.class);
//                    try {
//                        intent.putExtra(KEY_TYPE, args.get(0).toString());
//
//                        cordova.getActivity().startActivityForResult(intent, REQUEST_CODE);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                }
            });
            return true;
        }
        return false;
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        super.onActivityResult(requestCode, resultCode, intent);
//        if (requestCode == REQUEST_CODE && intent != null) {
//            Bundle extras = intent.getExtras();
//            String msg = extras.getString(MobileCertification.NAME);
//            Log.d(TAG, "onActivityResult " + msg);
//            this.ctx.sendPluginResult(new PluginResult(PluginResult.Status.OK,
//                    msg));
//        }
//    }

//    @Override
//    public void onActivityResult(ActivityResult result) {
//        if (result.getResultCode() == Activity.RESULT_OK) {
//            Intent intent = result.getData();
//            String msg = intent.getStringExtra(MobileCertification.NAME);
//            Log.d(TAG, "onActivityResult " + msg);
//            this.ctxCallback.sendPluginResult(
//                    new PluginResult(PluginResult.Status.OK, msg));
//        }
//    }
}
