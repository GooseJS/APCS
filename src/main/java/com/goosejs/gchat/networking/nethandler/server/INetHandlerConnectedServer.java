package com.goosejs.gchat.networking.nethandler.server;

import com.goosejs.gchat.networking.nethandler.INetHandler;
import com.goosejs.gchat.networking.packet.client.CPacketChatMessageSend;
import com.goosejs.gchat.networking.packet.client.CPacketConnectionInfo;
import com.goosejs.gchat.networking.packet.client.CPacketDisconnect;
import com.goosejs.gchat.networking.packet.server.SPacketUserList;

public interface INetHandlerConnectedServer extends INetHandler
{
    void processConnection(CPacketConnectionInfo cPacketConnectionInfo);

    void processDisconnection(CPacketDisconnect cPacketDisconnect);

    void processChatMessageSent(CPacketChatMessageSend cPacketChatMessageSend);

    void processPM(String sentUser, String receivingUser, String message);
}
