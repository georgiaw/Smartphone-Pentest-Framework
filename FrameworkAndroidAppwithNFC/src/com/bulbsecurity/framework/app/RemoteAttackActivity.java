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
import com.bulbsecurity.framework.app2.R;

public class RemoteAttackActivity extends Activity  implements AdapterView.OnItemSelectedListener, OnClickListener{
	
	 private Spinner spinner1, spinner2;
	  private Button btnSubmit;
	  private EditText edittext,edittext2;
	  
	@Override
	  public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.remoteattack);
		btnSubmit = (Button) findViewById(R.id.btnSubmit);
		btnSubmit.setEnabled(false);
		btnSubmit.setClickable(false);
		btnSubmit.setOnClickListener(this); 
		spinner2 = (Spinner) findViewById(R.id.spinner2);
		spinner2.setEnabled(false);
		edittext = (EditText) findViewById(R.id.edittext);
		edittext.setVisibility(View.INVISIBLE);
		edittext2 = (EditText) findViewById(R.id.edittext2);
		edittext2.setVisibility(View.INVISIBLE);
		spinner1 = (Spinner) findViewById(R.id.spinner1);
		spinner1.setOnItemSelectedListener(this);
		spinner2.setOnItemSelectedListener(this);
		
		
	}

	       @Override
	       public void onItemSelected(AdapterView<?> parent, 
	         View view, int pos, long id) 
	       {
	    	   int which = parent.getId();
	    	   if (which==R.id.spinner1)
	    	   {
	           Spinner type1 = (Spinner) findViewById(R.id.spinner1);
	          
	        		String sel = parent.getItemAtPosition(pos).toString();
	        		Spinner type2 = (Spinner) findViewById(R.id.spinner2);
	        		if (sel.equals("iPhone"))
	        		{
	        		
	    
	        			List<String> list = new ArrayList<String>();
	        			list.add("Select Exploit");
	        			list.add("Default SSH Password");
	        			list.add("Guess SSH Password");
	        			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(parent.getContext(),
	        					android.R.layout.simple_spinner_item, list);
	        				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        				type2.setAdapter(dataAdapter);
	        				type2.setEnabled(true);
	        				
	        		}
	        		else 
	        		{
	        			EditText text1 = (EditText) findViewById(R.id.edittext);
	        			text1.setVisibility(View.INVISIBLE);
	        			EditText text2 = (EditText) findViewById(R.id.edittext2);
	        			text2.setVisibility(View.INVISIBLE);
	        			  if (type2.isEnabled()) {
	        			      type2.setEnabled(false);
	        			  }
	        			  Button button1 = (Button) findViewById(R.id.btnSubmit);
	        			  if (button1.isEnabled()) {
	        			      button1.setEnabled(false);
	        			  }
	        			  if (button1.isClickable()) {
	        			      button1.setClickable(false);
	        			  }
	        		}
	        		
	    	   }
	    	   else {
	    		   if (which==R.id.spinner2)
		    	   { 
	    			   Spinner type2 = (Spinner) findViewById(R.id.spinner2);
	    	          
	    	        		String sel = parent.getItemAtPosition(pos).toString();
	    	        		EditText text1 = (EditText) findViewById(R.id.edittext);
	    	        		EditText text2 = (EditText) findViewById(R.id.edittext2);
	    	        		Button button1 = (Button) findViewById(R.id.btnSubmit);
	    	        		if (sel.equals("Default SSH Password"))
	    	        		{  
	    	        			text1.setVisibility(View.VISIBLE);
	    	        			text2.setVisibility(View.INVISIBLE);
	    	        			button1.setEnabled(true);
	    	        			button1.setClickable(true);
	    	        			
	    	        		}
	    	        		else if (sel.equals("Guess SSH Password"))
	    	        		{
	    	        			text1.setVisibility(View.VISIBLE);
	    	        			text2.setVisibility(View.VISIBLE);
	    	        			button1.setEnabled(true);
	    	        			button1.setClickable(true);
	    	        		}
	    	        		else {
	    	        				text1.setVisibility(View.INVISIBLE);
	    	        				text2.setVisibility(View.INVISIBLE);
	    	        			  if (button1.isEnabled()) {
	    	        			      button1.setEnabled(false);
	    	        			  }
	    	        			  if (button1.isClickable()) {
	    	        			      button1.setClickable(false);
	    	        			  }
	    	        			 
	    	        		}
		    	   }
	    	   }

	       }
	            @Override
	            public void onNothingSelected(AdapterView<?> parent) {
	                // TODO Auto-generated method stub

	            }
	            @Override
			    public void onClick(View v) {
	            	Spinner type2 = (Spinner) findViewById(R.id.spinner2);
	        		String sel = type2.getSelectedItem().toString();
			    	EditText text = (EditText) findViewById(R.id.edittext);
			    	EditText text2 = (EditText) findViewById(R.id.edittext2);
			    	if (sel.equals("Default SSH Password"))
	        		{
			    		String ip = text.getText().toString();
			    		String key = ((FrameworkAndroidApp)this.getApplication()).getkey();
			    		String sendstring1 = key.concat(" ");
			    		String sendstring2 = sendstring1.concat("alpine");
			    		String sendstring3 = sendstring2.concat(" ");
			    		String sendstring4 = sendstring3.concat(ip);
			    		Intent intent = new Intent(getApplicationContext(),WebUploadService2.class);
			    		intent.putExtra("uploadstring", sendstring4);
			    		startService(intent);
			    		finish();
	        		}
			    	else if (sel.equals("Guess SSH Password"))
	        		{
			    		String ip = text.getText().toString();
			    		String passfile = text2.getText().toString();
			    		String key = ((FrameworkAndroidApp)this.getApplication()).getkey();
			    		String sendstring1 = key.concat(" ");
			    		String sendstring2 = sendstring1.concat("guess");
			    		String sendstring3 = sendstring2.concat(" ");
			    		String sendstring4 = sendstring3.concat(ip);
			    		String sendstring5 = sendstring4.concat(" ");
			    		String sendstring6 = sendstring5.concat(passfile);
			    		Intent intent = new Intent(getApplicationContext(),WebUploadService2.class);
			    		intent.putExtra("uploadstring", sendstring6);
			    		startService(intent);
			    		finish();
	        		}
			    }
			  

				
	  
	}
	
	
