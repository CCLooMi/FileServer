package com.fileup;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import com.fileup.util.Paths;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class FileServerApp {

	public static Class<?> getAppClass() {
		return new Object() {
			public Class<?> getSuperObjectClass() {
				String className=getClass().getName();
				className=className.substring(0, className.lastIndexOf("$"));
				try {
					return Class.forName(className);
				} catch (ClassNotFoundException e) {
					return null;
				}
			}
		}.getSuperObjectClass();
	}
	public static void main(String[] args) {
		Properties properties=new Properties();
		File pfile=Paths.getUserDirFile("config","application.properties");
		try {
			FileInputStream fin=new FileInputStream(pfile);
			properties.load(fin);
			fin.close();
			SpringApplication app=new SpringApplication(getAppClass());
			app.setDefaultProperties(properties);
			app.run(args);
			
			WebSocketServer.main(args);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
