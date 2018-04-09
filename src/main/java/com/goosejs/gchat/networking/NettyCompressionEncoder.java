package com.goosejs.gchat.networking;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.zip.Deflater;

public class NettyCompressionEncoder extends MessageToByteEncoder<ByteBuf>
{
    private final byte[] buffer = new byte[8192];
    private final Deflater deflater;
    private int threshold;

    public NettyCompressionEncoder(int thresholdIn)
    {
        this.threshold = thresholdIn;
        this.deflater = new Deflater();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception
    {
        int compressionStrength = in.readableBytes();
        PacketBuffer packetbuffer = new PacketBuffer(out);

        if (compressionStrength < this.threshold)
        {
            packetbuffer.writeVarIntToBuffer(0);
            packetbuffer.writeBytes(in);
        }
        else
        {
            byte[] abyte = new byte[compressionStrength];
            in.readBytes(abyte);
            packetbuffer.writeVarIntToBuffer(abyte.length);
            this.deflater.setInput(abyte, 0, compressionStrength);
            this.deflater.finish();

            while (!this.deflater.finished())
            {
                int j = this.deflater.deflate(this.buffer);
                packetbuffer.writeBytes(this.buffer, 0, j);
            }

            this.deflater.reset();
        }
    }

    public void setCompressionThreshold(int thresholdIn)
    {
        this.threshold = thresholdIn;
    }

}