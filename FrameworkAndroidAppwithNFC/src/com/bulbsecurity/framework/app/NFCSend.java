package com.bulbsecurity.framework.app;

import com.bulbsecurity.framework.app2.R;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
public class NFCSend extends Activity implements CreateNdefMessageCallback,
OnNdefPushCompleteCallback {

	String url;
	private static final int MESSAGE_SENT = 1;
	
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.nfc);
	        Log.i("AAA", "NFC Started");
			Bundle extras = getIntent().getExtras();
			 url = extras.getString("url");

NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

// Register callback to set NDEF message
mNfcAdapter.setNdefPushMessageCallback(this, this);
// Register callback to listen for message-sent success
mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
}


@Override
public void onNdefPushComplete(NfcEvent event) {
	   // A handler is needed to send messages to the activity when this
    // callback occurs, because it happens from a binder thread
    mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
}

/** This handler receives a message from onNdefPushComplete */
private final Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
        case MESSAGE_SENT:
            Toast.makeText(getApplicationContext(), "Message sent!", Toast.LENGTH_LONG).show();
            break;
        }
    }
};

@Override
public NdefMessage createNdefMessage(NfcEvent event) {
	 NdefRecord uriRecord = NdefRecord.createUri(url);
	    return new NdefMessage(new NdefRecord[] { uriRecord });
}


}
