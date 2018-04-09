package com.goosejs.gchat.networking.packet.client;

import com.goosejs.gchat.networking.Packet;
import com.goosejs.gchat.networking.PacketBuffer;
import com.goosejs.gchat.networking.nethandler.server.INetHandlerConnectedServer;

import java.io.IOException;

public class CPacketPrivateMessage implements Packet<INetHandlerConnectedServer>
{
    private String sentUser;
    private String receivingUser;
    private String message;

    public CPacketPrivateMessage() {}

    public CPacketPrivateMessage(String sentUser, String receivingUser, String message)
    {
        this.sentUser = sentUser;
        this.receivingUser = receivingUser;
        this.message = message;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.sentUser = buf.readStringFromBuffer(255);
        this.receivingUser = buf.readStringFromBuffer(255);
        this.message = buf.readStringFromBuffer(1023);
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeString(sentUser);
        buf.writeString(receivingUser);
        buf.writeString(message);
    }

    @Override
    public void processPacket(INetHandlerConnectedServer handler)
    {
        handler.processPM(sentUser, receivingUser, message);
    }
}
