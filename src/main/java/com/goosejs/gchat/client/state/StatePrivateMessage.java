package com.goosejs.gchat.client.state;

import com.goosejs.gchat.backend.lwjgl.glfw.Window;
import com.goosejs.gchat.client.GChat;
import com.goosejs.gchat.client.gui.GuiManager;
import com.goosejs.gchat.client.gui.elements.*;
import com.goosejs.gchat.client.gui.elements.subelements.GuiString;
import com.goosejs.gchat.networking.NetworkManager;
import com.goosejs.gchat.networking.packet.client.CPacketDisconnect;
import com.goosejs.gchat.networking.packet.client.CPacketKeepAlive;
import com.goosejs.gchat.networking.packet.client.CPacketPrivateMessage;
import com.goosejs.gchat.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StatePrivateMessage extends State
{

    private StateConnected stateConnected;
    private String userName;
    private NetworkManager networkManager;
    private Window window;

    private HashMap<String, List<GuiString>> storedChats;

    private String chattingUser;

    private GuiManager guiManager;

    private GuiTextBox chatBox;
    private GuiInputField typingBox;
    private GuiLabel chattingLabel;

    private boolean refreshChat = false;

    public StatePrivateMessage(StateConnected stateConnected, String userName, NetworkManager networkManager, Window window)
    {
        this.stateConnected = stateConnected;
        this.userName = userName;
        this.networkManager = networkManager;
        this.window = window;

        this.storedChats = new HashMap<>();
        this.guiManager = new GuiManager(GChat.getInstance().getClient());

        initGui();
    }

    public void initGui()
    {
        GuiButton backButton = new GuiButton("Back", this::popState, 590, 30, 200, 500);
        backButton.setHoverColor(0.1f, 0.1f, 0.1f);
        backButton.drawOutline(true);
        chatBox = new GuiTextBox(1, 53, 500, 500);
        chatBox.setLocatedAtBottom(true);
        chatBox.setTopNewest(false);
        typingBox = new GuiCustomInputField("Message: ", text -> {
            networkManager.sendPacket(new CPacketPrivateMessage(userName, chattingUser, text));
            chatBox.addLine("<" + userName + "> " + text);
            return true;
        }, 1, 30, 500, 25);
        chattingLabel = new GuiLabel("Currently Chatting With: " + chattingUser, 1, 1);


        guiManager.addGuiElement(chattingLabel);
        guiManager.addGuiElement(backButton);
        guiManager.addGuiElement(chatBox);
        guiManager.addGuiElement(typingBox);
    }

    @Override
    public void update()
    {
        guiManager.update();

        if (!chattingLabel.getText().equals("Currently Chatting With: " + chattingUser))
            chattingLabel.setText("Currently Chatting With: " + chattingUser);

        if (networkManager != null)
        {
            if (networkManager.isChannelOpen())
            {
                networkManager.processReceivedPackets();
            }
        }

        if (refreshChat)
        {
            List<GuiString> chat = storedChats.get(chattingUser);

            if (chat != null) chatBox.setLines(chat);
            else chatBox.getLines().clear();

            refreshChat = false;
        }

        networkManager.sendPacket(new CPacketKeepAlive()); // TODO: Slow this down
    }

    @Override
    public void draw()
    {
        guiManager.draw();
    }

    @Override
    public void onActivated()
    {
        guiManager.registerCallbacks(window);

        List<GuiString> chat = storedChats.get(chattingUser);

        if (chat != null) chatBox.setLines(chat);
        else chatBox.getLines().clear();
    }

    @Override
    public void onDeactivated()
    {
        guiManager.unregisterCallbacks(window);

        List<GuiString> chat = new ArrayList<>();
        chat.addAll(chatBox.getLines());
        storedChats.put(chattingUser, chat);
    }

    @Override
    public void onShutdown()
    {
        networkManager.sendPacket(new CPacketDisconnect(userName, "Unknown"));
    }

    public void setChattingUser(String userName)
    {
        this.chattingUser = userName;
    }

    public void receivePM(String user, GuiString message)
    {
        List<GuiString> chat = storedChats.get(chattingUser);

        if (stateConnected.userExists(user))
        {
            stateConnected.notifyPM(user);
            if (chat != null)
            {
                message.setText("<" + user + "> " + message.toString());
                chat.add(0, message);
            }
            else
            {
                chat = new ArrayList<>();
                message.setText("<" + user + "> " + message.toString());
                chat.add(0, message);
                if (chatBox.getLines().size() > 0)
                    chat.add(0, chatBox.getLines().get(0));
            }
            storedChats.put(user, chat);
            refreshChat = true;
        }
        else Logger.warn("Received a PM from a user that doesn't exist!");
    }

    public String getChattingUser()
    {
        return chattingUser;
    }
}
