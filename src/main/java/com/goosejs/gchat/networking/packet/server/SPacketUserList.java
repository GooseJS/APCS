package com.goosejs.gchat.networking.packet.server;

import com.goosejs.gchat.networking.Packet;
import com.goosejs.gchat.networking.PacketBuffer;
import com.goosejs.gchat.networking.nethandler.client.INetHandlerConnectedClient;
import com.goosejs.gchat.user.UserPermissions;
import com.goosejs.gchat.user.UserProfile;

import java.io.IOException;
import java.util.ArrayList;

public class SPacketUserList implements Packet<INetHandlerConnectedClient>
{

    private UserProfile[] users;
    private int userCount;

    public SPacketUserList() {}

    public SPacketUserList(UserProfile[] users)
    {
        this.users = users;
        this.userCount = users.length;
    }

    public SPacketUserList(ArrayList<UserProfile> users)
    {
        this.users = new UserProfile[users.size()];
        for (int i = 0; i < this.users.length; i++) { this.users[i] = users.get(i); }
        this.userCount = this.users.length;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.userCount = buf.readVarIntFromBuffer();
        this.users = new UserProfile[userCount];

        for (int i = 0; i < userCount; i++)
        {
            this.users[i] = new UserProfile(buf.readStringFromBuffer(255), buf.readStringFromBuffer(255), buf.readStringFromBuffer(255), buf.readStringFromBuffer(255), buf.readVarIntFromBuffer(), new UserPermissions()); // TODO: Update UserPermissions once the permissions system is in place;
        }
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(userCount);

        for (int i = 0; i < userCount; i++)
        {
            buf.writeString(users[i].getUserName());
            buf.writeString(users[i].getIdentifier());
            buf.writeString(users[i].getAuthKey());
            buf.writeString(users[i].getIp());
            buf.writeVarIntToBuffer(users[i].getConnectionTime());
        }
    }

    @Override
    public void processPacket(INetHandlerConnectedClient handler)
    {
        handler.processUserList(this);
    }

    public UserProfile[] getUsers()
    {
        return users;
    }
}
