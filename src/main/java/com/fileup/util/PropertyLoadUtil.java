package com.fileup.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**@类名 PropertyLoadUtil
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2016年10月28日-下午3:58:00
 */
public class PropertyLoadUtil {
	public static Properties loadProperties(String name) {
		Properties p=new Properties();
		FileInputStream fin=null;
		try {
			File pFile=Paths.getUserDirFile("config",name.endsWith(".properties")?name:name+".properties");
			if(pFile.exists()) {
				fin = new FileInputStream(pFile);
				p.load(fin);
			}
		} catch (Exception e) {}
		finally {
			if(fin!=null) {try {fin.close();}catch (IOException e) {}}
		}
		return p;
	}
}
