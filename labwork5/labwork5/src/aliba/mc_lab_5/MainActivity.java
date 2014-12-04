package aliba.mc_lab_5;

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

	Messenger service1;
	boolean mBound;

	private Button submitButton;
	TextView textField;
	MyHandler h;
	static int count;

	static class MyHandler extends Handler {
		public void handleMessage(Message msg) {
			count = msg.arg1;
		}
	};

	final Messenger mMessenger = new Messenger(new MyHandler());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		h = new MyHandler();

		submitButton = (Button) findViewById(R.id.nextButton);
		textField = (TextView) findViewById(R.id.currentTV);

		submitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Message msg = Message.obtain(null, 0, 0, 0);
				msg.replyTo = mMessenger;
				try {
					service1.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
					
				}
				textField.setText("Current: "+ count);
				
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		Intent intent = new Intent(this, Service1.class);
		startService(intent);

		Intent intent2 = new Intent(this, Service1.class);
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
			service1 = new Messenger(service);
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			service1 = null;
			mBound = false;
		}
	};
}