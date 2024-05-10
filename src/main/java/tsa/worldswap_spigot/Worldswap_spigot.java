package tsa.worldswap_spigot;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public final class Worldswap_spigot extends JavaPlugin {

    private FileConfiguration config;

    @Override
    public void onEnable() {

        // Spielerdaten-Datei initialisieren
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File configFile = new File(getDataFolder(), "config.yml");

        // wenn config file noch nicht existiert, erstellen wir es
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                config = getConfig();
                config.options().copyDefaults(true);
                config.addDefault("worlds.world1", "world1");
                config.addDefault("worlds.world2", "world2");
                saveConfig();
                getLogger().info("Config file not found. Creating default config.");

            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Fehler beim Erstellen der config-Datei.", e);
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("swap")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be run by a player.");
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("worldswap.swap")) {
                player.sendMessage("You don't have permission to use this command.");
                return true;
            }

            String currentWorldName = player.getWorld().getName();
            String newWorldName;

            if (currentWorldName.equalsIgnoreCase(config.getString("worlds.world1"))) {
                newWorldName = config.getString("worlds.world2");
            } else if (currentWorldName.equalsIgnoreCase(config.getString("worlds.world2"))) {
                newWorldName = config.getString("worlds.world1");
            } else {
                getLogger().log(Level.WARNING, "Couldn't figure out world name, check for spelling mistakes.");
                return true;
            }

            // schauen ob es die neue Welt gibt
            if (newWorldName != null && Bukkit.getWorld(newWorldName) != null) {

                // location in alter Welt holen
                Location oldLocation = player.getLocation();

                // Konsole nutzt mvtp auf Spieler, sodass der keine Permission braucht
                getServer().dispatchCommand(getServer().getConsoleSender(), "mvtp "+ player.getName() + " " + newWorldName);

                // teleport an gleiche Stelle wie vorher
                player.teleport(oldLocation);

                return true;}
            else {
                getLogger().log(Level.WARNING, "World '" + newWorldName + "' does not exist.");
                return true;
            }
        }
        else  if (command.getName().equalsIgnoreCase("wsreload")) {
            if (!sender.hasPermission("worldswap.wsreload")) {
                sender.sendMessage("You don't have permission to use this command.");
                return true;
            }

            reloadConfig();
            config = getConfig();
            sender.sendMessage("Config reloaded successfully.");
            return true;
        }
        return false;
    }
}