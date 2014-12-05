package kz.aslan.lw_5;

import android.app.Activity;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	Messenger myService;
	boolean mBound;

	private Button submitButton;
	TextView textField;
	MyHandler h;
	static int x;

	static class MyHandler extends Handler {
		// @Override
		public void handleMessage(Message msg) {
			x = msg.arg1;
		}
	};

	final Messenger mMessenger = new Messenger(new MyHandler());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		h = new MyHandler();

		submitButton = (Button) findViewById(R.id.btn);
		textField = (TextView) findViewById(R.id.tv);

		submitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Message msg = Message.obtain(null, 0, 0, 0);
				msg.replyTo = mMessenger;
				try {
					myService.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				String s = textField.getText().toString();
				s+="\nX = "+x;;
				textField.setText(s);
				
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		Intent intent = new Intent(this, MyService.class);
		startService(intent);

		Intent intent2 = new Intent(this, MyService.class);
		bindService(intent2, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			myService = new Messenger(service);
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			myService = null;
			mBound = false;
		}
	};
}