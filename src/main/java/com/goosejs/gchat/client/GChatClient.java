package com.goosejs.gchat.client;

import com.goosejs.gchat.backend.lwjgl.glfw.Window;
import com.goosejs.gchat.client.state.StateManager;
import com.goosejs.gchat.client.state.StateStartup;

public class GChatClient
{

    private Window window; // NOTE: Used for callback stuff

    private StateManager stateManager;

    public GChatClient(Window window)
    {
        this.window = window;
        this.stateManager = new StateManager();
        this.stateManager.pushState(new StateStartup(window));
    }

    public void update()
    {
        stateManager.update();
    }

    public void draw()
    {
        stateManager.draw();
    }

    public void shutdown() { stateManager.shutdown(); }

    public StateManager getStateManager()
    {
        return stateManager;
    }
}