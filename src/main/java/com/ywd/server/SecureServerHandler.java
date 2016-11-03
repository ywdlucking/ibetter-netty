package com.ywd.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ywd.model.SecureModel;
import com.ywd.util.Encryptor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class SecureServerHandler extends ChannelInboundHandlerAdapter{
	
	private static Log log = LogFactory.getLog(SecureServerHandler.class);
    static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if(msg instanceof SecureModel){
			SecureModel secureModel = (SecureModel)msg;
			if(secureModel.getToken() != null ){
				String token = Encryptor.decrypt(secureModel.getToken());
				String vl = ctx.channel().remoteAddress().toString().split(":")[0].substring(1);
				if(vl.equals(token)){
					log.info("token validated sucess");
					channels.add(ctx.channel());
        			secureModel.setAutoSuccess(true);
                	ctx.writeAndFlush(secureModel);
        			return ;
				}
			}else{
				log.info("token is null");
				secureModel.setAutoSuccess(false);
	        	ctx.writeAndFlush(secureModel);
	        	ctx.close();
			}
		}else {
			if(!channels.contains(ctx.channel())) {
				log.info("channel is validated");
        		SecureModel secureModel = new SecureModel();
        		secureModel.setAutoSuccess(false);
            	ctx.writeAndFlush(secureModel);
            	ctx.close();
        	} else {
        		ctx.fireChannelRead(msg);
        	}
		}
	}

	
}
