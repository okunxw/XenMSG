package net.okunivaxx.xenmsg;

import net.okunivaxx.xenmsg.commands.MailboxCommand;
import net.okunivaxx.xenmsg.commands.XmsgCommand;
import net.okunivaxx.xenmsg.commands.ReloadCommand;
import net.okunivaxx.xenmsg.config.ConfigManager;
import net.okunivaxx.xenmsg.listeners.PlayerJoinListener;
import net.okunivaxx.xenmsg.storage.MailboxManager;
import net.okunivaxx.xenmsg.utils.MessagesManager;
import net.okunivaxx.xenmsg.utils.PermissionManager;
import org.bukkit.plugin.java.JavaPlugin;

public class XenMSG extends JavaPlugin {
    private static XenMSG instance;

    private MessagesManager messagesManager;
    private ConfigManager configManager;
    private MailboxManager mailboxManager;
    private PermissionManager permissionManager;

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        saveResource("messages.yml", false);

        saveDefaultConfig();
        reloadConfig();

        this.configManager = new ConfigManager(this);
        this.messagesManager = new MessagesManager(this);
        this.mailboxManager = new MailboxManager(this);
        this.permissionManager = new PermissionManager(this);

        getCommand("xmsg").setExecutor(new XmsgCommand(mailboxManager, messagesManager, permissionManager, this));
        getCommand("mailbox").setExecutor(new MailboxCommand(mailboxManager, messagesManager, permissionManager));
        getCommand("xenmsg").setExecutor(new ReloadCommand(configManager, messagesManager, permissionManager));
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(mailboxManager, messagesManager), this);

        getLogger().info("Плагин успешно включён!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Плагин выключен!");
    }

    public static XenMSG getInstance() {
        return instance;
    }
}