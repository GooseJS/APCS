package com.goosejs.gchat.networking;

import com.goosejs.gchat.util.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.IOException;

public class NettyPacketEncoder extends MessageToByteEncoder<Packet<?>>
{

    private static final Marker RECEIVED_PACKET_MARKER = MarkerManager.getMarker("PACKET_SENT");
    private final EnumPacketDirection direction;

    public NettyPacketEncoder(EnumPacketDirection direction)
    {
        this.direction = direction;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet<?> in, ByteBuf out) throws Exception
    {
        Integer packetID = ctx.channel().attr(NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get()
                .getPacketID(this.direction, in);

        if (Logger.isDebugEnabled())
        {
            if (!NetworkSystem.ignoredPackets.contains(in.getClass().getSimpleName().replace("SPacket", "").replace("CPacket", "")))
            {
                Logger.debug("OUT: [{}:{}] {}", ctx.channel().attr(NetworkManager
                        .PROTOCOL_ATTRIBUTE_KEY).get(), packetID, in.getClass().getName()); // TODO: Logger OUT Text
            }
        }

        if (packetID == null)
        {
            throw new IOException("Can\'t serialize unregistered packet");
        }
        else
        {
            PacketBuffer packetBuffer = new PacketBuffer(out);
            packetBuffer.writeVarIntToBuffer(packetID);
//troll
            try
            {
                in.writePacketData(packetBuffer);
            }
            catch (Throwable throwable)
            {
                Logger.error(throwable);
            }
        }
    }

}
