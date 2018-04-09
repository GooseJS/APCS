package com.goosejs.gchat.networking;

import com.goosejs.gchat.util.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.IOException;
import java.util.List;

public class NettyPacketDecoder extends ByteToMessageDecoder
{
    private static final Marker RECEIVED_PACKET_MARKER = MarkerManager.getMarker("PACKET_RECEIVED");
    private final EnumPacketDirection direction;

    public NettyPacketDecoder(EnumPacketDirection direction)
    {
        this.direction = direction;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        if (in.readableBytes() != 0)
        {
            PacketBuffer packetbuffer = new PacketBuffer(in);
            int i = packetbuffer.readVarIntFromBuffer();
            Packet<?> packet = ctx.channel().attr(NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get().getPacket(this.direction, i);

            if (packet == null)
            {
                throw new IOException("Bad packet id " + i);
            }
            else
            {
                packet.readPacketData(packetbuffer);

                if (packetbuffer.readableBytes() > 0)
                {
                    throw new IOException("Packet " + ctx.channel().attr(NetworkManager
                        .PROTOCOL_ATTRIBUTE_KEY).get().getID() + "/" + i + " (" + packet.getClass().getSimpleName()
                            + ") was larger than I expected, found " + packetbuffer.readableBytes() + " bytes extra whilst reading packet " + i);
                }
                else
                {
                    out.add(packet);

                    if (Logger.isDebugEnabled())
                    {
                        if (!NetworkSystem.ignoredPackets.contains(packet.getClass().getSimpleName().replace("SPacket", "").replace("CPacket", "")))
                        {
                            Logger.debug(RECEIVED_PACKET_MARKER, "IN: [{}:{}] {}", ctx.channel().attr
                                    (NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get(), i, packet.getClass().getName()); // TODO: Logger IN Text
                        }
                    }
                }
            }
        }
    }
}
