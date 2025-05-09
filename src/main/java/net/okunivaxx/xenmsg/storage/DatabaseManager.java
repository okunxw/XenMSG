package net.okunivaxx.xenmsg.storage;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    private final Plugin plugin;
    private Connection connection;

    public DatabaseManager(Plugin plugin) {
        this.plugin = plugin;
        setupDatabase();
    }

    private void setupDatabase() {
        FileConfiguration config = plugin.getConfig();
        String host = config.getString("mysql.host");
        String database = config.getString("mysql.database");
        String user = config.getString("mysql.user");
        String password = config.getString("mysql.password");

        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + "/" + database, user, password);
            createTables();
            Bukkit.getLogger().info("✅ [XenMSG] MySQL подключен!");
        } catch (SQLException e) {
            Bukkit.getLogger().warning("⚠ [XenMSG] База данных \"MYSQL\" не подключена! Проверьте config.yml.");
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Bukkit.getOnlinePlayers().stream()
                        .filter(player -> player.hasPermission("xenmsg.admin"))
                        .forEach(player -> player.sendMessage("§c⚠ База данных не подключена! Проверьте config.yml."));
            }, 40L);
        }
    }

    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS xenmsg_mailbox (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "target_uuid VARCHAR(36) NOT NULL, " +
                    "sender VARCHAR(16) NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        } catch (SQLException e) {
            Bukkit.getLogger().warning("⚠ [XenMSG] Ошибка при создании таблиц MySQL!");
        }
    }

    public void saveMessage(UUID target, String sender, String message) {
        if (connection == null) return;

        String query = "INSERT INTO xenmsg_mailbox (target_uuid, sender, message) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, target.toString());
            stmt.setString(2, sender);
            stmt.setString(3, message);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("⚠ [XenMSG] Ошибка записи сообщения в MySQL!");
        }
    }

    public List<String> getMessages(UUID target) {
        if (connection == null) return List.of();

        String query = "SELECT sender, message FROM xenmsg_mailbox WHERE target_uuid = ?";
        List<String> messages = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, target.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messages.add(rs.getString("sender") + ": " + rs.getString("message"));
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("⚠ [XenMSG] Ошибка получения сообщений из MySQL!");
        }

        return messages;
    }

    public void clearMessages(UUID target) {
        if (connection == null) return;

        String query = "DELETE FROM xenmsg_mailbox WHERE target_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, target.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("⚠ [XenMSG] Ошибка очистки сообщений в MySQL!");
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                Bukkit.getLogger().info("✅ [XenMSG] Соединение с MySQL закрыто.");
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("⚠ [XenMSG] Ошибка закрытия соединения с MySQL!");
        }
    }
}