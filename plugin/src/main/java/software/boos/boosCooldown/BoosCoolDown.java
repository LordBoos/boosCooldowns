package software.boos.boosCooldown;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.zaxxer.hikari.HikariDataSource;

import software.boos.boosCooldown.command.CommandGroup;
import software.boos.boosCooldown.command.CommandHandler;
import software.boos.boosCooldown.command.HelpCategory;
import software.boos.boosCooldown.command.SubCommand;
import software.boos.boosCooldown.command.subcommand.AuditSubCommand;
import software.boos.boosCooldown.command.subcommand.CancelSubCommand;
import software.boos.boosCooldown.command.subcommand.CheckSubCommand;
import software.boos.boosCooldown.command.subcommand.ClearCooldownsSubCommand;
import software.boos.boosCooldown.command.subcommand.ClearUsesSubCommand;
import software.boos.boosCooldown.command.subcommand.ClearWarmupsSubCommand;
import software.boos.boosCooldown.command.subcommand.ConfirmSubCommand;
import software.boos.boosCooldown.command.subcommand.ConfirmationsSubCommand;
import software.boos.boosCooldown.command.subcommand.DiffSubCommand;
import software.boos.boosCooldown.command.subcommand.HelpSubCommand;
import software.boos.boosCooldown.command.subcommand.ListResetsSubCommand;
import software.boos.boosCooldown.command.subcommand.ReloadSubCommand;
import software.boos.boosCooldown.command.subcommand.ScheduleGlobalResetSubCommand;
import software.boos.boosCooldown.command.subcommand.SetSubCommand;
import software.boos.boosCooldown.command.subcommand.StatusSubCommand;
import software.boos.boosCooldown.command.subcommand.TempSetSubCommand;
import software.boos.boosCooldown.command.subcommand.ValidateSubCommand;
import software.boos.boosCooldown.command.subcommand.clear.ClearAllSubCommand;
import software.boos.boosCooldown.command.subcommand.grant.GrantCooldownSubCommand;
import software.boos.boosCooldown.command.subcommand.grant.GrantUsesSubCommand;
import software.boos.boosCooldown.command.subcommand.reset.ResetNowSubCommand;
import software.boos.boosCooldown.command.subcommand.rule.RuleGetSubCommand;
import software.boos.boosCooldown.config.DatabaseConfig;
import software.boos.boosCooldown.config.LocaleMessages;
import software.boos.boosCooldown.config.MessageConfig;
import software.boos.boosCooldown.config.PluginConfig;
import software.boos.boosCooldown.economy.EconomyProvider;
import software.boos.boosCooldown.economy.EconomyProviders;
import software.boos.boosCooldown.economy.ItemCostProvider;
import software.boos.boosCooldown.economy.XpProvider;
import software.boos.boosCooldown.integrations.BoosPlaceholderExpansion;
import software.boos.boosCooldown.listener.CommandPreprocessListener;
import software.boos.boosCooldown.listener.PlayerConnectionListener;
import software.boos.boosCooldown.listener.PlayerDeathListener;
import software.boos.boosCooldown.listener.SignChangeListener;
import software.boos.boosCooldown.listener.SignInteractListener;
import software.boos.boosCooldown.listener.WarmupCancelListener;
import software.boos.boosCooldown.persistence.DataSourceFactory;
import software.boos.boosCooldown.persistence.SchemaMigrator;
import software.boos.boosCooldown.persistence.migration.YamlToJdbcMigrator;
import software.boos.boosCooldown.persistence.repository.AuditRepository;
import software.boos.boosCooldown.persistence.repository.ConfigVersionRepository;
import software.boos.boosCooldown.persistence.repository.CooldownRepository;
import software.boos.boosCooldown.persistence.repository.GlobalLimitResetRepository;
import software.boos.boosCooldown.persistence.repository.LimitRepository;
import software.boos.boosCooldown.persistence.repository.ServerCooldownRepository;
import software.boos.boosCooldown.persistence.repository.UserPreferencesRepository;
import software.boos.boosCooldown.persistence.repository.jdbc.JdbcAuditRepository;
import software.boos.boosCooldown.persistence.repository.jdbc.JdbcConfigVersionRepository;
import software.boos.boosCooldown.persistence.repository.jdbc.JdbcCooldownRepository;
import software.boos.boosCooldown.persistence.repository.jdbc.JdbcGlobalLimitResetRepository;
import software.boos.boosCooldown.persistence.repository.jdbc.JdbcLimitRepository;
import software.boos.boosCooldown.persistence.repository.jdbc.JdbcServerCooldownRepository;
import software.boos.boosCooldown.persistence.repository.jdbc.JdbcUserPreferencesRepository;
import software.boos.boosCooldown.service.ActionBarService;
import software.boos.boosCooldown.service.AliasService;
import software.boos.boosCooldown.service.AuditService;
import software.boos.boosCooldown.service.CommandDataFactory;
import software.boos.boosCooldown.service.ConfigSyncService;
import software.boos.boosCooldown.service.ConfirmationService;
import software.boos.boosCooldown.service.CooldownServiceImpl;
import software.boos.boosCooldown.service.EventTriggerService;
import software.boos.boosCooldown.service.LimitServiceImpl;
import software.boos.boosCooldown.service.PriceService;
import software.boos.boosCooldown.service.RegionService;
import software.boos.boosCooldown.service.ScheduledResetService;
import software.boos.boosCooldown.service.Services;
import software.boos.boosCooldown.service.WarmupEffectsService;
import software.boos.boosCooldown.service.WarmupServiceImpl;

public class BoosCoolDown extends JavaPlugin {

    private boolean debugMode = false;

    private PluginConfig pluginConfig;
    private MessageConfig messageConfig;
    private DataSource dataSource;
    private ExecutorService asyncExecutor;
    private Services services;
    private EventTriggerService eventTriggerService;

    @Override
    public void onEnable() {
        this.pluginConfig = new PluginConfig(this, "config.yml");
        this.messageConfig = new MessageConfig(pluginConfig);
        LocaleMessages localeMessages = new LocaleMessages(this, pluginConfig);
        localeMessages.reload();
        messageConfig.setLocaleMessages(localeMessages);
        this.debugMode = pluginConfig.getBoolean("options.options.debug", false);

        DatabaseConfig databaseConfig = DatabaseConfig.fromSection(
                pluginConfig.raw().getConfigurationSection("database"),
                getDataFolder());
        this.dataSource = DataSourceFactory.create(databaseConfig);
        new SchemaMigrator(dataSource, databaseConfig.backend(), getLogger()).migrate();

        this.asyncExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "BoosCooldowns-Async");
            t.setDaemon(true);
            return t;
        });

        CooldownRepository cooldownRepo = new JdbcCooldownRepository(dataSource, databaseConfig.backend());
        ServerCooldownRepository serverRepo = new JdbcServerCooldownRepository(dataSource, databaseConfig.backend());
        LimitRepository limitRepo = new JdbcLimitRepository(dataSource, databaseConfig.backend());
        GlobalLimitResetRepository resetRepo = new JdbcGlobalLimitResetRepository(dataSource, databaseConfig.backend());
        UserPreferencesRepository prefsRepo = new JdbcUserPreferencesRepository(dataSource, databaseConfig.backend());
        AuditRepository auditRepo = new JdbcAuditRepository(dataSource);
        ConfigVersionRepository configVersionRepo = new JdbcConfigVersionRepository(dataSource, databaseConfig.backend());

        new YamlToJdbcMigrator(getDataFolder(), pluginConfig, cooldownRepo, limitRepo, getLogger())
                .migrateIfNeeded();

        CooldownServiceImpl cooldownService = new CooldownServiceImpl(cooldownRepo, serverRepo,
                databaseConfig.cacheTtlMs(), asyncExecutor);
        WarmupServiceImpl warmupService = new WarmupServiceImpl(this);
        LimitServiceImpl limitService = new LimitServiceImpl(limitRepo, asyncExecutor);
        AliasService aliasService = new AliasService(pluginConfig);
        CommandDataFactory commandDataFactory = new CommandDataFactory(pluginConfig);
        ActionBarService actionBarService = new ActionBarService(this, pluginConfig, warmupService);
        ScheduledResetService scheduledResetService = new ScheduledResetService(this,
                resetRepo, limitRepo, asyncExecutor);

        List<EconomyProvider> providers = new ArrayList<>();
        EconomyProviders.loadVault(messageConfig, pluginConfig.isPricesEnabled(), getLogger())
                .ifPresent(providers::add);
        EconomyProviders.loadPlayerPoints(messageConfig, pluginConfig.isPlayerPointsEnabled(), getLogger())
                .ifPresent(providers::add);
        providers.add(new XpProvider(messageConfig, pluginConfig.isXpCostEnabled()));
        providers.add(new ItemCostProvider(messageConfig, pluginConfig.isItemCostEnabled()));
        PriceService priceService = new PriceService(providers);

        ConfirmationService confirmationService = new ConfirmationService(this, pluginConfig,
                messageConfig, limitService, prefsRepo, asyncExecutor);
        WarmupEffectsService warmupEffectsService = new WarmupEffectsService(this, pluginConfig);
        RegionService regionService = new RegionService(getLogger());
        AuditService auditService = new AuditService(auditRepo, pluginConfig, asyncExecutor);
        ConfigSyncService configSyncService = new ConfigSyncService(this, pluginConfig,
                configVersionRepo, asyncExecutor);

        this.services = new Services(pluginConfig, messageConfig, databaseConfig,
                cooldownService, warmupService, warmupService, limitService, priceService,
                aliasService, commandDataFactory, actionBarService, scheduledResetService,
                confirmationService, warmupEffectsService, regionService, auditService,
                configSyncService);

        scheduledResetService.scheduleAllPersisted();
        actionBarService.start();
        configSyncService.publishSelf();
        configSyncService.startMonitor();

        // Prefetch cache for any players already online (e.g. after /reload).
        for (var online : Bukkit.getOnlinePlayers()) {
            cooldownService.prefetch(online);
        }
        // Periodic cache eviction (runs async off main thread).
        Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                cooldownService::cleanUpCaches, 20L * 60L, 20L * 60L);

        registerListeners();
        registerCommand();

        this.eventTriggerService = new EventTriggerService(this, pluginConfig, services.cooldownService());
        this.eventTriggerService.registerAll();

        BoosCooldownAPI.initialize(services);
        // Checked here, not in the expansion: loading that class needs PlaceholderAPI's superclass.
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            BoosPlaceholderExpansion.registerWith(this);
        }

        initializeMetrics(databaseConfig);

        getLogger().info("[boosCooldowns] Enabled with " + databaseConfig.backend()
                + " backend at " + databaseConfig.jdbcUrl());
    }

    private void initializeMetrics(DatabaseConfig databaseConfig) {
        try {
            Metrics metrics = new Metrics(this, 30_839);

            // --- Backend & version drill-down ---
            metrics.addCustomChart(new DrilldownPie("storage_backend", () -> {
                String backend = databaseConfig.backend().name().toLowerCase();
                String driverVersion = resolveDriverVersion(databaseConfig);
                java.util.Map<String, Integer> inner = new java.util.HashMap<>();
                inner.put(driverVersion, 1);
                java.util.Map<String, java.util.Map<String, Integer>> outer = new java.util.HashMap<>();
                outer.put(backend, inner);
                return outer;
            }));

            // --- Feature toggles (single answer per server) ---
            metrics.addCustomChart(new SimplePie("confirmations_enabled",
                    () -> Boolean.toString(pluginConfig.isCommandConfirmationEnabled())));
            metrics.addCustomChart(new SimplePie("signs_enabled",
                    () -> Boolean.toString(pluginConfig.isCommandSigns())));
            metrics.addCustomChart(new SimplePie("action_bar_enabled",
                    () -> Boolean.toString(pluginConfig.isActionBarWarmup())));
            metrics.addCustomChart(new SimplePie("boss_bar_enabled",
                    () -> Boolean.toString(pluginConfig.isBossBarWarmup())));
            metrics.addCustomChart(new SimplePie("audit_enabled",
                    () -> Boolean.toString(pluginConfig.isAuditEnabled())));
            metrics.addCustomChart(new SimplePie("refund_on_warmup_cancel",
                    () -> Boolean.toString(pluginConfig.isRefundOnWarmupCancel())));

            // --- Locale & cluster shape ---
            metrics.addCustomChart(new SimplePie("default_locale",
                    () -> {
                        java.util.Locale loc = pluginConfig.getDefaultLocale();
                        return loc != null && !loc.getLanguage().isEmpty() ? loc.getLanguage() : "en";
                    }));
            metrics.addCustomChart(new SimplePie("is_multi_node",
                    () -> {
                        try {
                            return services != null && services.configSyncService().isMultiNode()
                                    ? "yes" : "no";
                        } catch (Throwable t) {
                            return "unknown";
                        }
                    }));

            // --- Integrations detected ---
            metrics.addCustomChart(new AdvancedPie("enabled_integrations", () -> {
                java.util.Map<String, Integer> values = new java.util.HashMap<>();
                if (Bukkit.getPluginManager().isPluginEnabled("Vault")) values.put("Vault", 1);
                if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) values.put("PlayerPoints", 1);
                if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) values.put("PlaceholderAPI", 1);
                if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) values.put("WorldGuard", 1);
                if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) values.put("LuckPerms", 1);
                if (values.isEmpty()) values.put("(none)", 1);
                return values;
            }));

            // --- Which individual plugin features does the admin actually use ---
            metrics.addCustomChart(new AdvancedPie("enabled_features", () -> {
                java.util.Map<String, Integer> values = new java.util.HashMap<>();
                if (pluginConfig.isCooldownEnabled()) values.put("cooldowns", 1);
                if (pluginConfig.isWarmupEnabled()) values.put("warmups", 1);
                if (pluginConfig.isLimitsEnabled()) values.put("limits", 1);
                if (pluginConfig.isPricesEnabled()) values.put("prices (money)", 1);
                if (pluginConfig.isXpCostEnabled()) values.put("prices (xp)", 1);
                if (pluginConfig.isItemCostEnabled()) values.put("prices (items)", 1);
                if (pluginConfig.isPlayerPointsEnabled()) values.put("prices (points)", 1);
                if (pluginConfig.isCommandSigns()) values.put("signs", 1);
                if (pluginConfig.isBossBarWarmup()) values.put("boss bar", 1);
                if (pluginConfig.isActionBarWarmup()) values.put("action bar", 1);
                if (pluginConfig.isAuditEnabled()) values.put("audit", 1);
                if (pluginConfig.isCommandConfirmationEnabled()) values.put("confirmations", 1);
                return values;
            }));

            // --- Config scale ---
            metrics.addCustomChart(new SimplePie("configured_groups", () ->
                    bucket(pluginConfig.getGroups().size(), 1, 2, 3, 5)));
            metrics.addCustomChart(new SimplePie("configured_commands", () -> {
                int total = 0;
                for (String group : pluginConfig.getGroups()) {
                    total += pluginConfig.getCommandsForGroup(group).size();
                }
                return bucket(total, 1, 10, 25, 50, 100);
            }));
            metrics.addCustomChart(new SimplePie("event_triggers", () -> {
                int count = pluginConfig.raw().getMapList("event_triggers").size();
                return bucket(count, 0, 1, 3, 5, 10);
            }));

            // --- Which warmup cancel sources are wired up ---
            metrics.addCustomChart(new AdvancedPie("warmup_cancel_sources", () -> {
                java.util.Map<String, Integer> values = new java.util.HashMap<>();
                if (pluginConfig.isCancelWarmUpOnMove()) values.put("move", 1);
                if (pluginConfig.isCancelWarmUpOnDamage()) values.put("damage", 1);
                if (pluginConfig.isCancelWarmUpOnSneak()) values.put("sneak", 1);
                if (pluginConfig.isCancelWarmUpOnSprint()) values.put("sprint", 1);
                if (pluginConfig.isCancelWarmUpOnGameModeChange()) values.put("gamemode change", 1);
                if (values.isEmpty()) values.put("(none)", 1);
                return values;
            }));
        } catch (Throwable t) {
            // bStats should never break plugin startup
            getLogger().warning("[boosCooldowns] bStats failed to initialise: " + t.getMessage());
        }
    }

    /**
     * Groups a numeric value into log-ish buckets suitable for a SimplePie. The
     * result is a human-readable label like {@code "1-9"}, {@code "10-24"},
     * {@code "100+"}.
     */
    private static String bucket(int value, int... thresholds) {
        if (value < thresholds[0]) return "0";
        for (int i = 0; i < thresholds.length - 1; i++) {
            if (value < thresholds[i + 1]) {
                return thresholds[i] + "-" + (thresholds[i + 1] - 1);
            }
        }
        return thresholds[thresholds.length - 1] + "+";
    }

    /**
     * Best-effort JDBC driver version lookup for the drill-down pie. Returns
     * "unknown" if the driver doesn't expose a version the way we expect.
     */
    private static String resolveDriverVersion(DatabaseConfig config) {
        try {
            String className = switch (config.backend()) {
                case SQLITE -> "org.sqlite.JDBC";
                case MYSQL -> "com.mysql.cj.jdbc.Driver";
            };
            java.sql.Driver driver = (java.sql.Driver) Class.forName(className)
                    .getDeclaredConstructor().newInstance();
            return driver.getMajorVersion() + "." + driver.getMinorVersion();
        } catch (Throwable t) {
            return "unknown";
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        if (services != null) {
            services.actionBarService().stop();
            services.scheduledResetService().shutdown();
            services.confirmationService().shutdown();
            services.warmupEffectsService().shutdown();
            services.configSyncService().shutdown();
        }
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    getLogger().warning("[boosCooldowns] Async executor did not terminate within 5s; "
                            + "pending DB writes may have been dropped.");
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                asyncExecutor.shutdownNow();
            }
        }
        if (dataSource instanceof HikariDataSource hds) {
            hds.close();
        }
        BoosCooldownAPI.shutdown();
    }

    public void reloadPluginConfig() {
        pluginConfig.reload();
        debugMode = pluginConfig.getBoolean("options.options.debug", false);
        services.actionBarService().start();
        registerListeners();
        if (eventTriggerService != null) eventTriggerService.registerAll();
        services.configSyncService().publishSelf();
    }

    private void registerCommand() {
        // Root-level singleton subcommands
        SubCommand reload = new ReloadSubCommand(this);
        SubCommand status = new StatusSubCommand(pluginConfig, services.commandDataFactory(),
                services.cooldownService(), services.limitService(), services.warmupService());
        SubCommand check = new CheckSubCommand(pluginConfig, messageConfig,
                services.commandDataFactory(), services.cooldownService());
        SubCommand confirmations = new ConfirmationsSubCommand(services.confirmationService());
        SubCommand confirm = new ConfirmSubCommand(services.confirmationService());
        SubCommand cancel = new CancelSubCommand(services.confirmationService());
        SubCommand validate = new ValidateSubCommand(pluginConfig);
        SubCommand audit = new AuditSubCommand(services.auditService());
        SubCommand diff = new DiffSubCommand(services.configSyncService());

        // Nested group: /bcd clear ...
        ClearCooldownsSubCommand clearCooldowns = new ClearCooldownsSubCommand(services.cooldownService());
        ClearUsesSubCommand clearUses = new ClearUsesSubCommand(services.limitService());
        ClearWarmupsSubCommand clearWarmups = new ClearWarmupsSubCommand(services.warmupService());
        ClearAllSubCommand clearAll = new ClearAllSubCommand(services.cooldownService(),
                services.limitService(), services.warmupService());
        CommandGroup clearGroup = new CommandGroup("clear",
                "Clear cooldowns, usage counters or warmups for a player", HelpCategory.ADMIN)
                .add(clearCooldowns).add(clearUses).add(clearWarmups).add(clearAll);

        // Nested group: /bcd rule ...
        SetSubCommand ruleSet = new SetSubCommand(this, pluginConfig, services.configSyncService());
        TempSetSubCommand ruleTempSet = new TempSetSubCommand(this, pluginConfig, services.configSyncService());
        RuleGetSubCommand ruleGet = new RuleGetSubCommand(pluginConfig);
        CommandGroup ruleGroup = new CommandGroup("rule",
                "Inspect or mutate command rules", HelpCategory.ADMIN)
                .add(ruleSet).add(ruleTempSet).add(ruleGet);

        // Nested group: /bcd grant ...
        GrantCooldownSubCommand grantCooldown = new GrantCooldownSubCommand(
                services.commandDataFactory(), services.cooldownService());
        GrantUsesSubCommand grantUses = new GrantUsesSubCommand(services.limitService());
        CommandGroup grantGroup = new CommandGroup("grant",
                "Manually seed cooldowns or usage counters", HelpCategory.ADMIN)
                .add(grantCooldown).add(grantUses);

        // Nested group: /bcd reset ...
        ResetNowSubCommand resetNow = new ResetNowSubCommand(services.limitService());
        ScheduleGlobalResetSubCommand resetSchedule = new ScheduleGlobalResetSubCommand(
                services.scheduledResetService());
        ListResetsSubCommand resetList = new ListResetsSubCommand(services.scheduledResetService());
        CommandGroup resetGroup = new CommandGroup("reset",
                "Global usage-counter resets — immediate or scheduled", HelpCategory.ADMIN)
                .add(resetNow).add(resetSchedule).add(resetList);

        CommandHandler handler = new CommandHandler()
                // Player
                .register(status)
                .register(check)
                .register(confirmations)
                // Internal (hidden from help)
                .register(confirm)
                .register(cancel)
                // Admin
                .register(reload)
                .register(clearGroup)
                .register(ruleGroup)
                .register(grantGroup)
                .register(resetGroup)
                // Diagnostics
                .register(validate)
                .register(audit)
                .register(diff);

        // Autogenerated /bcd help
        handler.register(new HelpSubCommand(handler));

        // Backward-compatible root aliases — every legacy top-level name keeps working.
        handler.registerAlias("clearcooldowns", clearCooldowns);
        handler.registerAlias("clearuses", clearUses);
        handler.registerAlias("clearwarmups", clearWarmups);
        handler.registerAlias("set", ruleSet);
        handler.registerAlias("tempset", ruleTempSet);
        handler.registerAlias("scheduleglobalreset", resetSchedule);
        handler.registerAlias("startglobalreset", resetNow);
        handler.registerAlias("listresets", resetList);
        handler.registerAlias("info", status);
        handler.registerAlias("limits", status);
        handler.registerAlias("checkcooldown", check);

        var command = getCommand("booscooldowns");
        if (command != null) {
            command.setExecutor(handler);
            command.setTabCompleter(handler);
        }
    }

    private void registerListeners() {
        var pm = Bukkit.getPluginManager();
        org.bukkit.event.HandlerList.unregisterAll(this);
        pm.registerEvents(new CommandPreprocessListener(this, pluginConfig, messageConfig,
                services.commandDataFactory(), services.aliasService(),
                services.cooldownService(), services.warmupService(),
                services.limitService(), services.priceService(),
                services.actionBarService(), services.confirmationService(),
                services.warmupEffectsService(), services.regionService()), this);
        pm.registerEvents(new WarmupCancelListener(pluginConfig, messageConfig,
                services.warmupService()), this);
        pm.registerEvents(new PlayerDeathListener(pluginConfig, services.commandDataFactory(),
                services.cooldownService(), services.limitService()), this);
        pm.registerEvents(new PlayerConnectionListener(services.cooldownService()), this);
        if (pluginConfig.isCommandSigns()) {
            pm.registerEvents(new SignChangeListener(messageConfig), this);
            pm.registerEvents(new SignInteractListener(this, messageConfig), this);
        }
    }

    public Services services() {
        return services;
    }

    public PluginConfig pluginConfig() {
        return pluginConfig;
    }

    public MessageConfig messageConfig() {
        return messageConfig;
    }

    public boolean isDebug() {
        return debugMode;
    }

    public void debug(String message) {
        if (debugMode) {
            getLogger().info("[DEBUG] " + message);
        }
    }
}
