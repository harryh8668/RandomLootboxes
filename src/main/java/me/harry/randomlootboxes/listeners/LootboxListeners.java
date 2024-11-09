package me.harry.randomlootboxes.listeners;

import me.harry.randomlootboxes.RandomLootboxes;
import me.harry.randomlootboxes.commands.AdminCommand;
import me.harry.randomlootboxes.utils.ChatUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LootboxListeners implements Listener {

    private RandomLootboxes plugin;

    public LootboxListeners(RandomLootboxes plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClaim(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        FileConfiguration config = plugin.getConfig();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            ItemStack lootbox = player.getInventory().getItemInMainHand();
            ItemMeta meta = lootbox.getItemMeta();

            if (meta != null) {
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                String lootboxName = pdc.get(new NamespacedKey(plugin, "lootbox"), PersistentDataType.STRING);


                List<ItemStack> items = new ArrayList<>();

                ConfigurationSection rewardsSection = config.getConfigurationSection("lootbox." + lootboxName + ".reward");
                if (rewardsSection != null) {
                    for (String key : rewardsSection.getKeys(false)) {
                        rewardsSection = config.getConfigurationSection("lootbox." + lootboxName + ".reward." + key);
                        if (rewardsSection != null) {
                            ItemStack item = config.getItemStack("lootbox." + lootboxName + ".reward." + key + ".item");
                            if (item != null) {
                                items.add(item);
                            }
                        }
                    }

                    Collections.shuffle(items);

                    if (!items.isEmpty()) {
                        int itemsToGive = Math.min(3, items.size());

                        for (Player loopPlayer : Bukkit.getOnlinePlayers()) {
                            loopPlayer.sendMessage(ChatUtils.colorCodes("&b&l" + player.getName() + " has just opened a " + lootboxName + " lootbox!"));
                        }
                        for (int i = 0; i < itemsToGive; i++) {
                            ItemStack randomizedItem = items.get(i);
                            ItemMeta randomMeta = randomizedItem.getItemMeta();
                            String display = randomMeta.getDisplayName();
                            if (display.isEmpty()) {
                                display = randomizedItem.getType().toString().toLowerCase();
                            }
                            player.getInventory().addItem(randomizedItem);
                            for (Player loopPlayer : Bukkit.getOnlinePlayers()) {
                                loopPlayer.sendMessage(ChatUtils.colorCodes("&f" + player.getName() + " received a " + display + "!"));
                                loopPlayer.playSound(loopPlayer, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                            }
                        }
                    } else {
                        player.sendMessage("No items found in the lootbox rewards.");
                    }
                } else {
                    player.sendMessage("No rewards found for this lootbox.");
                }
            }
            lootbox.setAmount(lootbox.getAmount() - 1 );
        }
    }
}
