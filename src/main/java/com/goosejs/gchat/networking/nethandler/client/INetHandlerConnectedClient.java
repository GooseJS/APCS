package com.goosejs.gchat.networking.nethandler.client;

import com.goosejs.gchat.networking.nethandler.INetHandler;
import com.goosejs.gchat.networking.packet.server.SPacketChatMessageReceived;
import com.goosejs.gchat.networking.packet.server.SPacketUserDisconnect;
import com.goosejs.gchat.networking.packet.server.SPacketUserJoined;
import com.goosejs.gchat.networking.packet.server.SPacketUserList;

public interface INetHandlerConnectedClient extends INetHandler
{
    void processUserList(SPacketUserList sPacketUserList);

    void processMessageReceived(SPacketChatMessageReceived sPacketChatMessageReceived);

    void handleUserDisconnect(SPacketUserDisconnect sPacketUserDisconnect);

    void handleUserJoined(SPacketUserJoined sPacketUserJoined);

    void processPM(String sentUser, String receivingUser, String message);
}
