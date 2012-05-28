//
//  PushManager.java
//
// Pushwoosh Push Notifications SDK
// www.pushwoosh.com
//
// MIT Licensed

package com.arellomobile.android.push;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.c2dm.C2DMessaging;

public class PushManager
{
    // app id in the backend
    private volatile String mAppId = null;
    private volatile String mSenderId;

    // message id in the notification bar
    public static final int MESSAGE_ID = 1001;

    private static final String HTML_URL_FORMAT = "https://cp.pushwoosh.com/content/%s";

    public static final String REGISTER_EVENT = "REGISTER_EVENT";
    public static final String REGISTER_ERROR_EVENT = "REGISTER_ERROR_EVENT";
    public static final String UNREGISTER_EVENT = "UNREGISTER_EVENT";
    public static final String UNREGISTER_ERROR_EVENT = "UNREGISTER_ERROR_EVENT";
    public static final String PUSH_RECEIVE_EVENT = "PUSH_RECEIVE_EVENT";

    private Context context;
    private Bundle lastPush;

    PushManager(Context context)
    {
        if (null == context)
        {
            throw new IllegalArgumentException("Context can't be null");
        }
        this.context = context;
        mAppId = C2DMessaging.getApplicationId(context);
        mSenderId = C2DMessaging.getSenderId(context);
    }

    public PushManager(Context context, String appId, String senderId)
    {
        if (null == context)
        {
            throw new IllegalArgumentException("Context can't be null");
        }
        this.context = context;
        mAppId = appId;
        mSenderId = senderId;
        C2DMessaging.setApplicationId(context, mAppId);
        C2DMessaging.setSenderId(context, senderId);
    }

    /**
     * @param savedInstanceState if this method calls in onCreate method, can be null
     * @param context            current context
     */
    public void onStartup(Bundle savedInstanceState, Context context)
    {
        if (null == savedInstanceState)
        {
            if(context instanceof Activity)
            {
                if(((Activity) context).getIntent().hasExtra(PushManager.PUSH_RECEIVE_EVENT))
                {
                    // if this method calls because of push message, we don't need to register
                    return;
                }
            }
            
            String id = C2DMessaging.getRegistrationId(context);
            if(id != null && !id.equals("")) {
            	DeviceRegistrar.registerWithServer(context, id);
            }
            else {
            	C2DMessaging.register(context);
            }
        }
        else
        {
            // calls if activity restarts
            String appId = C2DMessaging.getApplicationId(context);
            String id = C2DMessaging.getRegistrationId(context);

            if (id == null || id.equals("") || appId == null || !appId.equals(mAppId))
            {
                // if not register yet or an other id detected
                C2DMessaging.register(context);
            }
            else {
            	DeviceRegistrar.registerWithServer(context, id);
            }
        }
    }

    public void unregister()
    {
        C2DMessaging.unregister(context);
    }

    public String getCustomData()
    {
        if (lastPush == null)
        {
            return null;
        }

        String customData = (String) lastPush.get("u");
        return customData;
    }

    public boolean onHandlePush(Activity activity)
    {
        Bundle pushBundle = activity.getIntent().getBundleExtra("pushBundle");
        if (null == pushBundle || null == context)
        {
            return false;
        }

        lastPush = pushBundle;

        //user data
        String userData = (String) pushBundle.get("u");
        if (userData == null)
        {
            userData = (String) pushBundle.get("title");
        }
        PushEventsTransmitter.onMessageReceive(context, userData);

        // push message handling
        String url = (String) pushBundle.get("h");

        if (url != null)
        {
            url = String.format(HTML_URL_FORMAT, url);

            // show browser
            Intent intent = new Intent(activity, PushWebview.class);
            intent.putExtra("url", url);
            activity.startActivity(intent);
        }

        return true;
    }
}
