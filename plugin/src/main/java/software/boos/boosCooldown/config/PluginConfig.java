package software.boos.boosCooldown.config;

import software.boos.boosCooldown.util.TimeFormatter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class PluginConfig {

    /** Top-level sections whose missing keys are filled from the bundled file; the rest is user-owned. */
    private static final List<String> PLUGIN_OWNED_SECTIONS = List.of("options", "database");
    private static final DateTimeFormatter BACKUP_STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final JavaPlugin plugin;
    private final String fileName;
    private File file;
    private YamlConfiguration config;
    private boolean loadFailed;

    public PluginConfig(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        reload();
    }

    public final void reload() {
        if (file == null) {
            file = new File(plugin.getDataFolder(), fileName);
        }
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create config directory: {0}", parent);
                return;
            }
            plugin.saveResource(fileName, false);
        }
        config = new YamlConfiguration();
        try {
            config.load(file);
            loadFailed = false;
        } catch (IOException | InvalidConfigurationException e) {
            loadFailed = true;
            moveAsideAndLoadDefault(e);
            return;
        }
        addMissingPluginOptions();
    }

    private void moveAsideAndLoadDefault(Exception loadError) {
        File backup = backupFile(".invalid.bak");
        try {
            Files.move(file.toPath(), backup.toPath());
        } catch (IOException e) {
            loadError.addSuppressed(e);
            plugin.getLogger().log(Level.SEVERE, "Cannot load " + fileName
                    + " and could not move it aside; it will not be written until it loads", loadError);
            return;
        }
        plugin.getLogger().log(Level.SEVERE, "Cannot load " + fileName + "; moved it to " + backup.getName()
                + " and loaded the default config. Fix the backup and copy it back", loadError);
        plugin.saveResource(fileName, false);
        config = new YamlConfiguration();
        try {
            config.load(file);
            loadFailed = false;
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Cannot load the default " + fileName, e);
        }
    }

    /** A backup name for {@link #file} that is not taken yet, e.g. {@code config.yml.2026-10-13_14-05-09.bak}. */
    private File backupFile(String extension) {
        String stamp = LocalDateTime.now().format(BACKUP_STAMP);
        File backup = new File(file.getParentFile(), fileName + "." + stamp + extension);
        for (int i = 1; backup.exists(); i++) {
            backup = new File(file.getParentFile(), fileName + "." + stamp + "-" + i + extension);
        }
        return backup;
    }

    /**
     * Copies options the bundled file has under {@link #PLUGIN_OWNED_SECTIONS} but the loaded
     * file lacks, with their comments, then backs up and saves the file if anything was added.
     */
    private void addMissingPluginOptions() {
        YamlConfiguration bundled;
        try (InputStream in = plugin.getResource(fileName)) {
            if (in == null) {
                return;
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                bundled = YamlConfiguration.loadConfiguration(reader);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not read bundled " + fileName, e);
            return;
        }

        List<String> added = new ArrayList<>();
        for (String path : bundled.getKeys(true)) {
            if (!PLUGIN_OWNED_SECTIONS.contains(path.split("\\.", 2)[0]) || config.contains(path)) {
                continue;
            }
            int lastDot = path.lastIndexOf('.');
            // A user scalar where the bundled file has a section must not be replaced by one.
            if (lastDot >= 0 && !config.isConfigurationSection(path.substring(0, lastDot))) {
                continue;
            }
            if (bundled.isConfigurationSection(path)) {
                config.createSection(path);
            } else {
                config.set(path, bundled.get(path));
                added.add(path);
            }
            config.setComments(path, bundled.getComments(path));
            config.setInlineComments(path, bundled.getInlineComments(path));
        }
        if (added.isEmpty()) {
            return;
        }

        File backup = backupFile(".bak");
        try {
            Files.copy(file.toPath(), backup.toPath());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not back up " + fileName + "; not saving the added options", e);
            return;
        }
        if (save()) {
            plugin.getLogger().info("Added missing options to " + fileName + " (backup: " + backup.getName() + "): "
                    + String.join(", ", added));
        }
    }

    public boolean save() {
        if (config == null || file == null) {
            return false;
        }
        if (loadFailed) {
            plugin.getLogger().severe("Not saving " + fileName + ": it failed to load, saving would overwrite it");
            return false;
        }
        try {
            config.save(file);
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + fileName, e);
            return false;
        }
    }

    public YamlConfiguration raw() {
        return config;
    }

    // -------- generic accessors --------

    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    public long getLong(String path, long def) {
        return config.getLong(path, def);
    }

    public double getDouble(String path, double def) {
        return config.getDouble(path, def);
    }

    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public void set(String path, Object value) {
        config.set(path, value);
    }

    // -------- feature flags / options --------

    public boolean isDisabledForOps() {
        return config.getBoolean("options.options.disabled_for_ops", true);
    }

    public boolean isSyntaxBlockerDisabledForOps() {
        return config.getBoolean("options.options.disable_syntax_blocker_for_ops", true);
    }

    public boolean isSyntaxBlockerEnabled() {
        return config.getBoolean("options.options.syntax_blocker_enabled", true);
    }

    /**
     * When true, commands typed with a namespace prefix (e.g. {@code /minecraft:tp})
     * are rewritten to their bare form (e.g. {@code /tp}) so plugin rules apply
     * to them. Only consulted when {@link #isSyntaxBlockerEnabled()} is false —
     * if the blocker is on it takes precedence and the command is denied.
     */
    public boolean isApplyRulesToPrefixedSyntax() {
        return config.getBoolean("options.options.apply_rules_to_prefixed_syntax", true);
    }

    public boolean isCooldownEnabled() {
        return config.getBoolean("options.options.cooldowns_enabled", true);
    }

    public boolean isWarmupEnabled() {
        return config.getBoolean("options.options.warmups_enabled", true);
    }

    public boolean isLimitsEnabled() {
        return config.getBoolean("options.options.limits_enabled", true);
    }

    public boolean isPricesEnabled() {
        return config.getBoolean("options.options.prices_enabled", true);
    }

    public boolean isItemCostEnabled() {
        return config.getBoolean("options.options.item_cost_enabled", true);
    }

    public boolean isXpCostEnabled() {
        return config.getBoolean("options.options.xp_cost_enabled", true);
    }

    public boolean isPlayerPointsEnabled() {
        return config.getBoolean("options.options.player_points_prices_enabled", true);
    }

    public boolean isCommandLogging() {
        return config.getBoolean("options.options.command_logging", false);
    }

    public boolean isCommandSigns() {
        return config.getBoolean("options.options.command_signs", false);
    }

    public boolean isBlockInteractDuringWarmup() {
        return config.getBoolean("options.options.block_interact_during_warmup", false);
    }

    public boolean isClearCooldownsOnDeath() {
        return config.getBoolean("options.options.clear_cooldowns_on_death", false);
    }

    public boolean isClearUsesOnDeath() {
        return config.getBoolean("options.options.clear_uses_on_death", false);
    }

    public boolean isStartCooldownsOnDeath() {
        return config.getBoolean("options.options.start_cooldowns_on_death", false);
    }

    public boolean isCancelWarmUpOnDamage() {
        return config.getBoolean("options.options.cancel_warmup_on_damage", false);
    }

    public boolean isCancelWarmUpOnMove() {
        return config.getBoolean("options.options.cancel_warmup_on_move", false);
    }

    public boolean isCancelWarmUpOnSneak() {
        return config.getBoolean("options.options.cancel_warmup_on_sneak", false);
    }

    public boolean isCancelWarmUpOnSprint() {
        return config.getBoolean("options.options.cancel_warmup_on_sprint", false);
    }

    public boolean isCancelWarmUpOnGameModeChange() {
        return config.getBoolean("options.options.cancel_warmup_on_gamemode_change", false);
    }

    public boolean isCancelPotionsOnWarmupCancel() {
        return config.getBoolean("options.options.cancel_potions_on_warmup_cancel", false);
    }

    public boolean isCommandConfirmationEnabled() {
        return config.getBoolean("options.options.command_confirmation", true);
    }

    public boolean isActionBarWarmup() {
        return config.getBoolean("options.action_bar.warmup", false);
    }

    public boolean isActionBarCooldownOnAttempt() {
        return config.getBoolean("options.action_bar.cooldown_on_attempt", false);
    }

    public int getActionBarUpdateIntervalTicks() {
        return config.getInt("options.action_bar.update_interval_ticks", 10);
    }

    public boolean isBossBarWarmup() {
        return config.getBoolean("options.boss_bar.warmup", false);
    }

    public String getBossBarColor() {
        return config.getString("options.boss_bar.color", "BLUE");
    }

    public String getBossBarStyle() {
        return config.getString("options.boss_bar.style", "PROGRESS");
    }

    public boolean isRefundOnWarmupCancel() {
        return config.getBoolean("options.options.refund_on_warmup_cancel", false);
    }

    public boolean isAuditEnabled() {
        return config.getBoolean("options.options.audit_enabled", false);
    }

    public int getAuditRetentionDays() {
        return config.getInt("options.options.audit_retention_days", 30);
    }

    public int getConfigSyncIntervalSeconds() {
        return config.getInt("options.options.config_sync_interval_seconds", 60);
    }

    public java.util.Locale getDefaultLocale() {
        String raw = config.getString("options.options.default_locale", "en");
        return java.util.Locale.forLanguageTag(raw);
    }

    // -------- group/command lookups --------

    public String resolveGroup(Player player) {
        String group = "default";
        Set<String> groups = getGroups();
        for (String g : groups) {
            if (player.hasPermission("booscooldowns." + g)) {
                group = g;
            }
        }
        return group;
    }

    public Set<String> getGroups() {
        ConfigurationSection section = config.getConfigurationSection("commands.groups");
        return section != null ? section.getKeys(false) : Collections.emptySet();
    }

    public Set<String> getCommandsForGroup(String group) {
        java.util.Set<String> result = new java.util.LinkedHashSet<>();
        for (String current : inheritanceChain(group)) {
            ConfigurationSection section = config.getConfigurationSection("commands.groups." + current);
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    if (!"_inherits".equals(key)) result.add(key);
                }
            }
        }
        return result;
    }

    /**
     * Returns the group and its ancestors (most-specific first) based on the
     * optional {@code _inherits} key in each group section. Prevents cycles.
     */
    private java.util.List<String> inheritanceChain(String group) {
        java.util.List<String> chain = new java.util.ArrayList<>();
        java.util.Set<String> visited = new java.util.HashSet<>();
        String current = group;
        while (current != null && visited.add(current)) {
            chain.add(current);
            current = config.getString("commands.groups." + current + "._inherits");
        }
        return chain;
    }

    public Set<String> getCommandsForPlayer(Player player) {
        return getCommandsForGroup(resolveGroup(player));
    }

    public ConfigurationSection commandSection(Player player, String commandKey) {
        for (String group : inheritanceChain(resolveGroup(player))) {
            ConfigurationSection section = config.getConfigurationSection("commands.groups." + group);
            if (section == null) continue;
            ConfigurationSection cmd = section.getConfigurationSection(commandKey);
            if (cmd != null) return cmd;
        }
        return null;
    }

    /** Returns a string from a command's section, or {@code def} if missing. */
    public String getCommandString(Player player, String commandKey, String option, String def) {
        ConfigurationSection sec = commandSection(player, commandKey);
        return sec != null ? sec.getString(option, def) : def;
    }

    /** Returns an int from a command's section, or {@code def} if missing. */
    public int getCommandInt(Player player, String commandKey, String option, int def) {
        ConfigurationSection sec = commandSection(player, commandKey);
        return sec != null ? sec.getInt(option, def) : def;
    }

    /** Returns a double from a command's section, or {@code def} if missing. */
    public double getCommandDouble(Player player, String commandKey, String option, double def) {
        ConfigurationSection sec = commandSection(player, commandKey);
        return sec != null ? sec.getDouble(option, def) : def;
    }

    /** Returns a boolean from a command's section, or {@code def} if missing. */
    public boolean getCommandBoolean(Player player, String commandKey, String option, boolean def) {
        ConfigurationSection sec = commandSection(player, commandKey);
        return sec != null && sec.getBoolean(option, def);
    }

    /** Returns a string list from a command's section, or empty if missing. */
    public List<String> getCommandStringList(Player player, String commandKey, String option) {
        ConfigurationSection sec = commandSection(player, commandKey);
        return sec != null ? sec.getStringList(option) : Collections.emptyList();
    }

    // -------- per-command values --------

    public long getCooldownSeconds(Player player, String commandKey) {
        return TimeFormatter.parseSeconds(getCommandString(player, commandKey, "cooldown", "0"));
    }

    public long getWarmupSeconds(Player player, String commandKey) {
        return TimeFormatter.parseSeconds(getCommandString(player, commandKey, "warmup", "0"));
    }

    public long getServerCooldownSeconds(Player player, String commandKey) {
        return TimeFormatter.parseSeconds(getCommandString(player, commandKey, "server_cooldown", "0"));
    }

    public int getLimit(Player player, String commandKey) {
        return getCommandInt(player, commandKey, "limit", -1);
    }

    public long getLimitResetDelaySeconds(Player player, String commandKey) {
        return TimeFormatter.parseSeconds(getCommandString(player, commandKey, "limit_reset_delay", "0"));
    }

    public double getMoneyPrice(Player player, String commandKey) {
        return getCommandDouble(player, commandKey, "price", 0.0);
    }

    public int getXpPrice(Player player, String commandKey) {
        return getCommandInt(player, commandKey, "xpcost", 0);
    }

    public int getXpRequirement(Player player, String commandKey) {
        return getCommandInt(player, commandKey, "xprequirement", 0);
    }

    public int getPlayerPointsPrice(Player player, String commandKey) {
        return getCommandInt(player, commandKey, "playerpoints", 0);
    }

    private ConfigurationSection itemCostSection(Player player, String commandKey) {
        ConfigurationSection cmd = commandSection(player, commandKey);
        return cmd != null ? cmd.getConfigurationSection("itemcost") : null;
    }

    public String getItemCostMaterial(Player player, String commandKey) {
        ConfigurationSection s = itemCostSection(player, commandKey);
        return s != null ? s.getString("item", "") : "";
    }

    public int getItemCostCount(Player player, String commandKey) {
        ConfigurationSection s = itemCostSection(player, commandKey);
        return s != null ? s.getInt("count", 0) : 0;
    }

    public String getItemCostName(Player player, String commandKey) {
        ConfigurationSection s = itemCostSection(player, commandKey);
        return s != null ? s.getString("name", "") : "";
    }

    public List<String> getItemCostLore(Player player, String commandKey) {
        ConfigurationSection s = itemCostSection(player, commandKey);
        return s != null ? s.getStringList("lore") : Collections.emptyList();
    }

    public List<String> getItemCostEnchants(Player player, String commandKey) {
        ConfigurationSection s = itemCostSection(player, commandKey);
        return s != null ? s.getStringList("enchants") : Collections.emptyList();
    }

    public List<String> getSharedCooldowns(Player player, String commandKey) {
        return getCommandStringList(player, commandKey, "shared_cooldown");
    }

    public List<String> getSharedLimits(Player player, String commandKey) {
        return getCommandStringList(player, commandKey, "shared_limit");
    }

    public List<String> getPotionEffects(Player player, String commandKey) {
        return getCommandStringList(player, commandKey, "potion");
    }

    public boolean isCommandDisabled(Player player, String commandKey) {
        return getCommandBoolean(player, commandKey, "disabled", false);
    }

    public boolean isCancelCommand(Player player, String commandKey) {
        return getCommandBoolean(player, commandKey, "cancel_command", false);
    }

    public String getCommandMessage(Player player, String commandKey) {
        return getCommandString(player, commandKey, "message", "");
    }

    public String getRequiredPermission(Player player, String commandKey) {
        return getCommandString(player, commandKey, "permission", null);
    }

    public String getPermissionDeniedMessage(Player player, String commandKey) {
        return getCommandString(player, commandKey, "denied_message", null);
    }

    // -------- aliases --------

    public Set<String> getAliases() {
        ConfigurationSection section = config.getConfigurationSection("commands.aliases");
        return section != null ? section.getKeys(false) : Collections.emptySet();
    }

    public String getAlias(String commandKey) {
        return config.getString("commands.aliases." + commandKey);
    }

    // -------- global limit reset --------

    public Set<String> getGlobalLimitCommands() {
        ConfigurationSection section = config.getConfigurationSection("global");
        return section != null ? section.getKeys(false) : Collections.emptySet();
    }

    public long getGlobalLimitResetDelaySeconds(String commandKey) {
        String raw = config.getString("global." + commandKey + ".limit_reset_delay", "0");
        return TimeFormatter.parseSeconds(raw);
    }

    // -------- administrative --------

    public void setCommandOption(String group, String commandKey, String key, String value) {
        String path = "commands.groups." + group.toLowerCase() + "." + commandKey.toLowerCase() + "." + key;
        try {
            config.set(path, Integer.parseInt(value));
        } catch (NumberFormatException e) {
            config.set(path, value);
        }
    }

    public boolean getAutoSaveEnabled() {
        return config.getBoolean("options.options.auto_save_enabled_CAN_CAUSE_BIG_LAGS", false);
    }

    public int getSaveIntervalMinutes() {
        return config.getInt("options.options.save_interval_in_minutes", 15);
    }

    public boolean getClearOnRestart() {
        return config.getBoolean("options.options.clear_on_restart", false);
    }
}
