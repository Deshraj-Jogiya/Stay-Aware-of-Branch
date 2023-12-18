package com.schoolmgmtsys.root.ssg.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.app.AssignmentsPage;
import com.schoolmgmtsys.root.ssg.app.AttendancePage;
import com.schoolmgmtsys.root.ssg.app.ControlActivity;
import com.schoolmgmtsys.root.ssg.app.EventsViewPage;
import com.schoolmgmtsys.root.ssg.app.ExamsPage;
import com.schoolmgmtsys.root.ssg.app.HomeworkView;
import com.schoolmgmtsys.root.ssg.app.NewsViewPage;
import com.schoolmgmtsys.root.ssg.app.OnlineExamsPage;
import com.schoolmgmtsys.root.ssg.app.PaymentInvoiceView;
import com.schoolmgmtsys.root.ssg.app.SplashPage;
import com.schoolmgmtsys.root.ssg.app.StudentAttendancePage;
import com.schoolmgmtsys.root.ssg.app.StudentsPage;
import com.schoolmgmtsys.root.ssg.app.StudyMaterialPage;
import com.schoolmgmtsys.root.ssg.messages.MessagesItemsActivity;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String title = "";
        String message = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            String id = "id_product";
            // The user-visible name of the channel.
            CharSequence name = "Product";
            // The user-visible description of the channel.
            String description = "Notifications regarding our products";
            int importance = NotificationManager.IMPORTANCE_MAX;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            notificationManager.createNotificationChannel(mChannel);
        }


        JSONObject obj = new JSONObject(remoteMessage.getData());
        String jsonMessage = obj.toString();
        try {
            JSONObject jsonMain = new JSONObject(jsonMessage);
            Log.e("response"," "+jsonMain);
        } catch (JSONException e) {
            Log.e("response"," "+e.getMessage());
            e.printStackTrace();
        }

        try {
            RemoteMessage.Notification rmBody = remoteMessage.getNotification();
            if(rmBody != null && rmBody.getTitle() != null) title = rmBody.getTitle();
            if(rmBody != null && rmBody.getBody() != null)  message = rmBody.getBody();
            showNotification(title, message, remoteMessage.getData());
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("NEW_TOKEN",s);
        getSharedPreferences("token", MODE_PRIVATE).edit().putString("fb", s).apply();
    }

    public static String getToken(Context context) {
        return context.getSharedPreferences("token", MODE_PRIVATE).getString("fb", "empty");
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     */
    private void showNotification(String title, String message, Map<String, String> data) {

        Intent intent = new Intent(getApplicationContext(), SplashPage.class);

        if(data != null){
            intent = redirectNotification(getBaseContext(), data.get("where"), data.get("id"));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // This code targets Android O and Above (Channels).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String DEFAULT_CHANNEL_ID = getString(R.string.default_notification_channel_id);
            CharSequence NOTIFICATION_CHANNEL_NAME = "notification_channel_school_app";
            String CHANNEL_DESCRIPTION = "channel_description";
            NotificationChannel notificationChannel = new NotificationChannel(DEFAULT_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription(CHANNEL_DESCRIPTION);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);

            Notification.Builder notificationBuilder = new Notification.Builder(this, DEFAULT_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(Concurrent.fromHtml(message))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);
            notificationManager.notify(0, notificationBuilder.build());
        }


        // This Code targets Android N and lower.
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(Concurrent.fromHtml(message))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        notificationManager.notify(0, notificationBuilder.build());

    }

    public static Intent redirectNotification(Context mContext, String where, String id) {

        Intent MyIntent;

        if (where == null) return new Intent(mContext, SplashPage.class);
        switch (where) {
            case "newsboard":
                if (Concurrent.isUserHavePermission(mContext, "newsboard.list","newsboard.View")) {
                    MyIntent = new Intent(mContext, NewsViewPage.class);
                    MyIntent.putExtra("PageID", Integer.valueOf(id));
                }else{
                    MyIntent = new Intent(mContext, SplashPage.class);
                }

                break;
            case "events":
                if (Concurrent.isUserHavePermission(mContext, "events.list","events.View")) {
                    MyIntent = new Intent(mContext, EventsViewPage.class);
                    MyIntent.putExtra("PageID", Integer.valueOf(id));
                }else{
                    MyIntent = new Intent(mContext, SplashPage.class);
                }
                break;
            case "messages":
                MyIntent = new Intent(mContext, MessagesItemsActivity.class);
                MyIntent.putExtra("msg_id", id);
                break;
            case "classschedule":
                if (Concurrent.isUserHavePermission(mContext, "classSch.list")) {
                    MyIntent = new Intent(mContext, ControlActivity.class);
                    MyIntent.putExtra("TARGET_FRAGMENT", "ClassesSchPage");
                    MyIntent.putExtra("EXTRA_HEAD_FIND_WORD", "ClassSchedule");
                    MyIntent.putExtra("EXTRA_HEAD_REPLACE_WORD", "Class Schedule");
                }else{
                    MyIntent = new Intent(mContext, SplashPage.class);
                }

                break;
            case "attendance":
                if (Concurrent.isUserHavePermission(mContext, "Attendance.takeAttendance")) {
                    if (Concurrent.getAppRole(mContext) == Concurrent.APP_ROLE_STUDENT) {
                        MyIntent = new Intent(mContext, StudentAttendancePage.class);
                    } else if (Concurrent.getAppRole(mContext) == Concurrent.APP_ROLE_PARENT) {
                        MyIntent = new Intent(mContext, ControlActivity.class);
                        MyIntent.putExtra("TARGET_FRAGMENT", "ParentsAttendance");
                        MyIntent.putExtra("EXTRA_HEAD_FIND_WORD", "Attendance");
                        MyIntent.putExtra("EXTRA_HEAD_REPLACE_WORD", "Attendance");
                    } else {
                        MyIntent = new Intent(mContext, AttendancePage.class);
                    }
                }else{
                    MyIntent = new Intent(mContext, SplashPage.class);
                }
                break;
            case "exams":
                if (Concurrent.isUserHavePermission(mContext, "examsList.list","examsList.View","examsList.showMarks")) {
                    MyIntent = new Intent(mContext, ExamsPage.class);
                }else{
                    MyIntent = new Intent(mContext, SplashPage.class);
                }
                break;
            case "material":
                if (Concurrent.isUserHavePermission(mContext, "studyMaterial.list")) {
                    MyIntent = new Intent(mContext, StudyMaterialPage.class);
                }else{
                    MyIntent = new Intent(mContext, SplashPage.class);
                }
                break;
            case "assignment":
                if (Concurrent.isUserHavePermission(mContext, "Assignments.list")) {
                    MyIntent = new Intent(mContext, AssignmentsPage.class);
                }else{
                    MyIntent = new Intent(mContext, SplashPage.class);
                }
                break;
            case "exams_marks":
                MyIntent = new Intent(mContext, ExamsPage.class);
                break;
            case "marksheet":
                if (Concurrent.getAppRole(mContext) == Concurrent.APP_ROLE_STUDENT) {
                    MyIntent = new Intent(mContext, ControlActivity.class);
                    MyIntent.putExtra("TARGET_FRAGMENT", "studentShowMarks");
                    MyIntent.putExtra("EXTRA_INT_1", PreferenceManager.getDefaultSharedPreferences(mContext).getInt("app_user_id", 0));
                    MyIntent.putExtra("EXTRA_HEAD_FIND_WORD", "Marksheet");
                    MyIntent.putExtra("EXTRA_HEAD_REPLACE_WORD", "Marksheet");
                } else if (Concurrent.getAppRole(mContext) == Concurrent.APP_ROLE_PARENT) {
                    MyIntent = new Intent(mContext, StudentsPage.class);
                } else {
                    MyIntent = new Intent(mContext, SplashPage.class);
                }
                break;
            case "online_exams":
                if (Concurrent.isUserHavePermission(mContext, "onlineExams.list")) {
                    MyIntent = new Intent(mContext, OnlineExamsPage.class);
                }else{
                    MyIntent = new Intent(mContext, SplashPage.class);
                }
                break;
            case "mob_notif":
                MyIntent = new Intent(mContext, SplashPage.class);
                break;
            case "invoice":
                if (Concurrent.isUserHavePermission(mContext, "Invoices.list","Invoices.View")) {
                    MyIntent = new Intent(mContext, PaymentInvoiceView.class);
                    MyIntent.putExtra("invoice_id", String.valueOf(id));
                }else{
                    MyIntent = new Intent(mContext, SplashPage.class);
                }

                break;
            case "homework":
                if (Concurrent.isUserHavePermission(mContext, "Homework.list", "Homework.View")) {
                    MyIntent = new Intent(mContext, HomeworkView.class);
                    MyIntent.putExtra("homework_id", String.valueOf(id));
                }else{
                    MyIntent = new Intent(mContext, SplashPage.class);
                }

                break;

            default:
                MyIntent = new Intent(mContext, SplashPage.class);
                break;
        }
        return MyIntent;

    }
}
