//
//  PushNotifications.java
//
// Author Max Konev, 28/05/12.
//
// Pushwoosh Push Notifications Plugin for Cordova Android
// www.pushwoosh.com
//
// MIT Licensed

package com.pushwoosh.test.plugin.pushnotifications;

import java.util.HashMap;

import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.apache.cordova.api.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.arellomobile.android.push.PushManager;
import com.google.android.c2dm.C2DMessaging;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PushNotifications extends Plugin {
	public static final String REGISTER = "registerDevice";
	public static final String UNREGISTER = "unregisterDevice";
	
	HashMap<String, String> callbackIds = new HashMap<String, String>();
	
	private static PushNotifications instance = null;
	private static String pushMessage = null;
	
	public PushNotifications() {
		super();
	}
	
    /**
     * Called when the system is about to start resuming a previous activity. 
     * 
     * @param multitasking		Flag indicating if multitasking is turned on for app
     */
    public void onPause(boolean multitasking) {
    	super.onPause(multitasking);
    	instance = null;
    }

    /**
     * Called when the activity will start interacting with the user. 
     * 
     * @param multitasking		Flag indicating if multitasking is turned on for app
     */
    public void onResume(boolean multitasking) {
    	super.onResume(multitasking);
    	instance = this;
    	
		if(pushMessage != null) {
	    	String jsStatement = String.format("window.plugins.pushNotification.notificationCallback('%s');", pushMessage);
	    	sendJavascript(jsStatement);
			pushMessage = null;
		}
    }
    
    /**
     * Called when the activity receives a new intent. 
     */
    public void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
    }
    
    /**
     * The final call you receive before your activity is destroyed. 
     */
    public void onDestroy() {
    	super.onDestroy();
    	
    	instance = null;
    }

    /**
     * Called when a message is sent to plugin. 
     * 
     * @param id            The message id
     * @param data          The message data
     */
    public void onMessage(String id, Object data) {
    	super.onMessage(id, data);
    }
	
	@Override
	public void setContext(CordovaInterface ctx) {
		super.setContext(ctx);
		
		if(pushMessage != null) {
	    	String jsStatement = String.format("window.plugins.pushNotification.notificationCallback('%s');", pushMessage);
	    	sendJavascript(jsStatement);
			pushMessage = null;
		}
		
		instance = this;
	}

	@Override
	public PluginResult execute(String action, JSONArray data, String callbackId) {
		Log.d("PushNotifications", "Plugin Called");

		PluginResult result = null;
		if (REGISTER.equals(action)) {
			callbackIds.put("registerDevice", callbackId);
			
			JSONObject params = null;
			try {
				params = data.getJSONObject(0);
			} catch (JSONException e) {
				return new PluginResult(Status.ERROR);
			}
	        PushManager mPushManager = null;
			try {
				mPushManager = new PushManager((Context)this.ctx, params.getString("appid"), params.getString("email"));
			} catch (JSONException e) {
				return new PluginResult(Status.ERROR);
			}
			
	        mPushManager.onStartup(null, (Context)this.ctx);

			result = new PluginResult(Status.NO_RESULT);
			result.setKeepCallback(true);

			return result;
		}

		if (UNREGISTER.equals(action)) {
			callbackIds.put("unregisterDevice", callbackId);
			result = new PluginResult(Status.NO_RESULT);
			result.setKeepCallback(true);
			
			C2DMessaging.unregister((Context)this.ctx);
			return result;
		}
		
		Log.d("DirectoryListPlugin", "Invalid action : " + action + " passed");
		return new PluginResult(Status.INVALID_ACTION);
	}

    public void doOnRegistered(String registrationId) {
    	String callbackId = callbackIds.get("registerDevice");
    	PluginResult result = new PluginResult(Status.OK, registrationId);
    	success(result, callbackId);
    }

    public void doOnRegisteredError(String errorId) {
    	String callbackId = callbackIds.get("registerDevice");
    	PluginResult result = new PluginResult(Status.OK, errorId);
    	error(result, callbackId);
    }

    public void doOnUnregistered(String registrationId) {
    	String callbackId = callbackIds.get("unregisterDevice");
    	PluginResult result = new PluginResult(Status.OK, registrationId);
    	success(result, callbackId);
    }

    public void doOnUnregisteredError(String errorId) {
    	String callbackId = callbackIds.get("unregisterDevice");
    	PluginResult result = new PluginResult(Status.OK, errorId);
    	error(result, callbackId);
    }
    
    public void doOnMessageReceive(String message) {
    	String jsStatement = String.format("window.plugins.pushNotification.notificationCallback('%s');", message);
    	sendJavascript(jsStatement);
    }

    public static void onMessageReceive(String message) {
    	if(instance != null) {
    		instance.doOnMessageReceive(message);
    	}
    	else {
    		pushMessage = message;
    	}
    }

    public static void onRegistered(String registrationId) {
    	if(instance != null) {
    		instance.doOnRegistered(registrationId);
    	}
    }

    public static void onRegisteredError(String errorId) {
    	if(instance != null) {
    		instance.doOnRegisteredError(errorId);
    	}
    }

    public static void onUnregistered(String registrationId) {
    	if(instance != null) {
    		instance.doOnUnregistered(registrationId);
    	}
    }

    public static void onUnregisteredError(String errorId) {
    	if(instance != null) {
    		instance.doOnUnregisteredError(errorId);
    	}
    }
}
