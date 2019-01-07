package com.jonathanmackenzie.sms_morse;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Telephony;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A receiver that listens for incoming SMS messages.
 * On receive, it will play back the message in Morse code
 *
 * @author Jonathan Mackenzie
 * @email jonmac1@gmail.com
 */
public class SMSMorse extends NotificationListenerService {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "SMSMorse-Service";
    private static final int VERSION_SDK_INT = android.os.Build.VERSION.SDK_INT;

    private static final class ApplicationPackageNames {
        public static final String FACEBOOK_MESSENGER_PACK_NAME = "com.facebook.orca";
        public static final String GOOGLE_MESSAGE_PACK_NAME = "com.google.android.apps.messaging";
        public static final String WHATSAPP_PACK_NAME = "com.whatsapp";
        public static final String INSTAGRAM_PACK_NAME = "com.instagram.android";
    }

    public static final class InterceptedNotificationCode {
        public static final int FACEBOOK_CODE = 1;
        public static final int WHATSAPP_CODE = 2;
        public static final int INSTAGRAM_CODE = 3;
        public static final int OTHER_NOTIFICATIONS_CODE = 4; // We ignore all notification with code == 4
    }

    private SMSTone mTone = null;

    public static boolean supportsNotificationListenerSettings() {
        return VERSION_SDK_INT >= 19;
    }

    @SuppressLint("InlinedApi")
    @TargetApi(19)
    public static Intent getIntentNotificationListenerSettings() {
        final String ACTION_NOTIFICATION_LISTENER_SETTINGS;
        if (VERSION_SDK_INT >= 22) {
            ACTION_NOTIFICATION_LISTENER_SETTINGS = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;
        } else {
            ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
        }

        return new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, "Notification Posted for " + sbn.getPackageName());
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings.getBoolean("play_on_sms", true)) {
            String pkg_name = sbn.getPackageName();
            Set<String> otherApps = settings.getStringSet("other_apps", new HashSet<String>());
            if (otherApps.contains(pkg_name) || pkg_name.equals(Telephony.Sms.getDefaultSmsPackage(this))) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    String sender, message;
                    try {
                        Bundle b = sbn.getNotification().extras;
                        sender = b.getCharSequence(Notification.EXTRA_TITLE).toString();
                        message = b.getCharSequence(Notification.EXTRA_TEXT).toString();
                        int length = Integer.parseInt(settings.getString("play_limit", "4"));
                        if (settings.getBoolean("play_sender", false)) {
                            StringBuilder sb = new StringBuilder();
                            for (char c : sender.toLowerCase().toCharArray()) {
                                if (SMSTone.morseTable.get(c, null) != null) {
                                    sb.append(c);
                                }
                            }
                            sender = sb.toString();
                            message = sender;
                        }
                        message = message.substring(0, Math.min(message.length(), length));
                        if (message != null) {
                            Log.i(TAG, "Playing message: " + message);
                            if (mTone != null) {
                                mTone.stopTone();
                            } else {
                                mTone = new SMSTone(this);
                            }
                            mTone.stopTone();
                            String dots = mTone.convertToDots(message);
                            mTone.play(dots);
                        }
                    } catch (NullPointerException npe) {
                        // lol
                    }
                }
            }
        }
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d(TAG, "onListenerConnected()");
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> otherApps = settings.getStringSet("other_apps", new HashSet<String>());
        Log.d(TAG, "Listening for notifications from: ");
        Log.d(TAG, Telephony.Sms.getDefaultSmsPackage(this));
        for (String s : otherApps) {
            Log.d(TAG,"\t"+s);
        }
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.d(TAG, "onListenerDisconnected()");
    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (mTone != null) {
            mTone.stopTone();
        }

    }

}