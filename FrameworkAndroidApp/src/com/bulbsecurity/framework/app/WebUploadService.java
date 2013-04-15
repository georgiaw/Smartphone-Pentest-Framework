package com.bulbsecurity.framework.app;


import java.io.ByteArrayOutputStream;

import java.util.ArrayList;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;



public class WebUploadService extends Service {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStart(Intent intent, int startID) {
		final String uploadstring = intent.getStringExtra("uploadstring");
		final String controlIP = ((FrameworkAndroidApp) this.getApplication()).getcontrolIP();
		final String path = ((FrameworkAndroidApp) this.getApplication()).getpath();
		new Thread() {
				public void run() {
		
		if (uploadstring.equals("picture taken"))
		{
			
			Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/photo.jpg");
			ByteArrayOutputStream output = new ByteArrayOutputStream();
		
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);

			byte [] bytearray = output.toByteArray();

			String base =Base64.encodeBytes(bytearray);

			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

			nameValuePairs.add(new BasicNameValuePair("picture",base));
			try{

				HttpClient httpclient = new DefaultHttpClient();
				String http = "http://";
				
				String url = "/pictureupload.php";
				String uri = http.concat(controlIP);
				String postto1 = uri.concat(path);
				String postto = postto1.concat(url);
				HttpPost httppost = new HttpPost(postto);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				httpclient.execute(httppost);
				//HttpEntity entity = response.getEntity();
				//InputStream input = entity.getContent();
				}catch(Exception e){

				e.printStackTrace();

				}

		}
		else {
			
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("text", uploadstring));
			try{
				HttpClient httpclient = new DefaultHttpClient();
				String http = "http://";
				String url = "/textuploader.php";
				String uri = http.concat(controlIP);
				String postto1 = uri.concat(path);
				String postto = postto1.concat(url);
				HttpPost httppost = new HttpPost(postto);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				httpclient.execute(httppost);
				//HttpEntity entity = response.getEntity();
				//InputStream input = entity.getContent();
				}catch(Exception e){

				e.printStackTrace();

				}
			}	};
			}.start();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
