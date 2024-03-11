package com.schoolmgmtsys.root.ssg.utils;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;
//import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.io.File;
import java.io.IOException;
import java.util.List;
import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class Downloader {

    private SweetAlertDialog dialog;

    public void downloadFileProcess(final Context context, final String itemFILE, final String link) {
        final SweetAlertDialog LoadingDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText(Concurrent.getLangSubWords("downloading", "Downloading"))
                .setContentText(Concurrent.getLangSubWords("pleaseWait", "Please Wait"));

        LoadingDialog.setCanceledOnTouchOutside(true);
        try {
            LoadingDialog.show();
        } catch (Exception ignored) {
        }

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(com.solutionsbricks.solbricksframework.OkHttpClient.strip(link))
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            dialog = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText(Concurrent.getLangSubWords("errorOccurred", "Error Occurred"))
                                    .setContentText(Concurrent.getLangSubWords("tryAgain", "Try Again"));
                            dialog.setCanceledOnTouchOutside(true);
                            try {
                                dialog.show();
                                LoadingDialog.hide();
                            } catch (Exception ignored) {
                            }
                        }
                    });

                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {

                    if (!response.isSuccessful()) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                dialog = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText(Concurrent.getLangSubWords("errorOccurred", "Error Occurred"))
                                        .setContentText(Concurrent.getLangSubWords("tryAgain", "Try Again"));
                                dialog.setCanceledOnTouchOutside(true);
                                try {
                                    dialog.show();
                                    LoadingDialog.hide();
                                } catch (Exception ignored) {
                                }
                            }
                        });

                    } else {


                        BufferedSink sink = Okio.buffer(Okio.sink(new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)), itemFILE)));
                        sink.writeAll(response.body().source());
                        sink.close();
                        response.body().close();

                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                dialog = new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText(Concurrent.getLangSubWords("fileDownloaded", "File Downloaded"))
                                        .setContentText("Downloaded to Download directory");
                                dialog.setCanceledOnTouchOutside(true);
                                try {
                                    dialog.show();
                                    LoadingDialog.hide();
                                } catch (Exception ignored) {
                                }
                            }
                        });
                    }
                }
            });

        } catch (Exception e) {

            dialog = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(Concurrent.getLangSubWords("errorOccurred", "Error Occurred"))
                    .setContentText(Concurrent.getLangSubWords("tryAgain", "Try Again"));
            dialog.setCanceledOnTouchOutside(true);
            try {
                dialog.show();
                LoadingDialog.hide();
            } catch (Exception exc) {
            }
        }
    }

    public void downloadFile(final Context context, final String itemFILE, final String link) {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            downloadFileProcess(context, itemFILE, link);
        } else {
            Toast.makeText(context, "Need Storage Permission", Toast.LENGTH_LONG).show();

//            PermissionListener listener = new PermissionListener() {
//                @Override
//                public void onSucceed(int requestCode, List<String> grantedPermissions) {
//                    downloadFileProcess(context, itemFILE, link);
//                }
//
//                @Override
//                public void onFailed(int requestCode, List<String> deniedPermissions) {
//                    Toast.makeText(context, "Need Storage Permission", Toast.LENGTH_LONG).show();
//                }
//            };

            AndPermission.with(context)
                    .runtime()
                    .permission(Permission.WRITE_EXTERNAL_STORAGE)
                    .onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            downloadFileProcess(context, itemFILE, link);
                        }
                    })
                    .onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            Toast.makeText(context, "Need Storage Permission", Toast.LENGTH_LONG).show();
                        }
                    })
                    .start();
        }
    }

}
