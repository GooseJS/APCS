package com.goosejs.gchat.backend.lwjgl.glfw;

public interface CharacterCallback
{

    void invoke(long window, int codepoint, int mods);

}
