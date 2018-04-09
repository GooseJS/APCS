package com.goosejs.gchat.user;

public class UserProfile
{

    private String userName;
    private String identifier;
    private String authKey;
    private String ip;
    private int connectionTime;
    private UserPermissions userPermissions;

    public UserProfile(String userName, String identifier, String authKey, String ip, int connectionTime,
                       UserPermissions userPermissions)
    {
        this.userName = userName;
        this.identifier = identifier;
        this.authKey = authKey;
        this.ip = ip;
        this.connectionTime = connectionTime;
        this.userPermissions = userPermissions;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public void setConnectionTime(int connectionTime)
    {
        this.connectionTime = connectionTime;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public String getAuthKey()
    {
        return authKey;
    }

    public String getIp()
    {
        return ip;
    }

    public int getConnectionTime()
    {
        return connectionTime;
    }

    public UserPermissions getUserPermissions()
    {
        return userPermissions;
    }
}