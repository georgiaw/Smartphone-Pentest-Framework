package com.bulbsecurity.framework.app;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;



public class FunctionListActivity extends ListActivity {
	
		public void onCreate(Bundle icicle) {
			super.onCreate(icicle);
			String[] values = new String[] { "Send Commands to an Agent", "View Data Gathered from an Agent" , "Run a Remote Attack", "Run a Social Engineering or Client Side Attack" };
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, values);
			setListAdapter(adapter);
		}
		
		@Override
		protected void onListItemClick(ListView l, View v, int position, long id) {
		 // TODO Auto-generated method stub
		 //super.onListItemClick(l, v, position, id);
		 String selection = l.getItemAtPosition(position).toString();
		 if (selection.equals("Run a Remote Attack"))
		 {
			 Intent remoteintent = new Intent(getApplicationContext(),RemoteAttackActivity.class);
			 startActivity(remoteintent);
		 }
		if (selection.equals("Run a Social Engineering or Client Side Attack"))
		{
			Intent socialintent = new Intent(getApplicationContext(),SocialAttackActivity.class);
			 startActivity(socialintent);
		}
		if (selection.equals("Send Commands to an Agent"))
		{
			Intent getagents = new Intent(getApplicationContext(),GetAgentsActivity.class);
			getagents.putExtra("next", "command");
			startActivity(getagents);
		}
		if (selection.equals("View Data Gathered from an Agent"))
		{
			Intent getagents = new Intent(getApplicationContext(),GetAgentsActivity.class);
			getagents.putExtra("next", "data");
			startActivity(getagents);
		}
		
		}
	}

