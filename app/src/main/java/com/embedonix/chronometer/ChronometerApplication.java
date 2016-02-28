package com.embedonix.chronometer;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;

/**
 * Created by saeid on 27.02.16.
 *
 * Main application
 */
public class ChronometerApplication extends Application {

    private NotificationManager mNotificationManager;
    private Notification mNotificationAppState;
    public static final int APP_STATE_NOTIFICATION_ID = 0xFF00AA;
    public static final String APP_STATE_NOTIFICATION_TAG = "APP_STATE_NOTIFICATION_TAG";


    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    public void showNotification(ChronometerState state, String time) {

        String title = getString(R.string.app_short_name);
        String msg = time;


        int icon = R.drawable.ic_notification_icon;

        Intent notificationIntent = new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(this, APP_STATE_NOTIFICATION_ID
                , notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationAppState =
                getNotificationBuilder(title, msg
                        , icon).setContentIntent(contentIntent).build();

        mNotificationAppState.flags |= Notification.FLAG_ONGOING_EVENT;

        mNotificationManager.notify(APP_STATE_NOTIFICATION_TAG
                , APP_STATE_NOTIFICATION_ID, mNotificationAppState);
    }

    public void cancelNotification() {
        mNotificationManager.cancel(APP_STATE_NOTIFICATION_TAG, APP_STATE_NOTIFICATION_ID);
        mNotificationManager.cancelAll();
    }

    private Notification.Builder getNotificationBuilder(String title, String msg, int smallIcon) {
        Notification.Builder  builder = new Notification.Builder(this);
        builder.setContentTitle(title)
                .setContentText(msg)
                .setSmallIcon(smallIcon)
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
//                        R.drawable.ic_notification_icon))
//
                ;

        return builder;
    }

    public void startBackgroundServices(long startTime) {
        Intent service = new Intent(this, NotificationUpdateService.class);
        service.setAction(NotificationUpdateService.ACTION_START);
        service.putExtra("START_TIME", startTime);
        startService(service);
    }

    public void stopBackgroundServices() {
        Intent service = new Intent(this, NotificationUpdateService.class);
        service.setAction(NotificationUpdateService.ACTION_STOP);
        startService(service);

        cancelNotification();
    }


}
