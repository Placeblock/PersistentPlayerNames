package de.codelix.persistentplayernames;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class PersistentPlayerNames extends JavaPlugin implements Listener {
    private Connection con;

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

        try {
            this.con = DriverManager.getConnection("jdbc:mysql://localhost:3306/myDb", "user1", "pass");
            this.createTable();
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to Database", e);
        }
    }

    @Override
    public void onDisable() {
        if (this.con == null) return;
        try {
            this.con.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close connection to Database", e);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (this.con == null) return;
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
        PreparedStatement stmt = this.con.prepareStatement("""
            INSERT INTO player_names (uuid, name) VALUES (?, ?)
        """);
        stmt.setString(1, uuid.toString());
        stmt.setString(2, name);
    }

    private void createTable() throws SQLException {
        this.con.prepareStatement("""
            CREATE TABLE player_names (
                uuid VARCHAR(36) NOT NULL PRIMARY KEY,
                name VARCHAR(16) NOT NULL UNIQUE
            ); 
        """).execute();
    }

}
