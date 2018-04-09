package com.goosejs.gchat.networking.nethandler.client;

import com.goosejs.gchat.client.state.StateConnected;
import com.goosejs.gchat.networking.EnumConnectionState;
import com.goosejs.gchat.networking.NetworkManager;
import com.goosejs.gchat.networking.packet.client.CPacketConnectionInfo;
import com.goosejs.gchat.networking.packet.server.SPacketChatMessageReceived;
import com.goosejs.gchat.networking.packet.server.SPacketUserDisconnect;
import com.goosejs.gchat.networking.packet.server.SPacketUserJoined;
import com.goosejs.gchat.networking.packet.server.SPacketUserList;

public class NetHandlerConnectedClient implements INetHandlerConnectedClient
{
    private NetworkManager networkManager;
    private StateConnected client;

    public NetHandlerConnectedClient(NetworkManager networkManager, StateConnected client)
    {
        this.networkManager = networkManager;
        this.client = client;

        this.networkManager.sendPacket(new CPacketConnectionInfo(client.getUserProfile()));
        this.networkManager.setConnectionState(EnumConnectionState.CONNECTED);
    }

    @Override
    public void onDisconnect(String reason)
    {

    }

    @Override
    public void processUserList(SPacketUserList sPacketUserList)
    {
        client.updateUserList(sPacketUserList.getUsers());
    }

    @Override
    public void processMessageReceived(SPacketChatMessageReceived sPacketChatMessageReceived)
    {
        client.messageReceived(sPacketChatMessageReceived.getUser(), sPacketChatMessageReceived.getMessage());
    }

    @Override
    public void handleUserDisconnect(SPacketUserDisconnect sPacketUserDisconnect)
    {
        client.userDisconnected(sPacketUserDisconnect.getUserName()); // TODO: Include reason for disconnect (Time out, left, etc.)
    }

    @Override
    public void handleUserJoined(SPacketUserJoined sPacketUserJoined)
    {
        client.userJoined(sPacketUserJoined.getUserName());
    }

    @Override
    public void processPM(String sentUser, String receivingUser, String message)
    {
        client.receivedPM(sentUser, receivingUser, message);
    }
}
