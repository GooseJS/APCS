package com.goosejs.gchat.launcher;

import com.goosejs.gchat.client.GChat;
import com.goosejs.gchat.server.GChatServer;

public class Launcher
{

    public static void main(String[] args)
    {
        boolean client = true;

        for (String arg : args)
        {
            if (arg.equalsIgnoreCase("-server"))
                client = false;
        }

        if (client) GChat.main(args);
        else GChatServer.main(args);
    }

}