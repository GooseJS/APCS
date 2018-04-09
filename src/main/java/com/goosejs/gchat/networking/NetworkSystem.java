package com.goosejs.gchat.networking;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.goosejs.gchat.networking.nethandler.server.NetHandlerHandshakeTCP;
import com.goosejs.gchat.util.LoadBase;
import com.goosejs.gchat.util.Logger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalEventLoopGroup;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class NetworkSystem
{
    public static ArrayList<String> ignoredPackets = new ArrayList<>();

    static
    {
        ignoredPackets.add("KeepAlive");
        ignoredPackets.add("UserList");
    }

    public static final LoadBase<NioEventLoopGroup> SERVER_NIO_EVENT_LOOP = new LoadBase<NioEventLoopGroup>()
    {
        @Override
        protected NioEventLoopGroup load()
        {
            return new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Server IO #%d").setDaemon(true).build());
        }
    };
    public static final LoadBase<EpollEventLoopGroup> SERVER_EPOLL_EVENT_LOOP = new LoadBase<EpollEventLoopGroup>()
    {
        @Override
        protected EpollEventLoopGroup load()
        {
            return new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Epoll Server IO #%d").setDaemon(true).build());
        }
    };
    public static final LoadBase<LocalEventLoopGroup> SERVER_LOCAL_EVENT_LOOP = new LoadBase<LocalEventLoopGroup>()
    {
        @Override
        protected LocalEventLoopGroup load()
        {
            return new LocalEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Local Server IO #%d").setDaemon(true).build());
        }
    };

    public volatile boolean isAlive;
    private final List<ChannelFuture> endPoints = Collections.synchronizedList(Lists.newArrayList());
    private final List<NetworkManager> networkManagers = Collections.synchronizedList(Lists.newArrayList());

    public void addLanEndpoint(String address, int port) throws IOException
    {
        addLanEndpoint(InetAddress.getByName(address), port);
    }

    public void addLanEndpoint(InetAddress address, int port) throws IOException
    {
        synchronized (this.endPoints)
        {
            Class<? extends ServerSocketChannel> clazz;
            LoadBase<? extends EventLoopGroup> loadBase;

            if (Epoll.isAvailable())
            {
                clazz = EpollServerSocketChannel.class;
                loadBase = SERVER_EPOLL_EVENT_LOOP;
                Logger.info("Using epoll channel type");
            }
            else
            {
                clazz = NioServerSocketChannel.class;
                loadBase = SERVER_NIO_EVENT_LOOP;
                Logger.info("Using default channel type");
            }

            this.endPoints.add((new ServerBootstrap()).channel(clazz).childHandler(new ChannelInitializer<Channel>()
            {
                @Override
                protected void initChannel(Channel ch) throws Exception
                {
                    try
                    {
                        ch.config().setOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
                    }
                    catch (ChannelException var3)
                    {
                        Logger.debug(var3);
                    }

//                    ch.pipeline().addLast("timeout", new ReadTimeoutHandler(30)).addLast("legacy_query",
//                            new PingHandler(NetworkSystem.this)).addLast("splitter", new NettySplitter()).addLast("decoder",
//                            new NettyPacketDecoder(EnumPacketDirection.SERVERBOUND)).addLast("prepender",
//                            new NettyPrepender()).addLast("encoder", new NettyPacketEncoder(EnumPacketDirection
//                            .CLIENTBOUND)).addLast(new JavaNettyConnectionTest());
                    ch.pipeline().addLast("timeout", new ReadTimeoutHandler(30)).addLast("splitter",
                            new NettySplitter()).addLast("decoder",
                            new NettyPacketDecoder(EnumPacketDirection.SERVERBOUND)).addLast("prepender",
                            new NettyPrepender()).addLast("encoder", new NettyPacketEncoder(EnumPacketDirection
                            .CLIENTBOUND));

                    NetworkManager networkManager = new NetworkManager(EnumPacketDirection.CLIENTBOUND);
                    NetworkSystem.this.networkManagers.add(networkManager);
                    ch.pipeline().addLast("packet_handler", networkManager);
                    networkManager.setNetHandler(new NetHandlerHandshakeTCP(networkManager));
                }
            }).group(loadBase.getValue()).localAddress(address, port).bind().syncUninterruptibly());
        }
    }

    public SocketAddress addLocalEndpoint()
    {
        ChannelFuture channelFuture;

        synchronized (this.endPoints)
        {
            channelFuture = new ServerBootstrap().channel(LocalServerChannel
                    .class).childHandler(new ChannelInitializer<Channel>()
            {
                @Override
                protected void initChannel(Channel ch) throws Exception
                {
                    NetworkManager networkManager = new NetworkManager(EnumPacketDirection.SERVERBOUND);
                    networkManager.setNetHandler(new NetHandlerHandshakeTCP(networkManager));
                    NetworkSystem.this.networkManagers.add(networkManager);
                    ch.pipeline().addLast("packet_handler", networkManager);
                }
            }).group(SERVER_NIO_EVENT_LOOP.getValue()).localAddress(LocalAddress.ANY).bind()
                    .syncUninterruptibly();
            this.endPoints.add(channelFuture);
        }

        return channelFuture.channel().localAddress();
    }

    public void terminateEndpoints()
    {
        this.isAlive = false;

        for (ChannelFuture channelFuture : this.endPoints)
        {
            try
            {
                channelFuture.channel().close().sync();
            }
            catch (InterruptedException e)
            {
                Logger.debug("Interrupted whilst closing channels.", e);
            }
        }
    }

    /**
     * Will try to process the packets received by each NetworkManager, gracefully manage processing failures and
     * cleans up dead connections
     */
    public void networkTick()
    {
        synchronized (this.networkManagers)
        {
            Iterator<NetworkManager> iterator = this.networkManagers.iterator();

            while (iterator.hasNext())
            {
                final NetworkManager networkManager = iterator.next();

                if (!networkManager.hasNoChannel())
                {
                    if (networkManager.isChannelOpen())
                    {
                        try
                        {
                            networkManager.processReceivedPackets();
                        }
                        catch (Exception exception)
                        {
                            if (networkManager.isLocalChannel())
                            {
                                // TODO: Implement this once the crash report system is in place
                                /**
                                CrashReport crashreport = CrashReport.makeCrashReport(exception, "Ticking memory connection");
                                CrashReportCategory crashreportcategory = crashreport.makeCategory("Ticking connection");
                                crashreportcategory.func_189529_a("Connection", new ICrashReportDetail<String>()
                                {
                                    public String call() throws Exception
                                    {
                                        return networkmanager.toString();
                                    }
                                });
                                throw new ReportedException(crashreport);
                                 */
                            }

                            Logger.warn("Failed to handle packet for {}", networkManager.getRemoteAddress(), exception);
                            final String text = "Internal server error";
                            // TODO: Implement once packets are put in place - networkManager.sendPacket(new SPacketDisconnect(text), (GenericFutureListener<Future<? super Void>>) p_operationComplete_1_ -> networkManager.closeChannel(text), new GenericFutureListener[0]);
                            networkManager.disableAutoRead();
                        }
                    }
                    else
                    {
                        Logger.debug("Disconnecting NetworkManager");
                        iterator.remove();
                        networkManager.checkDisconnected();
                    }
                }
            }
        }
    }

    public void sendPacket(Packet<?> packet)
    {
        sendPacket(packet, (Channel) null);
    }

    public void sendPacket(Packet<?> packet, Channel... blacklistAddresses)
    {
        synchronized (this.networkManagers)
        {
            Iterator<NetworkManager> iterator = this.networkManagers.iterator();

            while (iterator.hasNext())
            {
                NetworkManager networkManager = iterator.next();
                if (blacklistAddresses != null && !contains(blacklistAddresses, networkManager.getChannel()))
                    networkManager.sendPacket(packet);
            }
        }
    }

    public void sendPacket(Packet<?> packet, EnumConnectionState connectionState)
    {
        sendPacket(packet, connectionState, (Channel[])null);
    }

    public void sendPacket(Packet<?> packet, EnumConnectionState connectionState, Channel... blacklistAddresses)
    {
        synchronized (this.networkManagers)
        {
            Iterator<NetworkManager> iterator = this.networkManagers.iterator();

            while (iterator.hasNext())
            {
                NetworkManager networkManager = iterator.next();
                if (networkManager.getConnectionState() == connectionState && !(blacklistAddresses != null && !contains
                    (blacklistAddresses, networkManager.getChannel())))
                {
                    networkManager.sendPacket(packet);
                }
            }
        }
    }

    private <E> boolean contains(E[] array, E contains)
    {
        for (E value : array)
            if (value.equals(contains)) return true;

        return false;
    }

}