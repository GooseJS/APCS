package com.goosejs.gchat.networking.packet.server;

import com.goosejs.gchat.networking.Packet;
import com.goosejs.gchat.networking.PacketBuffer;
import com.goosejs.gchat.networking.nethandler.client.INetHandlerConnectedClient;

import java.io.IOException;

public class SPacketPrivateMessage implements Packet<INetHandlerConnectedClient>
{
    private String sentUser;
    private String receivingUser;
    private String message;

    public SPacketPrivateMessage() {}

    public SPacketPrivateMessage(String sentUser, String receivingUser, String message)
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
    public void processPacket(INetHandlerConnectedClient handler)
    {
        handler.processPM(sentUser, receivingUser, message);
    }
}
