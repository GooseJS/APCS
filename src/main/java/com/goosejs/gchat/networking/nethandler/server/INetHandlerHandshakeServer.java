package com.goosejs.gchat.networking.nethandler.server;

import com.goosejs.gchat.networking.nethandler.INetHandler;
import com.goosejs.gchat.networking.packet.client.C00Handshake;

public interface INetHandlerHandshakeServer extends INetHandler
{
    void processHandshake(C00Handshake packetIn);
}