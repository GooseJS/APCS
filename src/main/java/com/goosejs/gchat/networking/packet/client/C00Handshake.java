package com.goosejs.gchat.networking.packet.client;

import com.goosejs.gchat.networking.EnumConnectionState;
import com.goosejs.gchat.networking.nethandler.server.INetHandlerHandshakeServer;
import com.goosejs.gchat.networking.Packet;
import com.goosejs.gchat.networking.PacketBuffer;

import java.io.IOException;

public class C00Handshake implements Packet<INetHandlerHandshakeServer>
{

    private int protocolVersion;
    private String ip;
    private int port;
    private EnumConnectionState requestedState;

    public C00Handshake()
    {

    }

    public C00Handshake(int version, String ip, int port, EnumConnectionState requestedState)
    {
        this.protocolVersion = version;
        this.ip = ip;
        this.port = port;
        this.requestedState = requestedState;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.protocolVersion = buf.readVarIntFromBuffer();
        this.ip = buf.readStringFromBuffer(255);
        this.port = buf.readUnsignedShort();
        int enumConnectionStateID = buf.readVarIntFromBuffer();
        this.requestedState = EnumConnectionState.getByID(enumConnectionStateID);
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(protocolVersion);
        buf.writeString(ip);
        buf.writeShort(port);
        buf.writeVarIntToBuffer(requestedState.getID());
    }

    @Override
    public void processPacket(INetHandlerHandshakeServer handler)
    {
        handler.processHandshake(this);
    }

    public EnumConnectionState getRequestedState()
    {
        return this.requestedState;
    }

    public int getProtocolVersion()
    {
        return this.protocolVersion;
    }
}
