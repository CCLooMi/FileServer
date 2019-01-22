package com.fileup.bean;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

/**© 2015-2019 Chenxj Copyright
 * 类    名：UploadCommand
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2019年1月15日-下午9:03:58
 */
public class UploadCommand implements CommandType{
	private int hd;
	private String id;
	private long indexStart;
	private long indexEnd;
	private int index;
	private float completePercent;
	
	/**获取 hd*/
	public int getHd() {
		return hd;
	}
	/**设置 hd*/
	public UploadCommand setHd(int hd) {
		this.hd = hd;
		return this;
	}
	/**获取 id*/
	public String getId() {
		return id;
	}
	/**设置 id*/
	public UploadCommand setId(String id) {
		this.id = id;
		return this;
	}
	/**获取 indexStart*/
	public long getIndexStart() {
		return indexStart;
	}
	/**设置 indexStart*/
	public UploadCommand setIndexStart(long indexStart) {
		this.indexStart = indexStart;
		return this;
	}
	/**获取 indexEnd*/
	public long getIndexEnd() {
		return indexEnd;
	}
	/**设置 indexEnd*/
	public UploadCommand setIndexEnd(long indexEnd) {
		this.indexEnd = indexEnd;
		return this;
	}
	/**获取 index*/
	public int getIndex() {
		return index;
	}
	/**设置 index*/
	public UploadCommand setIndex(int index) {
		this.index = index;
		return this;
	}
	/**获取 completePercent*/
	public float getCompletePercent() {
		return completePercent;
	}
	/**设置 completePercent*/
	public UploadCommand setCompletePercent(float completePercent) {
		this.completePercent = completePercent;
		return this;
	}
	public long blobSize() {
		return indexEnd-indexStart;
	}
	public BinaryWebSocketFrame toBinaryWebSocketFrame() {
		switch (hd) {
		case UPLOAD_COMMAND:
			return new BinaryWebSocketFrame(Unpooled.buffer(21)
					.writeByte(UPLOAD_COMMAND)
					.writeLong(indexStart)
					.writeLong(indexEnd)
					.writeInt((int)(completePercent*100000000)));
		case UPLOAD_COMPLETE:
			return new BinaryWebSocketFrame(Unpooled.buffer(1)
					.writeByte(UPLOAD_COMPLETE));
		default:
			return null;
		}
	}
}
