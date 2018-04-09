package com.goosejs.gchat.networking.packet.server;

import com.goosejs.gchat.networking.Packet;
import com.goosejs.gchat.networking.PacketBuffer;
import com.goosejs.gchat.networking.nethandler.client.INetHandlerLoginClient;

import java.io.IOException;

public class SPacketLoginAccepted implements Packet<INetHandlerLoginClient>
{
    private String userName;
    private String identifier;
    private String authKey;
    private String serverName;
    private String serverDescription;

    public SPacketLoginAccepted()
    {

    }

    public SPacketLoginAccepted(String userName, String identifier, String authKey, String serverName, String serverDescription)
    {
        this.userName = userName;
        this.identifier = identifier;
        this.authKey = authKey;
        this.serverName = serverName;
        this.serverDescription = serverDescription;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.userName = buf.readStringFromBuffer(255);
        this.identifier = buf.readStringFromBuffer(255);
        this.authKey = buf.readStringFromBuffer(255);
        this.serverName = buf.readStringFromBuffer(255);
        this.serverDescription = buf.readStringFromBuffer(255);
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeString(userName);
        buf.writeString(identifier);
        buf.writeString(authKey);
        buf.writeString(serverName);
        buf.writeString(serverDescription);
    }

    @Override
    public void processPacket(INetHandlerLoginClient handler)
    {
        handler.processConnection(this);
    }

    public String getAuthKey()
    {
        return authKey;
    }

    public String getServerName()
    {
        return serverName;
    }

    public String getServerDescription()
    {
        return serverDescription;
    }

}
