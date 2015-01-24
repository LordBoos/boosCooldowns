package cz.boosik.boosCooldown;

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

/**
 * T��da zaji��uj�c� ve�ker� metody, kter� se staraj� o konfiguraci pluginu a o
 * datab�zi.
 * 
 * @author Jakub Kol��
 * 
 */
public class BoosConfigManager {

	private static YamlConfiguration conf;
	private static YamlConfiguration confusers;
	private static File confFile;
	private static File confusersFile;

	/**
	 * Metoda zaji��uj�c� smaz�n� ve�ker�ch aktivn�ch cooldown a warmup �asova��
	 * v�ech hr���.
	 */
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

	/**
	 * Metoda vyma�e ve�ker� hodnoty specifick�ho hr��e ve specifick� sekci
	 * datab�ze.
	 * 
	 * @param co
	 *            sekce datab�ze (warmup, cooldown, uses)
	 * @param uuid
	 *            jm�no hr��e pro kret�ho se m� vymazat ��st datab�ze
	 */
	public static void clearSomething(String co, UUID uuid) {
		ConfigurationSection userSection = confusers
				.getConfigurationSection("users." + uuid + "." + co);
		if (userSection == null)
			return;
		confusers.set("users." + uuid + "." + co, null);
		saveConfusers();
		loadConfusers();
	}

	/**
	 * Metoda vyma�e hodnoty specifick�ho p��kazu, specifick�ho hr��e ve
	 * specifick� sekci datab�ze.
	 * 
	 * @param co
	 *            sekce datab�ze (warmup, cooldown, uses)
	 * @param uuid
	 *            jm�no hr��e pro kret�ho se m� vymazat ��st datab�ze
	 * @param command
	 *            p��kaz pro kter� se maj� vymazat hodnoty
	 */
	static void clearSomething(String co, UUID uuid, String command) {
		int pre2 = command.toLowerCase().hashCode();
		confusers.set("users." + uuid + "." + co + "." + pre2, 0);
		saveConfusers();
		loadConfusers();
	}

	/**
	 * @param message
	 * @return
	 */
	static String getAlias(String message) {
		return conf.getString("commands.aliases." + message);
	}

	/**
	 * @return
	 */
	static Set<String> getAliases() {
		Set<String> aliases = null;
		ConfigurationSection aliasesSection = conf
				.getConfigurationSection("commands.aliases");
		if (aliasesSection != null) {
			aliases = conf.getConfigurationSection("commands.aliases").getKeys(
					false);
		}
		return aliases;
	}

	/**
	 * @return
	 */
	static Set<String> getAliasesKeys() {
		Set<String> aliases = conf.getConfigurationSection("commands.aliases")
				.getKeys(true);
		return aliases;
	}

	/**
	 * @return
	 */
	static boolean getBlockInteractDuringWarmup() {
		return conf.getBoolean("options.options.block_interact_during_warmup",
				false);
	}

	/**
	 * @return
	 */
	public static String getCancelWarmupByGameModeChangeMessage() {
		return conf.getString(
				"options.messages.warmup_cancelled_by_gamemode_change",
				"&6Warm-ups have been cancelled due to changing gamemode.&f");
	}

	/**
	 * @return
	 */
	static boolean getCancelWarmUpOnDamage() {
		return conf
				.getBoolean("options.options.cancel_warmup_on_damage", false);
	}

	/**
	 * @return
	 */
	static boolean getCancelWarmUpOnGameModeChange() {
		return conf.getBoolean(
				"options.options.cancel_warmup_on_gamemode_change", false);
	}

	/**
	 * @return
	 */
	static boolean getCancelWarmupOnMove() {
		return conf.getBoolean("options.options.cancel_warmup_on_move", false);
	}

	/**
	 * @return
	 */
	static boolean getCancelWarmupOnSneak() {
		return conf.getBoolean("options.options.cancel_warmup_on_sneak", false);
	}

	/**
	 * @return
	 */
	public static String getCancelWarmupOnSneakMessage() {
		return conf.getString("options.messages.warmup_cancelled_by_sneak",
				"&6Warm-ups have been cancelled due to sneaking.&f");
	}

	/**
	 * @return
	 */
	static boolean getCancelWarmupOnSprint() {
		return conf
				.getBoolean("options.options.cancel_warmup_on_sprint", false);
	}

	/**
	 * @return
	 */
	public static String getCancelWarmupOnSprintMessage() {
		return conf.getString("options.messages.warmup_cancelled_by_sprint",
				"&6Warm-ups have been cancelled due to sprinting.&f");
	}

	/**
	 * @return
	 */
	public static String getCannotCreateSignMessage() {
		return conf.getString("options.messages.cannot_create_sign",
				"&6You are not allowed to create this kind of signs!&f");
	}

	/**
	 * @return
	 */
	public static String getCannotUseSignMessage() {
		return conf.getString("options.messages.cannot_use_sign",
				"&6You are not allowed to use this sign!&f");
	}

	/**
	 * @return
	 */
	public static boolean getCleanCooldownsOnDeath() {
		return conf.getBoolean("options.options.clear_cooldowns_on_death",
				false);
	}

	/**
	 * @return
	 */
	public static boolean getCleanUsesOnDeath() {
		return conf.getBoolean("options.options.clear_uses_on_death", false);
	}

	/**
	 * @return
	 */
	static boolean getClearOnRestart() {
		return conf.getBoolean("options.options.clear_on_restart", false);
	}

	/**
	 * @return
	 */
	static String getCommandBlockedMessage() {
		return conf.getString("options.messages.limit_achieved",
				"&6You cannot use this command anymore!&f");
	}

	/**
	 * @param player
	 * @return
	 */
	static String getCommandGroup(Player player) {
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

	/**
	 * @return
	 */
	static Set<String> getCommandGroups() {
		ConfigurationSection groupsSection = conf
				.getConfigurationSection("commands.groups");
		Set<String> groups = null;
		if (groupsSection != null) {
			groups = groupsSection.getKeys(false);
		}
		return groups;
	}

	/**
	 * @return
	 */
	static boolean getCommandLogging() {
		return conf.getBoolean("options.options.command_logging", false);
	}

	/**
	 * @param player
	 * @return
	 */
	static Set<String> getCommands(Player player) {
		String group = getCommandGroup(player);
		Set<String> commands = null;
		ConfigurationSection commandsSection = conf
				.getConfigurationSection("commands.groups." + group);
		if (commandsSection != null) {
			commands = commandsSection.getKeys(false);
		}
		return commands;
	}

	/**
	 * @param regexCommand
	 * @param player
	 * @return
	 */
	// static String[] getCommandValues(String regexCommand, Player player) {
	// String[] values;
	// String line = "";
	// String group = getCommandGroup(player);
	// line = conf.getString("commands.groups." + group + "." + regexCommand,
	// line);
	// values = line.split(",");
	// return values;
	// }

	/**
	 * @return
	 */
	static YamlConfiguration getConfusers() {
		return confusers;
	}

	/**
	 * @param regexCommand
	 * @param player
	 * @return
	 */
	static int getCoolDown(String regexCommand, Player player) {
		int coolDown;
		String coolDownString = "";
		String group = getCommandGroup(player);
		coolDownString = conf.getString("commands.groups." + group + "."
				+ regexCommand + ".cooldown", "0");
		coolDown = parseTime(coolDownString);
		return coolDown;
	}

	/**
	 * @return
	 */
	static boolean getCooldownEnabled() {
		return conf.getBoolean("options.options.cooldowns_enabled", true);
	}

	/**
	 * @return
	 */
	static String getCoolDownMessage() {
		return conf
				.getString(
						"options.messages.cooling_down",
						"&6Wait&e &seconds& seconds&6 before you can use command&e &command& &6again.&f");
	}

	/**
	 * @param player
	 * @return
	 */
	static Set<String> getCooldowns(Player player) {
		String cool = getCommandGroup(player);
		Set<String> cooldowns = conf.getConfigurationSection(
				"commands.groups." + cool).getKeys(false);
		return cooldowns;
	}

	/**
	 * @return
	 */
	static String getInsufficientFundsMessage() {
		return conf
				.getString(
						"options.messages.insufficient_funds",
						"&6You have insufficient funds!&e &command& &6costs &e%s &6but you only have &e%s");
	}

	/**
	 * @return
	 */
	public static String getInteractBlockedMessage() {
		return conf.getString(
				"options.messages.interact_blocked_during_warmup",
				"&6You can't do this when command is warming-up!&f");
	}

	/**
	 * @param regexCommand
	 * @param player
	 * @return
	 */
	static String getItemCostItem(String regexCommand, Player player) {
		String item = "";
		String temp;
		String[] command;
		String group = getCommandGroup(player);
		temp = conf.getString("commands.groups." + group + "." + regexCommand
				+ ".itemcost", "");
		command = temp.split(",");
		if (command.length == 2) {
			item = command[0];
		}
		return item;
	}

	/**
	 * @param regexCommand
	 * @param player
	 * @return
	 */
	static int getItemCostCount(String regexCommand, Player player) {
		int count = 0;
		String temp;
		String[] command;
		String group = getCommandGroup(player);
		temp = conf.getString("commands.groups." + group + "." + regexCommand
				+ ".itemcost", "");
		command = temp.split(",");
		if (command.length == 2) {
			count = Integer.valueOf(command[1]);
		}
		return count;
	}

	/**
	 * @param regexCommand
	 * @param player
	 * @return
	 */
	static int getLimit(String regexCommand, Player player) {
		int limit;
		String group = getCommandGroup(player);
		limit = conf.getInt("commands.groups." + group + "." + regexCommand
				+ ".limit", -1);
		return limit;
	}

	/**
	 * @return
	 */
	static boolean getLimitEnabled() {
		return conf.getBoolean("options.options.limits_enabled", true);
	}

	/**
	 * @return
	 */
	static String getLimitListMessage() {
		return conf
				.getString(
						"options.messages.limit_list",
						"&6Limit for command &e&command&&6 is &e&limit&&6. You can still use it &e&times&&6 times.&f");
	}

	/**
	 * @return
	 */
	static boolean getLimitsEnabled() {
		return conf.getBoolean("options.options.limits_enabled", true);
	}

	static Set<String> getAllPlayers() {
		ConfigurationSection users = confusers.getConfigurationSection("users");
		Set<String> list = users.getKeys(false);
		return list;
	}

	static List<String> getSharedCooldowns(String pre, Player player) {
		List<String> sharedCooldowns;
		String group = getCommandGroup(player);
		sharedCooldowns = conf.getStringList("commands.groups." + group + "."
				+ pre + ".shared_cooldown");
		return sharedCooldowns;
	}

	/**
	 * @param regexCommand
	 * @param player
	 * @return
	 */
	static String getMessage(String regexCommand, Player player) {
		String message = "";
		String group = getCommandGroup(player);
		message = conf.getString("commands.groups." + group + "."
				+ regexCommand + ".message", "");
		return message;
	}

	/**
	 * @return
	 */
	static String getPaidErrorMessage() {
		return conf.getString("options.messages.paid_error",
				"An error has occured: %s");
	}

	/**
	 * @return
	 */
	static String getPaidForCommandMessage() {
		return conf.getString("options.messages.paid_for_command",
				"Price of &command& was %s and you now have %s");
	}

	/**
	 * @param regexCommand
	 * @param player
	 * @return
	 */
	static String getPotionEffect(String regexCommand, Player player) {
		String effect = "";
		String temp;
		String[] command;
		String group = getCommandGroup(player);
		temp = conf.getString("commands.groups." + group + "." + regexCommand
				+ ".potion", "");
		command = temp.split(",");
		if (command.length == 2) {
			effect = command[0];
		}
		return effect;
	}

	/**
	 * @param regexCommand
	 * @param player
	 * @return
	 */
	static int getPotionEffectStrength(String regexCommand, Player player) {
		int effect = 0;
		String temp;
		String[] command;
		String group = getCommandGroup(player);
		temp = conf.getString("commands.groups." + group + "." + regexCommand
				+ ".potion", "");
		command = temp.split(",");
		if (command.length == 2) {
			effect = Integer.valueOf(command[1]);
		}
		return effect;
	}

	/**
	 * @param regexCommand
	 * @param player
	 * @return
	 */
	static double getPrice(String regexCommand, Player player) {
		double price;
		String group = getCommandGroup(player);
		price = conf.getDouble("commands.groups." + group + "." + regexCommand
				+ ".price", 0.0);
		return price;
	}

	/**
	 * @return
	 */
	static boolean getPriceEnabled() {
		return conf.getBoolean("options.options.prices_enabled", true);
	}

	/**
	 * @return
	 */
	static int getSaveInterval() {
		return conf.getInt("options.options.save_interval_in_minutes", 15);
	}

	/**
	 * @return
	 */
	static boolean getSignCommands() {
		return conf.getBoolean("options.options.command_signs", false);
	}

	/**
	 * @return
	 */
	public static boolean getStartCooldownsOnDeath() {
		return conf.getBoolean("options.options.start_cooldowns_on_death",
				false);
	}

	/**
	 * @return
	 */
	static String getUnitHoursMessage() {
		return conf.getString("options.units.hours", "hours");
	}

	/**
	 * @return
	 */
	static String getUnitMinutesMessage() {
		return conf.getString("options.units.minutes", "minutes");
	}

	/**
	 * @return
	 */
	static String getUnitSecondsMessage() {
		return conf.getString("options.units.seconds", "seconds");
	}

	/**
	 * @param regexCommand
	 * @param player
	 * @return
	 */
	static int getWarmUp(String regexCommand, Player player) {
		int warmUp;
		String warmUpString = "";
		String group = getCommandGroup(player);
		warmUpString = conf.getString("commands.groups." + group + "."
				+ regexCommand + ".warmup", "0");
		warmUp = parseTime(warmUpString);
		return warmUp;
	}

	/**
	 * @return
	 */
	static String getWarmUpAlreadyStartedMessage() {
		return conf.getString("options.messages.warmup_already_started",
				"&6Warm-Up process for&e &command& &6has already started.&f");
	}

	/**
	 * @return
	 */
	public static String getWarmUpCancelledByDamageMessage() {
		return conf.getString("options.messages.warmup_cancelled_by_damage",
				"&6Warm-ups have been cancelled due to receiving damage.&f");
	}

	/**
	 * @return
	 */
	public static String getWarmUpCancelledByMoveMessage() {
		return conf.getString("options.messages.warmup_cancelled_by_move",
				"&6Warm-ups have been cancelled due to moving.&f");
	}

	/**
	 * @return
	 */
	static boolean getWarmupEnabled() {
		return conf.getBoolean("options.options.warmups_enabled", true);
	}

	/**
	 * @return
	 */
	static String getWarmUpMessage() {
		return conf
				.getString("options.messages.warming_up",
						"&6Wait&e &seconds& seconds&6 before command&e &command& &6has warmed up.&f");
	}

	/**
	 * Metoda na�te konfigura�n� soubor z disku do pam�ti.
	 */
	static void load() {
		try {
			conf.load(confFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			BoosCoolDown.getLog().severe(
					"[boosCooldowns] Configuration file not found!");
		} catch (IOException e) {
			e.printStackTrace();
			BoosCoolDown.getLog().severe(
					"[boosCooldowns] Could not read configuration file!");
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			BoosCoolDown.getLog().severe(
					"[boosCooldowns] Configuration file is invalid!");
		}
	}

	/**
	 * Metoda na�te soubor datab�ze z disku do pam�ti.
	 */
	static void loadConfusers() {
		try {
			confusers.load(confusersFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			BoosCoolDown.getLog().severe(
					"[boosCooldowns] Storage file not found!");
		} catch (IOException e) {
			e.printStackTrace();
			BoosCoolDown.getLog().severe(
					"[boosCooldowns] Could not read storage file!");
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			BoosCoolDown.getLog().severe(
					"[boosCooldowns] Storage file is invalid!");
		}
	}

	/**
	 * Metoda znovu na�te konfigura�n� soubor z disku do pam�ti.
	 */
	static void reload() {
		conf = new YamlConfiguration();
		load();
	}

	/**
	 * Metoda ulo�� soubor datab�ze z pam�ti na disk.
	 */
	static void saveConfusers() {
		try {
			confFile.createNewFile();
			confusers.save(confusersFile);
		} catch (IOException e) {
			e.printStackTrace();
			BoosCoolDown.getLog().severe(
					"[boosCooldowns] Could not save storage file!");
		}
	}

	/**
	 * Metoda p�id�v� z�znamy do konfigura�n�ho souboru, kter� pot� ulo�� na
	 * disk a znovu jej na�te z disku do pam�ti.
	 * 
	 * @param coSetnout
	 *            n�zev skupiny pro kterou se m� p�idat z�znam do konfigura�n�ho
	 *            souboru
	 * @param co
	 *            p��kaz, pro kter� se m� p�idat hodnota do konfigura�n�ho
	 *            souboru
	 * @param hodnota
	 *            hodnota kter� se m� p�idat pro specifikovan� p��kaz
	 */
	static void setAddToConfigFile(String group, String command, String what,
			String value) {
		group = group.toLowerCase();
		command = command.toLowerCase();
		int value2;
		try {
			value2 = Integer.parseInt(value);
			reload();
			conf.set("commands.groups." + group + "." + command + "." + what,
					value2);
		} catch (NumberFormatException e1) {
			reload();
			conf.set("commands.groups." + group + "." + command + "." + what,
					value);
		}
		try {
			conf.save(confFile);
		} catch (IOException e) {
			BoosCoolDown.getLog().severe(
					"[boosCooldowns] Could not save configuration file!");

		}
		reload();
	}

	/**
	 * Metoda vytv��ej�ci konfigura�n� a datab�zov� sobour, pokud tyto soubory
	 * ji� neexistuj�. Pokud soubory ji� existuj�, jsou na�teny z disku do
	 * pam�ti.
	 * 
	 * @param boosCoolDown
	 */
	@SuppressWarnings("static-access")
	BoosConfigManager(BoosCoolDown boosCoolDown) {
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
				BoosCoolDown.getLog().severe(
						"[boosCooldowns] Could not save storage file!");
			}
		}
	}

	static boolean getAutoSave() {
		return conf.getBoolean(
				"options.options.auto_save_enabled_CAN_CAUSE_BIG_LAGS", false);
	}

	static String getPaidItemsForCommandMessage() {
		return conf.getString("options.messages.paid_items_for_command",
				"&6Price of&e &command& &6was &e%s");
	}

	static String getInsufficientItemsMessage() {
		return conf.getString("options.messages.insufficient_items",
				"&6You have not enough items!&e &command& &6needs &e%s");
	}

	static boolean getItemCostEnabled() {
		return conf.getBoolean("options.options.item_cost_enabled", true);
	}

	static String getPaidXPForCommandMessage() {
		return conf.getString("options.messages.paid_xp_for_command",
				"&6Price of&e &command& &6was &e%s");
	}

	static int getXpPrice(String regexCommand, Player player) {
		int price;
		String group = getCommandGroup(player);
		price = conf.getInt("commands.groups." + group + "." + regexCommand
				+ ".xpcost", 0);
		return price;
	}

	static boolean getXpPriceEnabled() {
		return conf.getBoolean("options.options.xp_cost_enabled", true);
	}

	static String getInsufficientXpMessage() {
		return conf.getString("options.messages.insufficient_xp",
				"&6You have not enough XP!&e &command& &6needs &e%s");
	}

	static String getInvalidCommandSyntaxMessage(Player player) {
		return conf
				.getString("options.messages.invalid_command_syntax",
						"&6You are not allowed to use command syntax /<pluginname>:<command>!");
	}

	static long getLimitResetDelay(String regexCommand, Player player) {
		long limitreset;
		String limitResetString = "";
		String group = getCommandGroup(player);
		limitResetString = conf.getString("commands.groups." + group + "."
				+ regexCommand + ".limit_reset_delay", "0");
		limitreset = parseTime(limitResetString);
		return limitreset;
	}

	static String getLimitResetMessage() {
		return conf
				.getString(
						"options.messages.limit_reset",
						"&6Wait&e &seconds& &unit&&6 before your limit for command&e &command& &6is reset.&f");
	}

	static void clearSomething2(String co, String uuid, int hashedCommand) {
		confusers.set("users." + uuid + "." + co + "." + hashedCommand, 0);
	}

	static long getLimitResetDelayGlobal(String command) {
		long delay = 0;
		String delayString = "";
		delayString = conf.getString(
				"global." + command + ".limit_reset_delay", "0");
		delay = parseTime(delayString);
		return delay;
	}

	static Set<String> getLimitResetCommandsGlobal() {
		return conf.getConfigurationSection("global").getKeys(false);
	}

	static int parseTime(String time) {
		String[] timeString = time.split(" ", 2);
		if (timeString[0].equals("cancel")) {
			return -65535;
		}
		int timeNumber = Integer.valueOf(timeString[0]);
		int timeMultiplier = 1;
		if (timeString.length > 1) {
			String timeUnit = timeString[1];
			if (timeUnit.equals("minute") || timeUnit.equals("minutes")) {
				timeMultiplier = 60;
			} else if (timeUnit.equals("hour") || timeUnit.equals("hours")) {
				timeMultiplier = 60 * 60;
			} else if (timeUnit.equals("day") || timeUnit.equals("days")) {
				timeMultiplier = 60 * 60 * 24;
			} else if (timeUnit.equals("week") || timeUnit.equals("weeks")) {
				timeMultiplier = 60 * 60 * 24 * 7;
			} else if (timeUnit.equals("month") || timeUnit.equals("months")) {
				timeMultiplier = 60 * 60 * 24 * 30;
			} else {
				timeMultiplier = 1;
			}
		}
		return timeNumber * timeMultiplier;
	}

	public static String getLimitResetNowMessage() {
		return conf.getString("options.messages.limit_reset_now",
				"&6Reseting limits for command&e &command& &6now.&f");
	}
}
