package com.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.bean.FileInfo;
import com.bean.UploadCommand;
import com.config.ServerConfigEnum;
import com.core.FileTarget;

@ServerEndpoint("/websocket/fileup")
public class FileServer {
	private static Map<String,FileTarget>fileTargetMap=new HashMap<String, FileTarget>();
	private boolean isCommandComplete=true;
	private UploadCommand uploadCommand;
	private Session session;
	private FileTarget fileTarget;
	ObjectMapper objMapper;
	@OnOpen
	public void onOpen(Session session){
		this.session=session;
		this.session.setMaxBinaryMessageBufferSize(ServerConfigEnum.config.getBlobSize()+8);
		this.objMapper=new ObjectMapper();
	}
	@OnClose
	public void onClose(){
		cancelCommand();
	}
	@OnMessage
	public void onTextMessage(String message){
		dataProcessing(message);
	}
	@OnMessage
	public void onBinaryMessage(ByteBuffer bb){
		dataProcessing(bb);
	}
	@OnError
	public void onError(Throwable t){
		t.printStackTrace();
	}
	private void dataProcessing(String message){
		if(!this.isCommandComplete){
			cancelCommand();
			System.out.println("command没有完成,已回滚.");
		}
		try {
			JsonParser jp=new JsonFactory().createJsonParser(message);
			FileInfo fileInfo=objMapper.readValue(jp, FileInfo.class);
			sendFirstUploadCommand(fileInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void dataProcessing(ByteBuffer bb){
		this.isCommandComplete=true;
		this.uploadCommand=this.fileTarget.saveByteBuffer(bb, this.uploadCommand).getUploadCommand();
		response(this.uploadCommand);
		this.isCommandComplete=false;
		if(this.uploadCommand.getCompletePercent()==1){
//			System.out.println("文件上完成");
			fileUloadComplete();
			this.isCommandComplete=true;
		}
	}
	private void response(String data){
		try {
			this.session.getBasicRemote().sendText(data);
//			System.out.println("data: "+data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void response(Object obj){
		try {
			response(this.objMapper.writeValueAsString(obj));
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void sendFirstUploadCommand(FileInfo fileInfo){
		synchronized (fileTargetMap) {
			if(FileTarget.isFileOK(fileInfo)){
//				System.out.println("已存在，无需再次上传_: "+fileInfo.getFileName());
				this.uploadCommand=UploadCommand.getSucccessCommand(fileInfo.getFileId());
			}else{
				this.fileTarget=FileServer.fileTargetMap.get(fileInfo.getFileId());
				if(this.fileTarget==null){
//					System.out.println("第一次上传_: "+fileInfo.getFileName());
					this.fileTarget=new FileTarget(fileInfo);
					this.fileTarget.addUploader(this);
					FileServer.fileTargetMap.put(fileInfo.getFileId(), this.fileTarget);
				}
//				else{
					//分两种情况，一种是没有正在上传的客户端，另一种是有正在上传的客户端。
//					System.out.println("断点续传_: "+fileInfo.getFileName());
//				}
				this.uploadCommand=this.fileTarget.getUploadCommand();
			}
		}
		response(this.uploadCommand);
		if(this.uploadCommand.getCompletePercent()==1){
			this.isCommandComplete=true;
		}else{
			this.isCommandComplete=false;
		}
	}
	private void cancelCommand(){
		if(this.fileTarget!=null&&this.uploadCommand!=null){
			this.fileTarget.removeUploader(this);
			if(this.uploadCommand.getCompletePercent()!=1){
				this.fileTarget.getSlicedInfo().blobUnComplete(this.uploadCommand.getIndex());
				this.isCommandComplete=true;
				//如果当前没有上传该文件的客户端则释放相应资源并保存进度到磁盘同时也删除map中该文件的target
				if(this.fileTarget.getCurrentFileUploaders().isEmpty()){
					this.fileTarget.getSlicedInfo().saveToDisk();
					this.fileTarget.closeFileWriteAccessChannel();
					fileTargetMap.remove(this.fileTarget.getFileInfo().getFileId());
				}
			}else{
				this.uploadCommand=null;
			}
			this.fileTarget=null;
		}//if end
	}
	private void fileUloadComplete(){
//		System.out.println("将本FileServer从文件uploader中移除。");
		this.fileTarget.removeUploader(this);
		if(this.fileTarget.getCurrentFileUploaders().isEmpty()){
//			System.out.println("当前文件没有上传客户端，从map中移除该文件的FileTarget。");
			fileTargetMap.remove(this.fileTarget.getFileInfo().getFileId());
		}
	}
}
