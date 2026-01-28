package dev.onelili.unichat.velocity.command;

import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.channel.Channel;
import dev.onelili.unichat.velocity.handler.ChatHistoryManager;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.module.PatternModule;
import dev.onelili.unichat.velocity.util.Config;
import dev.onelili.unichat.velocity.util.ShitMountainException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DirectMessageCommand implements SimpleCommand {
    public static Map<UUID, UUID> lastMessage = new ConcurrentHashMap<>();

    @Override
    public void execute(Invocation invocation) {
        if(!(invocation.source() instanceof Player sender)) return;
        Player target;
        String message;

        if(Config.getConfigTree().getStringList("message.message-command").contains(invocation.alias())) {
            if(invocation.arguments().length < 2){
                invocation.source().sendMessage(Message.getMessage("command.msg-usage").toComponent());
                return;
            }
            var targetOpt = UniChat.getProxy().getPlayer(invocation.arguments()[0]);
            if (targetOpt.isEmpty()) {
                invocation.source().sendMessage(Message.getMessage("command.player-not-found").add("player", invocation.arguments()[0]).toComponent());
                return;
            }
            target = targetOpt.get();
            if (target == sender) {
                invocation.source().sendMessage(Message.getMessage("command.msg-self").toComponent());
                return;
            }
            message = String.join(" ", List.of(invocation.arguments()).subList(1, invocation.arguments().length));
        }else if(Config.getConfigTree().getStringList("message.reply-command").contains(invocation.alias())){
            if(invocation.arguments().length < 1){
                invocation.source().sendMessage(Message.getMessage("command.reply-usage").toComponent());
                return;
            }

            if(!lastMessage.containsKey(sender.getUniqueId()) || UniChat.getProxy().getPlayer(lastMessage.get(sender.getUniqueId())).isEmpty()){
                sender.sendMessage(Message.getMessage("command.reply-no-last-message").toComponent());
                return;
            }
            target = UniChat.getProxy().getPlayer(lastMessage.get(sender.getUniqueId())).get();
            message = String.join(" ", List.of(invocation.arguments()));
        }else{
            throw new ShitMountainException("Unknown command alias "+invocation.alias() +" in DirectMessageCommand");
        }
        Component msg = PatternModule.handleMessage(sender, message, false);
        Component inbound = new Message(Config.getString("message.format-inbound")).add("name", sender.getUsername()).toComponent()
                         .append(msg),
                 outbound = new Message(Config.getString("message.format-outbound")).add("name", target.getUsername()).toComponent()
                         .append(msg),
                 thirdparty = new Message(Config.getString("message.format-third-party")).add("sender", sender.getUsername()).add("target", target.getUsername()).toComponent()
                         .append(msg);
        target.sendMessage(inbound);
        sender.sendMessage(outbound);
        UniChat.getProxy().getConsoleCommandSource().sendMessage(thirdparty);
        lastMessage.put(target.getUniqueId(), sender.getUniqueId());
        lastMessage.put(sender.getUniqueId(), target.getUniqueId());

        ChatHistoryManager.recordMessage(sender.getUsername(), "msg", target.getUsername(), LegacyComponentSerializer.legacyAmpersand().serialize(msg));
    }
    public List<String> suggest(Invocation invocation) {
        if(!(invocation.source() instanceof Player)) return List.of();
        if(Config.getConfigTree().getStringList("message.message-command").contains(invocation.alias())) {
            if(invocation.arguments().length <= 1) {
                return UniChat.getProxy().getAllPlayers().stream().map(Player::getUsername).toList();
            }
        }
        return List.of();
    }

    public static void registerCommand(){
        List<String> commands = new ArrayList<>();
        commands.addAll(Config.getConfigTree().getStringList("message.message-command"));
        commands.addAll(Config.getConfigTree().getStringList("message.reply-command"));
        CommandMeta meta = UniChat.getProxy().getCommandManager()
                .metaBuilder(commands.get(0))
                .aliases(commands.subList(1, commands.size()).toArray(String[]::new))
                .build();
        Channel.getRegisteredChannelCommands().add(meta);
        UniChat.getProxy().getCommandManager().register(meta, new DirectMessageCommand());
    }
}
