package ru.kromarong.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.kromarong.common.CMDList;
import ru.kromarong.common.Command;
import ru.kromarong.common.DataType;
import ru.kromarong.common.ProtocolHandler;

import java.sql.SQLException;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    private ConnectToDataBase connect;

    private boolean authOk = false;

    private int commandLen = -1;
    private String command;
    private DataType type = DataType.EMPTY;

    public AuthHandler(ConnectToDataBase connect) {
        this.connect = connect;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws SQLException {

        if (authOk) {
            ctx.fireChannelRead(msg);
            return;
        }

        ByteBuf buf = ((ByteBuf) msg);

        byte firstByte = buf.readByte();
        type = DataType.getDataTypeFromByte(firstByte);
        if (type == DataType.COMMAND) {
            commandLen = 4;

            if (buf.readableBytes() < commandLen) {
                return;
            }
            commandLen = buf.readInt();

            if (buf.readableBytes() < commandLen) {
                return;
            }

            byte[] data = new byte[commandLen];
            buf.readBytes(data);
            command = new String(data);

            String actionType = command.split(" ")[0];
            if (actionType.equals(CMDList.AUTH) || actionType.equals(CMDList.REGISTRATION)) {
                String username = command.split(" ")[1];
                String password = command.split(" ")[2];

                if (actionType.equals(CMDList.AUTH)){
                    authOk = connect.checkUser(username, password);
                    if (authOk){
                        createNewChannel(ctx, username);
                    } else {
                        ctx.writeAndFlush(new Command(CMDList.AUTH_ERROR));
                    }
                }else{
                    authOk = connect.createNewUser(username, password);
                    if (authOk){
                        createNewChannel(ctx, username);
                    } else {
                        ctx.writeAndFlush(new Command(CMDList.REG_ERROR));
                    }
                }
                System.out.println("auth: " + authOk);
            }
        }
    }

    private void createNewChannel(ChannelHandlerContext ctx, String username) {
        ctx.pipeline().addLast(new ProtocolHandler(username), new MainHandler(username));
        ctx.writeAndFlush(new Command(CMDList.AUTH_OK));
        ctx.pipeline().remove(this);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
