package com.fileup.util;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fileup.bitsyntax.CCBitsyntax;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;


public class BtxUtil {
	private static CCBitsyntax btx=new CCBitsyntax("hd:1/I,(hd){0:{id:16/HEX,size:8/L,suffix/U8,'#',type/U8,'#',name/U8},1:{indexStart:8/L,indexEnd:8/L,completePercent:4/I},2:{data}}");
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
