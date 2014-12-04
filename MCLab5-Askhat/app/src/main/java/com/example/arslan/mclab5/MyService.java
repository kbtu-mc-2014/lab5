package com.example.arslan.mclab5;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
	private int counter = 0;

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {

			Message m = Message.obtain(null, 0, counter, 0);
			try {
				msg.replyTo.send(m);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
    private Timer t;
    private int TimeCounter = 0;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                        counter++;


            }
        }, 1000, 1000);

        return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

}
