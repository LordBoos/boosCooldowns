package software.boos.boosCooldown.listener;

import software.boos.boosCooldown.BoosCoolDown;
import software.boos.boosCooldown.config.MessageConfig;
import software.boos.boosCooldown.config.PluginConfig;
import software.boos.boosCooldown.economy.EconomyProvider;
import software.boos.boosCooldown.model.CommandData;
import software.boos.boosCooldown.service.ActionBarService;
import software.boos.boosCooldown.service.AliasService;
import software.boos.boosCooldown.service.CommandDataFactory;
import software.boos.boosCooldown.service.ConfirmationService;
import software.boos.boosCooldown.service.CooldownService;
import software.boos.boosCooldown.service.LimitService;
import software.boos.boosCooldown.service.PriceService;
import software.boos.boosCooldown.service.RegionService;
import software.boos.boosCooldown.service.WarmupEffectsService;
import software.boos.boosCooldown.service.WarmupService;
import software.boos.boosCooldown.util.BoosChat;
import software.boos.boosCooldown.util.TimeFormatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Main command-intake pipeline. Replaces the legacy {@code BoosCoolDownListener}.
 * Ordered checks: exception → syntax blocker → alias → per-command data →
 * disabled → server cooldown → limit → permission → price → warmup → cooldown.
 */
public final class CommandPreprocessListener implements Listener {

    private final BoosCoolDown plugin;
    private final PluginConfig config;
    private final MessageConfig messages;
    private final CommandDataFactory factory;
    private final AliasService aliases;
    private final CooldownService cooldowns;
    private final WarmupService warmups;
    private final LimitService limits;
    private final PriceService prices;
    private final ActionBarService actionBar;
    private final ConfirmationService confirmations;
    private final WarmupEffectsService effects;
    private final RegionService regions;
    private final Set<UUID> bypassing = Collections.synchronizedSet(new HashSet<>());

    public CommandPreprocessListener(BoosCoolDown plugin, PluginConfig config, MessageConfig messages,
                                     CommandDataFactory factory, AliasService aliases,
                                     CooldownService cooldowns, WarmupService warmups,
                                     LimitService limits, PriceService prices,
                                     ActionBarService actionBar, ConfirmationService confirmations,
                                     WarmupEffectsService effects, RegionService regions) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
        this.factory = factory;
        this.aliases = aliases;
        this.cooldowns = cooldowns;
        this.warmups = warmups;
        this.limits = limits;
        this.prices = prices;
        this.actionBar = actionBar;
        this.confirmations = confirmations;
        this.effects = effects;
        this.regions = regions;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        try {
            process(event);
        } catch (RuntimeException e) {
            // Never let a bug here swallow commands for everybody on the
            // server. Log once and fail open — let Bukkit dispatch normally.
            plugin.getLogger().severe("[boosCooldowns] Command pipeline error for '"
                    + event.getMessage() + "': " + e.getMessage());
            if (plugin.isDebug()) e.printStackTrace();
        }
    }

    private void process(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String raw = event.getMessage();

        // Bypass the pipeline when this dispatch originated from our own
        // confirmation/warmup follow-through (see finishCommand).
        if (bypassing.contains(player.getUniqueId())) return;

        if (!isPluginOnForPlayer(player)) return;

        // Namespace-prefixed form like "/minecraft:tp spawn".
        String stripped = stripNamespacePrefix(raw);
        boolean wasPrefixed = !stripped.equals(raw);

        if (wasPrefixed && config.isSyntaxBlockerEnabled() && isSyntaxBlockerOn(player)) {
            // Admin explicitly chose to deny namespaced commands outright.
            BoosChat.sendMessage(player, messages.invalidCommandSyntax());
            event.setCancelled(true);
            return;
        }
        if (wasPrefixed && config.isApplyRulesToPrefixedSyntax()) {
            // Rewrite the event to the bare form so the downstream pipeline —
            // rule lookup, aliases, cooldowns, Bukkit dispatch — all see /tp
            // rather than /minecraft:tp. The player keeps the shortcut.
            event.setMessage(stripped);
            raw = stripped;
        }

        // Alias substitution
        String resolvedAlias = aliases.resolve(player, raw);
        if (resolvedAlias != null && !resolvedAlias.equalsIgnoreCase(raw)) {
            event.setMessage(resolvedAlias);
            raw = resolvedAlias;
        }

        if (player.hasPermission("booscooldowns.norestriction")) return;

        String configuredKey = factory.resolveConfiguredKey(player, raw);
        if (configuredKey == null) return; // not configured, leave alone

        CommandData data = factory.build(player, raw, configuredKey);

        // 1. Disabled check
        if (data.disabled() && !player.hasPermission("booscooldowns.nodisable")
                && !player.hasPermission("booscooldowns.nodisable." + raw.toLowerCase())) {
            BoosChat.sendMessage(player, messages.disabled(player, configuredKey)
                    .replace("&command&", raw));
            event.setCancelled(true);
            return;
        }

        // 1b. World restriction
        if (data.hasWorldRestriction()
                && !data.allowedWorlds().contains(player.getWorld().getName())) {
            return; // command not restricted in this world → pass through untouched
        }

        // 1c. Region restriction (WorldGuard soft-depend)
        if (data.hasRegionRestriction() && regions.isEnabled()
                && !regions.playerInAnyRegion(player, data.allowedRegions())) {
            return;
        }

        // 2. Required per-command permission
        if (data.requiredPermission() != null && !data.requiredPermission().isEmpty()
                && !player.hasPermission(data.requiredPermission())) {
            if (data.permissionDeniedMessage() != null) {
                BoosChat.sendMessage(player, data.permissionDeniedMessage());
            }
            event.setCancelled(true);
            return;
        }

        // 3. Server-wide cooldown
        if (config.isCooldownEnabled() && cooldowns.isServerOnCooldown(data)) {
            long remaining = cooldowns.getRemainingServerCooldown(data);
            String msg = formatCooldown(messages.serverCoolingDown(player, configuredKey), raw, remaining);
            BoosChat.sendMessage(player, msg);
            actionBar.sendCooldownNotice(player, msg);
            event.setCancelled(true);
            return;
        }

        // 4. Per-player cooldown
        if (config.isCooldownEnabled()
                && !player.hasPermission("booscooldowns.nocooldown")
                && !player.hasPermission("booscooldowns.nocooldown." + raw.toLowerCase())
                && cooldowns.isOnCooldown(player, data)) {
            long remaining = cooldowns.getRemainingCooldown(player, data);
            String msg = formatCooldown(messages.coolingDown(player, configuredKey), raw, remaining);
            BoosChat.sendMessage(player, msg);
            actionBar.sendCooldownNotice(player, msg);
            event.setCancelled(true);
            return;
        }

        // 5. Confirmation dialog (for commands that cost something)
        boolean needsConfirmation = (data.hasMoneyPrice() || data.hasXpPrice()
                || data.hasItemCost() || data.hasPlayerPointsPrice() || data.hasLimit())
                && confirmations.requiresConfirmation(player);
        if (needsConfirmation) {
            final String captured = raw;
            final String capturedKey = configuredKey;
            confirmations.request(player, data, () -> {
                PipelineResult result = runLimitPriceWarmup(player, captured, capturedKey, data);
                if (result == PipelineResult.ALLOW) {
                    // no warmup and command was not dispatched yet, dispatch now
                    finishCommand(player, captured, data);
                }
            });
            event.setCancelled(true);
            return;
        }

        PipelineResult result = runLimitPriceWarmup(player, raw, configuredKey, data);
        if (result != PipelineResult.ALLOW) {
            event.setCancelled(true);
        }
    }

    private PipelineResult runLimitPriceWarmup(Player player, String raw,
                                               String configuredKey, CommandData data) {
        // ---------- Phase 1: check every precondition WITHOUT mutating state ----------

        boolean limitApplies = config.isLimitsEnabled()
                && !player.hasPermission("booscooldowns.nolimit")
                && !player.hasPermission("booscooldowns.nolimit." + raw.toLowerCase());
        if (limitApplies && data.hasLimit() && limits.getRemainingUses(player, data) <= 0) {
            BoosChat.sendMessage(player, messages.limitAchieved().replace("&command&", raw));
            return PipelineResult.BLOCKED;
        }

        PriceService.Result affordability = prices.checkAffordable(player, data);
        if (!affordability.success()) {
            BoosChat.sendMessage(player, affordability.failureReason());
            return PipelineResult.BLOCKED;
        }

        boolean warmupApplies = config.isWarmupEnabled() && data.hasWarmup()
                && !player.hasPermission("booscooldowns.nowarmup")
                && !player.hasPermission("booscooldowns.nowarmup." + raw.toLowerCase());
        if (warmupApplies && warmups.hasWarmup(player)) {
            BoosChat.sendMessage(player, messages.warmupAlreadyStarted().replace("&command&", raw));
            return PipelineResult.BLOCKED;
        }

        // ---------- Phase 2: commit mutations in a defined order ----------

        if (limitApplies) {
            // tryConsume may still race with shared_limit handling — if it
            // somehow returns false here we fail closed without charging.
            if (!limits.tryConsume(player, data)) {
                BoosChat.sendMessage(player, messages.limitAchieved().replace("&command&", raw));
                return PipelineResult.BLOCKED;
            }
        }

        PriceService.Result pricing = prices.chargeAll(player, data);
        if (!pricing.success()) {
            // Very unlikely: balance disappeared between check and charge.
            // Refund anything we did manage to take, and bail out.
            prices.refundAll(player, data, pricing.chargedProviders());
            BoosChat.sendMessage(player, pricing.failureReason());
            return PipelineResult.BLOCKED;
        }
        for (EconomyProvider provider : pricing.chargedProviders()) {
            BoosChat.sendMessage(player, provider.describePayment(player, data));
        }

        if (warmupApplies) {
            BoosChat.sendMessage(player,
                    formatCooldown(messages.warmingUp(player, configuredKey), raw, data.warmupSeconds()));
            final String commandToRun = raw;
            final java.util.List<EconomyProvider> charged = pricing.chargedProviders();
            effects.startEffects(player, data);
            warmups.startWarmup(player, data,
                    () -> {
                        effects.playCompleteSound(player, data);
                        finishCommand(player, commandToRun, data);
                    },
                    () -> {
                        effects.stopEffects(player);
                        if (config.isRefundOnWarmupCancel() && !charged.isEmpty()) {
                            prices.refundAll(player, data, charged);
                        }
                    });
            return PipelineResult.CAPTURED;
        }

        cooldowns.setCooldown(player, data);
        if (data.hasServerCooldown()) {
            cooldowns.setServerCooldown(data);
        }
        if (config.isCommandLogging()) {
            plugin.getLogger().info(player.getName() + " used command " + raw);
        }
        return PipelineResult.ALLOW;
    }

    private enum PipelineResult {
        /** Command is allowed to proceed through Bukkit's default dispatch. */
        ALLOW,
        /** Command has been captured (e.g. scheduled behind a warmup). Event must be cancelled. */
        CAPTURED,
        /** Command was blocked (insufficient funds, limits, etc.). Event must be cancelled. */
        BLOCKED
    }

    private void finishCommand(Player player, String command, CommandData data) {
        if (!player.isOnline()) return;
        cooldowns.setCooldown(player, data);
        if (data.hasServerCooldown()) {
            cooldowns.setServerCooldown(data);
        }
        if (config.isCommandLogging()) {
            plugin.getLogger().info(player.getName() + " used command " + command);
        }
        UUID uuid = player.getUniqueId();
        bypassing.add(uuid);
        try {
            String stripped = command.startsWith("/") ? command.substring(1) : command;
            Bukkit.dispatchCommand(player, stripped);
        } finally {
            bypassing.remove(uuid);
        }
    }

    private String formatCooldown(String template, String command, long seconds) {
        String formatted = TimeFormatter.formatRemaining(seconds,
                messages.unitHours(), messages.unitMinutes(), messages.unitSeconds());
        return template
                .replace("&command&", command)
                .replace("&seconds&", formatted)
                .replace("&unit&", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean isPluginOnForPlayer(Player player) {
        if (player.hasPermission("booscooldowns.exception")) return false;
        if (player.hasPermission("booscooldowns.norestriction")) return true;
        return !config.isDisabledForOps() || !player.isOp();
    }

    private boolean isSyntaxBlockerOn(Player player) {
        if (player.hasPermission("booscooldowns.syntaxblockerexception")) return false;
        return !config.isSyntaxBlockerDisabledForOps() || !player.isOp();
    }

    /**
     * Returns {@code raw} with a leading namespace prefix removed. A namespace
     * prefix is the {@code plugin:} part in {@code /plugin:command args} — if
     * the colon appears inside the command token (before any whitespace), we
     * strip up to and including that colon. Otherwise {@code raw} is returned
     * unchanged (so player chat like {@code /me hi:bye} isn't touched).
     */
    static String stripNamespacePrefix(String raw) {
        if (raw == null || raw.length() < 2 || !raw.startsWith("/")) return raw;
        int colonIdx = raw.indexOf(':');
        if (colonIdx <= 1) return raw;
        int spaceIdx = raw.indexOf(' ');
        if (spaceIdx != -1 && spaceIdx < colonIdx) return raw;
        // Validate the prefix only contains lowercase letters, digits, _, - or .
        // to match Bukkit's plugin namespace grammar.
        for (int i = 1; i < colonIdx; i++) {
            char c = raw.charAt(i);
            if (!(Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.')) return raw;
        }
        return "/" + raw.substring(colonIdx + 1);
    }
}
