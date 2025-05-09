package net.okunivaxx.xenmsg.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MessageControlManager {
    private final Set<UUID> playerToggles = new HashSet<>(); // Игроки, отключившие свои сообщения
    private final Set<UUID> adminToggles = new HashSet<>(); // Игроки, которым админ отключил сообщения

    public boolean isMessagingDisabled(UUID playerUUID) {
        return playerToggles.contains(playerUUID) || adminToggles.contains(playerUUID);
    }

    public void togglePlayerMessaging(UUID playerUUID) {
        if (playerToggles.contains(playerUUID)) {
            playerToggles.remove(playerUUID);
        } else {
            playerToggles.add(playerUUID);
        }
    }

    public void toggleAdminMessaging(UUID playerUUID, boolean enable) {
        if (enable) {
            adminToggles.remove(playerUUID);
        } else {
            adminToggles.add(playerUUID);
        }
    }
}