package com.fileup;

import java.net.InetSocketAddress;

import com.fileup.netty.WebSocketChannelInitializer;
import com.fileup.util.Paths;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class WebSocketServer {
	public static void main(String[] args) throws Exception{
		LogbackInit.initLogback(Paths.getBaseUserDir("config","logback.xml"));
		System.setProperty("io.netty.noUnsafe","true");
		EventLoopGroup boss=new NioEventLoopGroup();
		EventLoopGroup woker=new NioEventLoopGroup();
		try {
			ServerBootstrap boot=new ServerBootstrap();
			boot.group(boss, woker)
			.channel(NioServerSocketChannel.class)
			.childHandler(new WebSocketChannelInitializer());
			
			ChannelFuture cf=boot
					.bind(new InetSocketAddress(Config
							.getConfigAsInteger("server.port",8899)))
					.sync();
			cf.channel().closeFuture().sync();
		}finally {
			boss.shutdownGracefully();
			woker.shutdownGracefully();
		}
	}
}
