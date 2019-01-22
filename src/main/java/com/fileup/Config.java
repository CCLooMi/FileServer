package com.fileup;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.fileup.util.PropertyLoadUtil;

public class Config {
	public static final int blobSize=512*1024;
	public static Properties p;
	public static boolean decktopWindow=false;
	public static Set<String> zimgTypes;
	static {
		p=PropertyLoadUtil.loadProperties("fileServer");
		decktopWindow="windows".equalsIgnoreCase(System.getProperty("sun.desktop"));
		zimgTypes=new HashSet<>();
		String[] ztypes=p.getProperty("server.zimg.types", "").split(",");
		for(String t:ztypes) {
			zimgTypes.add(t);
			zimgTypes.add('.'+t);
		}
	}
	public static boolean isZimgTypes(String s) {
		return zimgTypes.contains(s);
	}
	public static String getConfig(String key){
		return p.getProperty(key);
	}
	public static String getConfig(String key,String defaultValue){
		return p.getProperty(key, defaultValue);
	}
	public static Integer getConfigAsInteger(String key) {
		return Integer.valueOf(p.getProperty(key));
	}
	public static Integer getConfigAsInteger(String key,Integer defaultValue) {
		try {
			return Integer.valueOf(p.getProperty(key));
		}catch (Exception e) {
			return defaultValue;
		}
	}
}
