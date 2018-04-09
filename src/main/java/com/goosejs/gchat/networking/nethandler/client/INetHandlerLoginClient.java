package com.goosejs.gchat.networking.nethandler.client;

import com.goosejs.gchat.networking.nethandler.INetHandler;
import com.goosejs.gchat.networking.packet.server.SPacketLoginAccepted;

public interface INetHandlerLoginClient extends INetHandler
{
    void processConnection(SPacketLoginAccepted loginAccepted);

    void attemptLogin();
}