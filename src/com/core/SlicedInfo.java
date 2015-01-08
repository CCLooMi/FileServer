package com.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;

import com.bean.FileInfo;
import com.bean.UploadCommand;
import com.config.ServerConfigEnum;

/**
 * 文件切片信息类
 * @author xianjun
 *
 */
public class SlicedInfo implements Serializable {
	private static final long serialVersionUID = -297968415967865689L;
	private static final String basePath=ServerConfigEnum.config.getSavePath();
	private static final String suFix=".si";
	
	private long blobSize;
	private byte[] blobCompleteDetail;
	
	private long lastBlobSize;
	private String fileId;
	private File file;
	private long currentFileSize;
	
	
	private SlicedInfo(FileInfo fileInfo,long blobSize){
		this.blobSize=blobSize;
		int n=0;
		if(fileInfo.getFileSize()%this.blobSize==0){
			n=(int) (fileInfo.getFileSize()/this.blobSize);
			this.lastBlobSize=this.blobSize;
		}else{
			n=(int) (fileInfo.getFileSize()/this.blobSize)+1;
			this.lastBlobSize=fileInfo.getFileSize()%this.blobSize;
		}
		
		this.blobCompleteDetail=new byte[n];
		
		for(int i=0;i<n;i++){
			this.blobCompleteDetail[i]=0;
		}
		this.fileId=fileInfo.getFileId();
		this.file=new File(basePath+this.fileId+suFix);
	}
	private void blobLock(int index){
		this.blobCompleteDetail[index]=-1;
	}
	public void blobComplete(int index){
		this.blobCompleteDetail[index]=1;
	}
	public void blobUnComplete(int index){
		this.blobCompleteDetail[index]=0;
	}
	public synchronized long currentFileSizeAdd(long a){
		this.currentFileSize+=a;
		return this.currentFileSize;
	}
	public synchronized long currentFileSizeMinus(long a){
		this.currentFileSize-=a;
		return this.currentFileSize;
	}
	public void saveToDisk(){
//		System.out.println("Save blob info to disk.");
		this.doSerializableToDisk();
	}
	public static SlicedInfo getInstance(FileInfo fileInfo,long blobSize){
		SlicedInfo slicedInfo=null;
		File file=new File(basePath+fileInfo.getFileId()+suFix);
		if(file.exists()){
			slicedInfo=unSerializableFromDisk(file);
		}else{
			slicedInfo=new SlicedInfo(fileInfo, blobSize).doSerializableToDisk();
		}
		return slicedInfo;
	}
	private SlicedInfo doSerializableToDisk(){
		try{
			FileOutputStream out=new FileOutputStream(this.file);
			ObjectOutputStream objOut=new ObjectOutputStream(out);
			objOut.writeObject(this);
			objOut.close();
			out.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		return this;
	}
	private static SlicedInfo unSerializableFromDisk(File file){
		SlicedInfo slicedInfo=null;
		try {
			FileInputStream in=new FileInputStream(file);
			ObjectInputStream objIn=new ObjectInputStream(in);
			slicedInfo=(SlicedInfo) objIn.readObject();
			objIn.close();
			in.close();
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return slicedInfo;
	}
	public UploadCommand getUploadCommandRandom(){
		UploadCommand uc=new UploadCommand();
		uc.setFileId(this.fileId);
		int len=this.blobCompleteDetail.length;
		if(len==1){
			if(this.blobCompleteDetail[0]==0){
				uc.setIndex(0);
				uc.setIndexStart(0);
				uc.setIndexEnd(this.lastBlobSize);
				uc.setBlobSize(this.lastBlobSize);
				this.blobLock(0);
			}else{
				uc.setIndex(-1);
				uc.setIndexStart(-1);
				uc.setIndexEnd(-1);
				uc.setBlobSize(-1);
				uc.setCompletePercent(1);
			}
		}else{
			int ranIndex=new Random().nextInt(len);
			if(this.blobCompleteDetail[ranIndex]==0){
				if(ranIndex==(len-1)){
					uc.setIndex(ranIndex);
					uc.setIndexStart(ranIndex*this.blobSize);
					uc.setIndexEnd(uc.getIndexStart()+this.lastBlobSize);
					uc.setBlobSize(this.lastBlobSize);
					this.blobLock(ranIndex);
				}else{
					uc.setIndex(ranIndex);
					uc.setIndexStart(ranIndex*this.blobSize);
					uc.setIndexEnd(uc.getIndexStart()+this.blobSize);
					uc.setBlobSize(this.blobSize);
					this.blobLock(ranIndex);
				}
			}else{
				int total=0;
				while(this.blobCompleteDetail[ranIndex]!=0){
					if(ranIndex==(len-1)){
						ranIndex=0;
						if(this.blobCompleteDetail[0]==0){
							uc.setIndex(ranIndex);
							uc.setIndexStart(ranIndex*this.blobSize);
							uc.setIndexEnd(uc.getIndexStart()+this.blobSize);
							uc.setBlobSize(this.blobSize);
							this.blobLock(ranIndex);
							break;
						}
					}else if(this.blobCompleteDetail[ranIndex+1]==0){
						ranIndex++;
						if(ranIndex==(len-1)){
							uc.setIndex(ranIndex);
							uc.setIndexStart(ranIndex*this.blobSize);
							uc.setIndexEnd(uc.getIndexStart()+this.lastBlobSize);
							uc.setBlobSize(this.lastBlobSize);
							this.blobLock(ranIndex);
							break;
						}else{
							uc.setIndex(ranIndex);
							uc.setIndexStart(ranIndex*this.blobSize);
							uc.setIndexEnd(uc.getIndexStart()+this.blobSize);
							uc.setBlobSize(this.blobSize);
							this.blobLock(ranIndex);
							break;
						}
					}else{
						if(total==len){
							uc.setIndex(-1);
							uc.setIndexStart(-1);
							uc.setIndexEnd(-1);
							uc.setBlobSize(-1);
							uc.setCompletePercent(1);
							break;
						}
						ranIndex++;
						total++;
					}
				}//while end
			}
		}
		return uc;
	}
	public long getBlobSize() {
		return blobSize;
	}
	public void setBlobSize(long blobSize) {
		this.blobSize = blobSize;
	}
	public byte[] getBlobCompleteDetail() {
		return blobCompleteDetail;
	}
	public void setBlobCompleteDetail(byte[] blobCompleteDetail) {
		this.blobCompleteDetail = blobCompleteDetail;
	}
	public long getLastBlobSize() {
		return lastBlobSize;
	}
	public void setLastBlobSize(long lastBlobSize) {
		this.lastBlobSize = lastBlobSize;
	}
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public long getCurrentFileSize() {
		return currentFileSize;
	}
	public void setCurrentFileSize(long currentFileSize) {
		this.currentFileSize = currentFileSize;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public static String getBasepath() {
		return basePath;
	}
	public static String getSufix() {
		return suFix;
	}
}
