package ru.kromarong.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

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
            File file = new File(fm.getPath());
            byte[] data = new byte[1024 * 1024];
            long size = file.length();
            int n;
            ByteBuf buf = ctx.alloc().buffer();

            System.out.println("размер буфера " + buf.maxCapacity());
            System.out.println("размер fайла  " + size);

            buf.writeByte(15);
            buf.writeInt(filenameBytes.length);
            buf.writeBytes(filenameBytes);
            buf.writeLong(size);

            try(FileInputStream fis = new FileInputStream(file)){
                while (( n = fis.read(data)) != -1){
                    System.out.println("Зашли в цикл");
                    buf.writeBytes(data);
                }
            }

            ctx.writeAndFlush(buf);
//            ReferenceCountUtil.retain(buf);
            buf.release();

            System.out.println("SDH sending file " + fm.getFilename());

        }

    }
}
