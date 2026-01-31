package dev.onelili.unichat.velocity.command;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.command.SimpleCommand;
import dev.onelili.unichat.velocity.handler.RedisRemoteManager;
import dev.onelili.unichat.velocity.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OnlineCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        if(invocation.arguments().length <= 1) {
            invocation.source().sendMessage(Message.getMessage("command.online.usage").toComponent());
            return;
        }
        if(RedisRemoteManager.getInstance()!=null) {
            String subCommand = invocation.arguments()[0];
            switch (subCommand) {
                case "player" -> {
                    if(invocation.arguments().length < 2) {
                        invocation.source().sendMessage(Message.getMessage("command.online.usage").toComponent());
                        return;
                    }
                    var fetch = RedisRemoteManager.getInstance().fetchPlayerServer(invocation.arguments()[1]);
                    if(fetch.isEmpty()) {
                        invocation.source().sendMessage(Message.getMessage("command.online.no-player")
                                .add("player", invocation.arguments()[1])
                                .toComponent());
                    }else{
                        invocation.source().sendMessage(Message.getMessage("command.online.player")
                                .add("player", invocation.arguments()[1])
                                .add("servers", String.join(",", fetch))
                                .toComponent());
                    }
                }
                case "server" -> {
                    if(invocation.arguments().length < 2) {
                        invocation.source().sendMessage(Message.getMessage("command.online.usage").toComponent());
                        return;
                    }
                    if(!RedisRemoteManager.getInstance().getOnlinePlayerCache().containsKey(invocation.arguments()[1])) {
                        invocation.source().sendMessage(Message.getMessage("command.online.server-not-found").toComponent());
                        return;
                    }
                    var fetch = RedisRemoteManager.getInstance().getOnlinePlayerCache().get(invocation.arguments()[1]);
                    if(fetch.isEmpty()) {
                        invocation.source().sendMessage(Message.getMessage("command.online.empty-server")
                                .add("server", invocation.arguments()[1])
                                .toComponent());
                    }else{
                        invocation.source().sendMessage(Message.getMessage("command.online.server")
                                .add("server", invocation.arguments()[1])
                                .add("players", String.join(",", fetch))
                                .toComponent());
                    }
                }
                default -> {
                    invocation.source().sendMessage(Message.getMessage("command.online.usage").toComponent());
                }
            }
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if(RedisRemoteManager.getInstance()==null) {
            return ImmutableList.of();
        }
        if(invocation.arguments().length <= 1) {
            return ImmutableList.of("player", "server");
        }
        if(invocation.arguments().length == 2) {
            if(invocation.arguments()[0].equals("server")) {
                return RedisRemoteManager.getInstance().getOnlinePlayerCache().keySet().stream().filter(s->s.toLowerCase(Locale.ROOT).startsWith(invocation.arguments()[1].toLowerCase(Locale.ROOT))).toList();
            }else if(invocation.arguments()[0].equals("player")){
                return RedisRemoteManager.getInstance().getOnlinePlayers().stream().filter(s->s.toLowerCase(Locale.ROOT).startsWith(invocation.arguments()[1].toLowerCase(Locale.ROOT))).toList();
            }
        }
        return ImmutableList.of();
    }
}
