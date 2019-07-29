package ru.kromarong.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import ru.kromarong.common.Command;
import ru.kromarong.common.FileMessage;
import ru.kromarong.common.ProtocolHandler;
import ru.kromarong.common.SendDataHandler;

import java.net.InetSocketAddress;

public class Network {

    volatile private boolean auth;
    public boolean isAuth() {
        return auth;
    }
    public void setAuth(boolean auth) {
        this.auth = auth;
    }


    volatile private boolean auth_error;
    public boolean isAuth_error() {
        return auth_error;
    }
    public void setAuth_error(boolean auth_error) {
        this.auth_error = auth_error;
    }

    volatile private boolean reg_error;

    public boolean isReg_error() {
        return reg_error;
    }

    public void setReg_error(boolean reg_error) {
        this.reg_error = reg_error;
    }

    private static Network ourInstance = new Network();

    public static Network getInstance() {
        return ourInstance;
    }

    private Network() {
    }

    private Channel currentChannel;

    public Channel getCurrentChannel() {
        return currentChannel;
    }

    public void start() {
        Thread t = new Thread(() ->{
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap clientBootstrap = new Bootstrap();
                clientBootstrap.group(group);
                clientBootstrap.channel(NioSocketChannel.class);

                clientBootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
                clientBootstrap.remoteAddress(new InetSocketAddress("localhost", 8189));
                clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(
                                new ProtocolHandler("client"),
                                new MainHandler(),
                                new SendDataHandler()
                        );
                        currentChannel = socketChannel;
                    }
                });
                ChannelFuture channelFuture = clientBootstrap.connect().sync();
                channelFuture.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    group.shutdownGracefully().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }});
        t.setDaemon(true);
        t.start();
        }

    public void stop() {
        currentChannel.close();
    }

    public void sendCommand(String str) {
        Command command = new Command(str);
        currentChannel.writeAndFlush(command);
    }

    public void sendFile(String filename, String path){
        FileMessage fm  = new FileMessage(filename, path);
        currentChannel.writeAndFlush(fm);
    }

}
