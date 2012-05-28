//
//  PushEventsTransmitter.java
//
// Pushwoosh Push Notifications SDK
// www.pushwoosh.com
//
// MIT Licensed

package com.arellomobile.android.push;

import com.pushwoosh.test.plugin.pushnotifications.PushNotifications;

import android.content.Context;
import android.content.Intent;

public class PushEventsTransmitter
{
    private static void transmit(final Context context, String stringToShow, String messageKey)
    {
        Intent notifyIntent = new Intent(context, MessageActivity.class);
        notifyIntent.putExtra(messageKey, stringToShow);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(notifyIntent);
    }

    public static void onRegistered(final Context context, String registrationId)
    {
        PushNotifications.onRegistered(registrationId);
    }

    public static void onRegisterError(final Context context, String errorId)
    {
        PushNotifications.onRegisteredError(errorId);
    }

    public static void onUnregistered(final Context context, String registrationId)
    {
        PushNotifications.onUnregistered(registrationId);
    }

    public static void onUnregisteredError(Context context, String errorId)
    {
        PushNotifications.onUnregisteredError(errorId);
    }

    public static void onMessageReceive(final Context context, String message)
    {
    	PushNotifications.onMessageReceive(message);
        transmit(context, message, PushManager.PUSH_RECEIVE_EVENT);
    }
}
