package com.goosejs.gchat.networking;

import com.goosejs.gchat.server.GChatServer;
import com.goosejs.gchat.util.Logger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class JavaNettyConnectionTest extends ChannelInboundHandlerAdapter
{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        super.channelRead(ctx, msg);
        Logger.debug("Read message from \"" + ctx.channel().toString() + "\", message {" + msg + "}");
    }
}