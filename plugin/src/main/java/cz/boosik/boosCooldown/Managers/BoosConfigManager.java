package cz.boosik.boosCooldown.Managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import cz.boosik.boosCooldown.BoosCoolDown;
import util.boosChat;

public class BoosConfigManager {

    private static YamlConfiguration conf;
    private static YamlConfiguration confusers;
    private static File confFile;
    private static File confusersFile;

    @SuppressWarnings("static-access")
    public BoosConfigManager(BoosCoolDown boosCoolDown) {
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
            } catch (IOException e) {
                e.printStackTrace();
                BoosCoolDown.getLog().severe("[boosCooldowns] Could not save storage file!");
            }
        }
    }

    public static void clear() {
        ConfigurationSection userSection = confusers.getConfigurationSection("users");
        if (userSection == null) {
            return;
        }
        for (String user : userSection.getKeys(false)) {
            ConfigurationSection cooldown = confusers.getConfigurationSection("users." + user + ".cooldown");
            if (cooldown != null) {
                for (String key : cooldown.getKeys(false)) {
                    confusers.set("users." + user + ".cooldown." + key, null);
                }
            }
            confusers.set("users." + user + ".cooldown", null);

            ConfigurationSection warmup = confusers.getConfigurationSection("users." + user + ".warmup");
            if (warmup != null) {
                for (String key : warmup.getKeys(false)) {
                    confusers.set("users." + user + ".warmup." + key, null);
                }
            }
            confusers.set("users." + user + ".warmup", null);

            confusers.set("users." + user, null);
        }
        saveConfusers();
        loadConfusers();
    }

    public static void clearSomething(String co, UUID uuid) {
        ConfigurationSection userSection = confusers.getConfigurationSection("users." + uuid + "." + co);
        if (userSection == null) {
            return;
        }
        confusers.set("users." + uuid + "." + co, null);
        saveConfusers();
        loadConfusers();
    }

    public static void clearSomething(String co, UUID uuid, String command) {
        int pre2 = command.toLowerCase().hashCode();
        confusers.set("users." + uuid + "." + co + "." + pre2, 0);
        saveConfusers();
        loadConfusers();
    }

    static String getAlias(String message) {
        return conf.getString("commands.aliases." + message);
    }

    public static Set<String> getAliases() {
        Set<String> aliases = null;
        ConfigurationSection aliasesSection = conf.getConfigurationSection("commands.aliases");
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

    static String getCommandBlockedMessage() {
        return conf.getString("options.messages.limit_achieved", "&6You cannot use this command anymore!&f");
    }

    private static String getCommandGroup(Player player) {
        String cmdGroup = "default";
        Set<String> groups = getCommandGroups();
        if (groups != null) {
            for (String group : groups) {
                if (player.hasPermission("booscooldowns." + group)) {
                    cmdGroup = group;
                }
            }
        }
        return cmdGroup;
    }

    private static Set<String> getCommandGroups() {
        ConfigurationSection groupsSection = conf.getConfigurationSection("commands.groups");
        Set<String> groups = null;
        if (groupsSection != null) {
            groups = groupsSection.getKeys(false);
        }
        return groups;
    }

    public static boolean getCommandLogging() {
        return conf.getBoolean("options.options.command_logging", false);
    }

    public static Set<String> getCommands(Player player) {
        String group = getCommandGroup(player);
        Set<String> commands = null;
        ConfigurationSection commandsSection = conf.getConfigurationSection("commands.groups." + group);
        if (commandsSection != null) {
            commands = commandsSection.getKeys(false);
        }
        return commands;
    }

    public static YamlConfiguration getConfusers() {
        return confusers;
    }

    public static int getCoolDown(String regexCommand, Player player) {
        int coolDown;
        String coolDownString = "";
        String group = getCommandGroup(player);
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

    static Set<String> getCooldowns(Player player) {
        String cool = getCommandGroup(player);
        return conf.getConfigurationSection("commands.groups." + cool).getKeys(false);
    }

    public static String getInsufficientFundsMessage() {
        return conf.getString("options.messages.insufficient_funds",
                "&6You have insufficient funds!&e &command& &6costs &e%s &6but you only have &e%s");
    }

    public static String getInteractBlockedMessage() {
        return conf.getString("options.messages.interact_blocked_during_warmup", "&6You can't do this when command is warming-up!&f");
    }

    public static List<String> getItemCostLore(String regexCommand, Player player) {
        return conf.getStringList("commands.groups." + getCommandGroup(player) + "." + regexCommand + ".itemcost.lore");
    }

    public static List<String> getItemCostEnchants(String regexCommand, Player player) {
        return conf.getStringList("commands.groups." + getCommandGroup(player) + "." + regexCommand + ".itemcost.enchants");
    }

    public static String getItemCostName(String regexCommand, Player player) {
        return conf.getString("commands.groups." + getCommandGroup(player) + "." + regexCommand + ".itemcost.name", "");
    }

    public static String getItemCostItem(String regexCommand, Player player) {
        return conf.getString("commands.groups." + getCommandGroup(player) + "." + regexCommand + ".itemcost.item", "");
    }

    public static int getItemCostCount(String regexCommand, Player player) {
        return conf.getInt("commands.groups." + getCommandGroup(player) + "." + regexCommand + ".itemcost.count", 0);
    }

    public static int getLimit(String regexCommand, Player player) {
        int limit;
        String group = getCommandGroup(player);
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
        ConfigurationSection users = confusers.getConfigurationSection("users");
        return users.getKeys(false);
    }

    static List<String> getSharedCooldowns(String pre, Player player) {
        List<String> sharedCooldowns;
        String group = getCommandGroup(player);
        sharedCooldowns = conf.getStringList("commands.groups." + group + "." + pre + ".shared_cooldown");
        return sharedCooldowns;
    }

    public static List<String> getSharedLimits(String pre, Player player) {
        List<String> sharedLimits;
        String group = getCommandGroup(player);
        sharedLimits = conf.getStringList("commands.groups." + group + "." + pre + ".shared_limit");
        return sharedLimits;
    }

    public static String getMessage(String regexCommand, Player player) {
        String message = "";
        String group = getCommandGroup(player);
        message = conf.getString("commands.groups." + group + "." + regexCommand + ".message", "");
        return message;
    }

    static String getPaidErrorMessage() {
        return conf.getString("options.messages.paid_error", "An error has occured: %s");
    }

    static String getPaidForCommandMessage() {
        return conf.getString("options.messages.paid_for_command", "Price of &command& was %s and you now have %s");
    }

    static String getPotionEffect(String regexCommand, Player player) {
        String effect = "";
        String temp;
        String[] command;
        String group = getCommandGroup(player);
        temp = conf.getString("commands.groups." + group + "." + regexCommand + ".potion", "");
        command = temp.split(",");
        if (command.length == 2) {
            effect = command[0];
        }
        return effect;
    }

    static int getPotionEffectStrength(String regexCommand, Player player) {
        int effect = 0;
        String temp;
        String[] command;
        String group = getCommandGroup(player);
        temp = conf.getString("commands.groups." + group + "." + regexCommand + ".potion", "");
        command = temp.split(",");
        if (command.length == 2) {
            effect = Integer.valueOf(command[1]);
        }
        return effect;
    }

    public static double getPrice(String regexCommand, Player player) {
        double price;
        String group = getCommandGroup(player);
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

    public static int getWarmUp(String regexCommand, Player player) {
        int warmUp;
        String warmUpString = "";
        String group = getCommandGroup(player);
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            BoosCoolDown.getLog().severe("[boosCooldowns] Configuration file not found!");
        } catch (IOException e) {
            e.printStackTrace();
            BoosCoolDown.getLog().severe("[boosCooldowns] Could not read configuration file!");
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            BoosCoolDown.getLog().severe("[boosCooldowns] Configuration file is invalid!");
        }
    }

    public static void loadConfusers() {
        try {
            confusers.load(confusersFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            BoosCoolDown.getLog().severe("[boosCooldowns] Storage file not found!");
        } catch (IOException e) {
            e.printStackTrace();
            BoosCoolDown.getLog().severe("[boosCooldowns] Could not read storage file!");
        } catch (InvalidConfigurationException e) {
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
        } catch (IOException e) {
            e.printStackTrace();
            BoosCoolDown.getLog().severe("[boosCooldowns] Could not save storage file!");
        }
    }

    public static void setAddToConfigFile(String group, String command, String what, String value) {
        group = group.toLowerCase();
        command = command.toLowerCase();
        int value2;
        try {
            value2 = Integer.parseInt(value);
            reload();
            conf.set("commands.groups." + group + "." + command + "." + what, value2);
        } catch (NumberFormatException e1) {
            reload();
            conf.set("commands.groups." + group + "." + command + "." + what, value);
        }
        try {
            conf.save(confFile);
        } catch (IOException e) {
            BoosCoolDown.getLog().severe("[boosCooldowns] Could not save configuration file!");

        }
        reload();
    }

    public static void toggleConfirmations(Player player) {
        Boolean def = confusers.getBoolean("users." + player.getUniqueId() + ".confirmations", getConfirmCommandEnabled(player));
        confusers.set("users." + player.getUniqueId() + ".confirmations", !def);
        if (def) {
            boosChat.sendMessageToPlayer(player, "&6[boosCooldowns]&e " + getConfirmToggleMessageFalse());
        } else {
            boosChat.sendMessageToPlayer(player, "&6[boosCooldowns]&e " + getConfirmToggleMessageTrue());
        }
        saveConfusers();
        loadConfusers();
    }

    public static Boolean getConfirmationsPlayer(Player player) {
        return (Boolean) confusers.get("users." + player.getUniqueId() + ".confirmations", null);
    }

    public static boolean getAutoSave() {
        return conf.getBoolean("options.options.auto_save_enabled_CAN_CAUSE_BIG_LAGS", false);
    }

    static String getPaidItemsForCommandMessage() {
        return conf.getString("options.messages.paid_items_for_command", "&6Price of&e &command& &6was &e%s");
    }

    public static String getInsufficientItemsMessage() {
        return conf.getString("options.messages.insufficient_items", "&6You have not enough items!&e &command& &6needs &e%s");
    }

    public static boolean getItemCostEnabled() {
        return conf.getBoolean("options.options.item_cost_enabled", true);
    }

    static String getPaidXPForCommandMessage() {
        return conf.getString("options.messages.paid_xp_for_command", "&6Price of&e &command& &6was &e%s");
    }

    public static int getXpPrice(String regexCommand, Player player) {
        int price;
        String group = getCommandGroup(player);
        price = conf.getInt("commands.groups." + group + "." + regexCommand + ".xpcost", 0);
        return price;
    }

    public static int getXpRequirement(String regexCommand, Player player) {
        int price;
        String group = getCommandGroup(player);
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

    public static String getInvalidCommandSyntaxMessage() {
        return conf.getString("options.messages.invalid_command_syntax", "&6You are not allowed to use command syntax /<pluginname>:<command>!");
    }

    static long getLimitResetDelay(String regexCommand, Player player) {
        long limitreset;
        String limitResetString = "";
        String group = getCommandGroup(player);
        limitResetString = conf.getString("commands.groups." + group + "." + regexCommand + ".limit_reset_delay", "0");
        limitreset = parseTime(limitResetString);
        return limitreset;
    }

    static String getLimitResetMessage() {
        return conf.getString("options.messages.limit_reset",
                "&6Wait&e &seconds& &unit&&6 before your limit for command&e &command& &6is reset.&f");
    }

    static void clearSomething2(String co, String uuid, int hashedCommand) {
        confusers.set("users." + uuid + "." + co + "." + hashedCommand, 0);
    }

    public static long getLimitResetDelayGlobal(String command) {
        long delay = 0;
        String delayString = "";
        delayString = conf.getString("global." + command + ".limit_reset_delay", "0");
        delay = parseTime(delayString);
        return delay;
    }

    static Set<String> getLimitResetCommandsGlobal() {
        return conf.getConfigurationSection("global").getKeys(false);
    }

    private static int parseTime(String time) {
        String[] timeString = time.split(" ", 2);
        if (timeString[0].equals("cancel")) {
            return -65535;
        }
        int timeNumber = Integer.valueOf(timeString[0]);
        int timeMultiplier = 1;
        if (timeString.length > 1) {
            String timeUnit = timeString[1];
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

    public static String getPermission(Player player, String regexCommad) {
        String group = getCommandGroup(player);
        return conf.getString("commands.groups." + group + "." + regexCommad + ".permission");
    }

    public static String getPermissionMessage(Player player, String regexCommad) {
        String group = getCommandGroup(player);
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

    public static String getCommandCanceledMessage() {
        return conf.getString("options.messages.confirmation_command_cancelled", "&6Execution of command&e &command& &6was cancelled");
    }

    public static boolean getSyntaxBlocker() {
        return conf.getBoolean("options.options.syntax_blocker_enabled", true);
    }

    public static boolean getConfirmCommandEnabled(Player player) {
        Boolean playersChoice = getConfirmationsPlayer(player);
        if (playersChoice != null) {
            return playersChoice;
        } else {
            return conf.getBoolean("options.options.command_confirmation", true);
        }
    }
}
