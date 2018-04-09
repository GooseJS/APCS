package com.goosejs.gchat.networking;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyPrepender extends MessageToByteEncoder<ByteBuf>
{
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception
    {
        int readableBytes = msg.readableBytes();
        int readableSize = PacketBuffer.getVarIntSize(readableBytes);

        if (readableSize > 3)
        {
            throw new IllegalArgumentException("unable to fit " + readableBytes + " into " + 3);
        }
        else
        {
            PacketBuffer packetBUffer = new PacketBuffer(out);
            packetBUffer.ensureWritable(readableSize + readableBytes);
            packetBUffer.writeVarIntToBuffer(readableBytes);
            packetBUffer.writeBytes(msg, msg.readerIndex(), readableBytes);
        }
    }
}
