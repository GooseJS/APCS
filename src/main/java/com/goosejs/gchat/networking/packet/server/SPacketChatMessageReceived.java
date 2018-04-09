package com.goosejs.gchat.networking.packet.server;

import com.goosejs.gchat.networking.Packet;
import com.goosejs.gchat.networking.PacketBuffer;
import com.goosejs.gchat.networking.nethandler.client.INetHandlerConnectedClient;

import java.io.IOException;

public class SPacketChatMessageReceived implements Packet<INetHandlerConnectedClient>
{

    private String text;
    private String user;

    public SPacketChatMessageReceived() {}

    public SPacketChatMessageReceived(String text, String user)
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
    public void processPacket(INetHandlerConnectedClient handler)
    {
        handler.processMessageReceived(this);
    }

    public String getMessage()
    {
        return text;
    }

    public String getUser()
    {
        return user;
    }
}
