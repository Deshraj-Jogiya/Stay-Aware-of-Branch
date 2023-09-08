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
 * Created by 
 */

public class Detector {
    private WebView JsWebView;
    private ArrayList<String> nextDays = new ArrayList<>();
    private DetectorInterface mDetectorInterface;
    public boolean tryingToGetDays;
    ArrayList<String> lastDetectedDaysList = new ArrayList<>();
    private boolean locked;
    private Handler errorCheckHandler;
    private Runnable errorCheckRunnable;

    public Detector(DetectorInterface mDetectorInterface) {
        this.mDetectorInterface = mDetectorInterface;
    }

    public interface DetectorInterface{
        void daysListReady(ArrayList<String> nextDays);
    }

    public void getNextDays(Context mContext) {
        if (!locked){
            try {

                if(nextDays.size() == 0)tryingToGetDays = true;
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
                                JsWebView.loadUrl("javascript:getDays('" + MultiCalsUtils.MultiCalendarType + "','" + getLastDay() + "')");
                            }
                        });
                    }

                });

                // lock
                locked = true;
                errorCheckHandler = new Handler();
                errorCheckRunnable = new Runnable() {
                    @Override
                    public void run() {
                        locked = false;
                    }
                };
                errorCheckHandler.postDelayed(errorCheckRunnable, 5000);

            } catch (Exception ignored) {

            }
        }

    }

    @JavascriptInterface
    public void processDateResult(String[] dateValue) {

        if (dateValue != null) {
            lastDetectedDaysList.clear();
            Collections.addAll(lastDetectedDaysList, dateValue);
            Collections.addAll(nextDays, dateValue);
            if(mDetectorInterface != null) mDetectorInterface.daysListReady(lastDetectedDaysList);

        }
        locked = false;
        errorCheckHandler.removeCallbacks(errorCheckRunnable);
    }

    public String getLastDay(){
        // Must be yyyy-mm-dd
        if(lastDetectedDaysList.size() > 0) return lastDetectedDaysList.get(lastDetectedDaysList.size() - 1);
        else return "";
    }

}
