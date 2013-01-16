package com.bulbsecurity.framework;

import android.app.Application;

public class AndroidAgent extends Application {
			private String controlIP = "192.168.1.110";
			private String URL = "/control";
			private String key = "KEYKEY1";
			private String path = "/androidagent1";
			private int rooted = 0;
			
			private String controlNumber = "15555215554";

			public String getcontrolNumber() {
				return controlNumber;
			}
			public String getcontrolIP() {
				return controlIP;
			}
			public String getURL() {
				return URL;
			}
			public String getkey() {
				return key;
			}
			public String getpath() {
				return path;
			}
			public int getrooted() {
				return rooted;
			}
			public void setrooted(int root){
				this.rooted = root;
			}
}
