package com.goosejs.gchat.networking.nethandler.server;

import com.goosejs.gchat.networking.NetworkManager;
import com.goosejs.gchat.networking.NetworkSystem;
import com.goosejs.gchat.networking.packet.client.CPacketChatMessageSend;
import com.goosejs.gchat.networking.packet.client.CPacketConnectionInfo;
import com.goosejs.gchat.networking.packet.client.CPacketDisconnect;
import com.goosejs.gchat.networking.packet.server.SPacketChatMessageReceived;
import com.goosejs.gchat.networking.packet.server.SPacketPrivateMessage;
import com.goosejs.gchat.server.Server;

public class NetHandlerConnectedServer implements INetHandlerConnectedServer
{

    private NetworkSystem networkSystem;
    private NetworkManager networkManager;
    private Server server;

    public NetHandlerConnectedServer(NetworkSystem networkSystem, NetworkManager networkManager, Server server)
    {
        this.networkSystem = networkSystem;
        this.networkManager = networkManager;
        this.server = server;
    }

    @Override
    public void onDisconnect(String reason)
    {

    }

    @Override
    public void processConnection(CPacketConnectionInfo cPacketConnectionInfo)
    {
        server.addUser(cPacketConnectionInfo.getUserProfile());
    }

    @Override
    public void processDisconnection(CPacketDisconnect cPacketDisconnect)
    {
        server.removeUser(cPacketDisconnect.getUserName());
    }

    @Override
    public void processChatMessageSent(CPacketChatMessageSend cPacketChatMessageSend)
    {
        server.sendPacketToAllClients(new SPacketChatMessageReceived(cPacketChatMessageSend.getUser(), cPacketChatMessageSend.getMessage()), networkManager.getChannel());
    }

    @Override
    public void processPM(String sentUser, String receivingUser, String message)
    {
        server.sendPacketToAllClients(new SPacketPrivateMessage(sentUser, receivingUser, message));
    }
}
