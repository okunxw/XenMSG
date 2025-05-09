package net.okunivaxx.xenmsg.listeners;

import net.okunivaxx.xenmsg.storage.MailboxManager;
import net.okunivaxx.xenmsg.utils.MessagesManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class PlayerJoinListener implements Listener {
    private final MailboxManager mailboxManager;
    private final MessagesManager messagesManager;

    public PlayerJoinListener(MailboxManager mailboxManager, MessagesManager messagesManager) {
        this.mailboxManager = mailboxManager;
        this.messagesManager = messagesManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        List<String> messages = mailboxManager.getMessages(playerUUID);
        if (!messages.isEmpty()) {
            messagesManager.sendMessage(player, "mailbox-notification", "%count%", String.valueOf(messages.size()));
        }
    }
}