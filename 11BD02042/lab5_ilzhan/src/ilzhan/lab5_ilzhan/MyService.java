package ilzhan.lab5_ilzhan;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class MyService extends Service {
	private int x = 0;

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {

			Message m = Message.obtain(null, 0, x, 0);
			try {
				msg.replyTo.send(m);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	final Messenger mMessenger = new Messenger(new IncomingHandler());

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					x++;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		}).start();

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

}
