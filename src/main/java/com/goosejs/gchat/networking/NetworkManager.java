package com.goosejs.gchat.networking;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.goosejs.gchat.networking.nethandler.INetHandler;
import com.goosejs.gchat.util.LoadBase;
import com.goosejs.gchat.util.Logger;
import com.goosejs.gchat.util.interfaces.ITickable;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalEventLoopGroup;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NetworkManager extends SimpleChannelInboundHandler<Packet<?>>
{

    public static final Marker NETWORK_MARKER = MarkerManager.getMarker("NETWORK");
    public static final Marker NETWORK_PACKETS_MARKER = MarkerManager.getMarker("NETWORK_PACKETS");
    public static final AttributeKey<EnumConnectionState> PROTOCOL_ATTRIBUTE_KEY = AttributeKey.valueOf("protocol");
    public static final LoadBase<NioEventLoopGroup> CLIENT_NIO_EVENT_LOOP = new LoadBase<NioEventLoopGroup>()
    {
        protected NioEventLoopGroup load()
        {
            return new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Client IO #%d")
                    .setDaemon(true).build());
        }
    };
    public static final LoadBase<EpollEventLoopGroup> CLIENT_EPOLL_EVENT_LOOP = new LoadBase<EpollEventLoopGroup>()
    {
        @Override
        protected EpollEventLoopGroup load()
        {
            return new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Epoll Client IO #%d")
                    .setDaemon(true).build());
        }
    };
    public static final LoadBase<LocalEventLoopGroup> CLIENT_LOCAL_EVENT_LOOP = new LoadBase<LocalEventLoopGroup>()
    {
        @Override
        protected LocalEventLoopGroup load()
        {
            return new LocalEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Local Client IO #%d")
                    .setDaemon(true).build());
        }
    };

    private final EnumPacketDirection direction;
    private final Queue<NetworkManager.InboundHandlerTuplePacketListener> outboundPacketsQueue = Queues
            .newConcurrentLinkedQueue();
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /** The active channel */
    private Channel channel;

    /** The address of the remote party (Server if client, client if server) */
    private SocketAddress socketAddress;

    /** The INetHandler instance responsible for processing received packets */
    private INetHandler packetListener;

    /** A String indicating why the network has shutdown. */
    private String terminationReason;
    /** Will be used for encryption (if ever implemented) */
    private boolean isEncrypted;
    private boolean disconnected;

    public NetworkManager(EnumPacketDirection packetDirection)
    {
        this.direction = packetDirection;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        super.channelActive(ctx);
        this.channel = ctx.channel();
        this.socketAddress = this.channel.remoteAddress();

        try
        {
            this.setConnectionState(EnumConnectionState.HANDSHAKING);
        }
        catch (Throwable throwable)
        {
            Logger.fatal(throwable);
            throwable.printStackTrace();
        }
    }

    /**
     * Sets the new connection state and registers which packets this channel may send and receive
     * @param newState the new connection state
     */
    public void setConnectionState(EnumConnectionState newState)
    {
        this.channel.attr(PROTOCOL_ATTRIBUTE_KEY).set(newState);
        this.channel.config().setAutoRead(true);
        Logger.debug("Enabled auto read");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        this.closeChannel("Error: End of Stream");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        String text;

        if (cause instanceof TimeoutException)
        {
            text = "Error: Timed out";
        }
        else
        {
            text = "Error: Internal Exception on Netty Pipeline { " + cause + " }";
        }

        Logger.debug(text, cause);
        this.closeChannel(text);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet<?> msg) throws Exception
    {
        if (this.channel.isOpen())
        {
            ((Packet<INetHandler>)msg).processPacket(this.packetListener);
        }
    }

    public void setNetHandler(INetHandler handler)
    {
        Validate.notNull(handler, "packetListener");
        Logger.debug("Set listener of {} to {}", this, handler);
        this.packetListener = handler;
    }

    public void sendPacket(Packet<?> packet)
    {
        if (this.isChannelOpen())
        {
            this.flushOutboundQueue();
            this.dispatchPacket(packet, null);
        }
        else
        {
            this.readWriteLock.writeLock().lock();

            try
            {
                this.outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packet, (GenericFutureListener)null));
            }
            finally
            {
                this.readWriteLock.writeLock().unlock();
            }
        }
    }

    public void sendPacket(Packet<?> packetIn, GenericFutureListener<? extends Future<? super Void>> listener, GenericFutureListener <? extends Future<? super Void>> ... listeners)
    {
        if (this.isChannelOpen())
        {
            this.flushOutboundQueue();
            this.dispatchPacket(packetIn, ArrayUtils.add(listeners, 0, listener));
        }
        else
        {
            this.readWriteLock.writeLock().lock();

            try
            {
                this.outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packetIn, ArrayUtils.add(listeners, 0, listener)));
            }
            finally
            {
                this.readWriteLock.writeLock().unlock();
            }
        }
    }

    /**
     * Will commit the packet to the channel. If the current thread 'owns' the channel it will write and flush the
     * packet, otherwise it will add a task for the channel eventloop thread to do that.
     */
    private void dispatchPacket(final Packet<?> packet, final GenericFutureListener <? extends Future <? super Void>> [] futureListeners)
    {
        final EnumConnectionState currentConnectionState = this.channel.attr(PROTOCOL_ATTRIBUTE_KEY).get();
        final EnumConnectionState connectionState = EnumConnectionState.getFromPacket(packet);

        if (currentConnectionState != connectionState)
        {
            Logger.debug("Disabled auto read");
            this.channel.config().setAutoRead(false);
        }

        if (this.channel.eventLoop().inEventLoop())
        {
            if (connectionState != currentConnectionState)
            {
                this.setConnectionState(connectionState);
            }

            ChannelFuture channelFuture = this.channel.writeAndFlush(packet);

            if (futureListeners != null)
            {
                channelFuture.addListeners(futureListeners);
            }

            channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }
        else
        {
            this.channel.eventLoop().execute(() ->
            {
                if (connectionState != currentConnectionState)
                {
                    NetworkManager.this.setConnectionState(connectionState);
                }

                ChannelFuture channel = NetworkManager.this.channel.writeAndFlush(packet);

                if (futureListeners != null)
                {
                    channel.addListeners(futureListeners);
                }

                channel.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            });
        }
    }

    /**
     * Will iterate through the outboundPacketQueue and dispatch all Packets
     */
    private void flushOutboundQueue()
    {
        if (this.channel != null && this.channel.isOpen())
        {
            this.readWriteLock.readLock().lock();

            try
            {
                while (!this.outboundPacketsQueue.isEmpty())
                {
                    NetworkManager.InboundHandlerTuplePacketListener inboundPacketHandler = this.outboundPacketsQueue.poll();
                    this.dispatchPacket(inboundPacketHandler.packet, inboundPacketHandler.futureListeners);
                }
            }
            finally
            {
                this.readWriteLock.readLock().unlock();
            }
        }
    }

    /**
     * Checks timeouts and processes all packets received
     */
    public void processReceivedPackets()
    {
        this.flushOutboundQueue();

        if (this.packetListener instanceof ITickable)
        {
            ((ITickable)this.packetListener).update();
        }

        this.channel.flush();
    }

    /**
     * Returns the socket address of the remote side. Server-only.
     */
    public SocketAddress getRemoteAddress()
    {
        return this.socketAddress;
    }

    public Channel getChannel()
    {
        return this.channel;
    }

    /**
     * Closes the channel, the parameter can be used for an exit message (not certain how it gets sent)
     */
    public void closeChannel(String message)
    {
        if (this.channel.isOpen())
        {
            this.channel.close().awaitUninterruptibly();
            this.terminationReason = message;
        }
    }

    /**
     * True if this NetworkManager uses a memory connection (integrated server). False may imply both an active TCP
     * connection or simply no active connection at all
     */
    public boolean isLocalChannel()
    {
        return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
    }

    public static NetworkManager createNetworkManagerAndConnect(InetAddress address, int serverPort, boolean useNativeTransport)
    {
        return createNetworkManagerAndConnect(address, serverPort, useNativeTransport, null);
    }

    /**
     * Create a new NetworkManager from the server host and connect it to the server
     */
    public static NetworkManager createNetworkManagerAndConnect(InetAddress address, int serverPort, boolean useNativeTransport, GenericFutureListener<? extends Future<? super Void>> futureListener)
    {
        final NetworkManager networkManager = new NetworkManager(EnumPacketDirection.SERVERBOUND);
        Class<? extends SocketChannel> clazz;
        LoadBase<? extends EventLoopGroup > loadBase;

        if (Epoll.isAvailable() && useNativeTransport)
        {
            clazz = EpollSocketChannel.class;
            loadBase = CLIENT_EPOLL_EVENT_LOOP;
        }
        else
        {
            clazz = NioSocketChannel.class;
            loadBase = CLIENT_NIO_EVENT_LOOP;
        }

        ChannelFuture future = (new Bootstrap()).group(loadBase.getValue()).handler(new ChannelInitializer<Channel>()
        {
            protected void initChannel(Channel channel) throws Exception
            {
                try
                {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                }
                catch (ChannelException e)
                {
                    e.printStackTrace();
                }

                channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30)).addLast("splitter",
                        new NettySplitter()).addLast("decoder", new NettyPacketDecoder(EnumPacketDirection
                        .CLIENTBOUND)).addLast("prepender", new NettyPrepender()).addLast("encoder", new
                        NettyPacketEncoder(EnumPacketDirection.SERVERBOUND)).addLast("packet_handler", networkManager);
            }
        }).channel(clazz).connect(address, serverPort);

        future.addListener(futureListener);

        return networkManager;
    }

    /**
     * Prepares a clientside NetworkManager: establishes a connection to the socket supplied and configures the channel
     * pipeline. Returns the newly created instance.
     */
    public static NetworkManager provideLocalClient(SocketAddress address)
    {
        final NetworkManager networkManager = new NetworkManager(EnumPacketDirection.CLIENTBOUND);
        (new Bootstrap()).group(CLIENT_LOCAL_EVENT_LOOP.getValue()).handler(new ChannelInitializer<Channel>()
        {
            protected void initChannel(Channel p_initChannel_1_) throws Exception
            {
                p_initChannel_1_.pipeline().addLast("packet_handler", networkManager);
            }
        }).channel(LocalChannel.class).connect(address).syncUninterruptibly();
        return networkManager;
    }

    // TODO: Look into actually making encryption work
//    /**
//     * Adds an encoder+decoder to the channel pipeline. The parameter is the secret key used for encrypted communication
//     */
//    public void enableEncryption(SecretKey key)
//    {
//        this.isEncrypted = true;
//        this.channel.pipeline().addBefore("splitter", "decrypt", new NettyEncryptingDecoder(CryptManager.createNetCipherInstance(2, key)));
//        this.channel.pipeline().addBefore("prepender", "encrypt", new NettyEncryptingEncoder(CryptManager.createNetCipherInstance(1, key)));
//    }

    public boolean isEncrypted()
    {
        return this.isEncrypted;
    }

    /**
     * Returns true if this NetworkManager has an active channel, false otherwise
     */
    public boolean isChannelOpen()
    {
        return this.channel != null && this.channel.isOpen();
    }

    public boolean hasNoChannel()
    {
        return this.channel == null;
    }

    /**
     * Gets the current handler for processing packets
     */
    public INetHandler getNetHandler()
    {
        return this.packetListener;
    }

    /**
     * If this channel is closed, returns the exit message, null otherwise.
     */
    public String getExitMessage()
    {
        return this.terminationReason;
    }

    /**
     * Switches the channel to manual reading mode
     */
    public void disableAutoRead()
    {
        this.channel.config().setAutoRead(false);
    }

    public void setCompressionThreshold(int threshold)
    {
        if (threshold >= 0)
        {
            if (this.channel.pipeline().get("decompress") instanceof NettyCompressionDecoder)
            {
                ((NettyCompressionDecoder)this.channel.pipeline().get("decompress")).setCompressionThreshold(threshold);
            }
            else
            {
                this.channel.pipeline().addBefore("decoder", "decompress", new NettyCompressionDecoder(threshold));
            }

            if (this.channel.pipeline().get("compress") instanceof NettyCompressionEncoder)
            {
                ((NettyCompressionEncoder)this.channel.pipeline().get("compress")).setCompressionThreshold(threshold);
            }
            else
            {
                this.channel.pipeline().addBefore("encoder", "compress", new NettyCompressionEncoder(threshold));
            }
        }
        else
        {
            if (this.channel.pipeline().get("decompress") instanceof NettyCompressionDecoder)
            {
                this.channel.pipeline().remove("decompress");
            }

            if (this.channel.pipeline().get("compress") instanceof NettyCompressionEncoder)
            {
                this.channel.pipeline().remove("compress");
            }
        }
    }

    public void checkDisconnected()
    {
        if (this.channel != null && !this.channel.isOpen())
        {
            if (this.disconnected)
            {
                Logger.warn("handleDisconnection() called twice");
            }
            else
            {
                this.disconnected = true;

                if (this.getExitMessage() != null)
                {
                    this.getNetHandler().onDisconnect(this.getExitMessage());
                }
                else if (this.getNetHandler() != null)
                {
                    this.getNetHandler().onDisconnect("Disconnected");
                }
            }
        }
    }

    public EnumConnectionState getConnectionState()
    {
        return this.channel.attr(PROTOCOL_ATTRIBUTE_KEY).get();
    }

    private static class InboundHandlerTuplePacketListener
    {
        private final Packet<?> packet;
        private final GenericFutureListener <? extends Future<? super Void>> [] futureListeners;

        @SafeVarargs
        InboundHandlerTuplePacketListener(Packet<?> inPacket, GenericFutureListener<? extends Future<? super Void>>... inFutureListeners)
        {
            this.packet = inPacket;
            this.futureListeners = inFutureListeners;
        }
    }

}