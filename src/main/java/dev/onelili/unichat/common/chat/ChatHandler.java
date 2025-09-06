package dev.onelili.unichat.common.chat;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface ChatHandler {
    /**
     * @param message the content player sent
     * @return whether pass through the message
     */
    boolean onChat(@Nonnull String message);
}
