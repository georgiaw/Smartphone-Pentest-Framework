package com.bulbsecurity.framework.app;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;

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
			
			ArrayList<String> messageparts = sm.divideMessage(message);
			sm.sendMultipartTextMessage(number, null, messageparts, null, null);
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
