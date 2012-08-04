package com.bulbsecurity.framework;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Environment;
import android.os.IBinder;
import android.view.SurfaceView;

public class PictureService extends Service {
	Camera c;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStart(Intent intent, int startID) {
		Context context = getApplicationContext();
		try {
		 c = Camera.open();
		} catch (RuntimeException e){
			e.printStackTrace();
			Intent intent3 = new Intent(getApplicationContext(), WebUploadService.class);
			intent3.putExtra("uploadstring", "Camera open error");
			context.startService(intent3);
			return;
				
			}
		
		
		//Camera.Parameters params = c.getParameters();
		SurfaceView view = new SurfaceView(this);
		try {
			c.setPreviewDisplay(view.getHolder());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Intent intent3 = new Intent(getApplicationContext(), WebUploadService.class);
			intent3.putExtra("uploadstring", "Camera preview error");
			context.startService(intent3);
			return;
		}
		try { 
		c.startPreview();
		}
		catch (RuntimeException e) {
			e.printStackTrace();
			Intent intent3 = new Intent(getApplicationContext(), WebUploadService.class);
			intent3.putExtra("uploadstring", "Takepicture error");
			context.startService(intent3);
			return;
		}
		Camera.PictureCallback jpegPictureCallback;
		jpegPictureCallback = new Camera.PictureCallback() {
			
			public void onPictureTaken(byte[] data, Camera camera) {
				// TODO Auto-generated method stub
				c.stopPreview();
				c.release();
				Context context = getApplicationContext();	
				File photo = new File(Environment.getExternalStorageDirectory(), "photo.jpg");
				if (photo.exists())
				{
				 photo.delete();
				}
				
				try {
					FileOutputStream fos=new FileOutputStream(photo.getPath());
					fos.write(data);
					fos.close();
				}
				catch (IOException e) {
					e.printStackTrace();
					Intent intent3 = new Intent(getApplicationContext(), WebUploadService.class);
					intent3.putExtra("uploadstring", "Savepicture error");
					context.startService(intent3);
					return;
					
				}
				
					Intent intent3 = new Intent(getApplicationContext(), WebUploadService.class);
					intent3.putExtra("uploadstring", "picture taken");
					context.startService(intent3);
					return;
				
				
			}
		};
		try {
		c.takePicture(null, null, jpegPictureCallback);
		} catch (RuntimeException e) {
				e.printStackTrace();
				Intent intent3 = new Intent(getApplicationContext(), WebUploadService.class);
				intent3.putExtra("uploadstring", "take picture failed");
				context.startService(intent3);
				return;
			
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
