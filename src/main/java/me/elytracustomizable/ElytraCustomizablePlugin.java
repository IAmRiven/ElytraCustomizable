package me.elytracustomizable;

import me.elytracustomizable.command.ElytraCustomizableCommand;
import me.elytracustomizable.listener.ElytraListener;
import me.elytracustomizable.task.GlideParticleTask;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ElytraCustomizablePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Bukkit.getPluginManager().registerEvents(new ElytraListener(this), this);
        new GlideParticleTask(this).runTaskTimer(this, 1L, 2L);

        ElytraCustomizableCommand commandExecutor = new ElytraCustomizableCommand(this);
        PluginCommand pluginCommand = getCommand("elytracustomizable");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(commandExecutor);
            pluginCommand.setTabCompleter(commandExecutor);
        } else {
            getLogger().warning("No se pudo registrar el comando /elytracustomizable");
        }

        getLogger().info("ElytraCustomizable activado correctamente.");
    }
}