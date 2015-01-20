package com.bean;
/**
 * 文件上传命令信息类
 * 此类会被转成JSON发送到客户端
 * @author xianjun
 *
 */
public class UploadCommand {
	private final String typeId="uploadCommand";
	private String fileId;
	private int index;
	private long indexStart;
	private long indexEnd;
	private long blobSize;
	private float completePercent;
	public static UploadCommand getSucccessCommand(String fileId){
		UploadCommand uc=new UploadCommand();
		uc.setBlobSize(-1);
		uc.setCompletePercent(1);
		uc.setFileId(fileId);
		uc.setIndex(-1);
		uc.setIndexEnd(-1);
		uc.setIndexStart(-1);
		return uc;
	}
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public long getIndexStart() {
		return indexStart;
	}
	public void setIndexStart(long indexStart) {
		this.indexStart = indexStart;
	}
	public long getIndexEnd() {
		return indexEnd;
	}
	public void setIndexEnd(long indexEnd) {
		this.indexEnd = indexEnd;
	}
	public long getBlobSize() {
		return blobSize;
	}
	public void setBlobSize(long blobSize) {
		this.blobSize = blobSize;
	}
	public float getCompletePercent() {
		return completePercent;
	}
	public void setCompletePercent(float completePercent) {
		this.completePercent = completePercent;
	}
	public String getTypeId() {
		return typeId;
	}
	
}
