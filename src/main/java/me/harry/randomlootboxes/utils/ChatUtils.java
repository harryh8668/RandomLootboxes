package me.harry.randomlootboxes.utils;

import me.harry.randomlootboxes.RandomLootboxes;
import org.bukkit.ChatColor;

public class ChatUtils {

    private RandomLootboxes plugin;

    public ChatUtils(RandomLootboxes plugin) {
        this.plugin = plugin;
    }

    public static String colorCodes(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String prefix(RandomLootboxes plugin) {
        return plugin.getConfig().getString("messages.prefix");
    }
}
