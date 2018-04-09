package com.goosejs.gchat.server;

import com.goosejs.gchat.networking.NetworkSystem;
import com.goosejs.gchat.networking.packet.server.SPacketLoginAccepted;
import com.goosejs.gchat.networking.packet.server.SPacketUserList;
import com.goosejs.gchat.util.Logger;
import com.goosejs.gchat.util.Timer;
import com.goosejs.gchat.util.interfaces.IInvokable;

import java.io.IOException;

public class GChatServer
{
    private static GChatServer instance;

    private Server server;

    public static void main(String args[])
    {
        boolean foundIp = false;
        String ip = "localhost";
        for (String arg : args)
        {
            if (foundIp)
            {
                ip = arg;
                foundIp = false;
            }

            if (arg.equalsIgnoreCase("-ip")) foundIp = true;
        }
        instance = new GChatServer();
        instance.runServer(ip);
    }

    public void runServer(String ip)
    {
        boolean continuing = true;

        Logger.setDebug(true);
        Logger.info("Starting GChat Server...");
        Logger.info("Creating GChat Server instance...");
        NetworkSystem networkSystem = new NetworkSystem();
        server = new Server(networkSystem);
        Logger.info("Instance created successfully, attempting to bind to network...");
        try
        {
            networkSystem.addLanEndpoint(ip, 60053);
        }
        catch (IOException e)
        {
            Logger.error("Could not connect to server!", e);
            continuing = false;
        }

        if (continuing)
        {

            Logger.info("Successfully bound to network!");

            while (true)
            {
                server.update();

                try
                {
                    Thread.sleep(25); // TODO: Look into this so I don't kill the computer
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public static GChatServer getInstance()
    {
        return instance;
    }

    public Server getServer()
    {
        return server;
    }

}