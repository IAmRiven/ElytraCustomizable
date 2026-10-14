package me.KastelUtils.command;

import me.KastelUtils.KastelUtils;
import me.KastelUtils.changelog.ChangelogManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class KastelUtilsCommand implements CommandExecutor, TabCompleter {

    private static final String RELOAD_PERMISSION = "kastelutils.reload";
    private static final String CHANGELOG_PERMISSION = "kastelutils.changelog";
    private static final String CHANGELOG_VIEW_PERMISSION = "kastelutils.changelog.view";

    private final KastelUtils plugin;
    private final ChangelogManager changelogManager;

    public KastelUtilsCommand(KastelUtils plugin, ChangelogManager changelogManager) {
        this.plugin = plugin;
        this.changelogManager = changelogManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Uso: /" + label + " reload|changelog <versión|list|latest|view> [versión]");
            return true;
        }

        if (args[0].equalsIgnoreCase("changelog")) {
            return createChangelog(sender, label, args);
        }

        if (!args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(ChatColor.RED + "Subcomando desconocido. Usa /" + label + " reload o changelog <versión>");
            return true;
        }

        if (!sender.hasPermission(RELOAD_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return true;
        }

        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "La configuración de KastelUtils ha sido recargada.");
        return true;
    }

    private boolean createChangelog(CommandSender sender, String label, String[] args) {
        if (args.length >= 2 && args[1].equalsIgnoreCase("list")) {
            if (!checkViewPermission(sender)) {
                return true;
            }
            return listChangelogs(sender);
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("latest")) {
            if (!checkViewPermission(sender)) {
                return true;
            }
            return viewChangelog(sender, changelogManager.getLatestVersion());
        }
        if (args.length >= 3 && args[1].equalsIgnoreCase("view")) {
            if (!checkViewPermission(sender)) {
                return true;
            }
            return viewChangelog(sender, args[2]);
        }

        if (args.length >= 2 && args[1].equalsIgnoreCase("view")) {
            sender.sendMessage(ChatColor.YELLOW + "Uso: /" + label + " changelog view <versión>");
            return true;
        }

        if (args.length == 1 || args[1].equalsIgnoreCase("create")) {
            return createNextChangelog(sender);
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Este subcomando solo puede usarse dentro del servidor.");
            return true;
        }
        if (!sender.hasPermission(CHANGELOG_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para crear changelogs.");
            return true;
        }
        if (args.length < 2 || args[1].isBlank()) {
            sender.sendMessage(ChatColor.YELLOW + "Uso: /" + label + " changelog <versión>");
            return true;
        }

        String version = args[1];
        ItemStack book = changelogManager.createEditorBook(version);
        player.getInventory().setItemInMainHand(book);
        sender.sendMessage(ChatColor.GREEN + "Se ha entregado el libro del changelog. Ábrelo con clic derecho, escribe los cambios y ciérralo para guardarlo.");
        return true;
    }

    private boolean createNextChangelog(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Este subcomando solo puede usarse dentro del servidor.");
            return true;
        }
        if (!sender.hasPermission(CHANGELOG_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para crear changelogs.");
            return true;
        }

        String version = changelogManager.getNextVersion();
        ItemStack book = changelogManager.createEditorBook(version);
        player.getInventory().setItemInMainHand(book);
        sender.sendMessage(ChatColor.GREEN + "Se ha creado el changelog " + version + " automáticamente.");
        sender.sendMessage(ChatColor.GREEN + "Ábrelo con clic derecho, escribe los cambios y ciérralo para guardarlo.");
        return true;
    }

    private boolean checkViewPermission(CommandSender sender) {
        if (sender.hasPermission(CHANGELOG_VIEW_PERMISSION)) {
            return true;
        }
        sender.sendMessage(ChatColor.RED + "No tienes permiso para consultar changelogs manualmente.");
        return false;
    }

    private boolean listChangelogs(CommandSender sender) {
        List<String> versions = changelogManager.getVersions();
        if (versions.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Todavía no hay changelogs guardados.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "Changelogs disponibles:");
        for (String version : versions) {
            String marker = version.equals(changelogManager.getLatestVersion()) ? ChatColor.GREEN + " (último)" : "";
            sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.WHITE + version + marker);
        }
        return true;
    }

    private boolean viewChangelog(CommandSender sender, String version) {
        if (version == null || version.isBlank()) {
            sender.sendMessage(ChatColor.YELLOW + "Todavía no hay ningún changelog publicado.");
            return true;
        }
        if (!changelogManager.hasChangelogVersion(version)) {
            sender.sendMessage(ChatColor.RED + "No existe el changelog " + version + ".");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Solo un jugador puede abrir el libro del changelog.");
            return true;
        }

        player.openBook(changelogManager.createViewBook(version));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            if ("reload".startsWith(args[0].toLowerCase())) {
                suggestions.add("reload");
            }
            if ("changelog".startsWith(args[0].toLowerCase())) {
                suggestions.add("changelog");
            }
            return suggestions;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("changelog")) {
            List<String> suggestions = new ArrayList<>();
            String input = args[1].toLowerCase();
            for (String option : List.of("create", "list", "latest", "view")) {
                if (option.startsWith(input)) {
                    suggestions.add(option);
                }
            }
            for (String version : changelogManager.getVersions()) {
                if (version.toLowerCase().startsWith(input)) {
                    suggestions.add(version);
                }
            }
            return suggestions;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("changelog")
                && args[1].equalsIgnoreCase("view")) {
            String input = args[2].toLowerCase();
            return changelogManager.getVersions().stream()
                    .filter(version -> version.toLowerCase().startsWith(input))
                    .toList();
        }

        return Collections.emptyList();
    }
}