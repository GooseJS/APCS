package com.goosejs.gchat.networking.nethandler.server;

import com.goosejs.gchat.networking.EnumConnectionState;
import com.goosejs.gchat.networking.NetworkManager;
import com.goosejs.gchat.networking.packet.client.CPacketLoginStart;
import com.goosejs.gchat.networking.packet.server.SPacketLoginAccepted;
import com.goosejs.gchat.server.GChatServer;
import com.goosejs.gchat.server.Server;
import com.goosejs.gchat.util.Logger;
import com.goosejs.gchat.util.interfaces.ITickable;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class NetHandlerLoginServer implements INetHandlerLoginServer, ITickable
{
    private final NetworkManager networkManager;

    private int connectionTimer;
    private String serverID;
    private String loginName;
    private NetHandlerLoginServer.LoginState currentLoginState = NetHandlerLoginServer.LoginState.HELLO;

    public NetHandlerLoginServer(NetworkManager networkManager)
    {
        this.networkManager = networkManager;
    }

    public void update()
    {
        if (this.currentLoginState == NetHandlerLoginServer.LoginState.READY_TO_ACCEPT)
        {
            this.tryAcceptUser();
        }
        else if (this.currentLoginState == NetHandlerLoginServer.LoginState.DELAY_ACCEPT)
        {
            // TODO: Initialize connection to server, it should send all server info packets here
            currentLoginState = LoginState.READY_TO_ACCEPT;
        }

        if (this.connectionTimer++ == 600)
        {
            this.closeConnection("Took too long to log in");
        }
    }

    public void tryAcceptUser()
    {
        // TODO: Check they aren't banned / in the whitelist (if whitelisted server), ect.

        boolean canConnect = true;

        if (canConnect)
        {
            currentLoginState = LoginState.ACCEPTED;

            networkManager.sendPacket(new SPacketLoginAccepted(loginName, "loo", "authenticated", "GooseServer", "The Best Server Ever"), (ChannelFutureListener) future ->
            {
                if (future.isDone())
                {
                    networkManager.setConnectionState(EnumConnectionState.CONNECTED);
                }
            });
            networkManager.setNetHandler(new NetHandlerConnectedServer(GChatServer.getInstance().getServer().getNetworkSystem(), networkManager, GChatServer.getInstance().getServer()));
        }
        else
        {
            closeConnection("Termination Reason goes here"); // TODO: Update
        }
    }

    public void closeConnection(String reason)
    {
        try
        {
            Logger.info("Disconnecting " + this.getConnectionInfo() + ": " + reason);
            //this.networkManager.sendPacket(new SPacketDisconnect(reason));
            this.networkManager.closeChannel(reason);
        }
        catch (Exception exception)
        {
            Logger.error("Error whilst disconnecting player", exception);
        }
    }

    @Override
    public void onDisconnect(String reason)
    {

    }

    public String getConnectionInfo()
    {
        return this.loginName != null ? this.loginName + " (" + this.networkManager.getRemoteAddress().toString() + ")" : String.valueOf(this.networkManager.getRemoteAddress());
    }

    @Override
    public void processLogin(CPacketLoginStart packetIn)
    {
        this.loginName = packetIn.getLoginName();
        this.currentLoginState = LoginState.DELAY_ACCEPT;
    }

    private enum LoginState
    {
        HELLO,
        AUTHENTICATING,
        READY_TO_ACCEPT,
        DELAY_ACCEPT,
        ACCEPTED
    }

}
