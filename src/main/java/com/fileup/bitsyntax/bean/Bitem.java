package com.fileup.bitsyntax.bean;

import java.util.ArrayList;
import java.util.List;


/**© 2015-2017 Chenxj Copyright
 * 类    名：Bitem
 * 类 描 述：Bit item
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2017年9月15日-下午3:10:04
 */
public class Bitem extends BaseBean{
	private static final long serialVersionUID = -7844616599653861649L;
	private String name;
	//长度可以为数字或关联的字段名称
	private Object l;
	
	private BitemType type;
	private List<String>types;
	private int separatorDataIndex;
	private byte[]separatorData;
	private boolean named;
	
	private Bitem[]bitems;
	
	private byte[][]range;
	private Bitem[]rg;
	
	private Object[]exchange;
	
	private boolean dispensable;
	
	//start--------bitmap参数
	//bitmap position
	private int p;
	//bitmap bit item 占用字节长度
	private int bsl;
	private Boundary start;
	private Boundary end;
	//end----------bitmap参数
	
	public Bitem(){
		this.named=true;
		this.type=BitemType.NORMAL;
		this.types=new ArrayList<>();
		this.rg=new Bitem[0];
	}
	
	/**获取 name*/
	public String getName() {
		return name;
	}

	/**设置 name*/
	public void setName(String name) {
		this.name = name;
	}

	/**获取 l*/
	@SuppressWarnings("unchecked")
	public <T>T getL() {
		return (T)l;
	}

	/**设置 l*/
	public void setL(Object l) {
		this.l = l;
	}

	/**获取 type*/
	public BitemType getType() {
		return type;
	}

	/**设置 type*/
	public void setType(BitemType type) {
		this.type = type;
	}

	/**获取 types*/
	public List<String> getTypes() {
		return types;
	}

	/**设置 types*/
	public void setTypes(List<String> types) {
		this.types = types;
	}

	/**获取 separatorDataIndex*/
	public int getSeparatorDataIndex() {
		return separatorDataIndex;
	}

	/**设置 separatorDataIndex*/
	public void setSeparatorDataIndex(int separatorDataIndex) {
		this.separatorDataIndex = separatorDataIndex;
	}

	/**获取 separatorData*/
	public byte[] getSeparatorData() {
		return separatorData;
	}

	/**设置 separatorData*/
	public void setSeparatorData(byte[] separatorData) {
		this.separatorData = separatorData;
	}

	/**获取 named*/
	public boolean isNamed() {
		return named;
	}

	/**设置 named*/
	public void setNamed(boolean named) {
		this.named = named;
	}
	
	/**获取 bitems*/
	public Bitem[] getBitems() {
		return bitems;
	}

	/**设置 bitems*/
	public void setBitems(Bitem[] bitems) {
		this.bitems = bitems;
	}

	/**获取 range*/
	public byte[][] getRange() {
		return range;
	}

	/**设置 range*/
	public void setRange(byte[][] range) {
		this.range = range;
	}

	/**获取 rg*/
	public Bitem[] getRg() {
		return rg;
	}

	/**设置 rg*/
	public void setRg(Bitem[] rg) {
		this.rg = rg;
	}

	/**获取 exchange*/
	public Object[] getExchange() {
		return exchange;
	}

	/**设置 exchange*/
	public void setExchange(Object[] exchange) {
		this.exchange = exchange;
	}

	/**获取 dispensable*/
	public boolean isDispensable() {
		return dispensable;
	}

	/**设置 dispensable*/
	public void setDispensable(boolean dispensable) {
		this.dispensable = dispensable;
	}

	/**获取 p*/
	public int getP() {
		return p;
	}

	/**设置 p*/
	public void setP(int p) {
		this.p = p;
	}

	/**获取 bsl*/
	public int getBsl() {
		return bsl;
	}

	/**设置 bsl*/
	public void setBsl(int bsl) {
		this.bsl = bsl;
	}

	/**获取 start*/
	public Boundary getStart() {
		return start;
	}

	/**设置 start*/
	public void setStart(Boundary start) {
		this.start = start;
	}

	/**获取 end*/
	public Boundary getEnd() {
		return end;
	}

	/**设置 end*/
	public void setEnd(Boundary end) {
		this.end = end;
	}

	public enum BitemType {
		NORMAL,
		SEPARATOR,
		MIXED,
		RANGE,
		LIST,
		BITMAP,
		EXCHANGE,
		PLACEHOLDER
	}
}
