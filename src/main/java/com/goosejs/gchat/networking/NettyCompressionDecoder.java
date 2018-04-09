package com.goosejs.gchat.networking;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;

import java.util.List;
import java.util.zip.Inflater;

public class NettyCompressionDecoder extends ByteToMessageDecoder
{

    private final Inflater inflater;
    private int threshold;

    public NettyCompressionDecoder(int thresholdIn)
    {
        this.threshold = thresholdIn;
        this.inflater = new Inflater();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        if (in.readableBytes() != 0)
        {
            PacketBuffer packetBuffer = new PacketBuffer(in);
            int compressionStrength = packetBuffer.readVarIntFromBuffer();

            if (compressionStrength == 0)
            {
                out.add(packetBuffer.readBytes(packetBuffer.readableBytes()));
            }
            else
            {
                if (compressionStrength < this.threshold)
                {
                    throw new DecoderException("Badly compressed packet - size of " + compressionStrength + " is below server threshold of " + this.threshold);
                }

                if (compressionStrength > 2097152)
                {
                    throw new DecoderException("Badly compressed packet - size of " + compressionStrength + " is larger than protocol maximum of " + 2097152);
                }

                byte[] abyte = new byte[packetBuffer.readableBytes()];
                packetBuffer.readBytes(abyte);
                this.inflater.setInput(abyte);
                byte[] abyte1 = new byte[compressionStrength];
                this.inflater.inflate(abyte1);
                out.add(Unpooled.wrappedBuffer(abyte1));
                this.inflater.reset();
            }
        }
    }

    public void setCompressionThreshold(int thresholdIn)
    {
        this.threshold = thresholdIn;
    }

}