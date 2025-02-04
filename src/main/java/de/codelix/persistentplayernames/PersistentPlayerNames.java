package de.codelix.persistentplayernames;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class PersistentPlayerNames extends JavaPlugin implements Listener {
    private HikariDataSource ds;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        ConfigurationSection db = getConfig().getConfigurationSection("mysql");
        if (db == null) {
            throw new IllegalStateException("Failed to load config");
        }

        String host = db.getString("host");
        String port = db.getString("port");
        String user = db.getString("user");
        String password = db.getString("password");
        String database = db.getString("database");
        if (host == null) {
            throw new IllegalStateException("Failed to load config: Host missing in db section");
        }
        if (port == null) {
            throw new IllegalStateException("Failed to load config: Host missing in db section");
        }
        if (user == null) {
            throw new IllegalStateException("Failed to load config: Host missing in db section");
        }
        if (password == null) {
            throw new IllegalStateException("Failed to load config: Host missing in db section");
        }
        if (database == null) {
            throw new IllegalStateException("Failed to load config: Host missing in db section");
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(user);
        config.setPassword(password);
        this.ds = new HikariDataSource(config);

        try {
            this.createTable();
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to Database", e);
        }

        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        this.ds.close();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();
        try {
            this.storePlayerName(uuid, playerName);
        } catch (SQLException e) {
            throw new RuntimeException("Could not store player name in Database", e);
        }
    }

    private void storePlayerName(UUID uuid, String name) throws SQLException {
        try(PreparedStatement stmt = this.ds.getConnection().prepareStatement("""
            INSERT INTO player_names (uuid, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name=?
        """)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, name);
            stmt.setString(3, name);
            stmt.execute();
        }
    }

    private void createTable() throws SQLException {
        try(PreparedStatement stmt = this.ds.getConnection().prepareStatement("""
            CREATE TABLE IF NOT EXISTS player_names (
                uuid VARCHAR(36) NOT NULL PRIMARY KEY,
                name VARCHAR(16) NOT NULL UNIQUE
            );
        """)) {
            stmt.execute();
        }
    }
}
