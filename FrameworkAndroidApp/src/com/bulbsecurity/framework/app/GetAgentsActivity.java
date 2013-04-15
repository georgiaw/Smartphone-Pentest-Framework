package com.bulbsecurity.framework.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


public class GetAgentsActivity extends Activity implements AdapterView.OnItemSelectedListener, OnClickListener{
	 private Spinner spinner1;
	  private Button btnSubmit;
	  private String next;
	  
	  @Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Bundle extras = getIntent().getExtras();
		next = extras.getString("next");
		setContentView(R.layout.getagents);
		btnSubmit = (Button) findViewById(R.id.btnSubmit);
		btnSubmit.setEnabled(false);
		btnSubmit.setClickable(false);
		btnSubmit.setOnClickListener(this); 
		spinner1 = (Spinner) findViewById(R.id.spinner1);
		spinner1.setOnItemSelectedListener(this);
		
		String key = ((FrameworkAndroidApp)this.getApplication()).getkey();
		String sendstr = key.concat(" AGENTS DUMMY");
		Intent intent = new Intent(getApplicationContext(),WebUploadService2.class);
	    intent.putExtra("uploadstring", sendstr);
	    startService(intent);
	    Handler handler = new Handler(); 
	    handler.postDelayed(new Runnable() { 
	         public void run() { 
	        	    String http = "http://";
	     	  	String controlIP = ((FrameworkAndroidApp) getApplication()).getcontrolIP();
	     	  	String path = ((FrameworkAndroidApp) getApplication()).getpath(); 
	     	  	String key = ((FrameworkAndroidApp) getApplication()).getkey();
	     	  	String urii = "/text2.txt";
	     	  	String uri = http.concat(controlIP);
	     	  	String uri2 = uri.concat(path);
	     	  	String pullfrom = uri2.concat(urii);
	     	  	URL url = null;
	     	  	try {
	     	  	url = new URL(pullfrom);
	     	  	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	     	  	if (in != null)
	     	  	{
	     	  		String inputLine;
	     	  	if  ((inputLine = in.readLine()) != null)
	  				{
	  					if (inputLine.length() >= 7)
	  					{
	  						String checkkey = inputLine.substring(0,7);
	  						if (checkkey.equals(key))
	  						{
	  					
	  							if ((inputLine.length() >= 12))
	  							{
	  								String checkfun = inputLine.substring(8,12);
	  								if (checkfun.equals("AGEN"))
	  								{
	  								List<String> list = new ArrayList<String>();
	  				        			list.add("Select An Agent");
	  						        
	  						        	while  ((inputLine = in.readLine()) != null)
	  						        	{
	  						        	
	  						        		list.add(inputLine);
	  						        	}
	  						        	ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getApplicationContext(),
	  				        					android.R.layout.simple_spinner_item, list);
	  				        				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	  				        				spinner1.setAdapter(dataAdapter);
	  				        				spinner1.setEnabled(true);
	  								}
	  								else if (checkfun.equals("NONE"))
	  								{
	  									Toast.makeText(getApplicationContext(), "No Agents Found", Toast.LENGTH_SHORT).show();
	  							        finish();
	  								}
	     	  	}}}}}
	     	  	} catch (Exception e) {
	     	  		e.printStackTrace();
	     	  	}
	         }
	    }, 5000); 
	
	  		
	}
	  @Override
      public void onItemSelected(AdapterView<?> parent, 
        View view, int pos, long id) 
      {
		  String sel = parent.getItemAtPosition(pos).toString();
		 Button button = (Button) findViewById(R.id.btnSubmit);
		  if (sel.equals("Agents:"))
  		{
			  button.setEnabled(false);
				button.setClickable(false);
  		}
		  else
		  {
			  button.setEnabled(true);
				button.setClickable(true);  
		  }
      }
      @Override
      public void onNothingSelected(AdapterView<?> parent) {
          // TODO Auto-generated method stub

      }
      @Override
	    public void onClick(View v) {
    		Spinner type1 = (Spinner) findViewById(R.id.spinner1);
    		int agentnumber = type1.getSelectedItemPosition();
    		String agent = type1.getSelectedItem().toString();
    		String[] agentsplit = agent.split(" ");
    		String agentkey = agentsplit[2];
    		String number = agentsplit[1];
    		
	      if (next.equals("command"))
	    		  {
	    	  Intent intent = new Intent(getApplicationContext(),SendCommandActivity.class);
				intent.putExtra("agent", agentnumber);
				intent.putExtra("key", agentkey);
				intent.putExtra("number", number);
				startActivity(intent);
	    		  }
	      else if (next.equals("data"))
	      {
	    	  String key = ((FrameworkAndroidApp)this.getApplication()).getkey();
	  		String sendstr1 = key.concat(" DATA ");
	  		String agentstring = String.valueOf(agentnumber);
	  		String sendstr = sendstr1.concat(agentstring);
	  		Intent intent = new Intent(getApplicationContext(),WebUploadService2.class);
	  	    intent.putExtra("uploadstring", sendstr);
	  	    startService(intent);  
	    	  Handler handler = new Handler(); 
	    	    handler.postDelayed(new Runnable() { 
	    	         public void run() { 
	    	        	  String datastring = "Data:\n";
	    	        	  String http = "http://";
	    		     	  	String controlIP = ((FrameworkAndroidApp) getApplication()).getcontrolIP();
	    		     	  	String path = ((FrameworkAndroidApp) getApplication()).getpath(); 
	    		     	  	String key = ((FrameworkAndroidApp) getApplication()).getkey();
	    		     	  	String urii = "/text2.txt";
	    		     	  	String uri = http.concat(controlIP);
	    		     	  	String uri2 = uri.concat(path);
	    		     	  	String pullfrom = uri2.concat(urii);
	    		     	  	URL url = null;
	    		     	  	try {
	    		     	  	url = new URL(pullfrom);
	    		     	  	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	    		     	  	if (in != null)
	    		     	  	{
	    		     	  		String inputLine;
	    		     	  		while  ((inputLine = in.readLine()) != null)
						        	{
	    		     	  			datastring = datastring.concat(inputLine);
						        	}
	    		     	  	}
						        	
	    	    		  Intent intent = new Intent(getApplicationContext(),GetDataActivity.class);
	    					intent.putExtra("data", datastring);
	    					startActivity(intent);    
	    	          } catch (Exception e) {
	 	     	  		e.printStackTrace();
	    	          }}
	    	    }, 5000); 
	    		
					
	    	  }
	      }
	      
	
	  
}
