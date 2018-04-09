package com.goosejs.gchat.networking.packet.client;

import com.goosejs.gchat.networking.Packet;
import com.goosejs.gchat.networking.PacketBuffer;
import com.goosejs.gchat.networking.nethandler.server.INetHandlerConnectedServer;
import com.goosejs.gchat.user.UserPermissions;
import com.goosejs.gchat.user.UserProfile;

import java.io.IOException;

public class CPacketConnectionInfo implements Packet<INetHandlerConnectedServer>
{
    private UserProfile userProfile;

    public CPacketConnectionInfo() {}

    public CPacketConnectionInfo(UserProfile userProfile)
    {
        this.userProfile = userProfile;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.userProfile = new UserProfile(buf.readStringFromBuffer(255), buf.readStringFromBuffer(255), buf.readStringFromBuffer(255), buf.readStringFromBuffer(255), buf.readVarIntFromBuffer(), new UserPermissions()); // TODO: Update UserPermissions once the permissions system is in place
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeString(userProfile.getUserName());
        buf.writeString(userProfile.getIdentifier());
        buf.writeString(userProfile.getAuthKey());
        buf.writeString(userProfile.getIp());
        buf.writeVarIntToBuffer(userProfile.getConnectionTime());
    }

    @Override
    public void processPacket(INetHandlerConnectedServer handler)
    {
        handler.processConnection(this);
    }

    public UserProfile getUserProfile()
    {
        return userProfile;
    }
}
