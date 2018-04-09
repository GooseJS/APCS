package com.goosejs.gchat.networking.packet.server;

import com.goosejs.gchat.networking.Packet;
import com.goosejs.gchat.networking.PacketBuffer;
import com.goosejs.gchat.networking.nethandler.client.INetHandlerConnectedClient;

import java.io.IOException;

public class SPacketUserDisconnect implements Packet<INetHandlerConnectedClient>
{

    private String userName; // TODO: Change this over to identifiers

    public SPacketUserDisconnect() {}

    public SPacketUserDisconnect(String userName)
    {
        this.userName = userName;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.userName = buf.readStringFromBuffer(255);
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeString(this.userName);
    }

    @Override
    public void processPacket(INetHandlerConnectedClient handler)
    {
        handler.handleUserDisconnect(this);
    }

    public String getUserName()
    {
        return this.userName;
    }

}
