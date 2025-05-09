package net.okunivaxx.xenmsg.commands;

import net.okunivaxx.xenmsg.storage.MailboxManager;
import net.okunivaxx.xenmsg.utils.MessagesManager;
import net.okunivaxx.xenmsg.utils.PermissionManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Pattern;

public class MailboxCommand implements CommandExecutor {
    private final MailboxManager mailboxManager;
    private final MessagesManager messagesManager;
    private final PermissionManager permissionManager;
    private static final Pattern MESSAGE_SPLIT_PATTERN = Pattern.compile("^(.+?):\\s*(.+)$");

    public MailboxCommand(MailboxManager mailboxManager, MessagesManager messagesManager, PermissionManager permissionManager) {
        this.mailboxManager = mailboxManager;
        this.messagesManager = messagesManager;
        this.permissionManager = permissionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messagesManager.sendMessage(sender, "player-only");
            return false;
        }

        // 🔥 Проверяем права на `/mailbox`
        if (!permissionManager.hasPermission(player, "xenmsg.mailbox")) {
            messagesManager.sendMessage(player, "no-permission");
            return false;
        }

        List<String> messages = mailboxManager.getMessages(player.getUniqueId());

        if (messages.isEmpty()) {
            messagesManager.sendMessage(player, "mailbox-empty");
            return false;
        }

        List<String> mailboxMessages = messagesManager.getMessageList("mailbox-messages");

        if (mailboxMessages.isEmpty()) {
            messagesManager.sendMessage(player, "mailbox-error");
            return false;
        }

        // 🔥 Отображаем каждое сообщение в `mailbox-messages`
        messages.forEach(msg -> {
            var matcher = MESSAGE_SPLIT_PATTERN.matcher(msg);
            String senderName = matcher.matches() ? matcher.group(1) : "Неизвестный";
            String messageText = matcher.matches() ? matcher.group(2) : msg;

            mailboxMessages.forEach(line ->
                    player.sendMessage(messagesManager.formatMessage(
                            line.replace("%sender%", senderName).replace("%message%", messageText)
                    ))
            );
        });

        // 🔥 Очистка сообщений
        mailboxManager.clearMessages(player.getUniqueId());
        messagesManager.sendMessage(player, "mailbox-cleared");

        return true;
    }
}