package com.bulbsecurity.framework;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

public class SMSService extends Service {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStart(Intent intent, int startID) {
		// TODO Auto-generated method stub
		try {
	
		String number = intent.getStringExtra("number");
		String message = intent.getStringExtra("message");
		
		SmsManager sm = SmsManager.getDefault();
		if (message.length() > 160)
		{
			
			int interval = 152;
			
			 int arrayLength = (int) Math.ceil(((message.length() / (double)interval)));
			    String[] result = new String[arrayLength];

			    int j = 0;
			    int lastIndex = result.length - 1;
			    for (int i = 0; i < lastIndex; i++) {
			        result[i] = message.substring(j, j + interval);
			        j += interval;
			    } //Add the last bit
			    result[lastIndex] = message.substring(j);

			for (int i = 0; i<=lastIndex; i++)
			{
				String key = getApplicationContext().getResources().getString(R.string.key);
				String sender = key.concat(" ").concat(result[i]);
				Log.i("AAAA", sender);
				sm.sendTextMessage(number, null, sender, null, null);	
			}
		}
		else {
			
		
		sm.sendTextMessage(number, null, message, null, null);	
		}
		
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	


	

}

