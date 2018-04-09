package com.goosejs.gchat.networking.packet.client;

import com.goosejs.gchat.networking.Packet;
import com.goosejs.gchat.networking.PacketBuffer;
import com.goosejs.gchat.networking.nethandler.server.INetHandlerConnectedServer;

import java.io.IOException;

public class CPacketChatMessageSend implements Packet<INetHandlerConnectedServer>
{

    private String text;
    private String user;

    public CPacketChatMessageSend() {}

    public CPacketChatMessageSend(String text, String user)
    {
        this.text = text;
        this.user = user;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.text = buf.readStringFromBuffer(1023);
        this.user = buf.readStringFromBuffer(255);
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeString(text);
        buf.writeString(user);
    }

    @Override
    public void processPacket(INetHandlerConnectedServer handler)
    {
        handler.processChatMessageSent(this);
    }

    public String getMessage()
    {
        return this.text;
    }

    public String getUser()
    {
        return user;
    }
}
