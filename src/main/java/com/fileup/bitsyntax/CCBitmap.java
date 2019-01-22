package com.fileup.bitsyntax;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fileup.bitsyntax.bean.BaseBean;
import com.fileup.bitsyntax.bean.Bitem;
import com.fileup.bitsyntax.bean.Boundary;


/**© 2015-2017 Chenxj Copyright
 * 类    名：CCBitmap
 * 类 描 述：CCBitmap
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2017年9月15日-下午3:04:02
 */
public class CCBitmap extends BaseBean{
	private static final long serialVersionUID = 6915370138320994281L;
	private byte[] buffer;
	private int l;
	private Bitem[]bm;
	private Map<String, Bitem>im=new LinkedHashMap<>();
	public CCBitmap(Bitem[]bm,byte[]a){
		this.bm=bm;
		for(int i=0,p=0;i<this.bm.length;i++){
			this.im.put(this.bm[i].getName(), this.bm[i]);
			if(this.bm[i].getL()==null){
				this.bm[i].setL(1);
			}
			this.l+=(int)this.bm[i].getL();
			if(i==0){
				this.bm[i].setP(0);
			}else{
				p+=(int)this.bm[i-1].getL();
				this.bm[i].setP(p);
			}
			this.bm[i].setStart(new Boundary(this.bm[i].getP()>>3, this.bm[i].getP()%8));
			this.bm[i].setEnd(new Boundary(this.bm[i].getP()+(int)this.bm[i].getL()>>3,
					(this.bm[i].getP()+(int)this.bm[i].getL())%8));
			//计算占用buff长度
			if(this.bm[i].getEnd().getOffset()!=0){
				this.bm[i].setBsl(this.bm[i].getEnd().getIndex()-this.bm[i].getStart().getIndex()+1);
			}else{
				this.bm[i].setBsl(this.bm[i].getEnd().getIndex()-this.bm[i].getStart().getIndex());
			}
		}
		this.buffer=(a==null?new byte[(int)Math.ceil((double)this.l/8)]:a);
	}
	public byte[] buffer(){
		return this.buffer;
	}
	protected byte getByteMash(int n,boolean right){
		if(right){
			return (byte)((1<<n)-1);
		}else{
			return (byte)~((1 << (8 - n)) - 1);
		}
	}
	protected byte getByteMash(int n){
		return getByteMash(n, false);
	}
	protected byte getByteUnMash(int n,boolean right){
		if(right){
			return (byte)~((1 << n) - 1);
		}else{
			return (byte)((1 << (8 - n)) - 1);
		}
	}
	protected byte getByteUnMash(int n){
		return getByteUnMash(n, false);
	}
	public byte[] getItemValue(String name){
		Bitem b=this.im.get(name);
		byte[]rs=new byte[b.getBsl()];
		int cursor=0;
		int lack=b.getStart().getOffset();
		int lave=8-lack;
		int endLave=8-b.getEnd().getOffset();
		int bt=0x00;
		if(b.getBsl()<2){
			if(b.getEnd().getOffset()!=0){
				bt=((this.buffer[b.getStart().getIndex()]&this.getByteUnMash(endLave))&0xff)>>lack;
			}else{
				bt=(this.buffer[b.getStart().getIndex()]&0xff)>>lack;
			}
		}else{
			if(b.getEnd().getOffset()!=0){
				bt=(this.buffer[b.getStart().getIndex()]&0xff)>>lack;
			}
			for(int i=1;i<b.getEnd().getIndex()-1;i++){
				bt|=this.buffer[i]<<lave;
				rs[cursor++]=(byte)bt;
				//保存剩余的bits
				bt=(this.buffer[i]&0xff)>>lave;
			}
			bt|=(this.buffer[b.getEnd().getIndex()]&this.getByteUnMash(endLave))<<lave;
		}
		rs[cursor++]=(byte) bt;
		return rs;
	}
	public void setItemValue(String name,byte[]value){
		Bitem b=this.im.get(name);
		int lack=b.getStart().getOffset();
		int lave=8-lack;
		int cursor=0;
		int bt=value[cursor++];
		if(b.getBsl()<2){
			//先清除原来的值
			this.buffer[b.getStart().getIndex()]&=(byte)~(this.getByteMash(b.getL(),true)<<lack);
			if(lack!=0){
				this.buffer[b.getStart().getIndex()]|=bt<<lack;
			}else{
				this.buffer[b.getStart().getIndex()]|=bt;
			}
		}else if(b.getBsl()<3){
			//先清除原来的值
			this.buffer[b.getStart().getIndex()]&=this.getByteUnMash(lave);
			this.buffer[b.getStart().getIndex()]|=bt<<lack;
			//先清除原来的值
			this.buffer[b.getEnd().getIndex()]&=this.getByteUnMash(b.getEnd().getOffset(), true);
			this.buffer[b.getEnd().getIndex()]|=(bt&0xff)>>lave;
		}else{
			if(lave!=0){
				//先清除原来的值
				this.buffer[b.getStart().getIndex()]&=this.getByteUnMash(lave);
				this.buffer[b.getStart().getIndex()]|=bt<<lack;
				//保存剩余的bits
				bt=(bt&0xff)>>lave;
			}
			for(int i=1;i<b.getEnd().getIndex()-1;i++){
				this.buffer[i]=(byte) bt;
				bt=value[cursor++];
				this.buffer[i]|=bt<<lack;
				bt=(bt&0xff)>>lave;
			}
			this.buffer[b.getEnd().getIndex()]&=this.getByteUnMash(lave, true);
			this.buffer[b.getEnd().getIndex()]|=bt;
		}
	}
	public Map<String, Object>toMap(){
		Map<String, Object>m=new HashMap<>();
		for(int i=0;i<bm.length;i++){
			m.put(bm[i].getName(), getItemValue(bm[i].getName()));
		}
		return m;
	}
	public Bitem[] getBm() {
		return bm;
	}
}
