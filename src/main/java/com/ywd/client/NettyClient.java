package com.ywd.client;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ywd.model.RequestFile;
import com.ywd.util.FileCommons;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class NettyClient {
	
	private static Log log = LogFactory.getLog(NettyClient.class);
	
	private FileTransferClientHandler handler = null;
	
	private volatile static int flag = 1;
	
	public void connect(int port, String host, final RequestFile echoFile) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel ch) throws Exception {
					handler = new FileTransferClientHandler(echoFile);
					ch.pipeline().addLast(new ObjectEncoder());
					ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(null))); // 最大长度
					ch.pipeline().addLast(handler);
				}
			});
			ChannelFuture f = b.connect(host, port).sync();
			f.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		ExecutorService pool = Executors.newCachedThreadPool();
		for (int i = 5; i < 9; i++) {
			pool.execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						NettyClient client = new NettyClient();
						RequestFile eFile = getEFile(); 
						client.connect(10123,"10.1.43.101",eFile);
						boolean result = client.handler.getResult();
						if(result){
							log.info(eFile.getFile_name()+" transfer success");
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}								
				}
			});
		}
	}

	private static RequestFile getEFile() throws IOException {
		RequestFile result = new RequestFile();
		File file = new File("D:/test/cuteinfo.zip");
		result.setFile(file);
		result.setStarPos(0l);
		result.setFile_target_space("D:/test/cp/cuteinfo"+(flag++)+".zip");
		result.setFile_md5(FileCommons.fileMD5(file));
		return result;
	}

}
