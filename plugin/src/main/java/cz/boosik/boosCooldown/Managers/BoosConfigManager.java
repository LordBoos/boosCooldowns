package cz.boosik.boosCooldown.Managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import cz.boosik.boosCooldown.BoosCoolDown;
import util.BoosChat;

public class BoosConfigManager {

    private static YamlConfiguration conf;
    private static YamlConfiguration confusers;
    private static File confFile;
    private static File confusersFile;

    @SuppressWarnings("static-access")
    public BoosConfigManager(final BoosCoolDown boosCoolDown) {
        confFile = new File(boosCoolDown.getDataFolder(), "config.yml");
        if (confFile.exists()) {
            conf = new YamlConfiguration();
            load();
        } else {
            this.confFile = new File(boosCoolDown.getDataFolder(), "config.yml");
            this.conf = new YamlConfiguration();
        }
        if (confFile.exists()) {
            load();
        }
        confusersFile = new File(boosCoolDown.getDataFolder(), "users.yml");
        confusers = new YamlConfiguration();
        if (confusersFile.exists()) {
            loadConfusers();
        } else {
            try {
                confusersFile.createNewFile();
            } catch (final IOException e) {
                e.printStackTrace();
                BoosCoolDown.getLog().severe("[boosCooldowns] Could not save storage file!");
            }
        }
    }

    public static void clear() {
        final ConfigurationSection userSection = confusers.getConfigurationSection("users");
        if (userSection == null) {
            return;
        }
        for (final String user : userSection.getKeys(false)) {
            final ConfigurationSection cooldown = confusers.getConfigurationSection("users." + user + ".cooldown");
            if (cooldown != null) {
                for (final String key : cooldown.getKeys(false)) {
                    confusers.set("users." + user + ".cooldown." + key, null);
                }
            }
            confusers.set("users." + user + ".cooldown", null);

            final ConfigurationSection warmup = confusers.getConfigurationSection("users." + user + ".warmup");
            if (warmup != null) {
                for (final String key : warmup.getKeys(false)) {
                    confusers.set("users." + user + ".warmup." + key, null);
                }
            }
            confusers.set("users." + user + ".warmup", null);

            confusers.set("users." + user, null);
        }
        saveConfusers();
        loadConfusers();
    }

    public static void clearSomething(final String co, final UUID uuid) {
        final ConfigurationSection userSection = confusers.getConfigurationSection("users." + uuid + "." + co);
        if (userSection == null) {
            return;
        }
        confusers.set("users." + uuid + "." + co, null);
        saveConfusers();
        loadConfusers();
    }

    public static void clearSomething(final String co, final UUID uuid, final String command) {
        final int pre2 = command.replace("_", " ").toLowerCase().hashCode();
        confusers.set("users." + uuid + "." + co + "." + pre2, 0);
        saveConfusers();
        loadConfusers();
    }

    static String getAlias(final String message) {
        return conf.getString("commands.aliases." + message);
    }

    public static Set<String> getAliases() {
        Set<String> aliases = null;
        final ConfigurationSection aliasesSection = conf.getConfigurationSection("commands.aliases");
        if (aliasesSection != null) {
            aliases = conf.getConfigurationSection("commands.aliases").getKeys(false);
        }
        return aliases;
    }

    public static boolean getBlockInteractDuringWarmup() {
        return conf.getBoolean("options.options.block_interact_during_warmup", false);
    }

    public static String getCancelWarmupByGameModeChangeMessage() {
        return conf.getString("options.messages.warmup_cancelled_by_gamemode_change",
                "&6Warm-ups have been cancelled due to changing gamemode.&f");
    }

    public static boolean getCancelWarmUpOnDamage() {
        return conf.getBoolean("options.options.cancel_warmup_on_damage", false);
    }

    public static boolean getCancelWarmUpOnGameModeChange() {
        return conf.getBoolean("options.options.cancel_warmup_on_gamemode_change", false);
    }

    public static boolean getCancelWarmupOnMove() {
        return conf.getBoolean("options.options.cancel_warmup_on_move", false);
    }

    public static boolean getCancelWarmupOnSneak() {
        return conf.getBoolean("options.options.cancel_warmup_on_sneak", false);
    }

    public static String getCancelWarmupOnSneakMessage() {
        return conf.getString("options.messages.warmup_cancelled_by_sneak", "&6Warm-ups have been cancelled due to sneaking.&f");
    }

    public static boolean getCancelWarmupOnSprint() {
        return conf.getBoolean("options.options.cancel_warmup_on_sprint", false);
    }

    public static String getCancelWarmupOnSprintMessage() {
        return conf.getString("options.messages.warmup_cancelled_by_sprint", "&6Warm-ups have been cancelled due to sprinting.&f");
    }

    public static String getCannotCreateSignMessage() {
        return conf.getString("options.messages.cannot_create_sign", "&6You are not allowed to create this kind of signs!&f");
    }

    public static String getCannotUseSignMessage() {
        return conf.getString("options.messages.cannot_use_sign", "&6You are not allowed to use this sign!&f");
    }

    public static boolean getCleanCooldownsOnDeath() {
        return conf.getBoolean("options.options.clear_cooldowns_on_death", false);
    }

    public static boolean getCleanUsesOnDeath() {
        return conf.getBoolean("options.options.clear_uses_on_death", false);
    }

    public static boolean getClearOnRestart() {
        return conf.getBoolean("options.options.clear_on_restart", false);
    }

    public static String getCommandBlockedMessage() {
        return conf.getString("options.messages.limit_achieved", "&6You cannot use this command anymore!&f");
    }

    private static String getCommandGroup(final Player player) {
        String cmdGroup = "default";
        final Set<String> groups = getCommandGroups();
        if (groups != null) {
            for (final String group : groups) {
                if (player.hasPermission("booscooldowns." + group)) {
                    cmdGroup = group;
                }
            }
        }
        return cmdGroup;
    }

    private static Set<String> getCommandGroups() {
        final ConfigurationSection groupsSection = conf.getConfigurationSection("commands.groups");
        Set<String> groups = null;
        if (groupsSection != null) {
            groups = groupsSection.getKeys(false);
        }
        return groups;
    }

    public static boolean getCommandLogging() {
        return conf.getBoolean("options.options.command_logging", false);
    }

    public static Set<String> getCommands(final Player player) {
        final String group = getCommandGroup(player);
        Set<String> commands = null;
        final ConfigurationSection commandsSection = conf.getConfigurationSection("commands.groups." + group);
        if (commandsSection != null) {
            commands = commandsSection.getKeys(false);
        }
        return commands;
    }

    public static YamlConfiguration getConfusers() {
        return confusers;
    }

    public static int getCoolDown(final String regexCommand, final Player player) {
        final int coolDown;
        String coolDownString = "";
        final String group = getCommandGroup(player);
        coolDownString = conf.getString("commands.groups." + group + "." + regexCommand + ".cooldown", "0");
        coolDown = parseTime(coolDownString);
        return coolDown;
    }

    public static boolean getCooldownEnabled() {
        return conf.getBoolean("options.options.cooldowns_enabled", true);
    }

    static String getCoolDownMessage() {
        return conf.getString("options.messages.cooling_down", "&6Wait&e &seconds& seconds&6 before you can use command&e &command& &6again.&f");
    }

    public static String getCheckCoolDownMessage() {
        return conf.getString("options.messages.check_cooldown", "&6Command&e &command& &6is still on cooldown. It will be available again in&e &seconds& &unit&.&f");
    }

    public static String getCheckCoolDownOkMessage() {
        return conf.getString("options.messages.check_cooldown_ok", "&6Command&e &command& &6is available.");
    }

    static Set<String> getCooldowns(final Player player) {
        final String cool = getCommandGroup(player);
        return conf.getConfigurationSection("commands.groups." + cool).getKeys(false);
    }

    public static String getInsufficientFundsMessage() {
        return conf.getString("options.messages.insufficient_funds",
                "&6You have insufficient funds!&e &command& &6costs &e%s &6but you only have &e%s");
    }

    public static String getInteractBlockedMessage() {
        return conf.getString("options.messages.interact_blocked_during_warmup", "&6You can't do this when command is warming-up!&f");
    }

    public static List<String> getItemCostLore(final String regexCommand, final Player player) {
        return conf.getStringList("commands.groups." + getCommandGroup(player) + "." + regexCommand + ".itemcost.lore");
    }

    public static List<String> getItemCostEnchants(final String regexCommand, final Player player) {
        return conf.getStringList("commands.groups." + getCommandGroup(player) + "." + regexCommand + ".itemcost.enchants");
    }

    public static String getItemCostName(final String regexCommand, final Player player) {
        return conf.getString("commands.groups." + getCommandGroup(player) + "." + regexCommand + ".itemcost.name", "");
    }

    public static String getItemCostItem(final String regexCommand, final Player player) {
        return conf.getString("commands.groups." + getCommandGroup(player) + "." + regexCommand + ".itemcost.item", "");
    }

    public static int getItemCostCount(final String regexCommand, final Player player) {
        return conf.getInt("commands.groups." + getCommandGroup(player) + "." + regexCommand + ".itemcost.count", 0);
    }

    public static int getLimit(final String regexCommand, final Player player) {
        final int limit;
        final String group = getCommandGroup(player);
        limit = conf.getInt("commands.groups." + group + "." + regexCommand + ".limit", -1);
        return limit;
    }

    public static boolean getLimitEnabled() {
        return conf.getBoolean("options.options.limits_enabled", true);
    }

    static String getLimitListMessage() {
        return conf.getString("options.messages.limit_list",
                "&6Limit for command &e&command&&6 is &e&limit&&6. You can still use it &e&times&&6 times.&f");
    }

    static boolean getLimitsEnabled() {
        return conf.getBoolean("options.options.limits_enabled", true);
    }

    static Set<String> getAllPlayers() {
        final ConfigurationSection users = confusers.getConfigurationSection("users");
        return users.getKeys(false);
    }

    static List<String> getSharedCooldowns(final String pre, final Player player) {
        final List<String> sharedCooldowns;
        final String group = getCommandGroup(player);
        sharedCooldowns = conf.getStringList("commands.groups." + group + "." + pre + ".shared_cooldown");
        return sharedCooldowns;
    }

    public static List<String> getSharedLimits(final String pre, final Player player) {
        final List<String> sharedLimits;
        final String group = getCommandGroup(player);
        sharedLimits = conf.getStringList("commands.groups." + group + "." + pre + ".shared_limit");
        return sharedLimits;
    }

    public static String getMessage(final String regexCommand, final Player player) {
        String message = "";
        final String group = getCommandGroup(player);
        message = conf.getString("commands.groups." + group + "." + regexCommand + ".message", "");
        return message;
    }

    public static Boolean getCancelCommand(final String regexCommand, final Player player) {
        final String group = getCommandGroup(player);
        return conf.getBoolean("commands.groups." + group + "." + regexCommand + ".cancel_command", false);
    }

    static String getPaidErrorMessage() {
        return conf.getString("options.messages.paid_error", "An error has occured: %s");
    }

    static String getPaidForCommandMessage() {
        return conf.getString("options.messages.paid_for_command", "Price of &command& was %s and you now have %s");
    }

    static List<String> getPotionEffects(final String regexCommand, final Player player) {
        final String group = getCommandGroup(player);
        return conf.getStringList("commands.groups." + group + "." + regexCommand + ".potion");
    }

    public static boolean getCancelPotionsOnWarmupCancel() {
        return conf.getBoolean("options.options.cancel_potions_on_warmup_cancel", false);
    }

    public static double getPrice(final String regexCommand, final Player player) {
        final double price;
        final String group = getCommandGroup(player);
        price = conf.getDouble("commands.groups." + group + "." + regexCommand + ".price", 0.0);
        return price;
    }

    public static boolean getPriceEnabled() {
        return conf.getBoolean("options.options.prices_enabled", true);
    }

    public static int getSaveInterval() {
        return conf.getInt("options.options.save_interval_in_minutes", 15);
    }

    public static boolean getSignCommands() {
        return conf.getBoolean("options.options.command_signs", false);
    }

    public static boolean getStartCooldownsOnDeath() {
        return conf.getBoolean("options.options.start_cooldowns_on_death", false);
    }

    static String getUnitHoursMessage() {
        return conf.getString("options.units.hours", "hours");
    }

    static String getUnitMinutesMessage() {
        return conf.getString("options.units.minutes", "minutes");
    }

    static String getUnitSecondsMessage() {
        return conf.getString("options.units.seconds", "seconds");
    }

    public static int getWarmUp(final String regexCommand, final Player player) {
        final int warmUp;
        String warmUpString = "";
        final String group = getCommandGroup(player);
        warmUpString = conf.getString("commands.groups." + group + "." + regexCommand + ".warmup", "0");
        warmUp = parseTime(warmUpString);
        return warmUp;
    }

    static String getWarmUpAlreadyStartedMessage() {
        return conf.getString("options.messages.warmup_already_started", "&6Warm-Up process for&e &command& &6has already started.&f");
    }

    public static String getWarmUpCancelledByDamageMessage() {
        return conf.getString("options.messages.warmup_cancelled_by_damage", "&6Warm-ups have been cancelled due to receiving damage.&f");
    }

    public static String getWarmUpCancelledByMoveMessage() {
        return conf.getString("options.messages.warmup_cancelled_by_move", "&6Warm-ups have been cancelled due to moving.&f");
    }

    public static boolean getWarmupEnabled() {
        return conf.getBoolean("options.options.warmups_enabled", true);
    }

    static String getWarmUpMessage() {
        return conf.getString("options.messages.warming_up", "&6Wait&e &seconds& seconds&6 before command&e &command& &6has warmed up.&f");
    }

    public static void load() {
        try {
            conf.load(confFile);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            BoosCoolDown.getLog().severe("[boosCooldowns] Configuration file not found!");
        } catch (final IOException e) {
            e.printStackTrace();
            BoosCoolDown.getLog().severe("[boosCooldowns] Could not read configuration file!");
        } catch (final InvalidConfigurationException e) {
            e.printStackTrace();
            BoosCoolDown.getLog().severe("[boosCooldowns] Configuration file is invalid!");
        }
    }

    public static void loadConfusers() {
        try {
            confusers.load(confusersFile);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            BoosCoolDown.getLog().severe("[boosCooldowns] Storage file not found!");
        } catch (final IOException e) {
            e.printStackTrace();
            BoosCoolDown.getLog().severe("[boosCooldowns] Could not read storage file!");
        } catch (final InvalidConfigurationException e) {
            e.printStackTrace();
            BoosCoolDown.getLog().severe("[boosCooldowns] Storage file is invalid!");
        }
    }

    public static void reload() {
        conf = new YamlConfiguration();
        load();
    }

    public static void saveConfusers() {
        try {
            confFile.createNewFile();
            confusers.save(confusersFile);
        } catch (final IOException e) {
            e.printStackTrace();
            BoosCoolDown.getLog().severe("[boosCooldowns] Could not save storage file!");
        }
    }

    public static void setAddToConfigFile(String group, String command, final String what, final String value) {
        group = group.toLowerCase();
        command = command.toLowerCase();
        final int value2;
        try {
            value2 = Integer.parseInt(value);
            reload();
            conf.set("commands.groups." + group + "." + command + "." + what, value2);
        } catch (final NumberFormatException e1) {
            reload();
            conf.set("commands.groups." + group + "." + command + "." + what, value);
        }
        try {
            conf.save(confFile);
        } catch (final IOException e) {
            BoosCoolDown.getLog().severe("[boosCooldowns] Could not save configuration file!");

        }
        reload();
    }

    public static void toggleConfirmations(final Player player) {
        final Boolean def = confusers.getBoolean("users." + player.getUniqueId() + ".confirmations", getConfirmCommandEnabled(player));
        confusers.set("users." + player.getUniqueId() + ".confirmations", !def);
        if (def) {
            BoosChat.sendMessageToPlayer(player, "&6[boosCooldowns]&e " + getConfirmToggleMessageFalse());
        } else {
            BoosChat.sendMessageToPlayer(player, "&6[boosCooldowns]&e " + getConfirmToggleMessageTrue());
        }
        saveConfusers();
        loadConfusers();
    }

    public static Boolean getConfirmationsPlayer(final Player player) {
        return (Boolean) confusers.get("users." + player.getUniqueId() + ".confirmations", null);
    }

    public static boolean getAutoSave() {
        return conf.getBoolean("options.options.auto_save_enabled_CAN_CAUSE_BIG_LAGS", false);
    }

    static String getPaidItemsForCommandMessage() {
        return conf.getString("options.messages.paid_items_for_command", "&6Price of&e &command& &6was &e%s");
    }

    public static String getInsufficientItemsMessage() {
        return conf.getString("options.messages.insufficient_items", "&6You have not enough items!&e &command& &6needs");
    }

    public static boolean getItemCostEnabled() {
        return conf.getBoolean("options.options.item_cost_enabled", true);
    }

    public static boolean getPlayerPointsEnabled() {
        return conf.getBoolean("options.options.player_points_prices_enabled", true);
    }

    public static int getPlayerPointsPrice(final String regexCommand, final Player player) {
        final int price;
        final String group = getCommandGroup(player);
        price = conf.getInt("commands.groups." + group + "." + regexCommand + ".playerpoints", 0);
        return price;
    }

    static String getPaidXPForCommandMessage() {
        return conf.getString("options.messages.paid_xp_for_command", "&6Price of&e &command& &6was &e%s");
    }

    public static int getXpPrice(final String regexCommand, final Player player) {
        final int price;
        final String group = getCommandGroup(player);
        price = conf.getInt("commands.groups." + group + "." + regexCommand + ".xpcost", 0);
        return price;
    }

    public static int getXpRequirement(final String regexCommand, final Player player) {
        final int price;
        final String group = getCommandGroup(player);
        price = conf.getInt("commands.groups." + group + "." + regexCommand + ".xprequirement", 0);
        return price;
    }

    public static boolean getXpPriceEnabled() {
        return conf.getBoolean("options.options.xp_cost_enabled", true);
    }

    public static String getInsufficientXpMessage() {
        return conf.getString("options.messages.insufficient_xp", "&6You have not enough XP!&e &command& &6needs &e%s");
    }

    public static String getInsufficientXpRequirementMessage() {
        return conf.getString("options.messages.insufficient_xp_requirement", "&6Your level is too low to use this!&e &command& &6needs &e%s");
    }

    public static String getInsufficientPlayerPointsMessage() {
        return conf.getString("options.messages.insufficient_player_points", "'&6You have not enough PlayerPoints!&e &command& &6needs &e%s'");
    }

    public static String getInvalidCommandSyntaxMessage() {
        return conf.getString("options.messages.invalid_command_syntax", "&6You are not allowed to use command syntax /<pluginname>:<command>!");
    }

    static long getLimitResetDelay(final String regexCommand, final Player player) {
        final long limitreset;
        String limitResetString = "";
        final String group = getCommandGroup(player);
        limitResetString = conf.getString("commands.groups." + group + "." + regexCommand + ".limit_reset_delay", "0");
        limitreset = parseTime(limitResetString);
        return limitreset;
    }

    static String getLimitResetMessage() {
        return conf.getString("options.messages.limit_reset",
                "&6Wait&e &seconds& &unit&&6 before your limit for command&e &command& &6is reset.&f");
    }

    static void clearSomething2(final String co, final String uuid, final int hashedCommand) {
        confusers.set("users." + uuid + "." + co + "." + hashedCommand, 0);
    }

    public static long getLimitResetDelayGlobal(final String command) {
        long delay = 0;
        String delayString = "";
        delayString = conf.getString("global." + command + ".limit_reset_delay", "0");
        delay = parseTime(delayString);
        return delay;
    }

    static Set<String> getLimitResetCommandsGlobal() {
        return conf.getConfigurationSection("global").getKeys(false);
    }

    private static int parseTime(final String time) {
        final String[] timeString = time.split(" ", 2);
        if (timeString[0].equals("cancel")) {
            return -65535;
        }
        final int timeNumber = Integer.valueOf(timeString[0]);
        int timeMultiplier = 1;
        if (timeString.length > 1) {
            final String timeUnit = timeString[1];
            switch (timeUnit) {
                case "minute":
                case "minutes":
                    timeMultiplier = 60;
                    break;
                case "hour":
                case "hours":
                    timeMultiplier = 60 * 60;
                    break;
                case "day":
                case "days":
                    timeMultiplier = 60 * 60 * 24;
                    break;
                case "week":
                case "weeks":
                    timeMultiplier = 60 * 60 * 24 * 7;
                    break;
                case "month":
                case "months":
                    timeMultiplier = 60 * 60 * 24 * 30;
                    break;
                default:
                    timeMultiplier = 1;
                    break;
            }
        }
        return timeNumber * timeMultiplier;
    }

    public static String getLimitResetNowMessage() {
        return conf.getString("options.messages.limit_reset_now", "&6Reseting limits for command&e &command& &6now.&f");
    }

    public static String getPermission(final Player player, final String regexCommad) {
        final String group = getCommandGroup(player);
        return conf.getString("commands.groups." + group + "." + regexCommad + ".permission");
    }

    public static String getPermissionMessage(final Player player, final String regexCommad) {
        final String group = getCommandGroup(player);
        return conf.getString("commands.groups." + group + "." + regexCommad + ".denied_message");
    }

    public static String getCancelCommandMessage() {
        return conf.getString("options.messages.confirmation_cancel_command_execution", "No");
    }

    public static String getConfirmCommandMessage() {
        return conf.getString("options.messages.confirmation_confirm_command_execution", "Yes");
    }

    public static String getCancelCommandHint() {
        return conf.getString("options.messages.confirmation_cancel_command_execution_hint", "Click to cancel");
    }

    public static String getConfirmCommandHint() {
        return conf.getString("options.messages.confirmation_confirm_command_execution_hint", "Click to confirm");
    }

    public static String getConfirmToggleMessageTrue() {
        return conf.getString("options.messages.confirmation_toggle_enable", "Confirmation messages are now enabled for you!");
    }

    public static String getConfirmToggleMessageFalse() {
        return conf.getString("options.messages.confirmation_toggle_disable", "Confirmation messages are now disabled for you!");
    }

    public static String getItsPriceMessage() {
        return conf.getString("options.messages.confirmation_price_of_command", "&6its price is&e &price& &6and you now have &e&balance&");
    }

    public static String getQuestionMessage() {
        return conf.getString("options.messages.confirmation_message", "&6Would you like to use command&e &command& &6?");
    }

    public static String getItsItemCostMessage() {
        return conf.getString("options.messages.confirmation_item_price_of_command", "&6its price is&e &itemprice& &itemname&");
    }

    public static String getItsLimitMessage() {
        return conf.getString("options.messages.confirmation_limit_of_command",
                "&6it is limited to&e &limit& &6uses and you can still use it&e &uses& &6times");
    }

    public static String getItsXpPriceMessage() {
        return conf.getString("options.messages.confirmation_xp_price_of_command", "&6its price is&e &xpprice& experience levels");
    }

    public static String getItsPlayerPointsPriceMessage() {
        return conf.getString("options.messages.confirmation_player_points_price_of_command", "&6its price is&e &ppprice& PlayerPoints &6and you now have &e&ppbalance& PlayerPoints");
    }

    public static String getCommandCanceledMessage() {
        return conf.getString("options.messages.confirmation_command_cancelled", "&6Execution of command&e &command& &6was cancelled");
    }

    public static boolean getSyntaxBlocker() {
        return conf.getBoolean("options.options.syntax_blocker_enabled", true);
    }

    public static boolean getConfirmCommandEnabled(final Player player) {
        final Boolean playersChoice = getConfirmationsPlayer(player);
        if (playersChoice != null) {
            return playersChoice;
        } else {
            return conf.getBoolean("options.options.command_confirmation", true);
        }
    }

    public static boolean getDisabledForOpsEnabled() {
        return conf.getBoolean("options.options.disabled_for_ops", true);
    }

    public static boolean getSyntaxBlockerDisabledForOpsEnabled() {
        return conf.getBoolean("options.options.disable_syntax_blocker_for_ops", true);
    }

    public static String getPlayerPointsForCommandMessage() {
        return conf.getString("options.messages.paid_player_points_for_command", "Price of &command& was %s PlayerPoints and you now have %s" +
                " PlayerPoints");
    }
}
