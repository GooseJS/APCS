package com.goosejs.gchat.accounts.authentication;

import java.util.Arrays;

public class EncryptionManager
{

    public static final EncryptionManager instance = new EncryptionManager();

    public final EncryptionGenerator generator;

    private EncryptionManager()
    {
        this.generator = new EncryptionGenerator();
    }

    public boolean authenticate(String attemptedPassword, byte[] encryptedPassword, byte[] salt)
    {
        return authenticate(generator.getEncryptedPassword(attemptedPassword, salt), encryptedPassword);
    }

    public boolean authenticate(byte[] attemptedPassword, byte[] encryptedPassword)
    {
        return Arrays.equals(attemptedPassword, encryptedPassword);
    }

}
