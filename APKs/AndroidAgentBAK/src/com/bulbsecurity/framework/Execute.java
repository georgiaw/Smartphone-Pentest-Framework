package com.bulbsecurity.framework;

import jackpal.androidterm.Exec;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
public class Execute extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onStart(Intent intent, int startID) {
		 Log.i("AAA", "started execute");
		 final String downloaded = intent.getStringExtra("downloaded");
		 final String getcommand = intent.getStringExtra("command");
		 new Thread() {
				public void run() {
					try{
		    		if (downloaded.equals("yes"))
		    		{
		    			 Log.i("AAA", "downloaded");
		    			String command = getFilesDir() + "/" + getcommand + "\n";
					 Log.i("AAA", command);
		    			Process process = Runtime.getRuntime().exec(command);
						BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
  					String line;  
					OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("commandoutput.txt", getApplicationContext().MODE_PRIVATE));
					while ((line = input.readLine()) != null) {
        					outputStreamWriter.write(line.concat("\n"));
    					}
  
  input.close();
        outputStreamWriter.close();
    

		    		}
		    		else
		    		{
		    			 Log.i("AAA", "not downloaded");
		    			String command = getcommand + "\n";
					  Log.i("AAA", command);
		    			Process process = Runtime.getRuntime().exec(command);
					BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
  					String line;  
					OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("commandoutput.txt", getApplicationContext().MODE_PRIVATE));
					while ((line = input.readLine()) != null) {
        					outputStreamWriter.write(line.concat("\n"));
    					}
  
  input.close();
        outputStreamWriter.close();
    }
		    		

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		}.start();
	}
}
