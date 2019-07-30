package ru.kromarong.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SendDataHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Command){
            Command command = (Command) msg;
            byte[] authBytes = command.getCommand().getBytes();

            ByteBuf cmdBuf = ctx.alloc().buffer();

            cmdBuf.writeByte(16);
            cmdBuf.writeInt(authBytes.length);
            cmdBuf.writeBytes(authBytes);

            System.out.println("sending command " + command.getCommand());
            ctx.writeAndFlush(cmdBuf);
            cmdBuf.release();
        }

        if (msg instanceof FileMessage){

            FileMessage fm = (FileMessage) msg;

            byte[] filenameBytes = fm.getFilename().getBytes();
            ByteBuf buf = ctx.alloc().buffer();
            File file = new File(fm.getPath());
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            long size = file.length();
            byte[] data = new byte[1024 * 1024];

            System.out.println("размер буфера " + buf.maxCapacity());
            System.out.println("размер файла  " + size);

            buf.writeByte(15);
            buf.writeInt(filenameBytes.length);
            buf.writeBytes(filenameBytes);
            buf.writeLong(size);

            while (true){
                if (size > data.length){
                    bis.read(data);
                    buf.writeBytes(data);
                    size -= data.length;
                } else {
                    byte[] temp = new byte[(int) size];
                    bis.read(temp);
                    buf.writeBytes(temp);
                    break;
                }
            }

            ctx.writeAndFlush(buf);
            ReferenceCountUtil.retain(buf);
            buf.release();
            bis.close();
            System.out.println("SDH sending file " + fm.getFilename());

        }

    }
}
