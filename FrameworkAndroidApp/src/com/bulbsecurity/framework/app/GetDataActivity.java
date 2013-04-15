package com.bulbsecurity.framework.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;


public class GetDataActivity extends Activity {

	  @Override
		public void onCreate(Bundle icicle) {
			super.onCreate(icicle);
			Bundle extras = getIntent().getExtras();
			String data = extras.getString("data");
			setContentView(R.layout.getdata);
TextView current = 
(TextView)  this.findViewById(R.id.textview); 
current.setText(data); 
	  }
}