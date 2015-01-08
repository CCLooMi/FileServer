package com.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;


public enum ServerConfigEnum {
	config;
	private String savePath;
	private int blobSize;
	private ServerConfigEnum(){
		String fname=this.getClass().getResource("config.properties").getPath().substring(1);
		Properties properties=new Properties();
		InputStream inStream;
		try {
			inStream = new FileInputStream(fname);
			properties.load(inStream);
			inStream.close();
			this.savePath=properties.getProperty("savePath");
			this.blobSize=Integer.valueOf(properties.getProperty("blobSize"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public String getSavePath() {
		return savePath;
	}
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	public int getBlobSize() {
		return blobSize;
	}
	public void setBlobSize(int blobSize) {
		this.blobSize = blobSize;
	}
	
}
