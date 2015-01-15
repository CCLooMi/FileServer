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
			System.out.println("commandû�����,�ѻع�.");
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
//			System.out.println("�ļ������");
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
//				System.out.println("�Ѵ��ڣ������ٴ��ϴ�_: "+fileInfo.getFileName());
				this.uploadCommand=UploadCommand.getSucccessCommand(fileInfo.getFileId());
			}else{
				this.fileTarget=FileServer.fileTargetMap.get(fileInfo.getFileId());
				if(this.fileTarget==null){
//					System.out.println("��һ���ϴ�_: "+fileInfo.getFileName());
					this.fileTarget=new FileTarget(fileInfo);
					this.fileTarget.addUploader(this);
					FileServer.fileTargetMap.put(fileInfo.getFileId(), this.fileTarget);
				}
//				else{
					//�����������һ����û�������ϴ��Ŀͻ��ˣ���һ�����������ϴ��Ŀͻ��ˡ�
//					System.out.println("�ϵ�����_: "+fileInfo.getFileName());
//				}
				this.uploadCommand=this.fileTarget.getUploadCommand();
			}
		}
		response(this.uploadCommand);
		this.isCommandComplete=false;
	}
	private void cancelCommand(){
		if(this.fileTarget!=null&&this.uploadCommand!=null){
			this.fileTarget.removeUploader(this);
			if(this.uploadCommand.getCompletePercent()!=1){
				this.fileTarget.getSlicedInfo().blobUnComplete(this.uploadCommand.getIndex());
				this.isCommandComplete=true;
				//�����ǰû���ϴ����ļ��Ŀͻ������ͷ���Ӧ��Դ��������ȵ�����ͬʱҲɾ��map�и��ļ���target
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
//		System.out.println("����FileServer���ļ�uploader���Ƴ���");
		this.fileTarget.removeUploader(this);
		if(this.fileTarget.getCurrentFileUploaders().isEmpty()){
//			System.out.println("��ǰ�ļ�û���ϴ��ͻ��ˣ���map���Ƴ����ļ���FileTarget��");
			fileTargetMap.remove(this.fileTarget.getFileInfo().getFileId());
		}
	}
}
