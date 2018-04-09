package com.goosejs.gchat.networking;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;

public class NettySplitter extends ByteToMessageDecoder
{
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        in.markReaderIndex();
        byte[] readingBytes = new byte[3];

        for (int i = 0; i < readingBytes.length; ++i)
        {
            if (!in.isReadable())
            {
                in.resetReaderIndex();
                return;
            }

            readingBytes[i] = in.readByte();

            if (readingBytes[i] >= 0)
            {
                PacketBuffer packetbuffer = new PacketBuffer(Unpooled.wrappedBuffer(readingBytes));

                try
                {
                    int readableSize = packetbuffer.readVarIntFromBuffer();

                    if (in.readableBytes() >= readableSize)
                    {
                        out.add(in.readBytes(readableSize));
                        return;
                    }

                    in.resetReaderIndex();
                }
                finally
                {
                    packetbuffer.release();
                }

                return;
            }
        }

        throw new CorruptedFrameException("length wider than 21-bit");
    }
}