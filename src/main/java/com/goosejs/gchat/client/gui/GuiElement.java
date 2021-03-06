package com.goosejs.gchat.client.gui;

import com.goosejs.gchat.client.GChat;
import com.goosejs.gchat.renderer.font.TrueTypeFontRenderer;
import com.goosejs.gchat.renderer.glRendering.Tessellator;
import org.lwjgl.opengl.GL11;

public abstract class GuiElement
{

    private static final TrueTypeFontRenderer fontRenderer;

    static
    {
        fontRenderer = new TrueTypeFontRenderer(GChat.fontFile, 17.5f, 1f, 0, 0, 400, 600);
        fontRenderer.loadOrthoMatrix(0, 0, 0, 0);
    }

    private int x;
    private int y;
    private int width;
    private int height;

    private String identifier;

    public GuiElement(int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public final void drawStart()
    {
        fontRenderer.loadOrthoMatrix(0, 0, width, height);
        Tessellator.getInstance().loadOrthoMatrix(0, 0, width, height);
        GL11.glViewport(x, (GChat.getInstance().getWindow().getHeight() - height - y), width, height); // TODO: Add support for retina (THIS SHOULD BE TIMES 2 FOR RETINA
        onDrawStart();
    }

    public final void draw()
    {
        drawStart();
        onDraw();
        drawEnd();
    }

    public final void drawEnd()
    {
        onDrawEnd();
    }

    protected TrueTypeFontRenderer getFontRenderer()
    {
        return this.fontRenderer;
    }

    public int getX()
    {
        return this.x;
    }

    public int getY()
    {
        return this.y;
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return this.height;
    }

    public String getIdentifier()
    {
        return this.identifier;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public void onCreation() {}

    public void onDestruction() {}

    public void onDrawStart() {}

    public abstract void onDraw();

    public void onDrawEnd() {}

    public void onKeyPress(String s, int key, int scancode, boolean repeat) {}

    public void onMouseButtonPress(int button, double mouseX, double mouseY) {}

    public void onMouseButtonPressWhileOver(int button, double mouseX, double mouseY) {}

    public void onMouseHover(double mouseX, double mouseY) {}

    public void onMouseMove(double mouseX, double mouseY) {}

    public void update() {}

    public boolean isHovering(double mouseX, double mouseY)
    {
        return ((getX() < mouseX && (getX() + getWidth()) > mouseX) && (getY() < mouseY && (getY() + getHeight()) > mouseY));
    }
}