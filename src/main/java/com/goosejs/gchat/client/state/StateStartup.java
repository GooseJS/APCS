package com.goosejs.gchat.client.state;

import com.goosejs.gchat.backend.lwjgl.glfw.Window;
import com.goosejs.gchat.client.GChat;
import com.goosejs.gchat.client.gui.GuiManager;
import com.goosejs.gchat.client.gui.elements.GuiButton;
import com.goosejs.gchat.client.gui.elements.GuiCustomInputField;
import com.goosejs.gchat.client.gui.elements.GuiLabel;
import com.goosejs.gchat.client.gui.elements.GuiTextBox;
import com.goosejs.gchat.client.gui.elements.subelements.GuiString;
import com.goosejs.gchat.networking.EnumConnectionState;
import com.goosejs.gchat.networking.NetworkManager;
import com.goosejs.gchat.networking.nethandler.client.NetHandlerLoginClient;
import com.goosejs.gchat.networking.packet.client.C00Handshake;
import com.goosejs.gchat.util.Logger;
import com.goosejs.gchat.util.interfaces.IInvokable;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class StateStartup extends State
{

    private GuiManager guiManager;
    private Window window;

    private GuiTextBox textBox;

    private NetworkManager networkManager;

    private boolean attemptConnectionOnNextLoop;
    private boolean actuallyAttemptConnection;
    private String ip;

    private boolean connected;
    private String loginName;

    public StateStartup(Window window)
    {
        this.guiManager = new GuiManager(GChat.getInstance().getClient());
        this.window = window;

        initGui();
    }

    private void initGui()
    {
        GuiCustomInputField inputField = new GuiCustomInputField("UserName: ", null, 1, 30, 500, 25);
        inputField.setOnPressEnter(text ->
        {
            loginName = text;
            inputField.setCustomText("Server IP: ");
            inputField.setOnPressEnter(text1 ->
            {
                attemptConnection(text1);
                return true;
            });
            return true;
        });

        guiManager.addGuiElement(inputField);

        textBox = new GuiTextBox(1, 53, 500, 500);
        textBox.setTopNewest(true);
        guiManager.addGuiElement(new GuiLabel("Currently Connected To: ", 1, 1));
        guiManager.addGuiElement(textBox);
    }

    private void attemptConnection(String ip)
    {
        textBox.addLine("Attempting connection to \"" + ip + "\" on next tick.");
        textBox.addLine("Attempting to create connection...");
        this.attemptConnectionOnNextLoop = true;
        this.ip = ip;
    }

    @Override
    public void update()
    {
        guiManager.update();

        if (actuallyAttemptConnection)
        {
            try
            {
                // TODO: Mutli-thread the NetworkManager so this could all be done in the attemptConnection thread
                networkManager = NetworkManager.createNetworkManagerAndConnect(InetAddress.getByName(ip), 60053, true, future ->
                {
                    if (future.isSuccess())
                    {
                        textBox.addLine("Successfully connected to \"" + ip + "\"");
                        connected = true;
                    }
                    else
                    {
                        textBox.addLine("Could not connect to \"" + ip + "\", " + future.cause().getLocalizedMessage());
                    }
                }); // TODO: Add support for custom ports
            }
            catch (UnknownHostException e)
            {
                networkManager = null;
                textBox.addLine("Could not connect to \"" + ip + "\", Unknown Host!");
                Logger.error("Could not connect to ip { " + ip + "} ", e);
            }

            actuallyAttemptConnection = false;
        }

        if (attemptConnectionOnNextLoop)
        {
            actuallyAttemptConnection = true;
            attemptConnectionOnNextLoop = false;
        }

        if (connected)
        {
            connected = false;
            networkManager.setNetHandler(new NetHandlerLoginClient(networkManager, this));
            networkManager.sendPacket(new C00Handshake(0, "localhost", 60053, EnumConnectionState.LOGIN));
        }

        if (networkManager != null)
        {
            if (networkManager.isChannelOpen())
            {
                networkManager.processReceivedPackets();
            }
        }

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

    public String getLoginName()
    {
        // TODO: Change this to UserProfile
        return loginName;
    }

    public NetworkManager getNetworkManager()
    {
        return networkManager;
    }
}