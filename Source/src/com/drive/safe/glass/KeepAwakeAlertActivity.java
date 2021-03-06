/**
 * @author Victor Kaiser-Pendergrast
 */

package com.drive.safe.glass;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ImageView;

import com.drive.safe.glass.analytics.APIKeys;
import com.flurry.android.FlurryAgent;
import com.google.android.glass.media.Sounds;

/**
 * Activity showing the options menu.
 */
public class KeepAwakeAlertActivity extends Activity {
	private static final String TAG = "MenuActivity";

	private KeepAwakeService.KeepAwakeBinder mServiceBinder;

	private ServiceConnection mServiceConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (service instanceof KeepAwakeService.KeepAwakeBinder) {
				mServiceBinder = (KeepAwakeService.KeepAwakeBinder) service;
			}

			unbindService(this);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// Do nothing
		}
	};

	// private BluetoothManager mBtManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.card_alert);

		// Doesn't seem to work
		/*
		 * mBtManager = new BluetoothManager(); if (!mBtManager.isConnected()) {
		 * // Glass is not connected to a phone, TextView text = (TextView)
		 * findViewById(R.id.card_subtitle);
		 * text.setText(getString(R.string.text_no_phone));
		 * text.setTextColor(0xFFddbb11); }
		 */

		// Make image red
		ImageView image = (ImageView) findViewById(R.id.card_image);
		image.setColorFilter(0xFFcc3333);

		// Bind the service to get access to the getDirectionsToRestArea method
		bindService(new Intent(this, KeepAwakeService.class), mServiceConn, 0);
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent event) {
		if (/* mBtManager.isConnected() && */keycode == KeyEvent.KEYCODE_DPAD_CENTER) {
			// User tapped touchpad, get directions
			if (mServiceBinder != null) {
				mServiceBinder.getDirectionsToRestArea();
			}

			// Play the click sound
			AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			audio.playSoundEffect(Sounds.TAP);

			finish();
			return true;
		}

		return super.onKeyDown(keycode, event);
	}
	
	@Override
	public void onStart(){
		super.onStart();
		
		// Analytics
		FlurryAgent.onStartSession(this, APIKeys.FLURRY_API_KEY);
		FlurryAgent.logEvent("Alert_Displayed");
	}
	
	@Override
	public void onStop(){
		super.onStop();
		
		// Analytics
		FlurryAgent.onEndSession(this);
	}
	
	
}
