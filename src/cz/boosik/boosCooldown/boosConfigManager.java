package cz.boosik.boosCooldown;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class boosConfigManager {

	private static YamlConfiguration conf;
	private static YamlConfiguration confusers;
	private static File confFile;
	private static File confusersFile;
	static List<String> players = new LinkedList<String>();

	static void clear() {
		ConfigurationSection userSection = confusers
				.getConfigurationSection("users");
		if (userSection == null)
			return;
		for (String user : userSection.getKeys(false)) {
			// clear cooldown
			ConfigurationSection cooldown = confusers
					.getConfigurationSection("users." + user + ".cooldown");
			if (cooldown != null) {
				for (String key : cooldown.getKeys(false)) {
					confusers.set("users." + user + ".cooldown." + key, null);
				}
			}
			confusers.set("users." + user + ".cooldown", null);

			// clear warmup
			ConfigurationSection warmup = confusers
					.getConfigurationSection("users." + user + ".warmup");
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

	public static void clearSomething(String co, String player) {
		ConfigurationSection userSection = confusers
				.getConfigurationSection("users."
						+ player.toLowerCase().hashCode() + "." + co);
		if (userSection == null)
			return;
		confusers.set("users." + player.toLowerCase().hashCode() + "." + co,
				null);
		saveConfusers();
		loadConfusers();
	}

	static void clearSomething(String co, String player, String command) {
		int pre2 = command.toLowerCase().hashCode();
		confusers.set("users." + player.toLowerCase().hashCode() + "." + co
				+ "." + pre2, 0);
		saveConfusers();
		loadConfusers();
	}

	public static String getAlias(String message) {
		return conf.getString("commands.aliases." + message);
	}

	public static Set<String> getAliases() {
		Set<String> aliases = conf.getConfigurationSection("commands.aliases")
				.getKeys(false);
		return aliases;
	}

	public static boolean getBlockInteractDuringWarmup() {
		return conf.getBoolean("options.options.block_interact_during_warmup",
				false);
	}

	public static String getCancelWarmupByGameModeChangeMessage() {
		return conf.getString(
				"options.messages.warmup_cancelled_by_gamemode_change",
				"&6Warm-ups have been cancelled due to changing gamemode.&f");
	}

	public static boolean getCancelWarmUpOnDamage() {
		return conf
				.getBoolean("options.options.cancel_warmup_on_damage", false);
	}

	public static boolean getCancelWarmUpOnGameModeChange() {
		return conf.getBoolean(
				"options.options.cancel_warmup_on_gamemode_change", false);
	}

	public static boolean getCancelWarmupOnMove() {
		return conf.getBoolean("options.options.cancel_warmup_on_move", false);
	}

	public static boolean getCancelWarmupOnSneak() {
		return conf.getBoolean("options.options.cancel_warmup_on_sneak", false);
	}

	public static String getCancelWarmupOnSneakMessage() {
		return conf.getString("options.messages.warmup_cancelled_by_sneak",
				"&6Warm-ups have been cancelled due to sneaking.&f");
	}

	public static boolean getCancelWarmupOnSprint() {
		return conf
				.getBoolean("options.options.cancel_warmup_on_sprint", false);
	}

	public static String getCancelWarmupOnSprintMessage() {
		return conf.getString("options.messages.warmup_cancelled_by_sprint",
				"&6Warm-ups have been cancelled due to sprinting.&f");
	}

	public static String getCannotCreateSignMessage() {
		return conf.getString("options.messages.cannot_create_sign",
				"&6You are not allowed to create this kind of signs!&f");
	}

	public static String getCannotUseSignMessage() {
		return conf.getString("options.messages.cannot_use_sign",
				"&6You are not allowed to use this sign!&f");
	}

	public static boolean getCleanCooldownsOnDeath() {
		return conf.getBoolean("options.options.clear_cooldowns_on_death",
				false);
	}

	public static boolean getCleanUsesOnDeath() {
		return conf.getBoolean("options.options.clear_uses_on_death", false);
	}

	static boolean getClearOnRestart() {
		return conf.getBoolean("options.options.clear_on_restart", false);
	}

	public static String getCommandBlockedMessage() {
		return conf.getString("options.messages.limit_achieved",
				"&6You cannot use this command anymore!&f");
	}

	public static boolean getCommandLogging() {
		return conf.getBoolean("options.options.command_logging", false);
	}

	public static YamlConfiguration getConfusers() {
		return confusers;
	}

	static int getCoolDown(String pre, Player player) {
		int coolDown = 0;
		coolDown = Integer.parseInt(getCommandValues(pre, player)[1]);
		return coolDown;
	}

	public static boolean getCooldownEnabled() {
		return conf.getBoolean("options.options.cooldowns_enabled", true);
	}

	static String getCoolDownMessage() {
		return conf
				.getString(
						"options.messages.cooling_down",
						"&6Wait&e &seconds& seconds&6 before you can use command&e &command& &6again.&f");
	}

	public static Set<String> getCooldowns(Player player) {
		String cool = getCommandGroup(player);
		Set<String> cooldowns = conf.getConfigurationSection(
				"commands.groups." + cool).getKeys(false);
		return cooldowns;
	}

	public static String getInsufficientFundsMessage() {
		return conf
				.getString("options.messages.insufficient_funds",
						"&6You have insufficient funds!&e &command& &6costs &e%s &6but you only have &e%s");
	}

	public static String getInteractBlockedMessage() {
		return conf.getString(
				"options.messages.interact_blocked_during_warmup",
				"&6You can't do this when command is warming-up!&f");
	}

	public static int getLimit(String pre, Player player) {
		int limit = -1;
		limit = Integer.parseInt(getCommandValues(pre, player)[3]);
		return limit;
	}

	public static boolean getLimitEnabled() {
		return conf.getBoolean("options.options.limits_enabled", true);
	}

	public static String getLimitListMessage() {
		return conf
				.getString(
						"options.messages.limit_list",
						"&6Limit for command &e&command&&6 is &e&limit&&6. You can still use it &e&times&&6 times.&f");
	}

	public static Set<String> getLimits(Player player) {
		String lim = getCommandGroup(player);
		Set<String> limits = conf.getConfigurationSection(
				"commands.groups." + lim).getKeys(false);
		return limits;
	}

	public static boolean getLimitsEnabled() {
		return conf.getBoolean("options.options.limits_enabled", true);
	}

	public static String getLink(String pre) {
		String link = null;
		pre = pre.toLowerCase();
		link = conf.getString("commands.links.link." + pre, link);
		return link;
	}

	public static List<String> getLinkList(String link) {
		List<String> linkGroup;
		link = link.toLowerCase();
		linkGroup = conf.getStringList("commands.links.linkGroups." + link);
		return linkGroup;
	}

	public static String getPaidErrorMessage() {
		return conf.getString("options.messages.paid_error",
				"An error has occured: %s");
	}

	public static String getPaidForCommandMessage() {
		return conf.getString("options.messages.paid_for_command",
				"Price of &command& was %s and you now have %s");
	}

	public static String getPotionEffect(String pre, Player player) {
		String effect = "";
		pre = pre.toLowerCase();
		String[] command = getCommandValues(pre, player);
		if (command.length > 4) {
			effect = getCommandValues(pre, player)[4];
		}
		return effect;
	}

	public static int getPotionEffectStrength(String pre, Player player) {
		int effect = 0;
		pre = pre.toLowerCase();
		String[] command = getCommandValues(pre, player);
		if (command.length > 4) {
			effect = Integer.valueOf(getCommandValues(pre, player)[5]);
		}
		return effect;
	}

	public static double getPrice(String pre, Player player) {
		double price = 0.0;
		price = Double.parseDouble(getCommandValues(pre, player)[2]);
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
		return conf.getBoolean("options.options.start_cooldowns_on_death",
				false);
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

	public static int getWarmUp(String pre, Player player) {
		int warmUp = -1;
		String[] values = getCommandValues(pre, player);
		warmUp = Integer.parseInt(values[0]);
		return warmUp;
	}

	static String getWarmUpAlreadyStartedMessage() {
		return conf.getString("options.messages.warmup_already_started",
				"&6Warm-Up process for&e &command& &6has already started.&f");
	}

	public static String getWarmUpCancelledByDamageMessage() {
		return conf.getString("options.messages.warmup_cancelled_by_damage",
				"&6Warm-ups have been cancelled due to receiving damage.&f");
	}

	public static String getWarmUpCancelledByMoveMessage() {
		return conf.getString("options.messages.warmup_cancelled_by_move",
				"&6Warm-ups have been cancelled due to moving.&f");
	}

	public static boolean getWarmupEnabled() {
		return conf.getBoolean("options.options.warmups_enabled", true);
	}

	static String getWarmUpMessage() {
		return conf
				.getString("options.messages.warming_up",
						"&6Wait&e &seconds& seconds&6 before command&e &command& &6has warmed up.&f");
	}

	static void load() {
		try {
			conf.load(confFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	static void loadConfusers() {
		try {
			confusers.load(confusersFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	static void reload() {
		conf = new YamlConfiguration();
		load();
	}

	static void saveConfusers() {
		try {
			confFile.createNewFile();
			confusers.save(confusersFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void setAddToConfigFile(String coSetnout, String co, String hodnota) {
		co = co.toLowerCase();
		coSetnout = coSetnout.toLowerCase();
		reload();
		conf.set("commands.groups." + coSetnout + "." + co, hodnota);
		try {
			conf.save(confFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		reload();
	}

	// test
	@SuppressWarnings("static-access")
	public boosConfigManager(boosCoolDown boosCoolDown) {
		confFile = new File(boosCoolDown.getDataFolder(), "config.yml");
		if (confFile.exists()) {
			conf = new YamlConfiguration();
			load();
		} else {
			this.confFile = new File(boosCoolDown.getDataFolder(), "config.yml");
			this.conf = new YamlConfiguration();
			conf.options().copyDefaults(true);
			conf.addDefault("options.options.warmups_enabled", true);
			conf.addDefault("options.options.cooldowns_enabled", true);
			conf.addDefault("options.options.prices_enabled", true);
			conf.addDefault("options.options.limits_enabled", true);
			conf.addDefault("options.options.save_interval_in_minutes", 15);
			conf.addDefault("options.options.cancel_warmup_on_damage", false);
			conf.addDefault("options.options.cancel_warmup_on_move", false);
			conf.addDefault("options.options.cancel_warmup_on_sneak", false);
			conf.addDefault("options.options.cancel_warmup_on_sprint", false);
			conf.addDefault("options.options.cancel_warmup_on_gamemode_change",
					false);
			conf.addDefault("options.options.block_interact_during_warmup",
					false);
			conf.addDefault("options.options.clear_on_restart", false);
			conf.addDefault("options.options.clear_uses_on_death", false);
			conf.addDefault("options.options.clear_cooldowns_on_death", false);
			conf.addDefault("options.options.start_cooldowns_on_death", false);
			conf.addDefault("options.options.command_logging", false);
			conf.addDefault("options.options.command_signs", false);
			conf.addDefault("options.units.seconds", "seconds");
			conf.addDefault("options.units.minutes", "minutes");
			conf.addDefault("options.units.hours", "hours");
			conf.addDefault("options.messages.warmup_cancelled_by_damage",
					"&6Warm-ups have been cancelled due to receiving damage.&f");
			conf.addDefault("options.messages.warmup_cancelled_by_move",
					"&6Warm-ups have been cancelled due to moving.&f");
			conf.addDefault("options.messages.warmup_cancelled_by_sprint",
					"&6Warm-ups have been cancelled due to sprinting.&f");
			conf.addDefault("options.messages.warmup_cancelled_by_sneak",
					"&6Warm-ups have been cancelled due to sneaking.&f");
			conf.addDefault(
					"options.messages.warmup_cancelled_by_gamemode_change",
					"&6Warm-ups have been cancelled due to changing gamemode.&f");
			conf.addDefault("options.messages.cooling_down",
					"&6Wait&e &seconds& &unit&&6 before you can use command&e &command& &6again.&f");
			conf.addDefault("options.messages.warming_up",
					"&6Wait&e &seconds& &unit&&6 before command&e &command& &6has warmed up.&f");
			conf.addDefault("options.messages.warmup_already_started",
					"&6Warm-Up process for&e &command& &6has already started.&f");
			conf.addDefault("options.messages.paid_error",
					"&6An error has occured:&e %s");
			conf.addDefault(
					"options.messages.insufficient_funds",
					"&6You have insufficient funds!&e &command& &6costs &e%s &6but you only have &e%s");
			conf.addDefault("options.messages.paid_for_command",
					"&6Price of&e &command& &6was&e %s &6and you now have&e %s");
			conf.addDefault("options.messages.limit_achieved",
					"&6You cannot use this command anymore!&f");
			conf.addDefault(
					"options.messages.limit_list",
					"&6Limit for command &e&command&&6 is &e&limit&&6. You can still use it &e&times&&6 times.&f");
			conf.addDefault("options.messages.interact_blocked_during_warmup",
					"&6You can't do this when command is warming-up!&f");
			conf.addDefault("options.messages.cannot_create_sign",
					"&6You are not allowed to create this kind of signs!&f");
			conf.addDefault("options.messages.cannot_use_sign",
					"&6You are not allowed to use this sign!&f");
		}
		if (confFile.exists()) {
			try {
				conf.load(confFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		try {
			conf.addDefault("commands.groups.default.*", "1,1,0.0,-1");
			conf.addDefault("commands.groups.default./anothercommand", "0,2,0.0,-1");
			conf.addDefault("commands.groups.default./yetanothercommand", "5,0,10.0,5,WEAKNESS,3");
			conf.addDefault("commands.groups.VIP./command *", "5,30,10.0,0");
			conf.addDefault("commands.groups.VIP./anothercommand", "2,10,5.0,20");
			conf.addDefault("commands.links.link./lol", "default");
			conf.addDefault("commands.links.link./example", "default");
			conf.addDefault("commands.links.link./command", "default");
			conf.addDefault("commands.links.link./yourCommandHere",
					"yourNameHere");
			String[] def = { "/lol", "/example" };
			conf.addDefault("commands.links.linkGroups.default",
					Arrays.asList(def));
			String[] def2 = { "/yourCommandHere", "/someCommand",
					"/otherCommand" };
			conf.addDefault("commands.links.linkGroups.yourNameHere",
					Arrays.asList(def2));
			conf.addDefault("commands.aliases./newcommand", "/originalcommand");
			conf.addDefault("commands.aliases./new spawn command",
					"/original spawn command");
			conf.save(confFile);
		} catch (IOException e) {
			e.printStackTrace();
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
			}
		}
	}

	private static Set<String> getCommandGroups() {
		Set<String> groups = conf.getConfigurationSection("commands.groups")
				.getKeys(false);
		return groups;
	}

	public static String getCommandGroup(Player player) {
		String cmdGroup = "default";
		for (String group : getCommandGroups()) {
			if (player.hasPermission("booscooldowns." + group)) {
				cmdGroup = group;
			}
		}
		return cmdGroup;
	}

	public static Set<String> getCommands(Player player) {
		String group = getCommandGroup(player);
		Set<String> commands = conf.getConfigurationSection(
				"commands.groups." + group).getKeys(false);
		return commands;
	}

	public static String[] getCommandValues(String regexCommand, Player player) {
		String[] values;
		String line = "";
		String group = getCommandGroup(player);
		line = conf.getString("commands.groups." + group + "." + regexCommand,
				line);
		boosCoolDown.log.info("LINE: " + line);
		values = line.split(",");
		return values;
	}
}
