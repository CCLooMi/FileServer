package com.fileup.bitsyntax;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

/**© 2015-2017 Chenxj Copyright
 * 类    名：CCBytes
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2017年9月16日-下午5:09:24
 */
public class CCBytes {
	private ByteArrayOutputStream bout;
	public CCBytes(){
		this.bout=new ByteArrayOutputStream();
	}
	
	public CCBytes addAll(Collection<? extends byte[]>cbs){
		for(byte[]bs:cbs){
			this.addAll(bs);
		}
		return this;
	}
	public CCBytes addAll(byte[]bs){
		try {
			this.bout.write(bs);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}
	public byte[]getBytes(){
		return bout.toByteArray();
	}
}
