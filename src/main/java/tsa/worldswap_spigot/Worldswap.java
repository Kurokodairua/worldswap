package tsa.worldswap_spigot;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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

        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File configFile = new File(getDataFolder(), "config.yml");

        // create config file if missing
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
                getLogger().log(Level.SEVERE, "error creating config file :(", e);
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

            if (newWorldName != null && Bukkit.getWorld(newWorldName) != null) {

                //gets time from old world
               long time = player.getWorld().getTime();

                // get location from old world, change world from location to the new one
                Location oldLocation = player.getLocation();
                oldLocation.setWorld(Bukkit.getWorld(newWorldName));

                // console uses mvtp, player doesn't need the permission
                getServer().dispatchCommand(getServer().getConsoleSender(), "mvtp "+ player.getName() + " " + newWorldName);

                // teleports player back to the same position as before, just in the other world
                player.teleport(oldLocation);

                // teleports to first air block above player
               ensureOxygen(player);

                // sets time to match other world, ensures lighting is the same uwu
               getServer().dispatchCommand(getServer().getConsoleSender(), "time set "+ time + " " + newWorldName);

               // changes weather to clear
                getServer().dispatchCommand(getServer().getConsoleSender(), "weather " + newWorldName + " clear");

                return true;
            }
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

    /**
     * teleports to first air block above player
     */
    private void ensureOxygen(Player player) {
      Location loc = player.getLocation();
        int blockX = loc.getBlockX();
        int blockY = loc.getBlockY();
        int blockZ = loc.getBlockZ();

        Block block = loc.getWorld().getBlockAt(blockX, blockY, blockZ);
        if (block != null && block.getType() != Material.AIR) {
            // Find the next highest air block
            while (block.getType() != Material.AIR) {
                block = block.getRelative(0, 1, 0);
            }
            // Teleport player to the next highest air block
            Location newLocation = block.getLocation().add(0.5, 1, 0.5);

            // hard set yaw and pitch (in Germany we call this Blickrichtung, it richts the Blick)
            newLocation.setYaw(loc.getYaw());
            newLocation.setPitch(loc.getPitch());

            player.teleport(newLocation);
            getLogger().log(Level.FINEST, "Teleported player to next highest air block.");
        }
    }
}
