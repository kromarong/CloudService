package ru.kromarong.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.kromarong.common.CMDList;
import ru.kromarong.common.Command;
import ru.kromarong.common.FileMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainHandler extends ChannelInboundHandlerAdapter {
    private String path;

    public MainHandler(String username) {
        this.path = "server_storage/" + username + "/";
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Command)){
            ctx.fireChannelRead(msg);
            return;
        }

        Command command = (Command) msg;

        if (command.getCommand().equals(CMDList.REQUEST_FILES_LIST)){
            sendFilesList(ctx);
        }

        if (command.getCommand().equals(CMDList.FILE_ACCEPT)){
            sendFilesList(ctx);
        }

        if (command.getCommand().equals(CMDList.SEND_FILES_LIST)){
            sendFilesList(ctx);
        }

        if (command.getCommand().split(" ")[0].equals("/DOWNLOAD")){

            String filename = command.getCommand().split("\u0000")[1];
            String filepath = path + filename;
            System.out.println(" пришел запрос на скачивание файла " + filename);
            ctx.writeAndFlush(new FileMessage(filename, filepath));
        }

        if (command.getCommand().split(" ")[0].equals(CMDList.DELETE)){
            String filename = command.getCommand().split("\u0000")[1];
            File f = new File(path + filename);
            if (f.exists()){
                f.delete();
                sendFilesList(ctx);
            }
        }

    }

    private void sendFilesList(ChannelHandlerContext ctx) throws IOException {
        StringBuilder sb = new StringBuilder("/FILES_LIST ");
        List<String> filesList = new ArrayList<>();
        Files.list(Paths.get(path)).map(p -> p.getFileName().toString()).forEach(o -> filesList.add(o));
        sb.append(filesList.size() + " " + "\u0000");
        for (String value : filesList){
            sb.append(value + "\u0000");
        }
        ctx.writeAndFlush(new Command(sb.toString()));
    }
}
