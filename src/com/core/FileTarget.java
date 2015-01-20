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
	private File siFile;
	private File tempFile;
	private RandomAccessFile raFile;
	private FileChannel fileChannel;
	
	public FileTarget(){
		super();
	}
	public FileTarget(FileInfo fileInfo){
		this.currentFileUploaders=new CopyOnWriteArraySet<FileServer>();
		this.setFileInfo(fileInfo);
		this.openFileWriteAccessChannel();
//		System.out.println("Create FileTarget successed.: "+fileInfo.getFileId());
	}
	public static boolean isFileOK(FileInfo fileInfo){
		boolean isOK=false;
		String fileName=fileInfo.getFileId()+"."+fileInfo.getFileName().substring(fileInfo.getFileName().lastIndexOf(".")+1);
		if(new File(ServerConfigEnum.config.getSavePath()+fileName).exists()||fileInfo.getFileSize()==0){
			isOK=true;
		}
		return isOK;
	}
	public FileInfo getFileInfo() {
		return fileInfo;
	}
	public void setFileInfo(FileInfo fileInfo) {
		this.fileInfo = fileInfo;
		this.fileName=this.fileInfo.getFileId()+"."+this.fileInfo.getFileName().substring(this.fileInfo.getFileName().lastIndexOf(".")+1);
		this.file=new File(this.basePath+this.fileName);
		this.siFile=new File(this.basePath+this.fileInfo.getFileId()+ServerConfigEnum.config.getSuFix());
		this.tempFile=new File(this.basePath+this.fileInfo.getFileId()+".temp");
		if(!this.file.exists()&&this.slicedInfo==null){
			this.slicedInfo=SlicedInfo.getInstance(fileInfo);
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
		this.currentFileUploaders.remove(fs);
	}
	public synchronized UploadCommand getUploadCommand(){
		UploadCommand command;
		if(this.slicedInfo==null){
			command=UploadCommand.getSucccessCommand(this.fileInfo.getFileId());
		}else{
			command=this.slicedInfo.getUploadCommandRandom();
		}
		return command;
	}
	/**
	 * 保存数据
	 * @param command
	 * @return
	 */
	public synchronized FileTarget saveByteBuffer(ByteBuffer bb,UploadCommand command){
		if(command.getIndexStart()!=-1){
			try {
				this.fileChannel.write(bb, command.getIndexStart());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			this.slicedInfo.blobComplete(command.getIndex());
			this.completePercent=this.slicedInfo.currentFileSizeAdd(command.getBlobSize()).getCompletePercent();
			if(this.completePercent>=1){
				this.fileUploadComplete();
			}
		}
		return this;
	}
	public void openFileWriteAccessChannel(){
		try {
			this.raFile=new RandomAccessFile(this.tempFile, "rw");
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
		this.tempFile.renameTo(this.file);
//		System.out.println("Renam file success.");
		this.siFile.delete();
//		System.out.println("Delete si file success.");
	}
}
