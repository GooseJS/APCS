package com.goosejs.gchat.networking;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class PingHandler extends ChannelInboundHandlerAdapter
{
    private final NetworkSystem networkSystem;

    public PingHandler(NetworkSystem networkSystem)
    {
        this.networkSystem = networkSystem;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        // TODO: Figure out how to make this work
        ctx.fireChannelRead(msg);
    }
}
