package me.harry.randomlootboxes.utils;

import org.bukkit.ChatColor;

public class ChatUtils {

    public static String colorCodes(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
