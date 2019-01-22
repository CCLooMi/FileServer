package com.fileup.bitsyntax;

import java.nio.ByteBuffer;

/**© 2015-2017 Chenxj Copyright
 * 类    名：CCDataView
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2017年9月15日-下午1:37:52
 */
public class CCDataView {
	private byte[] buffer;
	private int pos;
	private int lim;
	public CCDataView(ByteBuffer buffer){
		this.buffer=new byte[buffer.limit()];
		buffer.get(this.buffer);
		this.pos=0;
		this.lim=buffer.limit();
	}
	public CCDataView(byte[] buffer){
		this.buffer=buffer;
		this.pos=0;
		this.lim=buffer.length;
	}
	public int position(){
		return this.pos;
	}
	public void position(int position){
		this.pos=position;
	}
	public int limit(){
		return this.lim;
	}
	public void limit(int limit){
		this.lim=limit;
	}
	public boolean isArrayMatchAt(int p,byte[]a){
		boolean ismatch=true;
		for(int i=0;i<a.length;i++){
			if(a[i]!=this.buffer[p+i]){
				ismatch=false;
				break;
			}
		}
		return ismatch;
	}
	public boolean isMatchsAt(int p,byte[][]as){
		boolean ismatch=false;
		for(int i=0;i<as.length;i++){
			if(this.isArrayMatchAt(p, as[i])){
				ismatch=true;
				break;
			}
		}
		return ismatch;
	}
	public Object[] matchsAt(int p,byte[][]as){
		for(int i=0;i<as.length;i++){
			if(this.isArrayMatchAt(p, as[i])){
				return new Object[]{true,as[i]};
			}
		}
		return new Object[]{false};
	}
	public int arrayMatch(byte[][]as,boolean d){
		int index=0;
		if(!d){//正向
			for(int i=this.pos;i<this.lim;i++){
				if(this.isMatchsAt(i, as)){
					return index;
				}else{
					index++;
				}
			}
		}else{//逆向
			Object[]mr;
			for(int i=this.lim-1;i>=this.pos;i--){
				mr=this.matchsAt(i, as);
				if((boolean)mr[0]){
					return index-((byte[])mr[1]).length;
				}else{
					index++;
				}
			}
		}
		return index;
	}
	public int arrayMatch(byte[][]as){
		return arrayMatch(as, false);
	}
	public byte[] getArray(Object l,boolean d){
		byte[]a=null;
		int al;
		if(!d){//正序
			if(l instanceof byte[][]){
				al=this.arrayMatch((byte[][])l);
			}else if(l==null){
				al=this.lim-this.pos;
			}else{
				al=(int)l;
			}
			a=new byte[al];
			System.arraycopy(this.buffer, this.pos, a, 0, al);
			this.pos+=al;
		}else{//逆序
			if(l instanceof byte[][]){
				al=this.arrayMatch((byte[][])l,d);
			}else if(l==null){
				al=this.lim-this.pos;
			}else{
				al=(int)l;
			}
			a=new byte[al];
			System.arraycopy(this.buffer, this.lim-al, a, 0, al);
			this.lim-=al;
		}
		return a;
	}
	public byte[] getArray(Object l){
		return getArray(l,false);
	}
}
