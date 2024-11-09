package me.harry.randomlootboxes;

import me.harry.randomlootboxes.commands.AdminCommand;
import me.harry.randomlootboxes.listeners.LootboxListeners;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class RandomLootboxes extends JavaPlugin {

    public void onEnable() {
        getCommand("alootboxes").setExecutor(new AdminCommand(this));
        Bukkit.getPluginManager().registerEvents(new LootboxListeners(this), this);

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getLogger().info("Config file not found, creating...");
            saveDefaultConfig();
        }
    }

    public void onDisable() {

    }
}
