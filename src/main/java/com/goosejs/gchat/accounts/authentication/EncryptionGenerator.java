package com.goosejs.gchat.accounts.authentication;

import com.goosejs.gchat.util.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class EncryptionGenerator
{
    public byte[] getEncryptedPassword(String password, byte[] salt)
    {
        String algorithm = "PBKDF2WithHmacSHA1";
        int derivedKeyLength = 160;
        int iterations = 20000;

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength);

        SecretKeyFactory f = null;
        try
        {
            f = SecretKeyFactory.getInstance(algorithm);
        }
        catch (NoSuchAlgorithmException e)
        {
            Logger.error(e);
        }

        try
        {
            return f.generateSecret(spec).getEncoded();
        }
        catch (InvalidKeySpecException e)
        {
            Logger.error(e);
        }

        return null;
    }

    public byte[] generateSalt()
    {
        SecureRandom random = null;
        try
        {
            random = SecureRandom.getInstance("SHA1PRNG");
        }
        catch (NoSuchAlgorithmException e)
        {
            Logger.error(e);
        }

        byte[] salt = new byte[8];

        try
        {
            random.nextBytes(salt);
        }
        catch (NullPointerException e)
        {
            Logger.error(e);
        }

        return salt;
    }

}
