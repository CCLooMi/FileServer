package com.service;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
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
	private static Map<String,FileTarget>fileTargetMap=new LinkedHashMap<String, FileTarget>();
	private UploadCommand uploadCommand;
	private boolean isCommandComplete=true;
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
			System.out.println("CommandCommplete: "+this.isCommandComplete);
			cancelCommand();
		}
		try {
			JsonParser jp=new JsonFactory().createJsonParser(message);
			FileInfo fileInfo=objMapper.readValue(jp, FileInfo.class);
			if(isFileOK(fileInfo)){
				response("{\"typeId\":\"uploadCommand\",\"fileId\":\""+fileInfo.getFileId()+"\",\"completePercent\":1}");
			}else{
				response(getUploadCommand(fileInfo));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void dataProcessing(ByteBuffer bb){
		this.uploadCommand=this.fileTarget.saveByteBuffer(bb, this.uploadCommand).getUploadCommand();
		this.isCommandComplete=true;
		response(this.uploadCommand);
		if(this.uploadCommand.getCompletePercent()==1){
			fileUploadComplete();
		}
	}
	private void cancelCommand(){
		if(this.fileTarget!=null&&this.uploadCommand!=null){
			this.fileTarget.removeUploader(this);
			if(this.uploadCommand.getCompletePercent()!=1){
				this.fileTarget.getSlicedInfo().blobUnComplete(this.uploadCommand.getIndex());
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
		}
	}
	private boolean isFileOK(FileInfo fileInfo){
		boolean isOK=false;
		String fileName=fileInfo.getFileId()+"."+fileInfo.getFileName().substring(fileInfo.getFileName().lastIndexOf(".")+1);
		if(new File(ServerConfigEnum.config.getSavePath()+fileName).exists()||fileInfo.getFileSize()==0){
			isOK=true;
		}
		return isOK;
	}
	private void response(String data){
		try {
			this.session.getBasicRemote().sendText(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void response(Object obj){
		try {
			response(this.objMapper.writeValueAsString(obj));
			this.isCommandComplete=false;
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void fileUploadComplete(){
		fileTargetMap.remove(this.fileTarget.getFileInfo().getFileId());
		this.isCommandComplete=true;
//		System.out.println("Remove current file target from fileTargetMap success.");
	}
	private UploadCommand getUploadCommand(FileInfo fileInfo){
		this.fileTarget=fileTargetMap.get(fileInfo.getFileId());
		if(this.fileTarget==null){
			this.fileTarget=new FileTarget(fileInfo);
			fileTargetMap.put(fileInfo.getFileId(), this.fileTarget);
		}
		this.fileTarget.addUploader(this);
		this.uploadCommand=this.fileTarget.getUploadCommand();
		return this.uploadCommand;
	}
}
