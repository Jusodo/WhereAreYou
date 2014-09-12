package de.ala.meetme;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class UploadService extends IntentService {

	private static final String LOGGER = UploadService.class.getSimpleName();

	// path to the json file
	private final String URL = "";

	private HttpClient mHttpClient = new DefaultHttpClient();
	private HttpPost mHttpPost = new HttpPost(URL);

	/**
	 * Default constructor.
	 * 
	 * @param name
	 */
	public UploadService() {
		super("UploadService");
		// nothing to do
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Log.i(LOGGER, "UploadService created.");

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.i(LOGGER, "UploadService destroyed.");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle bundle = intent.getExtras();
		double latitude = bundle.getDouble("latitude");
		double longitude = bundle.getDouble("longitude");
		
		JSONObject jsonObj = writeToJSONObject(latitude, longitude);
		
		try {
			StringEntity entity = new StringEntity(jsonObj.toString());
			Log.d(LOGGER, jsonObj.toString());
			
			entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json"));
			mHttpPost.setEntity(entity);

			// Execute HTTP Post Request
			HttpResponse response = mHttpClient.execute(mHttpPost);

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private JSONObject writeToJSONObject(double latitude, double longitude) {
		JSONObject json = new JSONObject();
		try {
			json.put("latitude", latitude);
			json.put("longitude", longitude);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}

}
