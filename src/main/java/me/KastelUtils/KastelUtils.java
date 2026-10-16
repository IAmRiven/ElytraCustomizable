package me.KastelUtils;

import me.KastelUtils.command.KastelUtilsCommand;
import me.KastelUtils.changelog.ChangelogListener;
import me.KastelUtils.changelog.ChangelogManager;
import me.KastelUtils.message.MessageManager;
import me.KastelUtils.listener.ElytraListener;
import me.KastelUtils.task.GlideParticleTask;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class KastelUtils extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Bukkit.getPluginManager().registerEvents(new ElytraListener(this), this);
        MessageManager messageManager = new MessageManager(this);
        ChangelogManager changelogManager = new ChangelogManager(this);
        Bukkit.getPluginManager().registerEvents(new ChangelogListener(this, changelogManager, messageManager), this);
        new GlideParticleTask(this).runTaskTimer(this, 1L, 2L);

        KastelUtilsCommand commandExecutor = new KastelUtilsCommand(this, changelogManager, messageManager);
        PluginCommand pluginCommand = getCommand("kastelutils");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(commandExecutor);
            pluginCommand.setTabCompleter(commandExecutor);
        } else {
            getLogger().warning("No se pudo registrar el comando /kastelutils");
        }

        getLogger().info("KastelUtils activado correctamente.");
    }
}