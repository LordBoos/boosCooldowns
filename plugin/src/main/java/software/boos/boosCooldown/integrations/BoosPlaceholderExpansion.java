package software.boos.boosCooldown.integrations;

import software.boos.boosCooldown.BoosCoolDown;
import software.boos.boosCooldown.model.CommandData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Exposes plugin state to PlaceholderAPI consumers. Placeholders:
 *
 * <ul>
 *   <li>{@code %boos_remaining_<cmd>%} — remaining cooldown, formatted.</li>
 *   <li>{@code %boos_remaining_seconds_<cmd>%} — raw integer seconds.</li>
 *   <li>{@code %boos_on_cooldown_<cmd>%} — "true" / "false".</li>
 *   <li>{@code %boos_server_cooldown_<cmd>%} — server-wide cooldown remaining.</li>
 *   <li>{@code %boos_uses_<cmd>%} — "remaining/limit".</li>
 *   <li>{@code %boos_uses_remaining_<cmd>%} — raw integer.</li>
 * </ul>
 *
 * The {@code <cmd>} part accepts the command with or without a leading slash;
 * underscores are converted back to spaces so PAPI-safe identifiers can be used.
 */
public final class BoosPlaceholderExpansion extends PlaceholderExpansion {

    private final BoosCoolDown plugin;

    public BoosPlaceholderExpansion(BoosCoolDown plugin) {
        this.plugin = plugin;
    }

    @Override public @NotNull String getIdentifier() { return "boos"; }
    @Override public @NotNull String getAuthor() { return "boosik"; }
    @Override public @NotNull String getVersion() { return plugin.getDescription().getVersion(); }
    @Override public boolean persist() { return true; }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null || !offlinePlayer.isOnline()) return "";
        Player player = offlinePlayer.getPlayer();
        if (player == null) return "";

        String[] parts = params.split("_", 2);
        if (parts.length < 2) return null;
        String prefix = parts[0].toLowerCase();
        String rest = parts[1];

        return switch (prefix) {
            case "remaining" -> handleRemaining(player, rest);
            case "on" -> handleOn(player, rest);
            case "server" -> handleServer(player, rest);
            case "uses" -> handleUses(player, rest);
            default -> null;
        };
    }

    private String handleRemaining(Player player, String rest) {
        if (rest.startsWith("seconds_")) {
            CommandData data = resolve(player, rest.substring("seconds_".length()));
            if (data == null) return "0";
            return Long.toString(plugin.services().cooldownService().getRemainingCooldown(player, data));
        }
        CommandData data = resolve(player, rest);
        if (data == null) return "0";
        long remaining = plugin.services().cooldownService().getRemainingCooldown(player, data);
        return formatRemaining(remaining);
    }

    private String handleOn(Player player, String rest) {
        if (!rest.startsWith("cooldown_")) return null;
        CommandData data = resolve(player, rest.substring("cooldown_".length()));
        if (data == null) return "false";
        return Boolean.toString(plugin.services().cooldownService().isOnCooldown(player, data));
    }

    private String handleServer(Player player, String rest) {
        if (!rest.startsWith("cooldown_")) return null;
        CommandData data = resolve(player, rest.substring("cooldown_".length()));
        if (data == null) return "0";
        return formatRemaining(plugin.services().cooldownService().getRemainingServerCooldown(data));
    }

    private String handleUses(Player player, String rest) {
        boolean rawOnly = rest.startsWith("remaining_");
        String cmd = rawOnly ? rest.substring("remaining_".length()) : rest;
        CommandData data = resolve(player, cmd);
        if (data == null) return "0";
        int remaining = plugin.services().limitService().getRemainingUses(player, data);
        if (rawOnly) return Integer.toString(remaining);
        if (!data.hasLimit()) return "∞";
        return remaining + "/" + data.limit();
    }

    private CommandData resolve(Player player, String paramCmd) {
        String cmd = paramCmd.replace('_', ' ');
        if (!cmd.startsWith("/")) cmd = "/" + cmd;
        String key = plugin.services().commandDataFactory().resolveConfiguredKey(player, cmd);
        return key == null ? null : plugin.services().commandDataFactory().build(player, cmd, key);
    }

    private String formatRemaining(long seconds) {
        if (seconds <= 0) return "0s";
        if (seconds < 60) return seconds + "s";
        if (seconds < 3600) return (seconds / 60) + "m " + (seconds % 60) + "s";
        return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "m";
    }

    public static void registerWith(BoosCoolDown plugin) {
        try {
            new BoosPlaceholderExpansion(plugin).register();
            plugin.getLogger().info("[boosCooldowns] PlaceholderAPI expansion registered (%boos_*%).");
        } catch (LinkageError e) {
            plugin.getLogger().warning("[boosCooldowns] PlaceholderAPI linkage error: " + e.getMessage());
        }
    }
}
