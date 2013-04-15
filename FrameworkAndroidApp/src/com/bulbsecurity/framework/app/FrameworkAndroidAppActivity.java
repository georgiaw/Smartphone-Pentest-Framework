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



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FrameworkAndroidAppActivity extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
	 EditText edittext1 = null;
     EditText edittext2 = null;
     EditText edittext3 = null;
     String connected = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
          edittext1 = (EditText) findViewById(R.id.EditText01);
          edittext2 = (EditText) findViewById(R.id.EditText02);
          edittext3 = (EditText) findViewById(R.id.EditText03);
        Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(this);
        int off = ((FrameworkAndroidApp)this.getApplication()).getstop();
        if (off == 1)
        {
        	button.setText("Attach");
        }
        else if (off == 0)
        {
        	button.setText("Dettach");
        }
    }
    @Override
	public void onClick(View v) {
    	 final Button button = (Button) findViewById(R.id.button1);
    	int off = ((FrameworkAndroidApp)this.getApplication()).getstop();
    	 if (off == 1)
    	 {
    		  final String controlIP = edittext1.getText().toString();
   	       final String path = edittext2.getText().toString();
   	       final String key = edittext3.getText().toString();
    		 new Thread() {
 				public void run() {
        		 
        	       String connectstring = key.concat(" CONNECT");
        	       ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
   				nameValuePairs.add(new BasicNameValuePair("text",connectstring));
   				try{
   					HttpClient httpclient = new DefaultHttpClient();
   					String http = "http://";
   					String url = "/connectuploader.php";
   					String uri = http.concat(controlIP);
   					String postto1 = uri.concat(path);
   					String postto = postto1.concat(url);
   					HttpPost httppost = new HttpPost(postto);
   					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
   					httpclient.execute(httppost);
   					String url2 = "/connect";
   					String pullfrom = postto1.concat(url2);
   					URL url3 = new URL(pullfrom);
   					for (int i = 0; i<100; i++)
   					{
   					BufferedReader in = new BufferedReader(new InputStreamReader(url3.openStream()));
   					if (in != null)
   					{
   						Log.i("AAA", "Stream opened");
   						String inputLine;
   						while ((inputLine = in.readLine()) != null){
   							Log.i("AAA", inputLine);
   							String connectedstring = key.concat(" CONNECTED");
   							if (inputLine.equals(connectedstring))
   							{
   								Log.i("BBB","CONNECTED");

   								i=100;
   								connectee(controlIP, key, path);
   								runOnUiThread(new Runnable(){
   								    @Override
   								    public void run(){
   								        // change UI elements here
   								    
   		   								button.setText("Dettach");
   								    }
   								});
   								
   	   							Intent intent = new Intent(getApplicationContext(),ServiceAutoStarter.class);
   	   					      sendBroadcast(intent);
   	   					      Intent intent2 = new Intent(getApplicationContext(),FunctionListActivity.class);
   	   					      startActivity(intent2);
   							}
   						}
   					}
   					}
   				}
   					catch (Exception e)
   	        		{
   	        			e.printStackTrace();
   	        		}

   					};
   							}.start();
   							
   							}

   				

    	  else if (off == 0)
    	  {
    		  button.setText("Attach");
    		  ((FrameworkAndroidApp)this.getApplication()).setstop(1);
    	  }
        	
    	 
    	 
        }
    
    private void connectee(String controlIP, String key, String path){
    	((FrameworkAndroidApp)this.getApplication()).setcontrolIP(controlIP);
				((FrameworkAndroidApp)this.getApplication()).setkey(key);
				((FrameworkAndroidApp)this.getApplication()).setpath(path);
				((FrameworkAndroidApp)this.getApplication()).setstop(0);
    }
      

}