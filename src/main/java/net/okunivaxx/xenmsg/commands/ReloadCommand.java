package net.okunivaxx.xenmsg.commands;

import net.okunivaxx.xenmsg.config.ConfigManager;
import net.okunivaxx.xenmsg.utils.MessagesManager;
import net.okunivaxx.xenmsg.utils.PermissionManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.Bukkit.getServer;

public class ReloadCommand implements CommandExecutor {
    private final ConfigManager configManager;
    private final MessagesManager messagesManager;
    private final PermissionManager permissionManager;

    public ReloadCommand(ConfigManager configManager, MessagesManager messagesManager, PermissionManager permissionManager) {
        this.configManager = configManager;
        this.messagesManager = messagesManager;
        this.permissionManager = permissionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 🔥 Проверяем права на `/xenmsg reload`, поддерживая консоль
        boolean hasPermission = sender instanceof Player
                ? permissionManager.hasPermission((Player) sender, "xenmsg.reload")
                : sender.hasPermission("xenmsg.reload");

        if (!hasPermission) {
            messagesManager.sendMessage(sender, "no-permission");
            return false;
        }

        // 🔥 Проверяем правильность аргументов (`reload`)
        if (args.length < 1 || !args[0].equalsIgnoreCase("reload")) {
            messagesManager.sendMessage(sender, "usage-reload");
            return false;
        }

        configManager.reloadConfig();
        messagesManager.reloadMessages();
        messagesManager.sendMessage(sender, "xenmsg-reloaded");

        // 🔥 Логируем в консоль
        getServer().getLogger().info("Конфигурация XenMSG успешно перезагружена!");

        return true;
    }
}