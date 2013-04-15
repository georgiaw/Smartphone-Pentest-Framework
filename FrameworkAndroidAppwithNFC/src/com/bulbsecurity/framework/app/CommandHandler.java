package com.bulbsecurity.framework.app;



import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.bulbsecurity.framework.app2.R;

public class CommandHandler extends Service {
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStart(Intent intent, int startID) {
		String body = intent.getStringExtra("message");
		String send = "SEND";
		String nfc = "NFCC";
		
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
			else if (checkfunction.equals(nfc))
			{
				Log.i("AAA", "NFC");
				String aString[] = body.split(" ");
				if (aString.length >= 3)
				{
					String url = aString[2];
		
					Intent intent2 = new Intent(getApplicationContext(),NFCSend.class);
					intent2.putExtra("url",url);
					intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					Context context = getApplicationContext();
					context.startActivity(intent2);
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
