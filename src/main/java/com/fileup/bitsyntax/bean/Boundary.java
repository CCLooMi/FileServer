package com.fileup.bitsyntax.bean;

/**© 2015-2017 Chenxj Copyright
 * 类    名：Boundary
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2017年9月15日-下午3:21:54
 */
public class Boundary extends BaseBean{
	private static final long serialVersionUID = 5568731424046832313L;
	private int index;
	private int offset;
	public Boundary(){
		
	}
	public Boundary(int index,int offset){
		this.index=index;
		this.offset=offset;
	}
	/**获取 index*/
	public int getIndex() {
		return index;
	}
	/**设置 index*/
	public void setIndex(int index) {
		this.index = index;
	}
	/**获取 offset*/
	public int getOffset() {
		return offset;
	}
	/**设置 offset*/
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
}
