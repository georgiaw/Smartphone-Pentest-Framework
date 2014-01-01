package com.bulbsecurity.framework;
import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class AndroidAgentActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("AAA", "Main Activity Started");
        Context context = getApplicationContext();
        File blah = context.getFilesDir();
        Log.i("AAA", blah.toString());
        Intent intent = new Intent(getApplicationContext(),ServiceAutoStarterr.class);
       sendBroadcast(intent);
       Log.i("AAA", "Broadcast sent");
        //setContentView(R.layout.main);
        finish();
      
      
    }
}
