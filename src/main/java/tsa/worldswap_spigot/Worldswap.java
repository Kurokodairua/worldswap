package tsa.worldswap_spigot;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public final class Worldswap extends JavaPlugin {

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

            World newWorld = Bukkit.getWorld(newWorldName);

            if (newWorldName != null && newWorld != null) {

                //hole Zeit aus alter Welt
                long time = player.getWorld().getGameTime();

                // location in alter Welt holen
                Location oldLocation = player.getLocation();
                oldLocation.setWorld(newWorld);

                // Konsole nutzt mvtp auf Spieler, sodass der keine Permission braucht
                getServer().dispatchCommand(getServer().getConsoleSender(), "mvtp "+ player.getName() + " " + newWorldName);

                // teleport an gleiche Stelle wie vorher
                player.teleport(oldLocation);

                ensureO2(oldLocation, newWorld, player);

                //setzt Zeit sodass Beleuchtung gleich ist
                getServer().dispatchCommand(getServer().getConsoleSender(), "time set "+ time + " " + newWorldName);

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

    private void ensureO2(Location oldLocation, World newWorld, Player player) {
        int blockX = oldLocation.getBlockX();
        int blockY = oldLocation.getBlockY();
        int blockZ = oldLocation.getBlockZ();

        Block block = newWorld.getBlockAt(blockX, blockY, blockZ);
        if (block.getType() != Material.AIR) {
            // Find the next highest air block
            while (block.getType() != Material.AIR) {
                block = block.getRelative(0, 1, 0);
            }
            // Teleport player to the next highest air block
            Location newLocation = block.getLocation().add(0.5, 1, 0.5);
            player.teleport(newLocation);
            getLogger().log(Level.FINEST, "Teleported player to next highest air block.");
        }
    }
}
