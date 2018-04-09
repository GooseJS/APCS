package com.goosejs.gchat.networking.packet.client;

import com.goosejs.gchat.networking.nethandler.server.INetHandlerLoginServer;
import com.goosejs.gchat.networking.Packet;
import com.goosejs.gchat.networking.PacketBuffer;

import java.io.IOException;

public class CPacketLoginStart implements Packet<INetHandlerLoginServer>
{
    private String loginName;

    public CPacketLoginStart()
    {

    }

    public CPacketLoginStart(String loginName)
    {
        this.loginName = loginName;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.loginName = buf.readStringFromBuffer(32);
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeString(loginName);
    }

    @Override
    public void processPacket(INetHandlerLoginServer handler)
    {
        handler.processLogin(this);
    }

    public String getLoginName()
    {
        return loginName;
    }

}