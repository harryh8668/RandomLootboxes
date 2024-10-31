package me.harry.randomlootboxes.commands;

import me.harry.randomlootboxes.RandomLootboxes;
import me.harry.randomlootboxes.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.security.Key;
import java.util.Objects;

public class AdminCommand implements CommandExecutor {

    private final RandomLootboxes plugin; 
    
    public AdminCommand(RandomLootboxes plugin) {
        this.plugin = plugin;
    }
    
    public String prefix() {
        return plugin.getConfig().getString("messages.prefix");
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            if (player.hasPermission("randomlootboxes.admin")) {

                if (args[0].equals("create")) {
                    String lootboxName = args[1];
                    System.out.println("Lootbox Name: " + lootboxName);

                    ItemStack heldItem = player.getInventory().getItemInMainHand();
                    System.out.println("Held Item: " + heldItem);

                    if (heldItem == null || heldItem.getType() == Material.AIR) {
                        player.sendMessage(ChatUtils.colorCodes(prefix() + "You must hold an item to save as a lootbox."));
                        return true;
                    }

                    System.out.println("About to save");
                    FileConfiguration config = this.plugin.getConfig();
                    config.set("lootbox." + lootboxName + ".item", heldItem);
                    plugin.saveConfig();

                    System.out.println("Saved");
                    String nameReplace = config.getString("messages.addedLootbox").replace("%lootbox%", lootboxName);
                    player.sendMessage(ChatUtils.colorCodes(prefix() + nameReplace));

                } else if (args[0].equals("remove")) {
                    String lootboxName = args[1];
                    FileConfiguration config = plugin.getConfig();
                    ConfigurationSection configKeys = config.getConfigurationSection("lootbox");

                    if (configKeys != null && configKeys.contains(lootboxName)) {
                        String lootboxPath = "lootbox." + lootboxName;
                        config.set(lootboxPath, null);
                        plugin.saveConfig();

                        String nameReplace = config.getString("messages.removedLootbox").replace("%lootbox%", lootboxName);
                        player.sendMessage(ChatUtils.colorCodes(prefix() + nameReplace));
                    } else {
                        player.sendMessage(ChatUtils.colorCodes(prefix() + "&cLootbox not found: " + lootboxName));
                    }

                } else if (args[0].equals("list")) {
                    ConfigurationSection boxList = plugin.getConfig().getConfigurationSection("lootbox");
                    player.sendMessage(ChatUtils.colorCodes("&b&lLOOTBOXES LIST"));
                    for (String key : boxList.getKeys(false)) {
                        player.sendMessage(ChatUtils.colorCodes("&f- " + key + " Lootbox"));
                    }

                } else if (args[0].equals("addreward")) {
                    String lootboxName = args[1];
                    FileConfiguration config = plugin.getConfig();
                    ConfigurationSection configKeys = config.getConfigurationSection("lootbox");

                    if (configKeys != null && configKeys.contains(lootboxName)) {
                        ItemStack heldItem = player.getInventory().getItemInMainHand();
                        System.out.println("Held Item: " + heldItem);

                        if (heldItem == null || heldItem.getType() == Material.AIR) {
                            player.sendMessage(ChatUtils.colorCodes(prefix() + "You must hold an item to save as a lootbox reward."));
                            return true;
                        }

                        ItemMeta meta = heldItem.getItemMeta();
                        String display = meta.getDisplayName();

                        int id = Integer.parseInt(args[2]);
                        config.set("lootbox." + lootboxName + ".reward." + id + ".name", display);
                        config.set("lootbox." + lootboxName + ".reward." + id + ".chance", args[3]);
                        config.set("lootbox." + lootboxName + ".reward." + id + ".item", heldItem);
                        plugin.saveConfig();
                    }
                }
            }
        }
        return true;
    }

}
