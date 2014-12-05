package com.example.lab5_mc_bissenbay;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private Messenger myService;
	private boolean mBound;
	private MyHandler h;
	private static int x;
	private Button buttonShow;

	static class MyHandler extends Handler {
		public void handleMessage(Message msg) {
			x = msg.arg1;
		}
	};

	final Messenger mMessenger = new Messenger(new MyHandler());

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		h = new MyHandler();
		buttonShow = (Button)findViewById(R.id.btnShow);
		buttonShow.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Message msg = Message.obtain(null, 0, 0, 0);
				msg.replyTo = mMessenger;
				try {
					myService.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				Toast.makeText(getApplicationContext(), "Count = "+x, Toast.LENGTH_SHORT).show();
			}
		});
	}

	protected void onResume() {
		super.onResume();
		Intent intent = new Intent(this, MyService.class);
		startService(intent);
		Intent intent2 = new Intent(this, MyService.class);
		bindService(intent2, mConnection, Context.BIND_AUTO_CREATE);
	}

	protected void onPause() {
		super.onPause();
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			myService = new Messenger(service);
			mBound = true;
		}
		
		public void onServiceDisconnected(ComponentName arg0) {
			myService = null;
			mBound = false;
		}
	};
}
