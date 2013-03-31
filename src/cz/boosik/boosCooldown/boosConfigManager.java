package cz.boosik.boosCooldown;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class boosConfigManager {

	private static YamlConfiguration conf;
	private static File confFile;
	static List<String> players = new LinkedList<String>();

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

	static int getCoolDown(String pre, Player player) {
		int coolDown = 0;
		String group = getCoolGrp(player);
		pre = pre.toLowerCase();
		coolDown = conf.getInt("commands.cooldowns." + group + "." + pre,
				coolDown);
		return coolDown;
	}

	public static boolean getCooldownEnabled() {
		return conf.getBoolean("options.options.cooldowns_enabled", true);
	}

	private static Set<String> getCooldownGroups() {
		Set<String> groups = conf.getConfigurationSection("commands.cooldowns")
				.getKeys(false);
		return groups;
	}

	static String getCoolDownMessage() {
		return conf
				.getString(
						"options.messages.cooling_down",
						"&6Wait&e &seconds& seconds&6 before you can use command&e &command& &6again.&f");
	}

	public static Set<String> getCooldowns(Player player) {
		String cool = getCoolGrp(player);
		Set<String> cooldowns = conf.getConfigurationSection(
				"commands.cooldowns." + cool).getKeys(false);
		return cooldowns;
	}

	private static String getCoolGrp(Player player) {
		String cool = "cooldown";
		for (String group : getCooldownGroups()) {
			if (player.hasPermission("booscooldowns." + group)) {
				cool = group;
			}
		}
		return cool;
	}

	public static String getInteractBlockedMessage() {
		return conf.getString(
				"options.messages.interact_blocked_during_warmup",
				"&6You can't do this when command is warming-up!&f");
	}

	public static String getLimGrp(Player player) {
		String lim = "limit";
		for (String group : getLimitGroups()) {
			if (player.hasPermission("booscooldowns." + group)) {
				lim = group;
			}
		}
		return lim;
	}

	public static int getLimit(String pre, Player player) {
		int limit = -1;
		String group = getLimGrp(player);
		pre = pre.toLowerCase();
		limit = conf.getInt("commands.limits." + group + "." + pre, limit);
		return limit;
	}

	public static boolean getLimitEnabled() {
		return conf.getBoolean("options.options.limits_enabled", true);
	}

	private static Set<String> getLimitGroups() {
		Set<String> groups = conf.getConfigurationSection("commands.limits")
				.getKeys(false);
		return groups;
	}

	public static String getLimitListMessage() {
		return conf
				.getString(
						"options.messages.limit_list",
						"&6Limit for command &e&command&&6 is &e&limit&&6. You can still use it &e&times&&6 times.&f");
	}

	public static Set<String> getLimits(Player player) {
		String lim = getLimGrp(player);
		Set<String> limits = conf.getConfigurationSection(
				"commands.limits." + lim).getKeys(false);
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

	public static String getPotionEffect(String pre) {
		String effect = null;
		pre = pre.toLowerCase();
		effect = conf.getString("commands.warmupPotionEffects.effect." + pre,
				effect);
		return effect;
	}

	public static double getPrice(String pre, Player player) {
		double price = 0.0;
		String group = getPriceGrp(player);
		pre = pre.toLowerCase();
		price = conf.getDouble("commands.prices." + group + "." + pre, price);
		return price;
	}

	public static boolean getPriceEnabled() {
		return conf.getBoolean("options.options.prices_enabled", true);
	}

	private static Set<String> getPriceGroups() {
		Set<String> groups = conf.getConfigurationSection("commands.prices")
				.getKeys(false);
		return groups;
	}

	private static String getPriceGrp(Player player) {
		String price = "price";
		for (String group : getPriceGroups()) {
			if (player.hasPermission("booscooldowns." + group)) {
				price = group;
			}
		}
		return price;
	}

	public static Set<String> getPrices(Player player) {
		String price = getPriceGrp(player);
		Set<String> prices = conf.getConfigurationSection(
				"commands.prices." + price).getKeys(false);
		return prices;
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

	public static String getWarmGrp(Player player) {
		String warm = "warmup";
		for (String group : getWarmupGroups()) {
			if (player.hasPermission("booscooldowns." + group)) {
				warm = group;
			}
		}
		return warm;
	}

	public static int getWarmUp(String pre, Player player) {
		int warmUp = -1;
		String group = getWarmGrp(player);
		pre = pre.toLowerCase();
		warmUp = conf.getInt("commands.warmups." + group + "." + pre, warmUp);
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

	public static Set<String> getWarmupGroups() {
		Set<String> groups = conf.getConfigurationSection("commands.warmups")
				.getKeys(false);
		return groups;
	}

	static String getWarmUpMessage() {
		return conf
				.getString("options.messages.warming_up",
						"&6Wait&e &seconds& seconds&6 before command&e &command& &6has warmed up.&f");
	}

	public static Set<String> getWarmups(Player player) {
		String warm = getWarmGrp(player);
		Set<String> warmups = conf.getConfigurationSection(
				"commands.warmups." + warm).getKeys(false);
		return warmups;
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

	static void reload() {
		conf = new YamlConfiguration();
		load();
	}

	static void setAddToConfigFile(String coSetnout, String co, int hodnota) {
		co = co.toLowerCase();
		coSetnout = coSetnout.toLowerCase();
		String sekce = null;
		if (coSetnout.contains("cooldown")) {
			sekce = "cooldowns";
		} else if (coSetnout.contains("warmup")) {
			sekce = "warmups";
		} else if (coSetnout.contains("limit")) {
			sekce = "limits";
		} else if (coSetnout.contains("price")) {
			sekce = "prices";
		} else {
			return;
		}
		reload();
		conf.set("commands." + sekce + "." + coSetnout + "." + co, hodnota);
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
			try {
				conf.load(confFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
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
			conf.addDefault("commands.cooldowns.cooldown./command", 60);
			conf.addDefault("commands.cooldowns.cooldown./anotherCommand *", 30);
			conf.addDefault("commands.cooldowns.VIP./home", 40);
			conf.addDefault("commands.cooldowns.Premium./home", 90);
			conf.addDefault("commands.cooldowns.Donator./home", 99);
			conf.addDefault("commands.cooldowns.something./home", 542);
			conf.addDefault("commands.warmups.warmup.'*'", 1);
			conf.addDefault("commands.warmups.warmup./anotherCommand *", 0);
			conf.addDefault("commands.warmups.Donor./home", 40);
			conf.addDefault("commands.warmups.example./home", 90);
			conf.addDefault("commands.warmupPotionEffects.effect./home",
					"WEAKNESS@3");
			conf.addDefault(
					"commands.warmupPotionEffects.howto1",
					"#You can use CONFUSION, DAMAGE_RESISTANCE, FAST_DIGGING, FIRE_RESISTANCE, HARM, HEAL, HUNGER, INCREASE_DAMAGE, INVISIBILITY, JUMP, NIGHT_VISION, POISON, REGENERATION, SLOW, SLOW_DIGGING, SPEED, WATER_BREATHING, WEAKNESS, WITHER");
			conf.addDefault(
					"commands.warmupPotionEffects.howto2",
					"#After effect add @number, for example WEAKNESS@3 will apply weakness III to player for the duration of warmup.");
			conf.addDefault("commands.prices.price./command *", 10.0);
			conf.addDefault("commands.prices.price./anotherCommand", 20.0);
			conf.addDefault("commands.prices.yourGroup./home", 40.0);
			conf.addDefault("commands.limits.limit./command *", 0);
			conf.addDefault("commands.limits.limit2./lol", 100);
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
	}
}
