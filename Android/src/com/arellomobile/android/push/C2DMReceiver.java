//
//  C2DMReceiver.java
//
// Pushwoosh Push Notifications SDK
// www.pushwoosh.com
//
// MIT Licensed

package com.arellomobile.android.push;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.google.android.c2dm.C2DMBaseReceiver;

import java.util.List;

public class C2DMReceiver extends C2DMBaseReceiver
{
    static final String TAG = "C2DMReceiver";

    public C2DMReceiver()
    {
        super(C2DMReceiver.class.getSimpleName());
    }

    @Override
    public void onRegistered(Context context, String registrationId)
    {
        DeviceRegistrar.registerWithServer(context, registrationId);
    }

    @Override
    public void onUnregistered(Context context, String registrationId)
    {
        DeviceRegistrar.unregisterWithServer(context, registrationId);
    }

    @Override
    public void onError(Context context, String errorId)
    {
        Log.e(TAG, "Messaging registration error: " + errorId);
        PushEventsTransmitter.onRegisterError(context, errorId);
    }

    @Override
    protected void onMessage(Context context, Intent intent)
    {
        Bundle extras = intent.getExtras();
        if (extras == null)
        {
            return;
        }

        extras.putBoolean("foregroud", isAppOnForeground(context));

        String title = (String) extras.get("title");
        String url = (String) extras.get("h");
        String link = (String) extras.get("l");

        // empty message with no data
        Intent notifyIntent = null;
        if (link != null)
        {
            // we want main app class to be launched
            notifyIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        else
        {
            notifyIntent = new Intent(context, PushHandlerActivity.class);
            notifyIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // pass all bundle
            notifyIntent.putExtra("pushBundle", extras);
        }

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // first string will appear on the status bar once when message is added
        CharSequence appName = context.getPackageManager().getApplicationLabel(context.getApplicationInfo());
        if (null == appName)
        {
            appName = "";
        }
        Notification notification = new Notification(context.getApplicationInfo().icon, appName + ": new message",
                System.currentTimeMillis());

        // remove the notification from the status bar after it is selected
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        PendingIntent contentIntent = PendingIntent
                .getActivity(this.getBaseContext(), 0, notifyIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // this will appear in the notifications list
        notification.setLatestEventInfo(context, appName, title, contentIntent);
        manager.notify(PushManager.MESSAGE_ID, notification);
    }

    private boolean isAppOnForeground(Context context)
    {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null)
        {
            return false;
        }

        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses)
        {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess
                    .processName.equals(packageName))
            {
                return true;
            }
        }

        return false;
    }

}
