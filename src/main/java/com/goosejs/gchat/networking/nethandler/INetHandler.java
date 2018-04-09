package com.goosejs.gchat.networking.nethandler;

public interface INetHandler
{

    /**
     * Invoked when disconnecting, the parameter is a string describing the reason for the termination
     * @param reason
     */
    void onDisconnect(String reason);

}