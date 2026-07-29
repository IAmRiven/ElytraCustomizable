package me.elytracustomizable.command;

import me.elytracustomizable.ElytraCustomizablePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;

public final class ElytraCustomizableCommand implements CommandExecutor, TabCompleter {

    private static final String RELOAD_PERMISSION = "elytracustomizable.reload";

    private final ElytraCustomizablePlugin plugin;

    public ElytraCustomizableCommand(ElytraCustomizablePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Uso: /" + label + " reload");
            return true;
        }

        if (!args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(ChatColor.RED + "Subcomando desconocido. Usa /" + label + " reload");
            return true;
        }

        if (!sender.hasPermission(RELOAD_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return true;
        }

        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "La configuración de ElytraCustomizable ha sido recargada.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && "reload".startsWith(args[0].toLowerCase())) {
            return Collections.singletonList("reload");
        }

        return Collections.emptyList();
    }
}