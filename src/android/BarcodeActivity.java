package org.apache.cordova.niceid;


import static org.apache.cordova.niceid.MobileCertification.DATA_TYPE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import org.snuh.healthcare.healthpilot.R;
import java.util.ArrayList;
import java.util.List;


public class BarcodeActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private Context mCtx;
    private DecoratedBarcodeView barcodeScannerView;
    private EditText mEtBarcode;
    private String lastText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "BarcodeActivity called");
        setContentView(R.layout.activity_barcode);
        mCtx = this;
        init();
        mEtBarcode.setSelection(mEtBarcode.length());
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
                    Toast.makeText(mCtx, R.string.info_71, Toast.LENGTH_SHORT).show();
                } else {
                    finishWithPtNo(patientNumber);
                }
            }
        });

        mEtBarcode = findViewById(R.id.et_barcode);
    }

    private BarcodeCallback barcodeCallback = new BarcodeCallback() {

        @Override
        public void barcodeResult(BarcodeResult result) {

            String barcode = result.getText();

            if (!isEmpty(barcode)) {

                // 1) 8자리 영숫자로 구성된 경우 (수진번호만)
                if (barcode.length()==8) {
                    mEtBarcode.setText(barcode);
                    Toast.makeText(mCtx, barcode, Toast.LENGTH_SHORT).show();
                    finishWithPtNo(barcode);
//                } else if (true) {
                    // 2) JSON 타입으로 추가정보들이 더 있는 경우
                }

//                if(barcode.equals(lastText)) {
//                    // Prevent duplicate scans
//                    return;
//                }
//                lastText = barcode;
//                Log.d(TAG, "BarcodeResult = " + barcode);

//                try {
//                    QRCodeData data = new Gson().fromJson(barcode, QRCodeData.class);
////                    Session.employeeNumber = data.loginId;
////                    Session.setCallYn(data.callYn);
////                    Session.setPatientNumber(data.patientNumber);
//                    String ptNo = data.patientNumber;
//                    mEtBarcode.setText(ptNo);
//
//                    if (isEmpty(ptNo) || ptNo.length() < 8) {
//                        showDialog(R.string.info_71);
//                    } else {
////                        checkTodayReservation(ptNo);
//                        login(ptNo);
//                    }
//                } catch (Exception e){
//                    showDialog(R.string.message_error_barcode_data);
//                }
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) { }

    };

    private void finishWithPtNo(String ptNo) {
        Intent intent = new Intent();
        intent.putExtra(DATA_TYPE, "ptNo");
        intent.putExtra(MobileCertification.PT_NO, ptNo);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void finishWithJSON(String jsonString) {
        Intent intent = new Intent();
        intent.putExtra(DATA_TYPE, "json");
        intent.putExtra(MobileCertification.PT_NO, jsonString);
        setResult(RESULT_OK, intent);
        finish();
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

    private boolean isEmpty(String aValue) {
        return (aValue == null || aValue.length() == 0);
    }

}
