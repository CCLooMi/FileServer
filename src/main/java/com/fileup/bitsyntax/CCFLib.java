package com.fileup.bitsyntax;

import java.util.HashMap;
import java.util.Map;

import com.fileup.bitsyntax.function.FromBytes;
import com.fileup.bitsyntax.function.ToBytes;



/**© 2015-2017 Chenxj Copyright
 * 类    名：CCFLib
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2017年9月16日-上午10:31:05
 */
public interface CCFLib {
	public static final Map<String, FromBytes>fromLib=new HashMap<>();
	public static final Map<String, ToBytes>toLib=new HashMap<>();
}
