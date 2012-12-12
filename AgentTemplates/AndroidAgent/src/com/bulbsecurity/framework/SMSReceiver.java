package com.bulbsecurity.framework;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;


public class SMSReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		Bundle bundle = arg1.getExtras();
		SmsMessage[] msgs = null;
		//String key = "#!$A*&?";
		//String key = "KEYKEY1";
		//String controlnumber = "15555215554";
		String key = arg0.getResources().getString(R.string.key);
		String controlnumber = arg0.getResources().getString(R.string.controlnumber);
		//String controlnumber = "+16013831619";
		if (bundle != null)
		{
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			for (int i=0; i < msgs.length; i++)
			{
				msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
				String fromnumber = msgs[i].getOriginatingAddress();
				if (fromnumber.equals(controlnumber))
				{
					
					String body = msgs[i].getMessageBody().toString();
					if (body.length() >= 7)
					{
						String checkkey = body.substring(0,7);
						if (checkkey.equals(key))
						{
							this.abortBroadcast();
							Intent intent = new Intent(arg0, CommandHandler.class);
							intent.putExtra("message",body);
							arg0.startService(intent);
							
						}
					
					}
				
				}
			}
		}

	}
}