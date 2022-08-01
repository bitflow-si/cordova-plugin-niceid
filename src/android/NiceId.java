package org.apache.cordova.niceid;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Toast;

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

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by SungJoon_Kim on 2022/7/28.
 */
public class NiceId extends CordovaPlugin {

    private final String TAG = "NiceId";
    private final int REQUEST_CODE = 6018;
    private final String ACTION_REQUEST_NICEID = "requestNiceId";
    private final String ACTION_DOWNLOADFILE = "downloadFile";
    private final String KEY_TYPE = "type";
    private CallbackContext ctxCallback;
    private Long mDownloadQueueId;
    private DownloadManager mDownloadManager;
    private String localDownloadPath;

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
//                    Intent intent = new Intent(cordova.getActivity(), CertificationWebActivity.class);
//                    startActivityResult.launch(intent);
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
            IntentFilter completeFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            cordova.getContext().registerReceiver(downloadCompleteReceiver, completeFilter);
            new DownloadFile().execute((String)args.get(0));
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

            String fileUrl = strings[0];   // e.g.)http://maven.apache.org/maven-1.x/maven.pdf
            String fileName = fileUrl.split("/")[fileUrl.split("/").length-1];

            File outputFilePath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS + "/" + fileName);

            if (mDownloadManager == null) {
                mDownloadManager = (DownloadManager) cordova.getContext()
                        .getSystemService(Context.DOWNLOAD_SERVICE);
            }

            File outputFile = new File(outputFilePath.getAbsolutePath());
            localDownloadPath = outputFile.getAbsolutePath();
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            Uri downloadUri = Uri.parse(fileUrl);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            List<String> pathSegmentList = downloadUri.getPathSegments();
            request.setTitle("[SNUH] 다운로드");
            request.setDestinationUri(Uri.fromFile(outputFile));
            request.setAllowedOverMetered(true);

            mDownloadQueueId  = mDownloadManager.enqueue(request);

            return null;
        }
    }

    private BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            cordova.getContext().unregisterReceiver(downloadCompleteReceiver);

            long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if(mDownloadQueueId == reference){
                DownloadManager.Query query = new DownloadManager.Query();  // 다운로드 항목 조회에 필요한 정보 포함
                query.setFilterById(reference);
                Cursor cursor = mDownloadManager.query(query);

                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);

                int status = cursor.getInt(columnIndex);
                int reason = cursor.getInt(columnReason);

                cursor.close();

                switch (status) {
                    case DownloadManager.STATUS_SUCCESSFUL :
                        Toast.makeText(cordova.getContext(), "다운로드를 완료하였습니다.", Toast.LENGTH_SHORT).show();

//                        Intent intent1 = new Intent(Intent.ACTION_VIEW);
//                        intent1.setDataAndType(Uri.parse(localDownloadPath), "application/pdf");
//                        intent1.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                        Intent intent1 = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent1.addCategory(Intent.CATEGORY_OPENABLE);
                        intent1.setType("application/pdf");
                        // Optionally, specify a URI for the file that should appear in the
                        // system file picker when it loads.
                        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, localDownloadPath);

                        context.startActivity(intent1);

                        break;

                    case DownloadManager.STATUS_PAUSED :
                        Toast.makeText(cordova.getContext(), "다운로드가 중단되었습니다.", Toast.LENGTH_SHORT).show();
                        break;

                    case DownloadManager.STATUS_FAILED :
                        Toast.makeText(cordova.getContext(), "다운로드가 취소되었습니다.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };

}
