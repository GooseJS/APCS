package com.goosejs.gchat.networking.packet.client;

import com.goosejs.gchat.networking.Packet;
import com.goosejs.gchat.networking.PacketBuffer;
import com.goosejs.gchat.networking.nethandler.server.INetHandlerConnectedServer;

import java.io.IOException;

public class CPacketKeepAlive implements Packet<INetHandlerConnectedServer>
{
    @Override
    public void readPacketData(PacketBuffer buf) throws IOException
    {

    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException
    {

    }

    @Override
    public void processPacket(INetHandlerConnectedServer handler)
    {

    }
}
