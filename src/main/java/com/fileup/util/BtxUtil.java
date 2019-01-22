package com.fileup.util;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fileup.bitsyntax.CCBitsyntax;
import com.fileup.bitsyntax.CCFLib;
import com.fileup.bitsyntax.function.BitConvertLib;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;


public class BtxUtil {
	private static CCBitsyntax btx=new CCBitsyntax("hd:1/I,(hd){0:{id:16/HEX,size:8/L,suffix/U8,'#',type/U8,'#',name/U8},1:{indexStart:8/L,indexEnd:8/L,completePercent:4/I},2:{data}}");
	static {
		CCFLib.fromLib.put("I", (o)->{return BitConvertLib.bytes2integer((byte[]) o);});
		CCFLib.fromLib.put("U8", (o)->{return BitConvertLib.bytes2utf8((byte[]) o);});
		CCFLib.fromLib.put("CCID", (o)->{return BytesUtil.bytesToCCString((byte[]) o);});
		CCFLib.fromLib.put("HEX", (o)->{return BytesUtil.bytesToHexString((byte[]) o);});
		CCFLib.fromLib.put("L", (o)->{return BitConvertLib.bytes2long((byte[]) o);});
		CCFLib.fromLib.put("F", (o)->{return BitConvertLib.bytes2float((byte[]) o);});
		CCFLib.fromLib.put("D", (o)->{return BitConvertLib.bytes2double((byte[]) o);});
		
		CCFLib.toLib.put("I", (o,l)->{if(l==null||l<1) {l=4;}return BitConvertLib.integer2bytes(o, l);});
		CCFLib.toLib.put("U8", (o,l)->{return BitConvertLib.utf82bytes(o);});
		CCFLib.toLib.put("CCID", (o,l)->{return BytesUtil.ccStringToBytes((String)o);});
		CCFLib.toLib.put("HEX", (o,l)->{return BytesUtil.hexStringToBytes((String)o);});
		CCFLib.toLib.put("L", (o,l)->{if(l==null||l<1) {l=8;}return BitConvertLib.long2bytes(o, l);});
		CCFLib.toLib.put("F", (o,l)->{if(l==null||l<1) {l=4;}return BitConvertLib.float2bytes(o, l);});
		CCFLib.toLib.put("D", (o,l)->{if(l==null||l<1) {l=8;}return BitConvertLib.double2bytes(o, l);});
	}
	public static CCBitsyntax btx() {
		return btx;
	}
	public static void main(String[] args) {
		ByteBuf bf=Unpooled.buffer(21);
		bf.writeByte(1);
		bf.writeLong(0);
		bf.writeLong(1024);
		bf.writeInt(1024);
		
		Map<String,Object>m=new HashMap<>();
		m.put("hd", 1);
		m.put("indexStart", 0l);
		m.put("indexEnd", 1024l);
		m.put("completePercent", 1024);
		System.out.println(Arrays.toString(btx.convertToByteArray(m)));
		
		ByteBuffer bb=ByteBuffer.allocate(21);
		bb.put((byte)1);
		bb.putLong(0l);
		bb.putLong(1024l);
		bb.putInt(1024);
		System.out.println(Arrays.toString(bb.array()));
	}
}
