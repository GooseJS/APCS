package com.goosejs.gchat.client.state;

import com.goosejs.gchat.backend.lwjgl.glfw.Window;
import com.goosejs.gchat.client.GChat;
import com.goosejs.gchat.client.gui.GuiManager;
import com.goosejs.gchat.client.gui.elements.*;
import com.goosejs.gchat.client.gui.elements.subelements.GuiString;
import com.goosejs.gchat.networking.NetworkManager;
import com.goosejs.gchat.networking.packet.client.CPacketChatMessageSend;
import com.goosejs.gchat.networking.packet.client.CPacketDisconnect;
import com.goosejs.gchat.networking.packet.client.CPacketKeepAlive;
import com.goosejs.gchat.user.UserPermissions;
import com.goosejs.gchat.user.UserProfile;

import java.util.Objects;

public class StateConnected extends State
{
    private GuiManager guiManager;
    private Window window;

    private NetworkManager networkManager;

    private String name;
    private String serverName;

    private GuiListBox userList;
    private GuiTextBox chatBox;
    private GuiCustomInputField typingBox;

    private StatePrivateMessage privateMessageState;

    public StateConnected(NetworkManager networkManager, String loginName, String serverName, Window window)
    {
        this.guiManager = new GuiManager(GChat.getInstance().getClient());
        this.window = window;
        this.name = loginName;
        this.serverName = serverName;
        this.networkManager = networkManager;

        privateMessageState = new StatePrivateMessage(this, getUserProfile().getUserName(), networkManager, window);

        initGui();
    }

    private void initGui()
    {
        userList = new GuiListBox(590, 30, 200, 500);
        chatBox = new GuiTextBox(1, 53, 500, 500);
        chatBox.setTopNewest(false);
        chatBox.setLocatedAtBottom(true);
        typingBox = new GuiCustomInputField("Message: ", text -> {
            networkManager.sendPacket(new CPacketChatMessageSend(getUserProfile().getUserName(), text));
            chatBox.addLine("<" + getUserProfile().getUserName() + "> " + text);
            return true;
        }, 1, 30, 500, 25);


        guiManager.addGuiElement(new GuiLabel("Currently Connected To: " + serverName, 1, 1));
        guiManager.addGuiElement(userList);
        guiManager.addGuiElement(chatBox);
        guiManager.addGuiElement(typingBox);
    }

    @Override
    public void update()
    {
        guiManager.update();

        if (networkManager != null)
        {
            if (networkManager.isChannelOpen())
            {
                networkManager.processReceivedPackets();
            }
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
    }

    @Override
    public void onDeactivated()
    {
        guiManager.unregisterCallbacks(window);
    }

    @Override
    public void onShutdown()
    {
        networkManager.sendPacket(new CPacketDisconnect(getUserProfile().getUserName(), "Unknown"));
    }

    public void updateUserList(UserProfile[] userProfiles)
    {
        for (UserProfile userProfile : userProfiles)
        {
            boolean contains = false;
            for (GuiButton guiButton : userList.getButtons())
            {
                if (guiButton.getText().toString().equals(userProfile.getUserName()))
                {
                    contains = true;
                    break;
                }
            }

            if (!contains)
            {
                String otherUserName = userProfile.getUserName();
                GuiButton addingButton = new GuiButton(otherUserName, 0, 0, 0, 30);
                addingButton.setOnClick(() ->
                {
                    privateMessageState.setChattingUser(otherUserName);
                    addingButton.setBackgroundColor(0, 0, 0);
                    addingButton.setHoverColor(0.1f, 0.1f, 0.1f);
                    pushState(privateMessageState);
                });
                addingButton.setHoverColor(0.1f, 0.1f, 0.1f);
                userList.addButton(addingButton, guiManager);
            }
        }

        for (GuiButton guiButton : userList.getButtons())
        {
            boolean found = false;
            for (UserProfile userProfile : userProfiles)
            {
                if (guiButton.getText().toString().equals(userProfile.getUserName()))
                {
                    found = true;
                    break;
                }
            }

            if (!found)
            {
                userList.getButtons().remove(guiButton);
            }
        }
    }

    public UserProfile getUserProfile()
    {
        return new UserProfile(name, "loo", "loo", "loo", 0, new UserPermissions());
    }

    public boolean userExists(String user)
    {
        for (GuiButton guiButton : userList.getButtons())
        {
            if (guiButton.getText().toString().equals(user)) return true;
        }
        return false;
    }

    public void messageReceived(String user, String message)
    {
        chatBox.addLine("<" + user + "> " + message);
    }

    public void userDisconnected(String userName)
    {
        chatBox.addLine(new GuiString(userName + " has left the chat.", 214, 33, 2));
    }

    public void userJoined(String userName)
    {
        chatBox.addLine(new GuiString(userName + " has joined the chat.", 40, 168, 1));
    }

    public void notifyPM(String user)
    {
        boolean isChattingWithUser = false;

        if (GChat.getInstance().getClient().getStateManager().getActiveState() == privateMessageState)
        {
            if (privateMessageState.getChattingUser() != null && privateMessageState.getChattingUser().equals(user))
                isChattingWithUser = true;
        }

        if (!isChattingWithUser)
        {
            userList.getButtons().stream().filter(button -> button.getText().toString().equals(user)).forEach(button ->
            {
                button.setBackgroundColor(255, 0, 123);
                button.setHoverColor(255, 255, 255);
            });
        }
    }

    public void receivedPM(String sentUser, String receivingUser, String message)
    {
        if (this.getUserProfile().getUserName().equals(receivingUser))
            privateMessageState.receivePM(sentUser, new GuiString(message));
    }
}