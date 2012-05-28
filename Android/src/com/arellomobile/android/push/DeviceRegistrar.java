//
//  DeviceRegistrar.java
//
// Pushwoosh Push Notifications SDK
// www.pushwoosh.com
//
// MIT Licensed

package com.arellomobile.android.push;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.google.android.c2dm.C2DMessaging;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

/**
 * Register/unregister with the App server.
 */
public class DeviceRegistrar
{
    private static final String TAG = "DeviceRegistrar";

    private static final String BASE_URL = "https://cp.pushwoosh.com/json";

    private static final String REGISTER_PATH = "/1.2/registerDevice";
    private static final String UNREGISTER_PATH = "/1.2/unregisterDevice";

    private static final String SHARED_KEY = "deviceid";
    private static final String SHARED_PREF_NAME = "com.arellomobile.android.push.deviceid";

    public static void registerWithServer(final Context context, final String deviceRegistrationID)
    {
        try
        {
            HttpResponse res = makeRequest(context, deviceRegistrationID, REGISTER_PATH);
            if (res.getStatusLine().getStatusCode() != 200)
            {
                PushEventsTransmitter.onRegisterError(context, "status code is " + res.getStatusLine().getStatusCode());
                Log.w(TAG, "Registration error " + String.valueOf(res.getStatusLine().getStatusCode()));
            }
            else
            {
                PushEventsTransmitter.onRegistered(context, deviceRegistrationID);
                Log.w(TAG, "Registered for pushes: " + deviceRegistrationID);
            }
        } catch (Exception e)
        {
            Log.w(TAG, "Registration error " + e.getMessage());
            PushEventsTransmitter.onRegisterError(context, e.getMessage());
        }

    }

    public static void unregisterWithServer(final Context context, final String deviceRegistrationID)
    {
        try
        {
            HttpResponse res = makeRequest(context, deviceRegistrationID, UNREGISTER_PATH);
            if (res.getStatusLine().getStatusCode() != 200)
            {
                Log.w(TAG, "Unregistration error " + String.valueOf(res.getStatusLine().getStatusCode()));
                PushEventsTransmitter.onUnregisteredError(context, "status code is " + res.getStatusLine().getStatusCode());
            }
            else
            {
                PushEventsTransmitter.onUnregistered(context, deviceRegistrationID);
            }
        } catch (Exception e)
        {
            Log.w(TAG, "Unegistration error " + e.getMessage());
            PushEventsTransmitter.onUnregisteredError(context, e.getMessage());
        }
    }

    private static HttpResponse makeRequest(Context context, String deviceRegistrationID, String urlPath) throws
            Exception
    {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(BASE_URL + urlPath);

        JSONObject innerRequestJson = new JSONObject();

        String deviceId = getDeviceUUID(context);
        innerRequestJson.put("hw_id", deviceId);

        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();

        innerRequestJson.put("device_name", isTablet(context) ? "Tablet" : "Phone");
        innerRequestJson.put("application", C2DMessaging.getApplicationId(context));
        innerRequestJson.put("device_type", "3");
        innerRequestJson.put("device_id", deviceRegistrationID);
        innerRequestJson.put("language", language);
        innerRequestJson.put("timezone",
                             Calendar.getInstance().getTimeZone()
                                     .getRawOffset() / 1000); // converting from milliseconds to seconds

        JSONObject requestJson = new JSONObject();
        requestJson.put("request", innerRequestJson);

        httpPost.setHeader("Content-Type", "application/json; charset=utf-8");
        httpPost.setEntity(new StringEntity(requestJson.toString(), "UTF-8"));

        if (Log.isLoggable(TAG, Log.VERBOSE))
        {
            Log.v(TAG, "POST request: " + requestJson.toString());
        }

        HttpResponse httpResponse = httpClient.execute(httpPost);
        return httpResponse;
    }

    private static String getDeviceUUID(Context context)
    {
        final String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (null != androidId)
        {
            return androidId;
        }
        try
        {
            final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                    .getDeviceId();
            if (null != deviceId)
            {
                return deviceId;
            }
        } catch (RuntimeException e)
        {
            // if no
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,
                                                                           Context.MODE_WORLD_WRITEABLE);
        // try to get from pref
        String deviceId = sharedPreferences.getString(SHARED_KEY, null);
        if (null != deviceId)
        {
            return deviceId;
        }
        // generate new
        deviceId = UUID.randomUUID().toString();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // and save it
        editor.putString(SHARED_KEY, deviceId);
        editor.commit();
        return deviceId;
    }

    static boolean isTablet(Context context)
    {
        // TODO: This hacky stuff goes away when we allow users to target devices
        int xlargeBit = 4; // Configuration.SCREENLAYOUT_SIZE_XLARGE;  // upgrade to HC SDK to get this
        Configuration config = context.getResources().getConfiguration();
        return (config.screenLayout & xlargeBit) == xlargeBit;
    }
}
