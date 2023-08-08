package org.apache.cordova.niceid;


import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;

import java.net.URISyntaxException;


public class CertificationWebActivity extends AppCompatActivity {

    private final String MOBILE = "mobile";
    private final String I_PIN = "i_pin";
    private final String KEY_TYPE = "type";
    private WebView webView;

    private final WebViewClient webViewClient = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (view != null && request != null) {
                Uri uri = request.getUrl();
                return this.shouldOverrideUrlLoading(view, uri.toString());
            } else {
                return false;
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url != null) {
                if (url.startsWith("intent://")) {
                    Intent intent = null;
                    try {
                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        if (intent != null) {
                            startActivity(intent);
                        }
                    } catch (URISyntaxException var7) {
                    } catch (ActivityNotFoundException var8) {
                        if (!TextUtils.isEmpty(intent.getPackage())) {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=" + intent.getPackage())));
                        }
                    }
                    return true;
                } else if (url.startsWith("https://play.google.com/store/apps/details?id=")
                        || url.startsWith("market://details?id=")) {
                    Uri uri = Uri.parse(url);
                    String packageName = uri != null ? uri.getQueryParameter("id") : null;
                    if (!TextUtils.isEmpty(packageName)) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + packageName)));
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (url != null) {
                if (url.contains("checkplus_ok")) {
                    Log.d("onPageFinished", "javascript:call()");
                    if (view != null) {
                        view.evaluateJavascript("javascript:call()",
                                new ValueCallback() {
                            // $FF: synthetic method
                            // $FF: bridge method
                            @Override
                            public void onReceiveValue(Object val) {
                                String msg = (String)val;
                                Log.d("onReceiveValue", "result : " + msg);
                                MobileCertification certification = new Gson().fromJson(msg,
                                        MobileCertification.class);
                                if (certification != null) {
                                    Intent intent = new Intent();
                                    intent.putExtra(MobileCertification.NAME, msg);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            }
                        });
                    }
                }
            }
        }
    };

    @Override
    @SuppressLint({"SetJavaScriptEnabled"})
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        this.setContentView(webView);
        WebSettings settings = webView.getSettings();
        if (settings != null) {
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setJavaScriptCanOpenWindowsAutomatically(true);
            settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            settings.setLoadsImagesAutomatically(true);
            settings.setBuiltInZoomControls(true);
            settings.setLoadWithOverviewMode(true);
            settings.setUseWideViewPort(true);
            settings.setSupportZoom(true);
            settings.setSupportMultipleWindows(true);
        }
        webView.setWebViewClient(this.webViewClient);

        Intent intent = this.getIntent();
        if (intent != null) {
            String keyType = intent.getStringExtra(KEY_TYPE);
            if (keyType != null) {
                if (MOBILE.equals(keyType)) {
                    webView.loadUrl(intent.getExtras().get("url").toString());
                } else if (I_PIN.equals(keyType)) {
                    webView.loadUrl("https://cert.vno.co.kr/ipin.cb");
                }
            }
        }
    }

}
