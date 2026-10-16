package me.KastelUtils.changelog;

import me.KastelUtils.KastelUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ChangelogManager {

    private static final Pattern SEMANTIC_VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");

    private final KastelUtils plugin;
    private final NamespacedKey versionKey;
    private final NamespacedKey seenVersionKey;
    private final File changelogsFile;
    private YamlConfiguration changelogs;

    public ChangelogManager(KastelUtils plugin) {
        this.plugin = plugin;
        this.versionKey = new NamespacedKey(plugin, "changelog_version");
        this.seenVersionKey = new NamespacedKey(plugin, "changelog_seen_version");
        this.changelogsFile = new File(plugin.getDataFolder(), "changelogs.yml");
        load();
    }

    public void load() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("No se pudo crear la carpeta de datos del plugin.");
        }
        changelogs = YamlConfiguration.loadConfiguration(changelogsFile);
    }

    public ItemStack createEditorBook(String version) {
        return createEditorBook(version, false);
    }

    public ItemStack createEditorBook(String version, boolean editExisting) {
        ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta == null) {
            return book;
        }

        meta.setTitle("Changelog " + version);
        meta.setAuthor(plugin.getConfig().getString("changelog.book-author", "KastelCraft"));
        meta.getPersistentDataContainer().set(versionKey, PersistentDataType.STRING, version);
        List<String> pages = editExisting ? getPages(version) : Collections.emptyList();
        meta.setPages(pages.isEmpty() ? createTemplate(version) : pages);
        book.setItemMeta(meta);
        return book;
    }

    private List<String> getPages(String version) {
        String storageKey = findStorageKey(version);
        if (storageKey == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(changelogs.getStringList("changelogs." + storageKey + ".pages"));
    }

    public boolean saveFromBook(ItemStack book) {
        if (book == null || book.getType() != Material.WRITABLE_BOOK && book.getType() != Material.WRITTEN_BOOK) {
            return false;
        }

        BookMeta meta = book.getItemMeta() instanceof BookMeta bookMeta ? bookMeta : null;
        if (meta == null) {
            return false;
        }

        String version = meta.getPersistentDataContainer().get(versionKey, PersistentDataType.STRING);
        if (!isValidVersion(version)) {
            return false;
        }

        List<String> pages = new ArrayList<>();
        for (String page : meta.getPages()) {
            pages.add(page == null ? "" : page);
        }

        String path = getStoragePath(version);
        changelogs.set(path + ".display-version", version);
        changelogs.set(path + ".pages", pages);
        changelogs.set(path + ".updated-at", System.currentTimeMillis());
        changelogs.set("latest-version", version);
        save(changelogs, changelogsFile);
        return true;
    }

    public boolean saveEditedBook(BookMeta previousMeta, BookMeta editedMeta, boolean signing) {
        if (previousMeta == null || editedMeta == null) {
            return false;
        }

        String version = previousMeta.getPersistentDataContainer().get(versionKey, PersistentDataType.STRING);
        if (!isValidVersion(version)) {
            return false;
        }

        editedMeta.getPersistentDataContainer().set(versionKey, PersistentDataType.STRING, version);
        ItemStack editedBook = new ItemStack(signing ? Material.WRITTEN_BOOK : Material.WRITABLE_BOOK);
        editedBook.setItemMeta(editedMeta);
        return saveFromBook(editedBook);
    }

    public boolean hasLatestChangelog() {
        return getLatestVersion() != null;
    }

    public void openLatest(Player player) {
        String latestVersion = getLatestVersion();
        if (latestVersion == null) {
            return;
        }

        ItemStack book = createViewBook(latestVersion);
        player.getPersistentDataContainer().set(seenVersionKey, PersistentDataType.STRING, latestVersion);
        player.openBook(book);
    }

    private String getSeenVersion(Player player) {
        return player.getPersistentDataContainer().get(seenVersionKey, PersistentDataType.STRING);
    }

    public boolean hasChangelogVersion(String version) {
        return findStorageKey(version) != null;
    }

    public String getNextVersion() {
        String latestVersion = getLatestVersion();
        if (latestVersion == null) {
            return "0.0.1";
        }

        Matcher matcher = SEMANTIC_VERSION_PATTERN.matcher(latestVersion);
        if (!matcher.find()) {
            return "0.0.1";
        }

        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        int patch = Integer.parseInt(matcher.group(3));
        return major + "." + minor + "." + (patch + 1);
    }

    public List<String> getVersions() {
        ConfigurationSection section = changelogs.getConfigurationSection("changelogs");
        if (section == null) {
            return Collections.emptyList();
        }
        return section.getKeys(false).stream()
                .map(this::getDisplayVersion)
                .filter(this::isValidVersion)
                .sorted(this::compareVersions)
                .collect(Collectors.toList());
    }

    public String getLatestVersion() {
        String latest = changelogs.getString("latest-version");
        if (isValidVersion(latest) && findStorageKey(latest) != null) {
            return latest;
        }

        ConfigurationSection section = changelogs.getConfigurationSection("changelogs");
        if (section == null || section.getKeys(false).isEmpty()) {
            return null;
        }
        return getVersions().stream().reduce((first, second) -> second).orElse(null);
    }

    public ItemStack createViewBook(String version) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta == null) {
            return book;
        }

        meta.setTitle("Changelog " + version);
        meta.setAuthor(plugin.getConfig().getString("changelog.book-author", "KastelCraft"));
        String storageKey = findStorageKey(version);
        List<String> pages = storageKey == null
                ? Collections.emptyList()
                : changelogs.getStringList("changelogs." + storageKey + ".pages");
        meta.setPages(pages.isEmpty() ? Collections.singletonList("No hay información disponible.") : colorize(pages));
        book.setItemMeta(meta);
        return book;
    }

    public String getVersion(ItemStack book) {
        if (book == null || !(book.getItemMeta() instanceof BookMeta meta)) {
            return null;
        }
        return meta.getPersistentDataContainer().get(versionKey, PersistentDataType.STRING);
    }

    private List<String> createTemplate(String version) {
        String minecraftVersion = plugin.getConfig().getString("changelog.minecraft-version", "26.2");
        String template = String.join("\n",
                "SV Versión: " + version,
                "MC Versión: " + minecraftVersion,
                "",
                "Novedades de la versión " + version + ":",
                "",
                "- Escribe aquí los cambios...");
        return Collections.singletonList(template);
    }

    private String getStoragePath(String version) {
        return "changelogs." + version.replace(".", "_dot_");
    }

    private String findStorageKey(String version) {
        String directKey = version.replace(".", "_dot_");
        if (changelogs.contains("changelogs." + directKey)) {
            return directKey;
        }

        ConfigurationSection section = changelogs.getConfigurationSection("changelogs");
        if (section == null) {
            return null;
        }
        for (String storageKey : section.getKeys(false)) {
            if (getDisplayVersion(storageKey).equals(version)) {
                return storageKey;
            }
        }
        return null;
    }

    private String getDisplayVersion(String storageKey) {
        String storedVersion = changelogs.getString("changelogs." + storageKey + ".display-version");
        if (storedVersion != null && !storedVersion.isBlank()) {
            return storedVersion;
        }

        Matcher matcher = Pattern.compile("version_(\\d+)_(\\d+)_(\\d+)").matcher(storageKey);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1)) + "."
                    + Integer.parseInt(matcher.group(2)) + "."
                    + Integer.parseInt(matcher.group(3));
        }
        return storageKey.replace("_dot_", ".");
    }

    private boolean isValidVersion(String version) {
        return version != null && SEMANTIC_VERSION_PATTERN.matcher(version).matches();
    }

    private int compareVersions(String first, String second) {
        Matcher firstMatcher = SEMANTIC_VERSION_PATTERN.matcher(first);
        Matcher secondMatcher = SEMANTIC_VERSION_PATTERN.matcher(second);
        if (firstMatcher.find() && secondMatcher.find()) {
            for (int i = 1; i <= 3; i++) {
                int comparison = Integer.compare(
                        Integer.parseInt(firstMatcher.group(i)),
                        Integer.parseInt(secondMatcher.group(i)));
                if (comparison != 0) {
                    return comparison;
                }
            }
            return 0;
        }
        return first.compareToIgnoreCase(second);
    }

    private List<String> colorize(List<String> pages) {
        List<String> result = new ArrayList<>();
        for (String page : pages) {
            result.add(ChatColor.translateAlternateColorCodes('&', page));
        }
        return result;
    }

    private void save(YamlConfiguration configuration, File file) {
        try {
            configuration.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("No se pudo guardar " + file.getName() + ": " + exception.getMessage());
        }
    }
}