package me.elytracustomizable.listener;

import io.papermc.paper.event.player.PlayerItemCooldownEvent;
import me.elytracustomizable.ElytraCustomizablePlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class ElytraListener implements Listener {

    private final ElytraCustomizablePlugin plugin;

    public ElytraListener(ElytraCustomizablePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerUseFirework(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND && event.getHand() != EquipmentSlot.OFF_HAND) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.FIREWORK_ROCKET) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.isGliding()) {
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerItemCooldown(PlayerItemCooldownEvent event) {
        if (event.getType() != Material.FIREWORK_ROCKET) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.isGliding()) {
            return;
        }

        int cooldownTicks = Math.max(0, plugin.getConfig().getInt("firework-cooldown-ticks", 15));
        event.setCooldown(cooldownTicks);
    }
}