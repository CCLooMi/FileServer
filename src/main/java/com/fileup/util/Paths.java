package com.fileup.util;

import java.io.File;

/**© 2015-2018 Chenxj Copyright
 * 类    名：Paths
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2018年12月24日-下午8:36:44
 */
public class Paths {
	public static String get(String base,String...sa) {
		StringBuilder sb=new StringBuilder();
		sb.append(base);
		for(String s:sa) {
			if(sb.length()>0&&sb.charAt(sb.length()-1)=='/') {
				if(s.charAt(0)=='/') {
					sb.append(new String(s.substring(1, s.length())));
				}else {
					sb.append(s);
				}
			}else {
				if(s.charAt(0)=='/') {
					sb.append(s);
				}else {
					sb.append('/').append(s);
				}
			}
		}
		return sb.toString();
	}
	public static String getBaseUserDir(String...sa) {
		return get(System.getProperty("user.dir"), sa);
	}
	public static File getFile(String base,String...sa) {
		return new File(get(base, sa));
	}
	public static File getUserDirFile(String...sa) {
		return new File(getBaseUserDir(sa));
	}
}
