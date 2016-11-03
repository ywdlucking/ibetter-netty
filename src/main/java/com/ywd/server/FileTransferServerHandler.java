package com.ywd.server;

import java.io.File;
import java.io.RandomAccessFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ywd.model.RequestFile;
import com.ywd.model.ResponseFile;
import com.ywd.util.FileCommons;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class FileTransferServerHandler extends ChannelInboundHandlerAdapter{
	
	private volatile int byteRead;
	private volatile long start = 0;
	private RandomAccessFile randomAccessFile; 
	private File file ;
	private long fileSize = -1 ;
	
	private static Log log = LogFactory.getLog(FileTransferServerHandler.class);

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof RequestFile) {
			RequestFile ef = (RequestFile) msg;
			byte[] bytes = ef.getBytes();
			byteRead = ef.getEndPos();
			
			String md5 = ef.getFile_md5();//文件MD5值
			
			if(start == 0){ //只有在文件开始传的时候才进入 这样就减少了对象创建 和可能出现的一些错误
				String path = ef.getFile_target_space();
				file = new File(path);
				fileSize = ef.getFile_size();
				
				//根据 MD5 和 文件类型 来确定是否存在这样的文件 如果存在就 秒传
				if(file.exists() && file.length() != 0l) {
					log.info("file existed");
					ResponseFile responseFile = new ResponseFile();
					if(md5.equals(FileCommons.fileMD5(file))){
						responseFile.setEnd(true);
						ctx.writeAndFlush(responseFile); 
					}else{
						start = file.length();
						responseFile.setStart(start);
						ctx.writeAndFlush(responseFile); 
						randomAccessFile = new RandomAccessFile(file, "rw");
					}					
					return;
				}
				randomAccessFile = new RandomAccessFile(file, "rw");
			}		
			randomAccessFile.seek(start);
			randomAccessFile.write(bytes);
			start = start + byteRead;
			
			if (byteRead > 0 && (start < fileSize && fileSize != -1)) {
				ResponseFile responseFile = new ResponseFile();
				responseFile.setStart(start);
				ctx.writeAndFlush(responseFile);
			} else {
				log.info("transfer file successed");
				ResponseFile responseFile = new ResponseFile();
				responseFile.setEnd(true);
				ctx.writeAndFlush(responseFile);
				randomAccessFile.close();
				file = null ;
				fileSize = -1;
				randomAccessFile = null;
			}
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		log.error(cause.getMessage());
		randomAccessFile.close();
		ctx.close();
	}
	
	
}
