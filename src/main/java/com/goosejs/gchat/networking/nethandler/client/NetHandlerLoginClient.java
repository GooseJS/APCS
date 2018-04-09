package com.goosejs.gchat.networking.nethandler.client;

import com.goosejs.gchat.client.GChat;
import com.goosejs.gchat.client.state.StateConnected;
import com.goosejs.gchat.client.state.StateStartup;
import com.goosejs.gchat.networking.NetworkManager;
import com.goosejs.gchat.networking.packet.client.CPacketLoginStart;
import com.goosejs.gchat.networking.packet.server.SPacketLoginAccepted;
import com.goosejs.gchat.util.Logger;

public class NetHandlerLoginClient implements INetHandlerLoginClient
{
    private StateStartup stateStartup;
    private NetworkManager networkManager;

    public NetHandlerLoginClient(NetworkManager networkmanager, StateStartup stateStartup)
    {
        this.networkManager = networkmanager;
        this.stateStartup = stateStartup;
    }

    @Override
    public void processConnection(SPacketLoginAccepted loginAccepted)
    {
        Logger.info("Successfully connected to " + loginAccepted.getServerName());
        stateStartup.setState(new StateConnected(networkManager, stateStartup.getLoginName(), loginAccepted.getServerName(), GChat.getInstance().getWindow()));
        networkManager.setNetHandler(new NetHandlerConnectedClient(networkManager, (StateConnected)GChat.getInstance().getClient().getStateManager().getActiveState()));
    }

    @Override
    public void attemptLogin()
    {
        networkManager.sendPacket(new CPacketLoginStart(stateStartup.getLoginName()));
    }

    @Override
    public void onDisconnect(String reason)
    {

    }
}
