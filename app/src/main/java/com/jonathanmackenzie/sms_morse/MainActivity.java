package com.jonathanmackenzie.sms_morse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A small test activity to demonstrate
 * the morse code playback functionality
 *
 * @author Jonathan Mackenzie
 * @email jonmac1@gmail.com
 */
public class MainActivity extends Activity {

    private SMSTone mTone;
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private AlertDialog enableNotificationListenerAlertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        setContentView(R.layout.activity_main);
        Button btn = (Button) findViewById(R.id.talk);
        btn.setText("Morse");

        final EditText et = (EditText) findViewById(R.id.input);
        final TextView tv = (TextView) findViewById(R.id.morse);
        tv.setTextSize(32);
        tv.setTypeface(Typeface.createFromAsset(getAssets(), "DroidSansMono.ttf"));


        /**
         * Play the inputted text
         */
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                (new Thread(new Runnable() {

                    @Override
                    public void run() {
                        String message = et.getEditableText().toString();
                        final String dots = mTone.convertToDots(message);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                tv.setText(dots.replace("|", "").replace(".", "•"));
                            }
                        });
                        mTone.stopTone();
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mTone.play(dots);

                    }
                })).start();
            }
        });
        if (!isNotificationServiceEnabled()) {
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }
        startService(new Intent(this, NotificationCollectorMonitorService.class));
        Intent mServiceIntent = new Intent(this, SMSMorse.class);
        startService(mServiceIntent);
    }

    /**
     * Is Notification Service Enabled.
     * Verifies if the notification listener service is enabled.
     * Got it from: https://github.com/kpbird/NotificationListenerService-Example/blob/master/NLSExample/src/main/java/com/kpbird/nlsexample/NLService.java
     *
     * @return True if eanbled, false otherwise.
     */
    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        mTone = new SMSTone(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mTone.stopTone();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem;
        menuItem = menu.findItem(R.id.action_application_info);
        if (menuItem != null) {
            menuItem.setVisible(BuildConfig.DEBUG);
        }
        menuItem = menu.findItem(R.id.action_notification_settings);
        if (menuItem != null) {
            menuItem.setVisible(BuildConfig.DEBUG && SMSMorse.supportsNotificationListenerSettings());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);
            return true;
        } else if (id == R.id.action_reference) {
            Intent i = new Intent(this, ReferenceActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);
            return true;
        } else if (id == R.id.action_application_info) {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())));
            return true;
        } else if (id == R.id.action_notification_settings) {
            startActivity(SMSMorse.getIntentNotificationListenerSettings());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     * @return An alert dialog which leads to the notification enabling screen
     */
    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                    }
                });
        return(alertDialogBuilder.create());
    }
}
