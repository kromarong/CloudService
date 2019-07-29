package ru.kromarong.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class ProtocolHandler extends ChannelInboundHandlerAdapter {

    private String path;

    public ProtocolHandler(String username) {
        if (!username.equals("client")){
            this.path = "server_storage/" + username + "/";
        }else {
            this.path = "client_storage/";
        }
    }

    private int state = -1;
    private int stringLen = -1;
    private long fileLen = -1;
    private String fileName;
    private String command;
    private DataType type = DataType.EMPTY;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        do {
            if (state == -1) {
                byte firstByte = buf.readByte();
                type = DataType.getDataTypeFromByte(firstByte);
                state = 0;
                stringLen = 4;
                fileLen = -1;
                System.out.println(type);
            }
//            считываем длину имени файла или команды
            if (state == 0) {
                if (buf.readableBytes() < stringLen) {
                    return;
                }
                stringLen = buf.readInt();
                state = 1;
            }
//        считываем имя файла или команду
            if (state == 1) {
                if (buf.readableBytes() < stringLen) {
                    return;
                }
                byte[] data = new byte[stringLen];
                buf.readBytes(data);
                String str = new String(data);
                System.out.println(type + " " + str);
                if (type == DataType.COMMAND) {
                    command = str;
                    ctx.fireChannelRead(new Command(command));
                    state = -1;
                } else if (type == DataType.FILE) {
                    fileLen = 8;
                    fileName = str;
                    state = 2;
                }
            }
//            считываем размер файла и проверяем, не существует ли он уже в хранилище
            if (state == 2) {
                if (buf.readableBytes() < fileLen) {
                    System.out.println("вылет в первом блоке" + buf.readableBytes());
                    return;
                }
                fileLen = buf.readLong();
                File f = new File(path + fileName);
                if (f.exists()){
                    fileName = "(copy)" + fileName;
                }
                state = 3;
            }

//            пишем данные в файл
            if (state == 3) {
                byte[] data;
                if (fileLen >= buf.readableBytes()) {
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path + fileName, true))) {
                        data = new byte[buf.readableBytes()];
                        buf.readBytes(data);
                        bos.write(data);
                        fileLen -= data.length;
                    }
                } else {
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path + fileName, true))) {
                        data = new byte[(int) fileLen];
                        buf.readBytes(data);
                        bos.write(data);
                        fileLen = -1;
                    }
                }
                if (fileLen <= 0) {
                    ctx.fireChannelRead(new Command(CMDList.FILE_ACCEPT));
                    state = -1;
                }
            }

        } while (buf.readableBytes() > 0);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}