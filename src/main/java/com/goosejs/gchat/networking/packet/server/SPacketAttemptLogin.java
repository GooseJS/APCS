package com.goosejs.gchat.networking.packet.server;

import com.goosejs.gchat.networking.Packet;
import com.goosejs.gchat.networking.PacketBuffer;
import com.goosejs.gchat.networking.nethandler.client.INetHandlerLoginClient;

import java.io.IOException;

public class SPacketAttemptLogin implements Packet<INetHandlerLoginClient>
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
    public void processPacket(INetHandlerLoginClient handler)
    {
        handler.attemptLogin();
    }
}
