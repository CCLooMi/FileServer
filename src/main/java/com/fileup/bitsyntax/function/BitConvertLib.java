package com.fileup.bitsyntax.function;

import java.nio.charset.Charset;

public interface BitConvertLib {
	public static final Charset utf8=Charset.forName("UTF-8");
	public static final Charset utf16=Charset.forName("UTF-16");
	public static final Charset utf32=Charset.forName("UTF-32");
	
	//to byte[]

	//Big-endian
	public static byte[] integer2bytes(Object o,int length){
		byte[]bs=new byte[length];
		for(int i=length-1,j=0;i>-1;i--,j++) {
			bs[i]|=((int)o)>>(j<<3);
		}
		return bs;
	}
	//Little-endian
	public static byte[] integer2bytes2(Object o,int length){
		byte[]bs=new byte[length];
		for (int i = 0; i < bs.length; i++) {
			bs[i]|=(byte)(((int)o)>>(i<<3));
		}
		return bs;
	}
	public static byte[] unsignedInteger2bytes(Object o,int length){
		return long2bytes(o, length);
	}
	public static byte[] double2bytes(Object o,int length){
		return long2bytes(Double.doubleToRawLongBits((double)o), length);
	}
	//Big-endian
	public static byte[] long2bytes(Object o,int length){
		byte[]bs=new byte[length];
		for(int i=length-1,j=0;i>-1;i--,j++) {
			bs[i]|=((long)o)>>(j<<3);
		}
		return bs;
	}
	//Little-endian
	public static byte[] long2bytes2(Object o,int length){
		byte[]bs=new byte[length];
		for (int i = 0; i < bs.length; i++) {
			bs[i]|=(byte)(((long)o)>>(i<<3));
		}
		return bs;
	}
	public static byte[] float2bytes(Object o,int length){
		return integer2bytes(Float.floatToRawIntBits((float)o), length);
	}
	
	public static byte[] utf82bytes(Object o){
		return ((String)o).getBytes(utf8);
	}
	public static byte[] utf162bytes(Object o){
		return ((String)o).getBytes(utf16);
	}
	public static byte[] utf322bytes(Object o){
		return ((String)o).getBytes(utf32);
	}
	
	//from byte[]

	//Big-endian
	public static Object bytes2integer(byte[]bytes){
		int value=0;
		for(int i=0,j=bytes.length-1;i<bytes.length;i++,j--) {
			value|=(bytes[i]&0xff)<<(j<<3);
		}
		return value;
	}
	//Little-endian
	public static Object bytes2integer2(byte[]bytes){
		int value=0;
		for (int i = bytes.length-1; i > -1; i--) {
			value|=(bytes[i]&0xff)<<(i<<3);
		}
		return value;
	}
	public static Object bytes2unsignedInteger(byte[]bytes){
		return bytes2long(bytes);
	}
	public static Object bytes2double(byte[]bytes){
		return Double.longBitsToDouble((long)bytes2long(bytes));
	}
	//Big-endian
	public static Object bytes2long(byte[]bytes){
		long value=0;
		for(int i=0,j=bytes.length-1;i<bytes.length;i++,j--) {
			value|=(long)(bytes[i]&0xff)<<(j<<3);
		}
		return value;
	}
	//Little-endian
	public static Object bytes2long2(byte[]bytes){
		long value=0;
		for (int i = bytes.length-1; i > -1; i--) {
			value|=(long)(bytes[i]&0xff)<<(i<<3);
		}
		return value;
	}
	public static Object bytes2float(byte[]bytes){
		return Float.intBitsToFloat((int)bytes2integer(bytes));
	}
	
	
	public static Object bytes2utf8(byte[]bytes){
		return new String(bytes, utf8);
	}
	public static Object bytes2utf16(byte[]bytes){
		return new String(bytes, utf16);
	}
	public static Object bytes2utf32(byte[]bytes){
		return new String(bytes, utf32);
	}
}
