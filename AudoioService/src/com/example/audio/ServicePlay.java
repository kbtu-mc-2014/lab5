package com.example.audio;

import java.io.IOException;

import com.glowingpigs.tutorialstreamaudiopart1b.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;




public class ServicePlay extends Service implements OnCompletionListener,
		OnPreparedListener, OnErrorListener, OnSeekCompleteListener,
		OnInfoListener, OnBufferingUpdateListener {

	private static final String TAG = "TELSERVICE";
	private MediaPlayer mediaPlayer = new MediaPlayer();
	private String sntAudioLink;
	
	private static final String URL_STRING = "http://www.glowingpigs.com/audioclip/";
	

	private static final int NOTIFICATION_ID = 1;
	private boolean isPausedInCall = false;
	private PhoneStateListener phoneStateListener;
	private TelephonyManager telephonyManager;

	
	String sntSeekPos;
	int intSeekPos;
	int mediaPosition;
	int mediaMax;
	
	private final Handler handler = new Handler();
	private static int songEnded;
	public static final String BROADCAST_ACTION = "com.glowingpigs.tutorialstreamaudiopart1b.seekprogress";


	public static final String BROADCAST_BUFFER = "com.glowingpigs.tutorialstreamaudiopart1b.broadcastbuffer";
	
	Intent bufferIntent;
	Intent seekIntent;

	
	private int headsetSwitch = 1;
	@Override
	public void onCreate() {
		Log.v(TAG, "Creating Service");
		
		bufferIntent = new Intent(BROADCAST_BUFFER);
		
		seekIntent = new Intent(BROADCAST_ACTION);
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnErrorListener(this);
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setOnBufferingUpdateListener(this);
		mediaPlayer.setOnSeekCompleteListener(this);
		mediaPlayer.setOnInfoListener(this);
		mediaPlayer.reset();

		
		registerReceiver(headsetReceiver, new IntentFilter(
				Intent.ACTION_HEADSET_PLUG));

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// Set up receiver for seekbar change 
		registerReceiver(broadcastReceiver, new IntentFilter(
				MainActivity.BROADCAST_SEEKBAR));

		
		Log.v(TAG, "Starting telephony");
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		Log.v(TAG, "Starting listener");
		phoneStateListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				
				Log.v(TAG, "Starting CallStateChange");
				switch (state) {
				case TelephonyManager.CALL_STATE_OFFHOOK:
				case TelephonyManager.CALL_STATE_RINGING:
					if (mediaPlayer != null) {
						pauseMedia();
						isPausedInCall = true;
					}

					break;
				case TelephonyManager.CALL_STATE_IDLE:
					// Phone idle. Start playing.
					if (mediaPlayer != null) {
						if (isPausedInCall) {
							isPausedInCall = false;
							playMedia();
						}

					}
					break;
				}

			}
		};

		// Register the listener with the telephony manager
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_CALL_STATE);

		
		initNotification();

		sntAudioLink = intent.getExtras().getString("sentAudioLink");
		mediaPlayer.reset();

		// Set up the MediaPlayer data source using the strAudioLink value
		if (!mediaPlayer.isPlaying()) {
			try {
				mediaPlayer
						.setDataSource(URL_STRING
								+ sntAudioLink);

				// Send message to Activity to display progress dialogue
				sendBufferingBroadcast();
			
				mediaPlayer.prepareAsync();

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
			}
		}
		
		setupHandler();

		return START_STICKY;
	}

	private void setupHandler() {
		handler.removeCallbacks(sendUpdatesToUI);
		handler.postDelayed(sendUpdatesToUI, 1000); // 1 second
	}

	private Runnable sendUpdatesToUI = new Runnable() {
		public void run() {
			

			LogMediaPosition();

			handler.postDelayed(this, 1000); // 2 seconds

		}
	};

	private void LogMediaPosition() {
		
		if (mediaPlayer.isPlaying()) {
			mediaPosition = mediaPlayer.getCurrentPosition();
			
			mediaMax = mediaPlayer.getDuration();
			
			seekIntent.putExtra("counter", String.valueOf(mediaPosition));
			seekIntent.putExtra("mediamax", String.valueOf(mediaMax));
			seekIntent.putExtra("song_ended", String.valueOf(songEnded));
			sendBroadcast(seekIntent);
		}
	}

	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateSeekPos(intent);
		}
	};

	// Update seek position from Activity
	public void updateSeekPos(Intent intent) {
		int seekPos = intent.getIntExtra("seekpos", 0);
		if (mediaPlayer.isPlaying()) {
			handler.removeCallbacks(sendUpdatesToUI);
			mediaPlayer.seekTo(seekPos);
			setupHandler();
		}

	}



	// If headset gets unplugged, stop music and service.
	private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
		private boolean headsetConnected = false;

		@Override
		public void onReceive(Context context, Intent intent) {
		
			if (intent.hasExtra("state")) {
				if (headsetConnected && intent.getIntExtra("state", 0) == 0) {
					headsetConnected = false;
					headsetSwitch = 0;
					
				} else if (!headsetConnected
						&& intent.getIntExtra("state", 0) == 1) {
					headsetConnected = true;
					headsetSwitch = 1;
					
				}

			}

			switch (headsetSwitch) {
			case (0):
				headsetDisconnected();
				break;
			case (1):
				break;
			}
		}

	};

	private void headsetDisconnected() {
		stopMedia();
		stopSelf();

	}

	
	// onDestroy, stop media player and release.  Also stop phoneStateListener, notification, receivers...
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mediaPlayer != null) {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.stop();
			}
			mediaPlayer.release();
		}

		if (phoneStateListener != null) {
			telephonyManager.listen(phoneStateListener,
					PhoneStateListener.LISTEN_NONE);
		}

		// Cancel the notification
		cancelNotification();

		
		unregisterReceiver(headsetReceiver); // Unregister headsetReceiver

		
		unregisterReceiver(broadcastReceiver);// Unregister seekbar receiver

		
		handler.removeCallbacks(sendUpdatesToUI);// Stop the seekbar handler from sending updates to UI

		
		resetButtonPlayStopBroadcast();// Service ends, need to tell activity to display "Play" button
	}

	
	private void sendBufferingBroadcast() {
		
		bufferIntent.putExtra("buffering", "1");
		sendBroadcast(bufferIntent);
	}

	
	private void sendBufferCompleteBroadcast() {
		
		bufferIntent.putExtra("buffering", "0");
		sendBroadcast(bufferIntent);
	}

	
	private void resetButtonPlayStopBroadcast() {
		// Log.v(TAG, "BufferCompleteSent");
		bufferIntent.putExtra("buffering", "2");
		sendBroadcast(bufferIntent);
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		
		if (!mediaPlayer.isPlaying()){
			playMedia();
			Toast.makeText(this,
					"SeekComplete", Toast.LENGTH_SHORT).show();
		}
		
	}

	
	//---Error processing ---
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		switch (what) {
		case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
			Toast.makeText(this,
					"MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra,
					Toast.LENGTH_SHORT).show();
			break;
		case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
			Toast.makeText(this, "MEDIA ERROR SERVER DIED " + extra,
					Toast.LENGTH_SHORT).show();
			break;
		case MediaPlayer.MEDIA_ERROR_UNKNOWN:
			Toast.makeText(this, "MEDIA ERROR UNKNOWN " + extra,
					Toast.LENGTH_SHORT).show();
			break;
		}
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {

		// Send a message to activity to end progress dialogue

		sendBufferCompleteBroadcast();
		playMedia();

	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// When song ends, need to tell activity to display "Play" button
		stopMedia();
		stopSelf();

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void playMedia() {
		if (!mediaPlayer.isPlaying()) {
			mediaPlayer.start();
		}
	}

	// Add for Telephony Manager
	public void pauseMedia() {
		// Log.v(TAG, "Pause Media");
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
		}

	}

	public void stopMedia() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
		}
	}

	// Create Notification
	private void initNotification() {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		int icon = R.drawable.icon;
		CharSequence tickerText = "Tutorial: Music In Service";
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		Context context = getApplicationContext();
		CharSequence contentTitle = "Music In Service App Tutorial";
		CharSequence contentText = "Listen To Music While Performing Other Tasks";
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}

	// Cancel Notification
	private void cancelNotification() {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		mNotificationManager.cancel(NOTIFICATION_ID);
	}
}
