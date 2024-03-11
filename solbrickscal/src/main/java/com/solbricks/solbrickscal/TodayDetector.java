package com.solbricks.solbrickscal;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 
 */

public class TodayDetector {
    private WebView JsWebView;
    private TodayDetectorInterface mDetectorInterface;

    public TodayDetector(TodayDetectorInterface mDetectorInterface) {
        this.mDetectorInterface = mDetectorInterface;
    }

    public interface TodayDetectorInterface {
        void getTodayDate(String today);
    }

    public void getToday(Context mContext) {
        try {

            //================================ Prepare WebView  =======================//
            JsWebView = new WebView(mContext);
            JsWebView.getSettings().setJavaScriptEnabled(true);
            JsWebView.addJavascriptInterface(this, "Android");

            JsWebView.loadUrl("file:///android_asset/cals/" + MultiCalsUtils.MultiCalendarType + ".html");
            JsWebView.setWebViewClient(new WebViewClient() {

                public void onPageFinished(WebView view, String url) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            JsWebView.loadUrl("javascript:getToday('" + MultiCalsUtils.MultiCalendarType + "')");
                        }
                    });
                }

            });

        } catch (Exception ignored) {

        }
    }

    @JavascriptInterface
    public void processDateResult(String dateValue) {
        if (dateValue != null) {
            if (mDetectorInterface != null) mDetectorInterface.getTodayDate(dateValue);
        }
    }


}
