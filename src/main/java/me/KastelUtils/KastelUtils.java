package me.KastelUtils;

import me.KastelUtils.command.KastelUtilsCommand;
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
        new GlideParticleTask(this).runTaskTimer(this, 1L, 2L);

        KastelUtilsCommand commandExecutor = new KastelUtilsCommand(this);
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