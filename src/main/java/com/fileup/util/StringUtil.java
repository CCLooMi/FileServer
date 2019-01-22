package com.fileup.util;

import java.util.Arrays;

public class StringUtil {

	public static long strtol(String str,int base){
		return strtol((str+"\0").toCharArray(),base);
	}
	public static long strtoul(String str,int base){
		return strtoul((str+"\0").toCharArray(),base);
	}
	
	//#####################################################################################
	//#
	//##
	//###                                一下所有为私有方法
	//##
	//#
	//#####################################################################################
	
	/**
	 * 描述：此方法只返回非负数
	 * 作者：Chenxj
	 * 日期：2016年7月20日 - 下午10:35:28
	 * @param cp
	 * @param base
	 * @return
	 */
	private static long strtoul(char[] cp,int base){
		long result=0,value;
		int i=0;
		if(base==0){
			base=10;
			if(cp[i]=='0'){
				base=8;
				i++;
				if(Character.toLowerCase(cp[i])=='x'&&isxdigit(cp[1])){
					i++;
					base=16;
				}
			}
		}else if(base==16){
			if(cp[0]=='0'&&Character.toLowerCase(cp[1])=='x')
				i+=2;
		}
		while(isxdigit(cp[i])&&(value = isdigit(cp[i]) ? cp[i]-'0' : Character.toLowerCase(cp[i])-'a'+10) < base){
			result=result*base+value;
			i++;
		}
		return result;
	}
	/**
	 * 描述：此会返回有符号数
	 * 作者：Chenxj
	 * 日期：2016年7月20日 - 下午10:36:08
	 * @param cp
	 * @param base
	 * @return
	 */
	private static long strtol(char[]cp,int base){
		if(cp[0]=='-'){
			return -strtoul(subChars(cp, 1),base);
		}
		return strtoul(cp, base);
	}
	/**
	 * 判断char是否是16进制以内的数
	 * @param c
	 * @return
	 */
	private static boolean isxdigit(char c){
		return ('0' <= c && c <= '9')||('a' <= c && c <= 'f')||('A' <= c && c <= 'F');
	}
	/**
	 * 判断char是否是10进制的数
	 * @param c
	 * @return
	 */
	private static boolean isdigit(char c){
		return '0' <= c && c <= '9';
	}
	/**
	 * 描述：char数组切分
	 * 作者：Chenxj
	 * 日期：2016年7月20日 - 下午10:37:59
	 * @param cp
	 * @param indexs
	 * @return
	 */
	private static char[] subChars(char[]cp,int...indexs){
		if(indexs.length==1){
			return Arrays.copyOfRange(cp, indexs[0], cp.length);
		}else if(indexs.length>1){
			return Arrays.copyOfRange(cp, indexs[0], indexs[0]+indexs[1]);
		}
		return cp;
	}
	public static void main(String[] args) {
		String md5="b1c5553067e312ff7f9fe0c456d1752b";
		System.out.println(strtol(md5.substring(0, 3),16)/4);
		System.out.println(strtol(md5.substring(3, 3+3),16)/4);
	}
}
