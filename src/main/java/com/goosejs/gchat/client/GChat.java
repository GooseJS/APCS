package com.goosejs.gchat.client;

import com.goosejs.gchat.backend.lwjgl.glfw.*;
import com.goosejs.gchat.backend.lwjgl.opengl.GlobalOrthoMatrix;
import com.goosejs.gchat.util.Logger;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GChat
{

    private static GChat instance;
    public static String fontFile = "fonts/SourceSansPro-Regular.ttf";

    private Window window;
    private GChatClient client;

    public static void main(String args[])
    {
        instance = new GChat();
        instance.runClient();
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                instance.getClient().shutdown();
            }
        });
    }

    public void runClient()
    {
        Logger.setDebug(true);
        Logger.info("Starting GChat...");

        window = createWindow(800, 600, "gChat", false);
        client = new GChatClient(window);

        while (!window.shouldClose())
        {
            window.swapBuffers();
            window.pollEvents();
            GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            try
            {
                Thread.sleep(25); // TODO: Look into this so I don't kill the computer
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            client.draw();
            client.update();
        }
    }

    public Window createWindow(int width, int height, String title, boolean resizable)
    {
        Window window = new Window(width, height, title, resizable);
        window.createWindow(3, 3, true, 0);
        window.setKeyboardCallback(new ExtendableKeyboardCallback());
        window.setMouseButtonCallback(new ExtendableMouseButtonCallback());
        window.setCursorPosCallback(new ExtendableCursorPosCallback());
        window.setCharacterCallback(new ExtendableCharacterCallback());
        GlobalOrthoMatrix.updateGlobalOrthoMatrix(0, 0, window.getWidth(), window.getHeight());

        return window;
    }

    public static GChat getInstance()
    {
        return instance;
    }

    public Window getWindow()
    {
        return this.window;
    }

    public GChatClient getClient()
    {
        return this.client;
    }

}