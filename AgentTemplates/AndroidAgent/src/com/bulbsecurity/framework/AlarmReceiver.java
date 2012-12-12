package com.bulbsecurity.framework;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try {

			Intent intent2 = new Intent(context, Phase2.class);
			context.startService(intent2);
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

}
