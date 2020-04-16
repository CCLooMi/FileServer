package com.fileup.bean;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.fileup.Config;
import com.fileup.util.Paths;
import com.fileup.util.RocksDbUtil;
import com.fileup.util.StringUtil;

import io.netty.buffer.ByteBuf;

/**
 * © 2015-2019 Chenxj Copyright
 * 类    名：FileTarget
 * 类 描 述：文件处理类，有并发需要注意
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2019年1月19日-下午12:31:44
 */
public class FileTarget implements CommandType{
	private String id;
	private String name;
	private String suffix;
	private String type;
	private long size;
	private BSet bSet;
	//bSet开始检查位置
	private int iStart=0;
	private long completeFileSize=0;
	private float completePercent=0;
	private Queue<String>uploadServers=new ConcurrentLinkedQueue<>();
	
	private RandomAccessFile raf;
	private FileChannel fc;
	private File tmpFile;
	public FileTarget(Map<String, Object>fileInfo) {
		this.id=(String) fileInfo.get("id");
		this.name=(String) fileInfo.get("name");
		this.suffix=(String) fileInfo.get("suffix");
		this.type=(String) fileInfo.get("type");
		this.size=new BigDecimal( fileInfo.get("size").toString()).longValue();
		
		byte[]bs=RocksDbUtil.get(id);
		if(null!=bs) {
			this.bSet=new BSet(bs);
			//查找iStart位置
			for(int i=0;i<bSet.getLength();i++) {
				if(!bSet.bit(i)) {
					this.iStart=i;
					break;
				}
			}
			//计算上传完成size
			long completeSize=iStart*Config.blobSize;
			for(int i=iStart;i<bSet.getLength();i++) {
				if(bSet.bit(i)) {
					if(i!=bSet.getLength()-1) {
						completeSize+=Config.blobSize;
					}else {
						completeSize+=this.size%Config.blobSize;
					}
				}
			}
			//设置完成size
			addCompleteFileSize(completeSize);
		}else {
			int bSetSize=(int)(this.size/(Config.blobSize));
			if(this.size%Config.blobSize>0) {
				bSetSize++;
			}
			this.bSet=new BSet(bSetSize);
		}
		try {
			this.tmpFile=fileWithEnd(this.id, this.suffix, ".tmp");
			if(!tmpFile.getParentFile().exists()) {
				tmpFile.getParentFile().mkdirs();
			}
			this.raf=new RandomAccessFile(this.tmpFile, "rw");
			this.fc=raf.getChannel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static boolean fileExist(String id,String suffix) {
		return fileWithEnd(id, suffix, "").exists();
	}
	public static String getFilePath(String id) {
		long v1=StringUtil.strtol(id.substring(0, 3), 16);
		long v2=StringUtil.strtol(id.substring(3, 6), 16);
		return "/"+v1/4+"/"+v2/4+"/"+id+"/";
	}
	public static UploadCommand commandSuccess() {
		return new UploadCommand().setHd(UPLOAD_COMPLETE);
	}
	private FileTarget addCompleteFileSize(long sz) {
		this.completeFileSize+=sz;
		this.completePercent=this.size>0?(float)completeFileSize/this.size:1;
		return this;
	}
	public boolean isUploadServerEmpty() {
		return this.uploadServers.isEmpty();
	}
	public FileTarget addUploadServer(String sId) {
		this.uploadServers.add(sId);
		return this;
	}
	public FileTarget removeUploadServer(String sId) {
		this.uploadServers.remove(sId);
		return this;
	}
	public FileTarget commandComplete(UploadCommand command,byte[]data) {
		return commandComplete(command, ByteBuffer.wrap(data));
	}
	public FileTarget commandComplete(UploadCommand command,ByteBuf data) {
		return commandComplete(command, data.nioBuffer());
	}
	public FileTarget commandComplete(UploadCommand command,ByteBuffer data) {
		try {
			this.fc.write(data,command.getIndexStart());
			addCompleteFileSize(command.blobSize());
			this.bSet.setBit(command.getIndex());
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(this.completeFileSize==this.size) {
				this.uploadServers.clear();
				RocksDbUtil.delete(id);
				this.releaseResource();
				if(this.tmpFile.exists()) {
					String p=this.tmpFile.getAbsolutePath();
					this.tmpFile.renameTo(new File(p.substring(0, p.length()-4)));
				}
			}
		}
		return this;
	}
	public  FileTarget nextUploadCommand(UploadCommand command) {
		for(int i=iStart;i<bSet.getLength();i++) {
			if(!bSet.bit(i)) {
				iStart=i+1;
				long indexStart=(long)i*Config.blobSize;
				long indexEnd=(indexStart+Config.blobSize)>size
						?size
						:(indexStart+Config.blobSize);
				command
				.setHd(UPLOAD_COMMAND)
				.setId(id)
				.setIndex(i)
				.setIndexStart(indexStart)
				.setIndexEnd(indexEnd)
				.setCompletePercent(completePercent);
				return this;
			}
		}
		//ID 不能为空，handler需要用来清除FileTarget
		command.setHd(UPLOAD_COMPLETE).setId(id);
		return this;
	}
	public FileTarget cancelCommand(UploadCommand command) {
		this.bSet.unsetBit(command.getIndex());
		if(command.getIndex()<iStart) {
			this.iStart=command.getIndex();
		}
		return this;
	}
	public FileTarget saveStatus() {
		RocksDbUtil.put(id, this.bSet.toBytes());
		return this;
	}
	public void releaseResource() {
		try {
			this.fc.close();
			this.raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static File fileWithEnd(String id,String suffix,String end) {
		File file=null;
		String savePath=Config.getConfig("server.file.save.path","upload");
		if(Config.decktopWindow) {
			if(savePath.charAt(0)=='/'||savePath.charAt(0)=='\\') {
				file=Paths.getFile(savePath,getFilePath(id),suffix+end);
			}else {
				file=Paths.getUserDirFile(savePath,getFilePath(id),suffix+end);
			}
		}else {
			if(Config.isZimgTypes(suffix)) {
				String zpath=Config.getConfig("server.file.zimg.path","");
				if(zpath.charAt(0)=='/') {
					file=Paths.getFile(zpath,getFilePath(id),"0*0"+end);
				}else {
					file=Paths.getUserDirFile(zpath,getFilePath(id),"0*0"+end);
				}
			}else {
				if(savePath.charAt(0)=='/') {
					file=Paths.getFile(savePath,getFilePath(id),suffix+end);
				}else {
					file=Paths.getUserDirFile(savePath,getFilePath(id),suffix+end);
				}
			}
		}
		return file;
	}
	/**获取 id*/
	public String getId() {
		return id;
	}
	/**获取 name*/
	public String getName() {
		return name;
	}
	public String getSuffix() {
		return suffix;
	}
	/**获取 type*/
	public String getType() {
		return type;
	}
	/**获取 size*/
	public long getSize() {
		return size;
	}
	/**获取 completeFileSize*/
	public long getCompleteFileSize() {
		return completeFileSize;
	}
	/**获取 completePercent*/
	public float getCompletePercent() {
		return completePercent;
	}
}
