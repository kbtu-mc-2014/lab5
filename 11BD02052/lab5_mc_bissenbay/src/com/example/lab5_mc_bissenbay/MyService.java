package com.example.lab5_mc_bissenbay;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class MyService extends Service {

	private int counter = 0;

	class IncomingHandler extends Handler {
		public void handleMessage(Message msg) {

			Message m = Message.obtain(null, 0, counter, 0);
			try {
				msg.replyTo.send(m);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		final Handler handler = new Handler();

		final Runnable r = new Runnable()
		{
		    public void run() 
		    {
					counter++;
			        handler.postDelayed(this, 1000);
			    }
			};

			handler.postDelayed(r, 1000);

		return START_STICKY;
	}

	final Messenger mMessenger = new Messenger(new IncomingHandler());

	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}
	
}
