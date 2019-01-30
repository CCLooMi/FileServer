package com.fileup.bitsyntax;

import static com.fileup.bitsyntax.function.BitConvertLib.*;
import static com.fileup.util.BytesUtil.*;
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
public abstract class CCFLib {
	public static final Map<String, FromBytes>fromLib=new HashMap<>();
	public static final Map<String, ToBytes>toLib=new HashMap<>();
	static {
		fromLib.put("I", (o)->{return bytes2integer((byte[]) o);});
		fromLib.put("U8", (o)->{return bytes2utf8((byte[]) o);});
		fromLib.put("CCID", (o)->{return bytesToCCString((byte[]) o);});
		fromLib.put("HEX", (o)->{return bytesToHexString((byte[]) o);});
		fromLib.put("L", (o)->{return bytes2long((byte[]) o);});
		fromLib.put("F", (o)->{return bytes2float((byte[]) o);});
		fromLib.put("D", (o)->{return bytes2double((byte[]) o);});
		toLib.put("I", (o,l)->{if(l==null||l<1) {l=4;}return integer2bytes(o, l);});
		toLib.put("U8", (o,l)->{return utf82bytes(o);});
		toLib.put("CCID", (o,l)->{return ccStringToBytes((String)o);});
		toLib.put("HEX", (o,l)->{return hexStringToBytes((String)o);});
		toLib.put("L", (o,l)->{if(l==null||l<1) {l=8;}return long2bytes(o, l);});
		toLib.put("F", (o,l)->{if(l==null||l<1) {l=4;}return float2bytes(o, l);});
		toLib.put("D", (o,l)->{if(l==null||l<1) {l=8;}return double2bytes(o, l);});
	}
}
