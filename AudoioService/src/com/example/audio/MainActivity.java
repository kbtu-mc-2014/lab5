package com.example.audio;

import com.glowingpigs.tutorialstreamaudiopart1b.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;




public class MainActivity extends Activity implements OnSeekBarChangeListener {
	Intent serviceIntent;
	private Button buttonPlayStop;

	String strAudioLink = "10.mp3";
	
	private boolean isOnline;
	private boolean boolMusicPlaying = false;
	TelephonyManager telephonyManager;
	PhoneStateListener listener;

	
	private SeekBar seekBar;
	private int seekMax;
	private static int songEnded = 0;
	boolean mBroadcastIsRegistered;

	
	public static final String BROADCAST_SEEKBAR = "com.example.audio.sendseekbar";
	Intent intent;

	
	boolean mBufferBroadcastIsRegistered;
	private ProgressDialog pdBuff = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		try {
			serviceIntent = new Intent(this, ServicePlay.class);

			// set up seekbar intent for broadcasting new position to service 
			intent = new Intent(BROADCAST_SEEKBAR);

			initViews();
			setListeners();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(),
					e.getClass().getName() + " " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	// Broadcast Receiver to update position of seekbar from service 
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent serviceIntent) {
			updateUI(serviceIntent);
		}
	};

	private void updateUI(Intent serviceIntent) {
		String counter = serviceIntent.getStringExtra("counter");
		String mediamax = serviceIntent.getStringExtra("mediamax");
		String strSongEnded = serviceIntent.getStringExtra("song_ended");
		int seekProgress = Integer.parseInt(counter);
		seekMax = Integer.parseInt(mediamax);
		songEnded = Integer.parseInt(strSongEnded);
		seekBar.setMax(seekMax);
		seekBar.setProgress(seekProgress);
		if (songEnded == 1) {
			buttonPlayStop.setBackgroundResource(R.drawable.playbuttonsm);
		}
	}

	// End of seekbar update code


	private void initViews() {
		buttonPlayStop = (Button) findViewById(R.id.ButtonPlayStop);
		buttonPlayStop.setBackgroundResource(R.drawable.playbuttonsm);


		seekBar = (SeekBar) findViewById(R.id.SeekBar01);
	}

	// Set up listeners 
	private void setListeners() {
		buttonPlayStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				buttonPlayStopClick();
			}
		});
        seekBar.setOnSeekBarChangeListener(this);
		
	}
		
	// invoked from ButtonPlayStop listener above 
	private void buttonPlayStopClick() {
		if (!boolMusicPlaying) {
			buttonPlayStop.setBackgroundResource(R.drawable.pausebuttonsm);
			playAudio();
			boolMusicPlaying = true;
		} else {
			if (boolMusicPlaying) {
				buttonPlayStop.setBackgroundResource(R.drawable.playbuttonsm);
				stopMyPlayService();
				boolMusicPlaying = false;
			}
		}
	}


	private void stopMyPlayService() {
	
		if (mBroadcastIsRegistered) {
			try {
				unregisterReceiver(broadcastReceiver);
				mBroadcastIsRegistered = false;
			} catch (Exception e) {
				

				e.printStackTrace();
				Toast.makeText(

				getApplicationContext(),

				e.getClass().getName() + " " + e.getMessage(),

				Toast.LENGTH_LONG).show();
			}
		}

		try {
			stopService(serviceIntent);

		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(),
					e.getClass().getName() + " " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
		boolMusicPlaying = false;
	}

	// start music
	private void playAudio() {

		checkConnectivity();
		if (isOnline) {
			stopMyPlayService();

			serviceIntent.putExtra("sentAudioLink", strAudioLink);

			try {
				startService(serviceIntent);
			} catch (Exception e) {

				e.printStackTrace();
				Toast.makeText(

				getApplicationContext(),

				e.getClass().getName() + " " + e.getMessage(),

				Toast.LENGTH_LONG).show();
			}

			
			registerReceiver(broadcastReceiver, new IntentFilter(
					ServicePlay.BROADCAST_ACTION));
			;
			mBroadcastIsRegistered = true;

		} else {
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("Network Not Connected...");
			alertDialog.setMessage("Please connect to a network and try again");
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
			alertDialog.setIcon(R.drawable.icon);
			buttonPlayStop.setBackgroundResource(R.drawable.playbuttonsm);
			alertDialog.show();
		}
	}

	// Handle progress dialogue for buffering.
	private void showPD(Intent bufferIntent) {
		String bufferValue = bufferIntent.getStringExtra("buffering");
		int bufferIntValue = Integer.parseInt(bufferValue);

		

		switch (bufferIntValue) {
		case 0:
			
			if (pdBuff != null) {
				pdBuff.dismiss();
			}
			break;

		case 1:
			BufferDialogue();
			break;

	
		case 2:
			buttonPlayStop.setBackgroundResource(R.drawable.playbuttonsm);
			break;

		}
	}

	// Progress dialogue
	private void BufferDialogue() {

		pdBuff = ProgressDialog.show(MainActivity.this, "Buffering...",
				"Acquiring song...", true);
	}

	
	private BroadcastReceiver broadcastBufferReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent bufferIntent) {
			showPD(bufferIntent);
		}
	};

	private void checkConnectivity() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
				.isConnectedOrConnecting()
				|| cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
						.isConnectedOrConnecting())
			isOnline = true;
		else
			isOnline = false;
	}

	// onPause, unregister broadcast receiver. To improve, also save screen data
	@Override
	protected void onPause() {
		// Unregister broadcast receiver
		if (mBufferBroadcastIsRegistered) {
			unregisterReceiver(broadcastBufferReceiver);
			mBufferBroadcastIsRegistered = false;
		}
		super.onPause();
	}

	
	@Override
	protected void onResume() {
		// Register broadcast receiver
		if (!mBufferBroadcastIsRegistered) {
			registerReceiver(broadcastBufferReceiver, new IntentFilter(
					ServicePlay.BROADCAST_BUFFER));
			mBufferBroadcastIsRegistered = true;
		}
		super.onResume();
	}

	

	@Override
	public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
	
		 if (fromUser) {
			 int seekPos = sb.getProgress();
				intent.putExtra("seekpos", seekPos);
				sendBroadcast(intent);
		 }
	}

	
 // The following two methods are alternatives to track seekbar if moved. 	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

		
	}

}