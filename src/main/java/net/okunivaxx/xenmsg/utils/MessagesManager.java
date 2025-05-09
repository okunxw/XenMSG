package net.okunivaxx.xenmsg.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessagesManager {
    private final Plugin plugin;
    private FileConfiguration config;
    private final File messagesFile;

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public MessagesManager(Plugin plugin) {
        this.plugin = plugin;
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            plugin.getLogger().warning("[XenMSG] 📄 messages.yml не найден! Загружаем из .jar...");
            loadFromJar();
        } else {
            config = YamlConfiguration.loadConfiguration(messagesFile);
            if (config.getKeys(false).isEmpty()) {
                plugin.getLogger().warning("[XenMSG] ⚠ messages.yml пуст! Используем дефолтные значения из .jar...");
                config = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("messages.yml")));
            }
        }
    }

    private void loadFromJar() {
        config = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("messages.yml")));
        saveMessages();
    }

    private void saveMessages() {
        if (!messagesFile.exists()) { // 🔥 Теперь файл НЕ перезаписывается, если он уже существует
            try {
                config.save(messagesFile);
                plugin.getLogger().info("[XenMSG] ✅ messages.yml загружен из .jar.");
            } catch (IOException e) {
                plugin.getLogger().warning("[XenMSG] ⚠ Ошибка при сохранении messages.yml!");
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(CommandSender sender, String key, Object... replacements) {
        List<String> messages = config.getStringList("messages." + key);

        if (!messages.isEmpty()) {
            messages.forEach(line -> sender.sendMessage(formatMessage(replacePlaceholders(line, replacements))));
        } else {
            sender.sendMessage(formatMessage(replacePlaceholders(config.getString("messages." + key, "§cОшибка: сообщение не найдено!"), replacements)));
        }
    }

    public String formatMessage(String message) {
        if (message == null) return "§cОшибка: пустое сообщение!";
        return applyHexColors(ChatColor.translateAlternateColorCodes('&', message));
    }

    public List<String> getMessageList(String key) {
        return config.getStringList("messages." + key).stream().map(this::formatMessage).toList();
    }

    public void reloadMessages() {
        if (messagesFile.exists()) {
            config = YamlConfiguration.loadConfiguration(messagesFile);
        } else {
            loadFromJar();
        }
    }

    private String applyHexColors(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            matcher.appendReplacement(result, ChatColor.of("#" + hexCode).toString());
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private String replacePlaceholders(String message, Object... replacements) {
        if (message == null) return "§cОшибка: пустое сообщение!";

        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(String.valueOf(replacements[i]), String.valueOf(replacements[i + 1]));
        }

        return message;
    }
}
