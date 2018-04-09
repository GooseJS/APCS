package com.goosejs.gchat.networking.nethandler.server;

import com.goosejs.gchat.networking.EnumConnectionState;
import com.goosejs.gchat.networking.NetworkManager;
import com.goosejs.gchat.networking.packet.client.C00Handshake;
import com.goosejs.gchat.networking.packet.server.SPacketAttemptLogin;

public class NetHandlerHandshakeTCP implements INetHandlerHandshakeServer
{

    private final NetworkManager networkManager;

    public NetHandlerHandshakeTCP(NetworkManager networkManager)
    {
        this.networkManager = networkManager;
    }

    @Override
    public void processHandshake(C00Handshake packetIn)
    {
        switch (packetIn.getRequestedState())
        {
            case LOGIN:
                this.networkManager.setConnectionState(EnumConnectionState.LOGIN);

                if (packetIn.getProtocolVersion() > 0) // TODO: Replace with actual version
                {
                    //this.networkManager.sendPacket(new SPacketDisconnect("Outdated Server!"));
                    this.networkManager.closeChannel("Outdated Server!");
                }
                else if (packetIn.getProtocolVersion() < 0)
                {
                    //this.networkManager.sendPacket(new SPacketDisconnect("Outdated Client!"));
                    this.networkManager.closeChannel("Outdated Client!");
                }
                else
                {
                    this.networkManager.setNetHandler(new NetHandlerLoginServer(this.networkManager));
                    this.networkManager.sendPacket(new SPacketAttemptLogin());
                    this.networkManager.setConnectionState(EnumConnectionState.LOGIN);
                }
                break;

            default:
                throw new UnsupportedOperationException("Invalid intention " + packetIn.getRequestedState());
        }
    }

    @Override
    public void onDisconnect(String reason)
    {

    }
}
