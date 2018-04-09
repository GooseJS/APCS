package com.goosejs.gchat.networking;

import com.goosejs.gchat.networking.nethandler.INetHandler;

import java.io.IOException;



public interface Packet<T extends INetHandler>
{

    /**
     * Reads the raw packet data from the data stream
     * @param buf the data stream to read from
     * @throws IOException
     */
    void readPacketData(PacketBuffer buf) throws IOException;

    /**
     * Writes the raw packet data to the data stream
     * @param buf the data stream to write to
     * @throws IOException
     */
    void writePacketData(PacketBuffer buf) throws IOException;

    /**
     * Passes this packet on to the NetHandler for processing
     * @param handler the NetHandler to use for processing
     */
    void processPacket(T handler);

}