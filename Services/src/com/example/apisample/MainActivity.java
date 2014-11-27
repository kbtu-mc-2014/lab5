package com.example.apisample;

import java.util.HashMap;
import java.util.Map;

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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.example.apisample.MyService.IncomingHandler;


public class MainActivity extends ActionBarActivity {
	private static final int CACHE_SIZE = 5;
	
	Messenger myService;
	boolean mBound;
	
	private Button submitButton;
	private EditText textEdit;
	private TextView textView;
	private AQuery aq;
	int cnt;
	Map<Integer, Integer> cache = new HashMap<Integer, Integer>();
	Map<Integer, Long> timestamps = new HashMap<Integer, Long>();
	
	MyHandler h;
	
	static class MyHandler extends Handler {
//		public MyHandler(Looper looper) {
//	          super(looper);
//	      }
	      @Override
	      public void handleMessage(Message msg) {
	        	Log.e("KBTU", "activity");
	    	  Log.e("KBTU", "new message " + msg.arg1);
	      }
	};
	
	final Messenger mMessenger = new Messenger(new MyHandler());
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        h = new MyHandler();
        
        submitButton = (Button)findViewById(R.id.submit);
	        textEdit = (EditText)findViewById(R.id.stream);
	        textView = (TextView)findViewById(R.id.text);
	        
        submitButton.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				Message msg = Message.obtain(null, MyService.MSG_DEC, 0, 0);
				msg.replyTo = mMessenger;
		        try {
		            myService.send(msg);
		        } catch (RemoteException e) {
		            e.printStackTrace();
		        }
				return true;
			}
        	
        });
        submitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Message msg = Message.obtain(null, MyService.MSG_INC, 0, 0);
				msg.replyTo = mMessenger;
		        try {
		            myService.send(msg);
		        } catch (RemoteException e) {
		            e.printStackTrace();
		        }
				
//				if (mBound) {
//					Toast.makeText(MainActivity.this, "" + myService.getNext(), Toast.LENGTH_SHORT).show();
//				} else {
//					Toast.makeText(MainActivity.this, "Not bound yet", Toast.LENGTH_SHORT).show();
//				}
/*				textView.setText("");
				final int n = Integer.parseInt(textEdit.getText().toString());
				if (cache.containsKey(n)) {
					Log.e("KBTU", "from cache " + n);
					textView.setText(cache.get(n) + "");
					timestamps.put(n, new Date().getTime());
				} else {
					Log.e("KBTU", "calculating " + n);
					new Thread(new Runnable() {
						@Override
						public void run() {
							int cnt = 0;
							for (int i = 2; i < n; ++i) {
								boolean prime = true;
								for (int j = 2; j < i; ++j) {
									if (i % j == 0) {
										prime = false;
										break;
									}
								}
								if (prime) cnt++;
							}
							if (cache.size() == CACHE_SIZE) {
								int oldestK = 0;
								long oldestTS = new Date().getTime() + 10;
								for (Integer k: cache.keySet()) {
									long v = timestamps.get(k);
									if (v < oldestTS) {
										oldestTS = v;
										oldestK = k;
									}
								}
								cache.remove(oldestK);
								timestamps.remove(oldestTS);
							}
							cache.put(n, cnt);
							timestamps.put(n, new Date().getTime());
							final int result = cnt;
							
							Message msg = new Message();
							msg.arg1 = 123;
							h.sendMessage(msg);
							h.post(new Runnable() {
								@Override
								public void run() {
									textView.setText(result + "");
								}
							});
							h.postDelayed(new Runnable() {
								@Override
								public void run() {
									textView.setText("");
								}
							}, 5000);
						}
					}).start();*/
/*					new AsyncTask<Integer, Integer, Integer>() {
	
						@Override
						protected Integer doInBackground(Integer... params) {
							int cnt = 0;
							for (int i = 2; i < n; ++i) {
								boolean prime = true;
								for (int j = 2; j < i; ++j) {
									if (i % j == 0) {
										prime = false;
										break;
									}
								}
								if (prime) cnt++;
							}
							return cnt;
						}
						
						protected void onPostExecute(Integer result) {
							if (cache.size() == CACHE_SIZE) {
								int oldestK = 0;
								long oldestTS = new Date().getTime() + 10;
								for (Integer k: cache.keySet()) {
									long v = timestamps.get(k);
									if (v < oldestTS) {
										oldestTS = v;
										oldestK = k;
									}
								}
								cache.remove(oldestK);
								timestamps.remove(oldestTS);
							}
							cache.put(n, result);
							timestamps.put(n, new Date().getTime());
							textView.setText(result + "");
						}
						
					}.execute(0);*/
//				}
				
/*				new Thread() {
					public void run() {
						textView.post(new Runnable() {
							public void run() {
							}
						});
					}
				}.start();*/
			}
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        Intent intent = new Intent(this, MyService.class);
        startService(intent);        
        // Bind to LocalService
        
        Intent intent2 = new Intent(this, MyService.class);
        bindService(intent2, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
        	Log.e("KBTU", "service connected");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
//            LocalBinder binder = (LocalBinder) service;
//            myService = binder.getService();
        	myService = new Messenger(service);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        	Log.e("KBTU", "service disconnected");
        	myService = null;
            mBound = false;
        }
    };
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
