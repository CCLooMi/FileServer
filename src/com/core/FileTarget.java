package com.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.bean.FileInfo;
import com.bean.UploadCommand;
import com.config.ServerConfigEnum;
import com.service.FileServer;

public class FileTarget {
	/**上传文件基本目录*/
	private final String basePath=ServerConfigEnum.config.getSavePath();
	/**上传文件信息*/
	private FileInfo fileInfo;
	/**上传文件分片信息*/
	private SlicedInfo slicedInfo;
	/**所有上传该文件的客户端*/
	private Set<FileServer>currentFileUploaders;
	/**文件上传进度*/
	private float completePercent=0;
	/**服务器端文件名*/
	private String fileName;
	
	private File file;
	private RandomAccessFile raFile;
	private FileChannel fileChannel;
	
	public FileTarget(){
		super();
	}
	public FileTarget(FileInfo fileInfo){
		this.currentFileUploaders=new CopyOnWriteArraySet<FileServer>();
		this.setFileInfo(fileInfo);
		this.openFileWriteAccessChannel();
	}
	
	public FileInfo getFileInfo() {
		return fileInfo;
	}
	public void setFileInfo(FileInfo fileInfo) {
		this.fileInfo = fileInfo;
		this.fileName=this.fileInfo.getFileId()+"."+this.fileInfo.getFileName().substring(this.fileInfo.getFileName().lastIndexOf(".")+1);
		if(this.slicedInfo==null){
			this.slicedInfo=SlicedInfo.getInstance(fileInfo, ServerConfigEnum.config.getBlobSize());
		}
	}
	public SlicedInfo getSlicedInfo() {
		return slicedInfo;
	}
	public void setSlicedInfo(SlicedInfo slicedInfo) {
		this.slicedInfo = slicedInfo;
	}
	public Set<FileServer> getCurrentFileUploaders() {
		return currentFileUploaders;
	}
	public void setCurrentFileUploaders(Set<FileServer> currentFileUploaders) {
		this.currentFileUploaders = currentFileUploaders;
	}
	public float getCompletePercent() {
		return completePercent;
	}
	public void setCompletePercent(float completePercent) {
		this.completePercent = completePercent;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getBasePath() {
		return basePath;
	}
	public void addUploader(FileServer fs){
		this.currentFileUploaders.add(fs);
	}
	public void removeUploader(FileServer fs){
		if(this.currentFileUploaders.contains(fs)){
			this.currentFileUploaders.remove(fs);
		}
	}
	public UploadCommand getUploadCommand(){
		UploadCommand command=null;
		if(this.slicedInfo!=null){
			command=this.slicedInfo.getUploadCommandRandom();
			command.setCompletePercent(this.completePercent);
		}else{
			command=new UploadCommand();
			command.setFileId(this.getFileInfo().getFileId());
			command.setIndexStart(-1);
			command.setIndexEnd(-1);
			command.setIndex(-1);
			command.setCompletePercent(1);
		}
		return command;
	}
	/**
	 * 保存数据
	 * @param command
	 * @return
	 */
	public FileTarget saveByteBuffer(ByteBuffer bb,UploadCommand command){
		if(command.getIndexStart()!=-1){
			try {
				this.fileChannel.write(bb, command.getIndexStart());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			this.slicedInfo.blobComplete(command.getIndex());
			long currentFileSize=this.slicedInfo.currentFileSizeAdd(command.getBlobSize());
			this.completePercent=(float)currentFileSize/(float)this.fileInfo.getFileSize();
			if(this.completePercent>=1){
				this.fileUploadComplete();
			}
		}
		return this;
	}
	public void openFileWriteAccessChannel(){
		this.file=new File(this.basePath+this.fileInfo.getFileId()+".temp");
		try {
			this.raFile=new RandomAccessFile(file, "rw");
			this.raFile.setLength(this.fileInfo.getFileSize());
			this.fileChannel=this.raFile.getChannel();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void closeFileWriteAccessChannel(){
		try {
			this.fileChannel.close();
			this.raFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void fileUploadComplete(){
//		System.out.println("File upload complete.");
		this.closeFileWriteAccessChannel();
//		System.out.println("Close file access channel success.");
		this.file.renameTo(new File(this.basePath+this.fileName));
//		System.out.println("Renam file success.");
		new File(this.basePath+this.fileInfo.getFileId()+".si").delete();
//		System.out.println("Delete si file success.");
	}
}
