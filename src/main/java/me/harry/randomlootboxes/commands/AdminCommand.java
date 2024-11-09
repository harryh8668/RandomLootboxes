package me.harry.randomlootboxes.commands;

import me.harry.randomlootboxes.RandomLootboxes;
import me.harry.randomlootboxes.utils.ChatUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.lang.reflect.Type;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Integer.TYPE;

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
                        config.set("lootbox." + lootboxName + ".reward." + id + ".item", heldItem);
                        plugin.saveConfig();
                    }

                } else if (args[0].equals("removereward")) {
                    String lootboxName = args[1];
                    FileConfiguration config = plugin.getConfig();
                    ConfigurationSection configKeys = config.getConfigurationSection("lootbox");

                    if (configKeys != null && configKeys.contains(lootboxName)) {

                        if (args.length < 3) {
                            player.sendMessage(ChatUtils.colorCodes(prefix() + "You must specify a reward ID to remove."));
                            return true;
                        }

                        int id = Integer.parseInt(args[2]);
                        String rewardPath = "lootbox." + lootboxName + ".reward." + id;

                        if (config.contains(rewardPath)) {
                            config.set(rewardPath, null);
                            player.sendMessage(ChatUtils.colorCodes(prefix() + "Removed reward with ID " + id + " from lootbox " + lootboxName + "."));
                        } else {
                            player.sendMessage(ChatUtils.colorCodes(prefix() + "No reward found with ID " + id + " for lootbox " + lootboxName + "."));
                        }

                        plugin.saveConfig();
                    } else {
                        player.sendMessage(ChatUtils.colorCodes(prefix() + "Lootbox " + lootboxName + " does not exist."));
                    }

                } else if (args[0].equals("give")) {
                    FileConfiguration config = plugin.getConfig();
                    Player target = Bukkit.getPlayer(args[1]);
                    String lootboxName = args[2];
                    if (target != null) {
                        int amount = Integer.parseInt(args[3]);
                        ItemStack lootbox = new ItemStack(config.getItemStack("lootbox." + lootboxName + ".item"));
                        lootbox.setAmount(amount);
                        ItemMeta meta = lootbox.getItemMeta();
                        PersistentDataContainer pdc = meta.getPersistentDataContainer();
                        pdc.set(new NamespacedKey(plugin, "lootbox"), PersistentDataType.STRING, lootboxName);
                        lootbox.setItemMeta(meta);
                        player.getInventory().addItem(lootbox);

                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Unable to find the player.");
                }
            }
        }
        return true;
    }
}
