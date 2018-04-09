package com.goosejs.gchat.networking.nethandler.server;

import com.goosejs.gchat.networking.nethandler.INetHandler;
import com.goosejs.gchat.networking.packet.client.CPacketLoginStart;

public interface INetHandlerLoginServer extends INetHandler
{

    void processLogin(CPacketLoginStart packetIn);

}
