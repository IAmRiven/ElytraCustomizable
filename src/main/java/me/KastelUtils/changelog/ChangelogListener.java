package me.KastelUtils.changelog;

import me.KastelUtils.KastelUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChangelogListener implements Listener {

    private final ChangelogManager manager;
    private final KastelUtils plugin;
    private final Map<UUID, Long> lastPlayedBeforeLogin = new ConcurrentHashMap<>();

    public ChangelogListener(KastelUtils plugin, ChangelogManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        lastPlayedBeforeLogin.put(event.getPlayer().getUniqueId(), event.getPlayer().getLastPlayed());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Long storedLastPlayed = lastPlayedBeforeLogin.remove(event.getPlayer().getUniqueId());
        long lastPlayed = storedLastPlayed == null ? 0L : storedLastPlayed;
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> manager.openLatestIfNeeded(event.getPlayer(), lastPlayed), 20L);
    }

    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent event) {
        manager.saveEditedBook(event.getPreviousBookMeta(), event.getNewBookMeta(), event.isSigning());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (manager.getVersion(item) != null) {
            manager.saveFromBook(item);
        }
    }
}