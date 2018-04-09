package com.goosejs.gchat.server;

import com.goosejs.gchat.networking.EnumConnectionState;
import com.goosejs.gchat.networking.NetworkSystem;
import com.goosejs.gchat.networking.Packet;
import com.goosejs.gchat.networking.packet.server.SPacketUserDisconnect;
import com.goosejs.gchat.networking.packet.server.SPacketUserJoined;
import com.goosejs.gchat.networking.packet.server.SPacketUserList;
import com.goosejs.gchat.user.UserProfile;
import com.goosejs.gchat.util.Logger;
import io.netty.channel.Channel;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;

public class Server
{

    private ArrayList<UserProfile> userProfiles;

    private NetworkSystem networkSystem;

    public Server(NetworkSystem networkSystem)
    {
        this.userProfiles = new ArrayList<>();
        this.networkSystem = networkSystem;
    }

    private int userListUpdateCounter = 100;
    public void update()
    {
        networkSystem.networkTick();

        if (userListUpdateCounter-- == 0)
        {
            userListUpdateCounter = 20;
            networkSystem.sendPacket(new SPacketUserList(userProfiles), EnumConnectionState.CONNECTED);
        }
    }

    public String addUser(UserProfile user)
    {
        String duplicate = checkForDuplicateUser(user);
        if (duplicate == "pass")
        {
            if (userProfiles.add(user))
            {
                networkSystem.sendPacket(new SPacketUserJoined(user.getUserName()), EnumConnectionState.CONNECTED);
                Logger.info(user + " has connected.w");
                return "pass";
            }
            else
                return "fail";
        }
        else
        {
            return duplicate;
        }
    }

    public boolean removeUser(String user) // TODO: Start using UserProfiles
    {
        Iterator<UserProfile> profileIterator = userProfiles.iterator();
        while (profileIterator.hasNext())
        {
            UserProfile tempUser = profileIterator.next();

            if (tempUser.getUserName().equals(user))
            {
                profileIterator.remove();
                networkSystem.sendPacket(new SPacketUserDisconnect(user), EnumConnectionState.CONNECTED);
                Logger.info(user + " has disconnected.");
                return true;
            }
            else if (tempUser.getIdentifier().equals(user))
            {
                profileIterator.remove();
                Logger.info(user + " has disconnected.");
                networkSystem.sendPacket(new SPacketUserDisconnect(user), EnumConnectionState.CONNECTED);
                return true;
            }
        }
        return false;
    }

    private String checkForDuplicateUser(UserProfile userProfile)
    {
        if (userProfiles.contains(userProfile)) return "username";

        for (UserProfile profile : userProfiles)
        {
            if (profile.getUserName().equalsIgnoreCase(userProfile.getUserName()))
                return "username";
//            else if (profile.getIdentifier().equalsIgnoreCase(userProfile.getIdentifier())) // TODO: Implement once identifiers are in place
//                return "identifier";
        }

        return "pass";
    }

    public void sendPacketToAllClients(Packet<?> packet, Channel...  blacklistAddresses)
    {
        networkSystem.sendPacket(packet, blacklistAddresses);
    }

    public NetworkSystem getNetworkSystem()
    {
        return networkSystem;
    }
}