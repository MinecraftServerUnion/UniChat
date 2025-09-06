package dev.onelili.unichat.velocity.util;

import dev.onelili.unichat.velocity.UniChat;

public class Logger {
    public static void info(String message){
        UniChat.getLogger().info(message);
    }
    public static void warn(String message){
        UniChat.getLogger().warn(message);
    }
    public static void error(String message) {
        UniChat.getLogger().error(message);
    }
}
