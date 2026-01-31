package cn.jason31416.chatx.channel;

import cn.jason31416.chatx.channel.type.RedisChannelHandler;
import cn.jason31416.chatx.util.Config;
import cn.jason31416.chatx.util.Logger;
import cn.jason31416.chatx.util.MapTree;
import cn.jason31416.chatx.util.SimplePlayer;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.proxy.Player;
import cn.jason31416.chatx.ChatX;
import cn.jason31416.chatx.channel.type.*;
import cn.jason31416.chatx.channel.type.serverwide.GlobalChannelHandler;
import cn.jason31416.chatx.channel.type.serverwide.LocalChannelHandler;
import cn.jason31416.chatx.channel.type.serverwide.RoomChannelHandler;
import cn.jason31416.chatx.util.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@SuppressWarnings("unchecked")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class Channel {
    @Getter
    private static Set<CommandMeta> registeredChannelCommands = new HashSet<>();

    public static Map<String, Channel> channelPrefixes = new ConcurrentHashMap<>();

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
        registerChannelType("local", LocalChannelHandler::new);
        registerChannelType("global", GlobalChannelHandler::new);
        registerChannelType("redis", RedisChannelHandler::new);
        registerChannelType("room", RoomChannelHandler::new);
    }

    private String id;
    private String displayName;
    private boolean passthrough;
    @Builder.Default
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
    @Builder.Default
    private int rateLimitTime = 0;
    @Builder.Default
    private int rateLimitCount = 0;
    @Builder.Default
    private RateLimiter rateLimiter=null;

    public MapTree getChannelConfig() {
        return Config.getChannelTree().getSection(id);
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

    public static void loadChannels(){
        defaultChannel = null;
        channelPrefixes.clear();
        for(String i: Config.getChannelTree().getKeys()){
            try {
                Channel channel = Channel.builder()
                        .id(i)
                        .displayName(Config.getChannelTree().getString(i + ".name"))
                        .logToConsole(Config.getChannelTree().getBoolean(i + ".log-console", true))
                        .receivePermission(Config.getChannelTree().getString(i + ".receive-permission", null))
                        .sendPermission(Config.getChannelTree().getString(i + ".send-permission", null))
                        .passthrough(Config.getChannelTree().getBoolean(i + ".passthrough", false))
                        .rateLimitTime(Config.getChannelTree().getInt(i + ".rate-limit.time", 0))
                        .rateLimitCount(Config.getChannelTree().getInt(i + ".rate-limit.count", 0))
                        .build();
                if(channel.rateLimitTime!=0)
                    channel.rateLimiter = new RateLimiter(channel.rateLimitTime*1000L, channel.rateLimitCount);
                channel.handler = channelTypes.get(Config.getChannelTree().getString(i + ".type").toLowerCase(Locale.ROOT)).apply(channel);
                if(Config.getChannelTree().contains(i + ".restricted-servers")) {
                    channel.restrictedServers = (List<String>) Config.getChannelTree().get(i + ".restricted-servers");
                }
                channels.put(i.toLowerCase(Locale.ROOT), channel);
                for(String prefix: Config.getChannelTree().getStringList(i + ".prefixes")){
                    channelPrefixes.put(prefix.toLowerCase(Locale.ROOT), channel);
                }
                List<String> commands = (List<String>) Config.getChannelTree().get(i + ".commands");
                if(!commands.isEmpty()){
                    CommandMeta meta = ChatX.getProxy().getCommandManager()
                            .metaBuilder(commands.get(0))
                                    .aliases(commands.subList(1, commands.size()).toArray(String[]::new))
                                    .build();
                    registeredChannelCommands.add(meta);
                    ChatX.getProxy().getCommandManager().register(meta, channel.handler.getCommand(channel));
                }
                if(defaultChannel == null) defaultChannel = channel;
            } catch (Exception e) {
                Logger.error("Failed to load channel " + i);
                // noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
    }
    public static void handleChat(Player player, Channel channel, String message){
        channel.getHandler().handle(new SimplePlayer(player), message);
    }
}
