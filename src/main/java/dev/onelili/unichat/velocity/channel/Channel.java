package dev.onelili.unichat.velocity.channel;

import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.onelili.unichat.velocity.UniChat;
import dev.onelili.unichat.velocity.channel.type.*;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.util.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class Channel {
    private static Set<CommandMeta> registeredChannelCommands = new HashSet<>();

    public static Channel defaultChannel = null;
    @Getter
    private static Map<UUID, Channel> playerChannels = new ConcurrentHashMap<>();

    @Getter
    private static Map<String, Channel> channels = new ConcurrentHashMap<>();

    @Getter
    private static Map<String, Function<Channel, ChannelHandler>> channelTypes = new ConcurrentHashMap<>();

    public static void registerChannelType(@Nonnull String id, @Nullable Function<Channel, ChannelHandler> handler) {
        channelTypes.put(id.toLowerCase(Locale.ROOT), handler);
    }

    static {
        registerChannelType("local", channel -> null);
        registerChannelType("global", GlobalChannelHandler::new);
        registerChannelType("redis", RedisChannelHandler::new);
    }

    private String id;
    private String displayName;
    @Builder.Default
    @Nullable
    private ChannelHandler handler = null;
    @Builder.Default
    private boolean logToConsole = true;
    @Builder.Default
    private List<String> restrictedServers = new ArrayList<>();
    @Builder.Default
    @Nullable
    private String sendPermission = null;
    @Builder.Default
    @Nullable
    private String receivePermission = null;

    public MapTree getChannelConfig() {
        return Config.getSection("channels." + id);
    }
    @Nullable
    public static Channel getPlayerChannel(SimplePlayer player) {
        return playerChannels.get(player.getPlayerUUID());
    }
    @Nullable
    public static Channel getChannel(String id) {
        return channels.get(id.toLowerCase(Locale.ROOT));
    }
    @Nullable
    public static Channel getPlayerChannel(Player player){
        return playerChannels.get(player.getUniqueId());
    }
    public Collection<Player> getRecipients(Player sender){
        if(handler == null){
            if(sender.getCurrentServer().isPresent())
                return sender.getCurrentServer().get().getServer().getPlayersConnected();
            else
                return new ArrayList<>();
        }
        return handler.recipients(new SimplePlayer(sender));
    }

    public static void loadChannels(){
        defaultChannel = null;
        for(String i: Config.getSection("channels").getKeys()){
            try {
                Channel channel = Channel.builder()
                        .id(i)
                        .displayName(Config.getString("channels." + i + ".name"))
                        .logToConsole(Config.getConfigTree().getBoolean("channels." + i + ".log-console", true))
                        .receivePermission(Config.getConfigTree().getString("channels." + i + ".receive-permission", null))
                        .sendPermission(Config.getConfigTree().getString("channels." + i + ".send-permission", null))
                        .build();
                channel.handler = channelTypes.get(Config.getString("channels." + i + ".type").toLowerCase(Locale.ROOT)).apply(channel);
                if(Config.contains("channels." + i + ".restricted-servers")) {
                    channel.restrictedServers = (List<String>) Config.getItem("channels." + i + ".restricted-servers");
                }
                channels.put(i.toLowerCase(Locale.ROOT), channel);
                List<String> commands = (List<String>) Config.getItem("channels." + i + ".commands");
                CommandMeta meta = UniChat.getProxy().getCommandManager()
                        .metaBuilder(commands.getFirst())
                                .aliases(commands.subList(1, commands.size()).stream().toArray(String[]::new))
                                .build();
                UniChat.getProxy().getCommandManager().register(meta, (SimpleCommand) invocation -> {
                    if(!(invocation.source() instanceof Player pl)){
                        invocation.source().sendMessage(Message.getMessage("command.cannot-execute-from-console").toComponent());
                        return;
                    }
                    if(invocation.arguments().length == 0) {
                        pl.sendMessage(Message.getMessage("command.joined-channel").add("channel", channel.getDisplayName()).toComponent());
                        playerChannels.put(pl.getUniqueId(), channel);
                    }else{
                        handleChat(pl, channel, String.join(" ", invocation.arguments()));
                    }
                });
                if(defaultChannel == null) defaultChannel = channel;
            }catch (Exception e) {
                Logger.error("Failed to load channel " + i);
                e.printStackTrace();
            }
        }
    }
    public static void handleChat(Player player, Channel channel, String message){
        if(channel.getHandler() == null) return;
        channel.getHandler().handle(new SimplePlayer(player), message);
    }
}
