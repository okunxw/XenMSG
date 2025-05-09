package net.okunivaxx.xenmsg.storage;

import net.okunivaxx.xenmsg.XenMSG;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class MailboxManager {
    private final YamlConfiguration config;
    private final File file;
    private final String storageMethod;
    private final DatabaseManager databaseManager;

    public MailboxManager(XenMSG plugin) {
        this.storageMethod = plugin.getConfig().getString("storage-method", "YAML");

        if (storageMethod.equalsIgnoreCase("YAML")) {
            this.file = new File(plugin.getDataFolder(), "mailbox.yml");
            this.config = YamlConfiguration.loadConfiguration(file);
            this.databaseManager = null; // БД не используется
        } else {
            this.file = null;
            this.config = null;
            this.databaseManager = new DatabaseManager(plugin); // 🔥 Создаём объект БД вместо `connect()`
        }
    }

    public void saveMessage(UUID target, String sender, String message) {
        if (storageMethod.equalsIgnoreCase("YAML")) {
            List<String> messages = config.getStringList(target.toString());
            messages.add(sender + ": " + message);
            config.set(target.toString(), messages);
            save();
        } else {
            databaseManager.saveMessage(target, sender, message); // 🔥 Вызываем метод БД через объект
        }
    }

    public List<String> getMessages(UUID target) {
        return storageMethod.equalsIgnoreCase("YAML") ? config.getStringList(target.toString()) : databaseManager.getMessages(target);
    }

    public void clearMessages(UUID target) {
        if (storageMethod.equalsIgnoreCase("YAML")) {
            config.set(target.toString(), null);
            save();
        } else {
            databaseManager.clearMessages(target); // 🔥 Вызываем метод через объект, а не статически
        }
    }

    private void save() {
        if (storageMethod.equalsIgnoreCase("YAML") && file != null) {
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}