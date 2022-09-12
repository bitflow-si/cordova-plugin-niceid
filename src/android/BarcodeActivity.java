package org.apache.cordova.niceid;

import static org.snuh.healthcare.checkup.network.Common.EXTRA_KEY_AUTO_LOGON;
import static org.snuh.healthcare.checkup.network.Common.EXTRA_KEY_PATIENT_NUMBER;
import static org.snuh.healthcare.checkup.network.NetType.API_TODAY_RESERVATION_INFO;
import static org.snuh.healthcare.checkup.network.Session.getPatientNumber;
import static org.snuh.healthcare.checkup.util.RequestJsonUtil.todayReservationRequest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import org.snuh.healthcare.checkup.BaseActivity;
import org.snuh.healthcare.checkup.BuildConfig;
import org.snuh.healthcare.checkup.R;
import org.snuh.healthcare.checkup.data.Bool;
import org.snuh.healthcare.checkup.data.QRCodeData;
import org.snuh.healthcare.checkup.dialog.CustomDialog;
import org.snuh.healthcare.checkup.network.Common;
import org.snuh.healthcare.checkup.network.NetData;
import org.snuh.healthcare.checkup.network.NetworkTask;
import org.snuh.healthcare.checkup.network.Session;
import org.snuh.healthcare.checkup.util.DialogUtil;
import org.snuh.healthcare.checkup.util.PreferenceUtil;
import org.snuh.healthcare.checkup.util.ProgressUtil;
import org.snuh.healthcare.checkup.util.Util;

import java.util.ArrayList;
import java.util.List;

public class BarcodeActivity extends BaseActivity implements NetworkTask.onResponseListener {
    private Context mCtx;
    private BackPressCloseHandler backPressCloseHandler;
    private DecoratedBarcodeView barcodeScannerView;
    private EditText mEtBarcode;

    //자동 로그인 여부(예약App -> 검진App)
    private Boolean isAutoLogin = false;
    private String intentPatientNumber = null;
    private Intent nextIntent;
    private CustomDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        mCtx = this;
        backPressCloseHandler = new BackPressCloseHandler(BarcodeActivity.this);
        init();

        if( BuildConfig.DEBUG) {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            final int width = size.x;
            int height = size.y;
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            // 해상도 구하는 방법
            float widthDp = dm.widthPixels / dm.density;

            String str_ScreenSize = "The Android Screen is: " + dm.widthPixels + " x " + dm.heightPixels + " / " + "Width DP : " + widthDp;
            Log.i("size", "w = " + width + " / h = " + height + " / " + str_ScreenSize);
        }

        if( getIntent() != null ) intentPatientNumber = getIntent().getStringExtra(EXTRA_KEY_PATIENT_NUMBER);


        String num;
        if( Util.isEmpty(intentPatientNumber) ) {
            isAutoLogin = false;
            num = (String) PreferenceUtil.getPreferences(mCtx, Common.HMPS_NO, "", PreferenceUtil.PREF_TYPE.STRING);

            mEtBarcode.setText(num);
        } else {
            isAutoLogin = true;
            Session.initCallYn();
            barcodeScannerView.pause();
            ProgressUtil.startAutoLoginProgress(mCtx, ProgressUtil.FLAG_BARCODE);
            mEtBarcode.setText(intentPatientNumber);
            PreferenceUtil.setPreferences(mCtx, Common.HMPS_NO, intentPatientNumber, PreferenceUtil.PREF_TYPE.STRING);

            if (intentPatientNumber.length() < 8) {
                showDialog(getString(R.string.info_71));
                return;
            }
            mEtBarcode.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkTodayReservation(intentPatientNumber, Bool.FALSE);
                }
            }, 500);
        }
        mEtBarcode.setSelection(mEtBarcode.length());

        ((TextView)findViewById(R.id.tv_version)).setText(Util.getVersionName(this));
    }

    private void init() {
        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);
        List<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QR_CODE);
        barcodeScannerView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));

        barcodeScannerView.initializeFromIntent(getIntent());
        barcodeScannerView.decodeContinuous(barcodeCallback);

        (findViewById(R.id.btn_save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String patientNumber = mEtBarcode.getText().toString();
                if (patientNumber.length() < 8) {
                    showDialog(getString(R.string.info_71));
                    return;
                }
                Session.initCallYn();
                if (!Util.isEmpty(patientNumber)){
                    checkTodayReservation(patientNumber, Bool.FALSE);
                }
            }
        });

        mEtBarcode = findViewById(R.id.et_barcode);
    }

    private String lastText;
    BarcodeCallback barcodeCallback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            String barcode = result.getText();

            if (!Util.isEmpty(barcode)) {
                if(barcode.equals(lastText)) {
                    // Prevent duplicate scans
                    return;
                }
                lastText = barcode;
                Common.logger.info("BarcodeResult = " + barcode);
                Log.i("Barcode", "BarcodeResult = " + barcode);

                try {
                    QRCodeData data = new Gson().fromJson(barcode, QRCodeData.class);

                    Session.employeeNumber = data.loginId;
                    Session.setCallYn(data.callYn);
                    Session.setPatientNumber(data.patientNumber);

                    mEtBarcode.setText(getPatientNumber());

                    if (!Util.isEmpty(getPatientNumber()) && getPatientNumber().length() >= 8)
                        checkTodayReservation(getPatientNumber(), Bool.TRUE);
                    else{
                        showDialog(getString(R.string.info_71));
                    }
                } catch (Exception e){
                    showDialog(getString(R.string.message_error_barcode_data));
                }
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) { }
    };

    private View.OnClickListener resumeBarcode = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mDialog.dismiss();
            barcodeScannerView.resume();
        }
    };

    private void showDialog(String msg){
        if( mDialog != null && mDialog.isShowing() ) {
            mDialog.dismiss();
            mDialog = null;
        }

        mDialog = new DialogUtil(mCtx).showOneButtonDialog(msg, resumeBarcode);
    }

    /**
     * 금일 예약 조회(접수)
     */
    private void checkTodayReservation(String patientNumber, Bool barcodeYn) {
        NetworkTask networkTask = new NetworkTask(BarcodeActivity.this, false);
        networkTask.execute(new NetData(API_TODAY_RESERVATION_INFO, todayReservationRequest(mCtx, patientNumber, "", barcodeYn)));
    }

    @Override
    public void onResult(NetData result) {
        if(result == null || result.mBaseNetworkData == null) {
            barcodeScannerView.resume();
            Toast.makeText(mCtx, getString(R.string.again), Toast.LENGTH_SHORT).show();
            return;
        }

        if (result.mType == API_TODAY_RESERVATION_INFO)
            resultData(result);
    }

    /**
     * 결과값 저장
     */
    private void resultData(NetData result) {
        if (Util.isSuccess(result.mBaseNetworkData.resRsltCd)) {
            barcodeScannerView.resume();

            String status = result.mTodayReservationData.loginCd;
            if (!Util.isEmpty(status)) {
                //로그인 시 채혈 팝업 나오는 이슈 방어코드 추가
                if (status.equals("00"))
                    PreferenceUtil.setPreferences(mCtx, Common.BEFORE_NUM, "0", PreferenceUtil.PREF_TYPE.STRING);

                login(result);
            }
        } else {
            if( result.mBaseNetworkData.resRsltCd.equals("03") )
                showDialog(getString(R.string.message_include_etc_package_for_dialog));
            else
                showDialog(result.mBaseNetworkData.resRsltCnte);
        }
    }

    private void login(NetData result) {
        Session.setUserData(result.mTodayReservationData);

        nextIntent = new Intent(mCtx, ReservationInfoActivity.class);
        nextIntent.putExtra(EXTRA_KEY_AUTO_LOGON, isAutoLogin);

        if( isAutoLogin ){
            mEtBarcode.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(nextIntent);
                    finish();
                }
            }, 1000);
        } else
            startActivity(nextIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScannerView.resume();
        lastText = "";
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause();
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    public class BackPressCloseHandler {

        private long backKeyPressedTime = 0;
        private Snackbar toast;
        private Activity activity;

        BackPressCloseHandler(Activity activity) {
            this.activity = activity;
        }

        void onBackPressed() {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                showGuide();
                return;
            }
            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                activity.finishAffinity();
                toast.dismiss();
            }
        }

        private void showGuide() {
            toast = Snackbar.make(activity.getWindow().getDecorView().findViewById(android.R.id.content), getString(R.string.message_application_close), Snackbar.LENGTH_SHORT);
            toast.show();
        }
    }
}
