package dev.onelili.unichat.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import dev.onelili.unichat.velocity.channel.Channel;
import dev.onelili.unichat.velocity.handler.ChatHistoryManager;
import dev.onelili.unichat.velocity.message.Message;
import dev.onelili.unichat.velocity.util.Config;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public class ChatHistoryCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0) {
            invocation.source().sendMessage(Message.getMessage("command.chat-history.channel-header").toComponent());
            for(Channel channel: Channel.getChannels().values()){
                invocation.source().sendMessage(Message.getMessage("command.chat-history.channel-item")
                                .add("channel_name", channel.getDisplayName())
                                .add("channel_id", channel.getId())
                        .toComponent());
            }
        } else if (args[0].equals("search")) {
            if(args.length < 4){
                invocation.source().sendMessage(Message.getMessage("command.invalid-arguments").toComponent());
                return;
            }
            String channelId = args[1];
            int page;
            try{
                page = Integer.parseInt(args[2]);
            }catch (NumberFormatException e){
                invocation.source().sendMessage(Message.getMessage("command.invalid-arguments").toComponent());
                return;
            }
            String keyword = String.join(" ", Stream.of(args).skip(3).toList());
            List<ChatHistoryManager.ChatMessage> chatHistory = ChatHistoryManager.searchHistory(keyword, channelId, Config.getInt("chat-history.message-per-page"), Config.getInt("chat-history.message-per-page")*(page-1));
            invocation.source().sendMessage(Message.getMessage("command.chat-history.chat-header").add("channel_name", Channel.getChannels().get(channelId).getDisplayName()).toComponent());
            for(ChatHistoryManager.ChatMessage message: chatHistory) {
                Date time = new Date(message.time());
                invocation.source().sendMessage(Message.getMessage("command.chat-history.chat-item")
                        .add("time", String.format("%02d:%02d:%02d", time.getHours(), time.getMinutes(), time.getSeconds()))
                        .add("date", time.toLocaleString())
                        .add("server", message.server())
                        .add("sender", message.sender())
                        .add("message", message.message())
                        .toComponent());
            }
            invocation.source().sendMessage(Message.getMessage("command.chat-history.chat-search-footer")
                    .add("channel_id", channelId)
                    .add("page_prev", page-1)
                    .add("page_next", page+1)
                    .add("keyword", keyword)
                    .toComponent());
        } else {
            List<ChatHistoryManager.ChatMessage> chatHistory=List.of();
            if(args.length == 1) {
                String channelId = args[0];
                if(!Channel.getChannels().containsKey(channelId)) {
                    invocation.source().sendMessage(Message.getMessage("command.channel-not-found").toComponent());
                    return;
                }
                chatHistory = ChatHistoryManager.listHistoryBefore(System.currentTimeMillis(), Config.getInt("chat-history.message-per-page"), channelId).reversed();
            }else if(args.length == 3){
                String channelId = args[0];
                String operator = args[1];
                String time = args[2];
                if(!Channel.getChannels().containsKey(channelId)) {
                    invocation.source().sendMessage(Message.getMessage("command.channel-not-found").toComponent());
                    return;
                }
                long timestamp;
                try {
                    timestamp = Long.parseLong(time);
                } catch (NumberFormatException e) {
                    invocation.source().sendMessage(Message.getMessage("command.invalid-timestamp").toComponent());
                    return;
                }
                if(operator.equals("before")) {
                    chatHistory = ChatHistoryManager.listHistoryBefore(timestamp, Config.getInt("chat-history.message-per-page"), channelId).reversed();
                }else if(operator.equals("after")) {
                    chatHistory = ChatHistoryManager.listHistoryAfter(timestamp, Config.getInt("chat-history.message-per-page"), channelId);
                }else{
                    invocation.source().sendMessage(Message.getMessage("command.invalid-operator").toComponent());
                    return;
                }
            }else{
                invocation.source().sendMessage(Message.getMessage("command.invalid-arguments").toComponent());
                return;
            }
            if(chatHistory.isEmpty()) {
                invocation.source().sendMessage(Message.getMessage("command.chat-history.empty").toComponent());
                return;
            }
            invocation.source().sendMessage(Message.getMessage("command.chat-history.chat-header").add("channel_id", args[0]).add("channel_name", Channel.getChannels().get(args[0]).getDisplayName()).toComponent());
            for(ChatHistoryManager.ChatMessage message: chatHistory) {
                Date time = new Date(message.time());
                invocation.source().sendMessage(Message.getMessage("command.chat-history.chat-item")
                                .add("time", String.format("%02d:%02d:%02d", time.getHours(), time.getMinutes(), time.getSeconds()))
                                .add("date", time.toLocaleString())
                                .add("server", message.server())
                                .add("sender", message.sender())
                                .add("message", message.message())
                        .toComponent());
            }
            invocation.source().sendMessage(Message.getMessage("command.chat-history.chat-footer")
                            .add("channel_id", args[0])
                            .add("timestamp_prev", chatHistory.get(0).time()-1)
                            .add("timestamp_next", chatHistory.get(chatHistory.size()-1).time()+1)
                    .toComponent());
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return SimpleCommand.super.suggest(invocation);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("unichat.chathistory");
    }
}
