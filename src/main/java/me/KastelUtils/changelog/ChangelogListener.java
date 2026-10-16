package me.KastelUtils.changelog;

import me.KastelUtils.KastelUtils;
import me.KastelUtils.message.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class ChangelogListener implements Listener {

    private final ChangelogManager manager;
    private final KastelUtils plugin;
    private final MessageManager messages;

    public ChangelogListener(KastelUtils plugin, ChangelogManager manager, MessageManager messages) {
        this.plugin = plugin;
        this.manager = manager;
        this.messages = messages;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!manager.hasLatestChangelog()) {
            return;
        }

        Component message = messages.component("changelog.join.prefix", Map.of())
                .append(messages.component("changelog.join.click", Map.of())
                        .clickEvent(ClickEvent.runCommand("/kastelutils changelog open"))
                        .hoverEvent(HoverEvent.showText(messages.component("changelog.join.hover", Map.of()))))
                .append(messages.component("changelog.join.suffix", Map.of()));
        player.sendMessage(message);
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