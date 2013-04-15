package com.bulbsecurity.framework.app;

import android.app.Application;


public class FrameworkAndroidApp extends Application{
	private String controlIP = "0.0.0.0";
	private String key = "KEYKEY1";
	private String path = "/androidapp";
	private int stop = 1;

	public String getcontrolIP() {
		return controlIP;
	}
	public String getkey() {
		return key;
	}
	public String getpath() {
		return path;
	}
	
	public int getstop(){
		return stop;
	}
	
	public void setcontrolIP(String ip){
		this.controlIP = ip;
	}
	public void setkey(String ke){
		this.key = ke;
	}
	public void setpath(String pa){
		this.path = pa;
	}
	public void setstop(int st) {
		this.stop = st;
	}
	

}

