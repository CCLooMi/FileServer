package com.fileup.bean;

/**@desc Bit Set
 * @date 2019年2月15日 下午3:04:36
 * @author chenxianjun
 */
public class BSet {
	private byte[]bs;
	private int length;
	public BSet(int length) {
		this.length=length;
		int i=length>>3;
		int r=(i<<3)^length;
		if(r>0) {
			i++;
		}
		this.bs=new byte[i+1];
		//save r in the end byte
		this.bs[i]|=r;
	}
	public BSet(byte[]bs) {
		this.bs=bs;
		//reset length from end byte
		if(bs[bs.length-1]>0) {
			this.length=((bs.length-1)<<3)-(8-bs[bs.length-1]);
		}else {
			this.length=(bs.length-1)<<3;
		}
	}
	public BSet setBit(int index) {
		this.bs[index>>3]|=(1<<(8-(((index>>3)<<3)^index)));
		return this;
	}
	public BSet unsetBit(int index) {
		this.bs[index>>3]^=(1<<(8-(((index>>3)<<3)^index)));
		return this;
	}
//	public int getBit(int index) {
//		return (this.bs[index>>3]&(1<<(8-(((index>>3)<<3)^index))))>0?1:0;
//	}
	public boolean bit(int index) {
		return (this.bs[index>>3]&(1<<(8-(((index>>3)<<3)^index))))>0;
	}
	public int getLength() {
		return this.length;
	}
	public byte[] toBytes() {
		return this.bs;
	}
	public static void main(String[] args) {
		BSet bSet=new BSet(56);
		BSet bSet2=new BSet(bSet.toBytes());
		
		System.out.println(bSet2.length);
	}
}
