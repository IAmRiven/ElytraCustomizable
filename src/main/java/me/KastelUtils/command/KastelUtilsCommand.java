package me.KastelUtils.command;

import me.KastelUtils.KastelUtils;
import me.KastelUtils.changelog.ChangelogManager;
import me.KastelUtils.message.MessageManager;
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
import java.util.Map;

public final class KastelUtilsCommand implements CommandExecutor, TabCompleter {

    private static final String RELOAD_PERMISSION = "kastelutils.reload";
    private static final String CHANGELOG_PERMISSION = "kastelutils.changelog";
    private static final String CHANGELOG_VIEW_PERMISSION = "kastelutils.changelog.view";

    private final KastelUtils plugin;
    private final ChangelogManager changelogManager;
    private final MessageManager messages;

    public KastelUtilsCommand(KastelUtils plugin, ChangelogManager changelogManager, MessageManager messages) {
        this.plugin = plugin;
        this.changelogManager = changelogManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(messages.get("command.usage", Map.of("label", label)));
            return true;
        }

        if (args[0].equalsIgnoreCase("changelog")) {
            return createChangelog(sender, label, args);
        }

        if (!args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(messages.get("command.unknown", Map.of("label", label)));
            return true;
        }

        if (!sender.hasPermission(RELOAD_PERMISSION)) {
            sender.sendMessage(messages.get("command.no-permission"));
            return true;
        }

        plugin.reloadConfig();
        messages.reload();
        sender.sendMessage(messages.get("command.reload"));
        return true;
    }

    private boolean createChangelog(CommandSender sender, String label, String[] args) {
        if (args.length >= 2 && args[1].equalsIgnoreCase("open")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(messages.get("changelog.open.console-only-player"));
                return true;
            }
            changelogManager.openLatest(player);
            return true;
        }

        if (args.length >= 3 && args[1].equalsIgnoreCase("edit")) {
            return editChangelog(sender, args[2]);
        }

        if (args.length >= 2 && args[1].equalsIgnoreCase("edit")) {
            sender.sendMessage(messages.get("changelog.edit.usage", Map.of("label", label)));
            return true;
        }

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
                sender.sendMessage(messages.get("changelog.view.usage", Map.of("label", label)));
            return true;
        }

        if (args.length == 1 || args[1].equalsIgnoreCase("create")) {
            return createNextChangelog(sender);
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("changelog.create.console-only-player"));
            return true;
        }
        if (!sender.hasPermission(CHANGELOG_PERMISSION)) {
            sender.sendMessage(messages.get("changelog.create.no-permission"));
            return true;
        }
        if (args.length < 2 || args[1].isBlank()) {
            sender.sendMessage(messages.get("changelog.create.manual-usage", Map.of("label", label)));
            return true;
        }

        String version = args[1];
        ItemStack book = changelogManager.createEditorBook(version);
        player.getInventory().setItemInMainHand(book);
        sender.sendMessage(messages.get("changelog.create.manual-created"));
        return true;
    }

    private boolean createNextChangelog(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("changelog.create.auto-console-only-player"));
            return true;
        }
        if (!sender.hasPermission(CHANGELOG_PERMISSION)) {
            sender.sendMessage(messages.get("changelog.create.auto-no-permission"));
            return true;
        }

        String version = changelogManager.getNextVersion();
        ItemStack book = changelogManager.createEditorBook(version);
        player.getInventory().setItemInMainHand(book);
        sender.sendMessage(messages.get("changelog.create.created", Map.of("version", version)));
        sender.sendMessage(messages.get("changelog.create.instructions"));
        return true;
    }

    private boolean editChangelog(CommandSender sender, String version) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("changelog.edit.console-only-player"));
            return true;
        }
        if (!sender.hasPermission(CHANGELOG_PERMISSION)) {
            sender.sendMessage(messages.get("changelog.edit.no-permission"));
            return true;
        }
        if (!changelogManager.hasChangelogVersion(version)) {
            sender.sendMessage(messages.get("changelog.edit.not-found", Map.of("version", version)));
            return true;
        }

        player.getInventory().setItemInMainHand(changelogManager.createEditorBook(version, true));
        sender.sendMessage(messages.get("changelog.edit.started", Map.of("version", version)));
        sender.sendMessage(messages.get("changelog.create.instructions"));
        return true;
    }

    private boolean checkViewPermission(CommandSender sender) {
        if (sender.hasPermission(CHANGELOG_VIEW_PERMISSION)) {
            return true;
        }
        sender.sendMessage(messages.get("changelog.view.no-permission"));
        return false;
    }

    private boolean listChangelogs(CommandSender sender) {
        List<String> versions = changelogManager.getVersions();
        if (versions.isEmpty()) {
            sender.sendMessage(messages.get("changelog.list.empty"));
            return true;
        }

        sender.sendMessage(messages.get("changelog.list.header"));
        for (String version : versions) {
            String marker = version.equals(changelogManager.getLatestVersion())
                    ? messages.get("changelog.list.latest") : "";
            sender.sendMessage(messages.get("changelog.list.entry", Map.of("version", version, "latest", marker)));
        }
        return true;
    }

    private boolean viewChangelog(CommandSender sender, String version) {
        if (version == null || version.isBlank()) {
            sender.sendMessage(messages.get("changelog.view.empty"));
            return true;
        }
        if (!changelogManager.hasChangelogVersion(version)) {
            sender.sendMessage(messages.get("changelog.view.not-found", Map.of("version", version)));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("changelog.view.console-only-player"));
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
            for (String option : List.of("create", "edit", "list", "latest", "view")) {
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