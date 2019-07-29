package ru.kromarong.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.kromarong.common.CMDList;
import ru.kromarong.common.Command;

import java.util.ArrayList;
import java.util.List;

public class MainHandler extends ChannelInboundHandlerAdapter {

    private static MainController mainController;

    public static void setController(MainController controller) {
        mainController = controller;
    }

    public MainHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Command)){
            ctx.fireChannelRead(msg);
            return;
        }

        Command command = (Command) msg;

        if (command.getCommand().split(" ")[0].equals(CMDList.FILES_LIST)){
            int count = Integer.parseInt(command.getCommand().split(" ")[1]);
            List<String> filesList = new ArrayList<>();
            for (int i = 1; i <= count; i++){
                filesList.add(command.getCommand().split("\\u0000")[i]);
            }
            mainController.refreshServerFilesList(filesList);
            return;
        }

        switch (command.getCommand()) {
            case CMDList.FILE_ACCEPT:
                mainController.updateLocalFilesList();
                break;
            case CMDList.AUTH_OK:
                Network.getInstance().setAuth(true);
                break;
            case CMDList.AUTH_ERROR:
                Network.getInstance().setAuth_error(true);
                break;
            case CMDList.REG_ERROR:
                Network.getInstance().setReg_error(true);
        }
    }
}
