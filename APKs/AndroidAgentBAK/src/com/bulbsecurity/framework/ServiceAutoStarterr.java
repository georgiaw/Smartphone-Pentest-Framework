package com.bulbsecurity.framework;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ServiceAutoStarterr extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		Log.i("AAA", "Starter Started");
		AlarmManager alarm = (AlarmManager) arg0.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(arg0, InternetPoller.class);
		PendingIntent pending = PendingIntent.getService(arg0, 0, intent, 0);
		Calendar time = Calendar.getInstance();
		time.set(Calendar.MINUTE, 0);
		time.set(Calendar.SECOND, 0);
		time.set(Calendar.MILLISECOND, 0);
		alarm.setRepeating(AlarmManager.RTC, time.getTime().getTime(), 30 * 1000, pending);
	}

}
