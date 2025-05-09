package net.okunivaxx.xenmsg.utils;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class PermissionManager {
    private final LuckPerms luckPerms;

    public PermissionManager(Plugin plugin) {
        RegisteredServiceProvider<LuckPerms> provider = plugin.getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
            plugin.getLogger().info("✅ LuckPerms подключен!");
        } else {
            luckPerms = null;
            plugin.getLogger().warning("⚠ LuckPerms не найден, используем стандартные права.");
        }
    }

    public boolean hasPermission(Player player, String permission) {
        if (luckPerms != null) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                return user.getCachedData().getPermissionData(QueryOptions.defaultContextualOptions()).checkPermission(permission).asBoolean();
            }
        }
        return player.hasPermission(permission);
    }
}


