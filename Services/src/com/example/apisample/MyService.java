package com.example.apisample;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class MyService extends Service {
	public static final int MSG_INC = 0;
	public static final int MSG_DEC = 1;
	
	private int counter;
	
	class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	Log.e("KBTU", "service");
            switch (msg.what) {
                case MSG_INC: {
//                    Toast.makeText(getApplicationContext(), "" + counter++, Toast.LENGTH_SHORT).show();
    				Message m = Message.obtain(null, 0, counter++, 0);
					try {
						msg.replyTo.send(m);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    break;
                }
                case MSG_DEC: {
    				Message m = Message.obtain(null, 0, counter--, 0);
					try {
						msg.replyTo.send(m);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    break;
                }
                default:
                    super.handleMessage(msg);
            }
        }
    }
	
	final Messenger mMessenger = new Messenger(new IncomingHandler());

//	private final IBinder mBinder = new LocalBinder();
//	
//	public class LocalBinder extends Binder {
//		MyService getService() {
//            // Return this instance of LocalService so clients can call public methods
//            return MyService.this;
//        }
//    }
	
	 @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
	      Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

	      return START_STICKY;
	  }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mMessenger.getBinder();
	}
	
	public int getNext() {
		return counter++;
	}
}
