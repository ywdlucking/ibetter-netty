package com.ywd.client;

import java.io.RandomAccessFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ywd.model.RequestFile;
import com.ywd.model.ResponseFile;
import com.ywd.model.SecureModel;
import com.ywd.util.Encryptor;
import com.ywd.util.LocalIp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class FileTransferClientHandler extends ChannelInboundHandlerAdapter{
	
	private RequestFile requestFile;
	private boolean result;
	private int byteRead;
	private volatile long start = 0;
	public RandomAccessFile randomAccessFile;
	private final int minReadBufferSize = 8192;
	private static Log log = LogFactory.getLog(FileTransferClientHandler.class);
	
	public FileTransferClientHandler(RequestFile requestFile) {
		this.requestFile = requestFile;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		SecureModel secure = new SecureModel();
		secure.setToken(Encryptor.encrypt(LocalIp.ip));
		ctx.writeAndFlush(secure);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if(msg instanceof SecureModel){
			SecureModel sm = (SecureModel)msg;
			if(sm.isSuccess()){
				randomAccessFile = new RandomAccessFile(requestFile.getFile(), "r");
				randomAccessFile.seek(requestFile.getStarPos());
				byte[] bytes = new byte[minReadBufferSize];
				if ((byteRead = randomAccessFile.read(bytes)) != -1) {
					requestFile.setEndPos(byteRead);
					requestFile.setBytes(bytes);
					requestFile.setFile_size(randomAccessFile.length());
					ctx.writeAndFlush(requestFile);
				} else {
					result = true;
					log.info("file transfer success");
				}				
				return;
			}else{
				result = false;
				log.error("velidated failed");
				return;
			}
		}
		
		if(msg instanceof ResponseFile){
			ResponseFile response = (ResponseFile)msg;
			if(response.isEnd()){
				result = true;
				randomAccessFile.close();
				ctx.close();
			}else{
				start = response.getStart();
				if (start != -1) {
					randomAccessFile = new RandomAccessFile(requestFile.getFile(), "r");
					randomAccessFile.seek(start);
					int a = (int) (randomAccessFile.length() - start);
					int sendLength = minReadBufferSize;
					if (a < minReadBufferSize) {
						sendLength = a;
					}
					byte[] bytes = new byte[sendLength];
					if ((byteRead = randomAccessFile.read(bytes)) != -1 && (randomAccessFile.length() - start) > 0) {
						requestFile.setEndPos(byteRead);
						requestFile.setBytes(bytes);
						ctx.writeAndFlush(requestFile);
					} else {
						result = true;
						randomAccessFile.close();
						ctx.close();
					}
				}
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error(cause.getMessage());
		ctx.close();
	}
	
	public boolean getResult() {
		return result;
	}

}
