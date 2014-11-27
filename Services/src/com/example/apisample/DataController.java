package com.example.apisample;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

public class DataController {
	interface Callback {
		public void process(Object o);
	}
	private static DataController instance;
	private Context context;
	private DataController(Context context) {
		this.context = context;
	}
	public static DataController getInstance(Context context) {
		if (instance == null) {
			instance = new DataController(context);
		}
		return instance;
	}
	public void getRadio(String stream, final Callback callback) {
//		Radio r = new Radio();
//		r.setTitle("Test");
//		r.setDescription("Description");
//		callback.process(r);

		AQuery aq = new AQuery(context);
		aq.ajax("http://kivvi.kz/api/radio/info?stream=" + stream, JSONObject.class,
				new AjaxCallback<JSONObject>() {
			@Override
            public void callback(String url, JSONObject data, AjaxStatus status) {
				try {
					Radio r = new Radio();
					r.setTitle(data.getString("title"));
					r.setDescription(data.getString("description"));
					callback.process(r);
				} catch (JSONException e) {
					e.printStackTrace();
					callback.process(null);
				}
			}
		});
	}
}
