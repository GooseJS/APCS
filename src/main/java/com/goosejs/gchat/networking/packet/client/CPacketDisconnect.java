package com.goosejs.gchat.networking.packet.client;

import com.goosejs.gchat.networking.Packet;
import com.goosejs.gchat.networking.PacketBuffer;
import com.goosejs.gchat.networking.nethandler.server.INetHandlerConnectedServer;

import java.io.IOException;

public class CPacketDisconnect implements Packet<INetHandlerConnectedServer>
{

    private String userName;
    private String disconnectReason;

    public CPacketDisconnect() {}

    public CPacketDisconnect(String userName, String disconnectReason)
    {
        this.userName = userName;
        this.disconnectReason = disconnectReason;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        userName = buf.readStringFromBuffer(255);
        disconnectReason = buf.readStringFromBuffer(255);
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeString(userName);
        buf.writeString(disconnectReason);
    }

    @Override
    public void processPacket(INetHandlerConnectedServer handler)
    {
        handler.processDisconnection(this);
    }

    public String getUserName()
    {
        return userName;
    }
}
