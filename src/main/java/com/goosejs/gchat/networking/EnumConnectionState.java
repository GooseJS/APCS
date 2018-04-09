package com.goosejs.gchat.networking;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.goosejs.gchat.networking.packet.client.*;
import com.goosejs.gchat.networking.packet.server.*;
import com.goosejs.gchat.util.Logger;

import java.util.Map;

public enum EnumConnectionState
{

    HANDSHAKING(-1)
        {
            {
                // TODO: This must get done once packets start getting added
                this.registerPacket(EnumPacketDirection.SERVERBOUND, C00Handshake.class);

                this.registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketAttemptLogin.class);
            }
        },
    LOGIN(0)
        {
            {
                this.registerPacket(EnumPacketDirection.SERVERBOUND, CPacketLoginStart.class);

                this.registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketLoginAccepted.class);
            }
        },
    CONNECTED(1)
        {
            {
                this.registerPacket(EnumPacketDirection.SERVERBOUND, CPacketConnectionInfo.class);
                this.registerPacket(EnumPacketDirection.SERVERBOUND, CPacketKeepAlive.class);
                this.registerPacket(EnumPacketDirection.SERVERBOUND, CPacketDisconnect.class);
                this.registerPacket(EnumPacketDirection.SERVERBOUND, CPacketChatMessageSend.class);
                this.registerPacket(EnumPacketDirection.SERVERBOUND, CPacketPrivateMessage.class);

                this.registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketUserList.class);
                this.registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketChatMessageReceived.class);
                this.registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketUserJoined.class);
                this.registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketUserDisconnect.class);
                this.registerPacket(EnumPacketDirection.CLIENTBOUND, SPacketPrivateMessage.class);
            }
        };

    public static final int NUM_OF_STATES = values().length;

    private static final EnumConnectionState[] STATES_BY_ID = new EnumConnectionState[NUM_OF_STATES + 1];
    private static final Map<Class<? extends Packet<?>>, EnumConnectionState> STATES_BY_CLASS = Maps.newHashMap();
    private final int id;
    private final Map<EnumPacketDirection, BiMap<Integer, Class<? extends Packet<?>>>> directionMaps;

    EnumConnectionState(int protocolID)
    {
        this.directionMaps = Maps.newEnumMap(EnumPacketDirection.class);
        this.id = protocolID;
    }

    protected EnumConnectionState registerPacket(EnumPacketDirection direction, Class<? extends Packet<?>> packetClass)
    {
        BiMap<Integer, Class<? extends Packet<?>>> biMap = (BiMap)this.directionMaps.get(direction);

        if (biMap == null)
        {
            biMap = HashBiMap.create();
            this.directionMaps.put(direction, biMap);
        }

        if (biMap.containsValue(packetClass))
        {
            String s = direction + " packet " + packetClass + " is already known to ID " + biMap.inverse().get
                    (packetClass);
            Logger.fatal(s);
            throw new IllegalStateException(s);
        }
        else
        {
            biMap.put(biMap.size(), packetClass);
            return this;
        }
    }

    public Integer getPacketID(EnumPacketDirection direction, Packet<?> packetIn)
    {
        return (Integer)((BiMap)this.directionMaps.get(direction)).inverse().get(packetIn.getClass());
    }

    public Packet<?> getPacket(EnumPacketDirection direction, int packetID) throws InstantiationException, IllegalAccessException
    {
        Class<? extends Packet<?>> clazz = (Class)((BiMap)this.directionMaps.get(direction)).get(packetID);
        return clazz == null ? null : clazz.newInstance();
    }

    public int getID()
    {
        return this.id;
    }

    public static EnumConnectionState getByID(int stateID)
    {
        return stateID >= -1 && stateID <= NUM_OF_STATES - 1 ? STATES_BY_ID[stateID + 1] : null;
    }

    public static EnumConnectionState getFromPacket(Packet<?> packetIn)
    {
        return STATES_BY_CLASS.get(packetIn.getClass());
    }

    static
    {
        for (EnumConnectionState enumConnectionState : values())
        {
            int i = enumConnectionState.getID();

            if (i < -1 || i > NUM_OF_STATES - 1)
            {
                throw new Error("Invalid protocol ID " + Integer.toString(i));
            }

            STATES_BY_ID[i + 1] = enumConnectionState;

            for (EnumPacketDirection enumpacketdirection : enumConnectionState.directionMaps.keySet())
            {
                for (Class<? extends Packet<?>> clazz : (enumConnectionState.directionMaps.get(enumpacketdirection)).values())
                {
                    if (STATES_BY_CLASS.containsKey(clazz) && STATES_BY_CLASS.get(clazz) != enumConnectionState)
                    {
                        throw new Error("Packet " + clazz + " is already assigned to protocol " + STATES_BY_CLASS.get(clazz) + " - can\'t reassign to " + enumConnectionState);
                    }

                    try
                    {
                        clazz.newInstance();
                    }
                    catch (Throwable e)
                    {
                        throw new Error("Packet " + clazz + " fails instantiation checks! " + clazz);
                    }

                    STATES_BY_CLASS.put(clazz, enumConnectionState);
                }
            }
        }
    }

}