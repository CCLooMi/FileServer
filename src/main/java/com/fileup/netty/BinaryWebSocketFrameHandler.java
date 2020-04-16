package com.fileup.netty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fileup.bean.CommandType;
import com.fileup.bean.FileTarget;
import com.fileup.bean.UploadCommand;
import com.fileup.util.BtxUtil;
import com.fileup.util.BytesUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;


public class BinaryWebSocketFrameHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> 
implements CommandType{
	private static final Map<String, FileTarget>ftm=new ConcurrentHashMap<>();
	private Logger log=LoggerFactory.getLogger(getClass());
	private UploadCommand command=new UploadCommand();
	private String sid;
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) throws Exception {
		FileTarget ft=null;
		ByteBuf buf=msg.content();
		switch (buf.getByte(0)) {
		case UPLOAD_FILE:
			//有未完成的command需要先cancel
			this.cancelCommand();
			Map<String, Object>m=BtxUtil.btx().convertToMap(buf.nioBuffer());
			if(FileTarget.fileExist((String)m.get("id"),(String)m.get("suffix"))) {
				ctx.channel().writeAndFlush(FileTarget
						.commandSuccess()
						.toBinaryWebSocketFrame());
				break;
			}
			ft=ftm.get(m.get("id"));
			if(ft==null) {
				ft=new FileTarget(m);
				ftm.put(ft.getId(), ft);
			}
			ft.addUploadServer(sid);
			ft.nextUploadCommand(this.command);
			ctx.channel().writeAndFlush(command.toBinaryWebSocketFrame());
			break;
		case UPLOAD_DATA:
			ft=ftm.get(command.getId());
			if(ft!=null) {
				buf.readByte();//跳过第一个字节
				ft.commandComplete(command,buf);
				ft.nextUploadCommand(this.command);
				ctx.channel().writeAndFlush(command.toBinaryWebSocketFrame());
			}
			break;
		default:
			break;
		}
	}
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		this.sid=BytesUtil.bytesToCCString(BytesUtil
				.hexStringToBytes(ctx.channel()
						.id().asLongText()
						.replaceAll("-", "")));
		log.debug("handlerAdded sId:\t{}",sid);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		log.debug("handlerRemoved sid:\t{}",sid);
		cancelCommand();
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cancelCommand();
		log.debug( "",cause);
		ctx.close();
	}
	private void cancelCommand() {
		if(null==command.getId()) {
			return;
		}
		FileTarget ft=ftm.get(command.getId());
		if(ft!=null) {
			if(UPLOAD_COMPLETE==command.getHd()) {
				ftm.remove(command.getId());
			}else {
				ft.cancelCommand(command);
				ft.removeUploadServer(sid);
				if(ft.isUploadServerEmpty()) {
					//如果没有其他的上传客户端则需要移除并保存状态且释放占用资源
					ftm.remove(command.getId())
					.saveStatus()
					.releaseResource();
				}
			}
		}
	}
}
