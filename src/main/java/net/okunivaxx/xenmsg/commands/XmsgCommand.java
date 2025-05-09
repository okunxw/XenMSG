package net.okunivaxx.xenmsg.commands;

import net.okunivaxx.xenmsg.storage.MailboxManager;
import net.okunivaxx.xenmsg.utils.MessagesManager;
import net.okunivaxx.xenmsg.utils.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class XmsgCommand implements CommandExecutor {
    private final MailboxManager mailboxManager;
    private final MessagesManager messagesManager;
    private final PermissionManager permissionManager;
    private final NamespacedKey xmsgToggleKey;

    public XmsgCommand(MailboxManager mailboxManager, MessagesManager messagesManager, PermissionManager permissionManager, Plugin plugin) {
        this.mailboxManager = mailboxManager;
        this.messagesManager = messagesManager;
        this.permissionManager = permissionManager;
        this.xmsgToggleKey = new NamespacedKey(plugin, "xmsg-toggle");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messagesManager.sendMessage(sender, "player-only");
            return false;
        }

        if (!permissionManager.hasPermission(player, "xenmsg.use")) {
            messagesManager.sendMessage(player, "no-permission");
            return false;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off")) {
                boolean enabled = args[0].equalsIgnoreCase("on");
                player.getPersistentDataContainer().set(xmsgToggleKey, PersistentDataType.BYTE, enabled ? (byte) 1 : (byte) 0);
                messagesManager.sendMessage(player, enabled ? "xmsg-enabled" : "xmsg-disabled");
                return true;
            }

            if (args[0].equalsIgnoreCase("help")) {
                messagesManager.sendMessage(player, "xmsg-help");
                return true;
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("toggle")) {
            if (!permissionManager.hasPermission(player, "xmsg.toggle")) {
                messagesManager.sendMessage(player, "no-permission");
                return false;
            }

            Player target = Bukkit.getPlayer(args[1]);
            boolean enable = args[2].equalsIgnoreCase("on");

            if (target == null) {
                messagesManager.sendMessage(player, "toggle-offline-error", "%target%", args[1]); // 🔥 Теперь вместо офлайн-сохранения выдаётся предупреждение!
                return false;
            }

            target.getPersistentDataContainer().set(xmsgToggleKey, PersistentDataType.BYTE, enable ? (byte) 1 : (byte) 0);
            messagesManager.sendMessage(player, enable ? "xmsg-enabled-admin" : "xmsg-disabled-admin", "%target%", target.getName());
            messagesManager.sendMessage(target, enable ? "xmsg-enabled-by-admin" : "xmsg-disabled-by-admin");

            return true;
        }

        if (args.length < 3 || !args[0].equalsIgnoreCase("send")) {
            messagesManager.sendMessage(sender, "usage-xmsg");
            return false;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        if (player.getName().equalsIgnoreCase(targetName)) {
            List<String> errorMessages = messagesManager.getMessageList("self-message-error");
            errorMessages.forEach(player::sendMessage);
            return false;
        }

        if (target != null) {
            boolean messagesEnabled = target.getPersistentDataContainer().getOrDefault(xmsgToggleKey, PersistentDataType.BYTE, (byte) 1) == 1;

            if (!messagesEnabled && !permissionManager.hasPermission(player, "xmsg.bypass")) {
                messagesManager.sendMessage(sender, "xmsg-blocked", "%target%", target.getName());
                return false;
            }

            List<String> formattedMessages = messagesManager.getMessageList("xmsg-format");

            formattedMessages.forEach(line -> {
                String formattedMessage = messagesManager.formatMessage(line.replace("%sender%", player.getName()).replace("%message%", message));
                target.sendMessage(formattedMessage);
            });

            messagesManager.sendMessage(sender, "xmsg-sent", "%target%", target.getName());
        } else {
            messagesManager.sendMessage(sender, "player-offline", "%target%", targetName); // 🔥 Теперь при офлайн-игроке сообщение НЕ сохраняется в mailbox.yml
        }

        return true;
    }
}