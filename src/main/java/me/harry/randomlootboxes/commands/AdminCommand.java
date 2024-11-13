package me.harry.randomlootboxes.commands;

import me.harry.randomlootboxes.RandomLootboxes;
import me.harry.randomlootboxes.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AdminCommand implements CommandExecutor {

    private final RandomLootboxes plugin;

    public AdminCommand(RandomLootboxes plugin) {
        this.plugin = plugin;
    }

    public String prefix() {
        return plugin.getConfig().getString("messages.prefix");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be executed by players.");
            return true;
        }

        if (!player.hasPermission("randomlootboxes.admin")) {
            player.sendMessage(ChatUtils.colorCodes(prefix() + "&cYou don't have permission to use this command."));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatUtils.colorCodes(prefix() + "&cInvalid command. Use /alootboxes help for assistance."));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatUtils.colorCodes(prefix() + "&cPlease specify a name for the lootbox."));
                    return true;
                }

                String lootboxName = args[1];
                ConfigurationSection boxList = plugin.getConfig().getConfigurationSection("lootbox");
                if (boxList != null && boxList.getKeys(false).contains(lootboxName)) {
                    player.sendMessage(ChatUtils.colorCodes(prefix() + "&cA lootbox with the name '" + lootboxName + "' already exists."));
                    return true;
                }

                ItemStack heldItem = player.getInventory().getItemInMainHand();
                if (heldItem == null || heldItem.getType() == Material.AIR) {
                    player.sendMessage(ChatUtils.colorCodes(prefix() + "&cYou must hold an item to save as a lootbox."));
                    return true;
                }

                FileConfiguration config = plugin.getConfig();
                config.set("lootbox." + lootboxName + ".item", heldItem);
                plugin.saveConfig();

                String nameReplace = Objects.requireNonNull(config.getString("messages.addedLootbox")).replace("%lootbox%", lootboxName);
                player.sendMessage(ChatUtils.colorCodes(prefix() + nameReplace));
            }
            case "replace" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatUtils.colorCodes(prefix() + "&cPlease specify a name for the lootbox."));
                    return true;
                }

                String lootboxName = args[1];
                ItemStack heldItem = player.getInventory().getItemInMainHand();
                if (heldItem == null || heldItem.getType() == Material.AIR) {
                    player.sendMessage(ChatUtils.colorCodes(prefix() + "&cYou must hold an item to save as a lootbox."));
                    return true;
                }

                FileConfiguration config = plugin.getConfig();
                config.set("lootbox." + lootboxName + ".item", heldItem);
                player.sendMessage(ChatUtils.colorCodes(prefix() + " &a Replaced lootbox!"));
                plugin.saveConfig();

                String nameReplace = Objects.requireNonNull(config.getString("messages.replacedLootbox")).replace("%lootbox%", lootboxName);
                player.sendMessage(ChatUtils.colorCodes(prefix() + nameReplace));
            }
            case "remove" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatUtils.colorCodes(prefix() + "&cPlease specify a lootbox name to remove."));
                    return true;
                }

                String lootboxName = args[1];
                FileConfiguration config = plugin.getConfig();
                if (config.contains("lootbox." + lootboxName)) {
                    config.set("lootbox." + lootboxName, null);
                    plugin.saveConfig();

                    String nameReplace = Objects.requireNonNull(config.getString("messages.removedLootbox")).replace("%lootbox%", lootboxName);
                    player.sendMessage(ChatUtils.colorCodes(prefix() + nameReplace));
                } else {
                    player.sendMessage(ChatUtils.colorCodes(prefix() + "&cLootbox not found: " + lootboxName));
                }
            }
            case "list" -> {
                ConfigurationSection boxList = plugin.getConfig().getConfigurationSection("lootbox");
                if (boxList == null || boxList.getKeys(false).isEmpty()) {
                    player.sendMessage(ChatUtils.colorCodes(prefix() + "&cNo lootboxes available."));
                    return true;
                }
                player.sendMessage(ChatUtils.colorCodes("&b&lLOOTBOXES LIST"));
                for (String key : boxList.getKeys(false)) {
                    player.sendMessage(ChatUtils.colorCodes("&f- " + key + " Lootbox"));
                }
            }
            case "addreward" -> {
                if (args.length < 3) {
                    player.sendMessage(ChatUtils.colorCodes(prefix() + "&cUsage: /alootboxes addreward [lootbox name] [itemID]"));
                    return true;
                }

                String lootboxName = args[1];
                if (!plugin.getConfig().contains("lootbox." + lootboxName)) {
                    player.sendMessage(ChatUtils.colorCodes(prefix() + "&cLootbox does not exist: " + lootboxName));
                    return true;
                }

                ItemStack heldItem = player.getInventory().getItemInMainHand();
                if (heldItem == null || heldItem.getType() == Material.AIR) {
                    player.sendMessage(ChatUtils.colorCodes(prefix() + "&cYou must hold an item to save as a reward."));
                    return true;
                }

                int id;
                try {
                    id = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatUtils.colorCodes(prefix() + "&cInvalid reward ID. Please enter a number."));
                    return true;
                }

                FileConfiguration config = plugin.getConfig();
                ItemMeta meta = heldItem.getItemMeta();
                String display = (meta != null && meta.hasDisplayName()) ? meta.getDisplayName() : heldItem.getType().name();
                config.set("lootbox." + lootboxName + ".reward." + id + ".name", display);
                config.set("lootbox." + lootboxName + ".reward." + id + ".item", heldItem);
                plugin.saveConfig();
                player.sendMessage(ChatUtils.colorCodes(prefix() + "&aReward added to lootbox " + lootboxName + "."));
            }
            case "removereward" -> {
                if (args.length < 3) {
                    player.sendMessage(ChatUtils.colorCodes(prefix() + "&cUsage: /alootboxes removereward [lootbox name] [itemID]"));
                    return true;
                }

                String lootboxName = args[1];
                if (!plugin.getConfig().contains("lootbox." + lootboxName)) {
                    player.sendMessage(ChatUtils.colorCodes(prefix() + "&cLootbox does not exist: " + lootboxName));
                    return true;
                }

                int id;
                try {
                    id = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatUtils.colorCodes(prefix() + "&cInvalid reward ID. Please enter a number."));
                    return true;
                }

                String rewardPath = "lootbox." + lootboxName + ".reward." + id;
                if (plugin.getConfig().contains(rewardPath)) {
                    plugin.getConfig().set(rewardPath, null);
                    plugin.saveConfig();
                    player.sendMessage(ChatUtils.colorCodes(prefix() + "&aRemoved reward ID " + id + " from lootbox " + lootboxName + "."));
                } else {
                    player.sendMessage(ChatUtils.colorCodes(prefix() + "&cNo reward found with ID " + id + " for lootbox " + lootboxName + "."));
                }
            }
            case "give" -> {
                if (args.length < 4) {
                    player.sendMessage(ChatUtils.colorCodes(prefix() + "&cUsage: /alootboxes give [player] [lootbox name] [amount]"));
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ChatUtils.colorCodes(prefix() + "&cPlayer not found: " + args[1]));
                    return true;
                }

                String lootboxName = args[2];
                if (!plugin.getConfig().contains("lootbox." + lootboxName)) {
                    player.sendMessage(ChatUtils.colorCodes(prefix()) + lootboxName + "&c is not a lootbox");
                } else {
                    int amount = Integer.parseInt(args[3]);
                    ItemStack lootbox = new ItemStack(plugin.getConfig().getItemStack("lootbox." + lootboxName + ".item"));
                    lootbox.setAmount(amount);
                    ItemMeta meta = lootbox.getItemMeta();
                    PersistentDataContainer pdc = meta.getPersistentDataContainer();
                    pdc.set(new NamespacedKey(plugin, "lootbox"), PersistentDataType.STRING, lootboxName);
                    lootbox.setItemMeta(meta);
                    player.getInventory().addItem(lootbox);
                }
            }
        }
        return true;
    }
}