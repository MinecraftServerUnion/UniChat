package cn.jason31416.chatx.command;

import cn.jason31416.chatx.handler.PunishmentHandler;
import cn.jason31416.chatx.util.*;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import cn.jason31416.chatx.ChatX;
import cn.jason31416.chatx.channel.Channel;
import cn.jason31416.chatx.handler.ChatHistoryManager;
import cn.jason31416.chatx.handler.RedisRemoteManager;
import cn.jason31416.chatx.message.Message;
import cn.jason31416.chatx.module.PatternModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DirectMessageCommand implements SimpleCommand {
    public static Map<UUID, String> lastMessage = new ConcurrentHashMap<>();

    @Override
    public void execute(Invocation invocation) {
        if(!(invocation.source() instanceof Player sender)) {
            if(Config.getConfigTree().getStringList("message.message-command").contains(invocation.alias())) {
                if(invocation.arguments().length < 2){
                    invocation.source().sendMessage(Message.getMessage("command.msg-usage").toComponent());
                    return;
                }
                String target = invocation.arguments()[0];
                String message = String.join(" ", List.of(invocation.arguments()).subList(1, invocation.arguments().length));
                if(ChatX.getProxy().getPlayer(target).isPresent()) {
                    Component msg = PatternModule.handleMessage(null, message, List.of(new SimplePlayer(ChatX.getProxy().getPlayer(target).get())));
                    Component inbound = new Message(Config.getString("message.format-inbound")).add("name", "CONSOLE").toComponent()
                            .append(msg),
                            outbound = new Message(Config.getString("message.format-outbound")).add("name", target).toComponent()
                                    .append(msg);
                    ChatX.getProxy().getPlayer(target).get().sendMessage(inbound);
                    ChatX.getProxy().getConsoleCommandSource().sendMessage(outbound);
                }else{
                    invocation.source().sendMessage(Message.getMessage("command.player-not-found").add("player", target).toComponent());
                }
            }
            return;
        }
        long timeMuted=PunishmentHandler.fetchMuted(new SimplePlayer(sender));
        if(timeMuted!=-1){
            sender.sendMessage(Message.getMessage("chat.player-is-muted").add("time_left", TimeUtil.displayMillis(timeMuted-System.currentTimeMillis())).toComponent());
            return;
        }
        String target;
        String message;

        if(Config.getConfigTree().getStringList("message.message-command").contains(invocation.alias())) {
            if(invocation.arguments().length < 2){
                invocation.source().sendMessage(Message.getMessage("command.msg-usage").toComponent());
                return;
            }
//            var targetOpt = UniChat.getProxy().getPlayer(invocation.arguments()[0]);
//            if (targetOpt.isEmpty()) {
//                invocation.source().sendMessage(Message.getMessage("command.player-not-found").add("player", invocation.arguments()[0]).toComponent());
//                return;
//            }
            target = invocation.arguments()[0];
            if (target.equals(sender.getUsername())) {
                invocation.source().sendMessage(Message.getMessage("command.msg-self").toComponent());
                return;
            }
            message = String.join(" ", List.of(invocation.arguments()).subList(1, invocation.arguments().length));
        }else if(Config.getConfigTree().getStringList("message.reply-command").contains(invocation.alias())){
            if(invocation.arguments().length < 1){
                invocation.source().sendMessage(Message.getMessage("command.reply-usage").toComponent());
                return;
            }

            if(!lastMessage.containsKey(sender.getUniqueId()) || ChatX.getProxy().getPlayer(lastMessage.get(sender.getUniqueId())).isEmpty()){
                sender.sendMessage(Message.getMessage("command.reply-no-last-message").toComponent());
                return;
            }
            target = lastMessage.get(sender.getUniqueId());
            message = String.join(" ", List.of(invocation.arguments()));
        }else{
            throw new ShitMountainException("Unknown command alias "+invocation.alias() +" in DirectMessageCommand");
        }
        var targetPlayer = ChatX.getProxy().getPlayer(target);
        List<SimplePlayer> tlist = targetPlayer.map(player -> List.of(new SimplePlayer(player))).orElseGet(List::of);
        Component msg = PatternModule.handleMessage(sender, message, tlist);
        Component inbound = new Message(Config.getString("message.format-inbound")).add("name", sender.getUsername()).toComponent()
                         .append(msg),
                 outbound = new Message(Config.getString("message.format-outbound")).add("name", target).toComponent()
                         .append(msg),
                 thirdparty = new Message(Config.getString("message.format-third-party")).add("sender", sender.getUsername()).add("receiver", target).toComponent()
                         .append(msg);
        if(targetPlayer.isPresent()) {
            targetPlayer.get().sendMessage(inbound);
            lastMessage.put(targetPlayer.get().getUniqueId(), sender.getUsername());
            ChatHistoryManager.recordMessage(sender.getUsername(), "msg", target, LegacyComponentSerializer.legacyAmpersand().serialize(msg));
        }else if(RedisRemoteManager.getInstance()!=null && RedisRemoteManager.getInstance().getOnlinePlayers().contains(target)){
            MapTree cont = new MapTree()
                    .put("msg", MiniMessage.miniMessage().serialize(msg))
                    .put("sender", sender.getUsername())
                    .put("server", Config.getString("server-name"))
                    .put("type", "msg")
                    .put("target", target);
            RedisRemoteManager.getInstance().getJedis().publish("unichat-channel", cont.toJson());
        }else{
            invocation.source().sendMessage(Message.getMessage("command.player-not-found").add("player", target).toComponent());
            return;
        }
        sender.sendMessage(outbound);
        lastMessage.put(sender.getUniqueId(), target);
        if(Config.getConfigTree().getBoolean("message.log-console", false)) {
            ChatX.getProxy().getConsoleCommandSource().sendMessage(thirdparty);
        }
    }
    public List<String> suggest(Invocation invocation) {
        if(!(invocation.source() instanceof Player)) return List.of();
        if(Config.getConfigTree().getStringList("message.message-command").contains(invocation.alias())) {
            if(invocation.arguments().length <= 1) {
                Set<String> ret = new HashSet<>(ChatX.getProxy().getAllPlayers().stream().map(Player::getUsername).toList());
                if(invocation.arguments().length<1||invocation.arguments()[0].isEmpty()){
                    return new ArrayList<>(ret);
                }
                if(RedisRemoteManager.getInstance()!=null)
                    ret.addAll(RedisRemoteManager.getInstance().getOnlinePlayers());
                return ret.stream().filter(s->s.toLowerCase(Locale.ROOT).startsWith(invocation.arguments()[0].toLowerCase(Locale.ROOT))).toList();
            }
        }
        return List.of();
    }

    public static void registerCommand(){
        List<String> commands = new ArrayList<>();
        commands.addAll(Config.getConfigTree().getStringList("message.message-command"));
        commands.addAll(Config.getConfigTree().getStringList("message.reply-command"));
        CommandMeta meta = ChatX.getProxy().getCommandManager()
                .metaBuilder(commands.get(0))
                .aliases(commands.subList(1, commands.size()).toArray(String[]::new))
                .build();
        Channel.getRegisteredChannelCommands().add(meta);
        ChatX.getProxy().getCommandManager().register(meta, new DirectMessageCommand());
    }
}
