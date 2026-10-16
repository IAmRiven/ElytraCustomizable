package me.KastelUtils.message;

import me.KastelUtils.KastelUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public final class MessageManager {

    private final KastelUtils plugin;
    private final File file;
    private YamlConfiguration messages;

    public MessageManager(KastelUtils plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "messages.yml");
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public String get(String path, Map<String, String> placeholders) {
        String message = messages.getString(path, "");
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String get(String path) {
        return get(path, Map.of());
    }

    public Component component(String path, Map<String, String> placeholders) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(getRaw(path, placeholders));
    }

    private String getRaw(String path, Map<String, String> placeholders) {
        String message = messages.getString(path, "");
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }

    public void save() {
        try {
            messages.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("No se pudo guardar messages.yml: " + exception.getMessage());
        }
    }
}