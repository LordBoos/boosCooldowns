package cz.boosik.boosCooldown;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class boosConfigManager {

	private static YamlConfiguration conf;
	private static File confFile;
	static List<String> players = new LinkedList<String>();

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
			conf.addDefault("options.options.command_logging", false);
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
			// conf.addDefault(
			// "commands.options.message_warmup_cancelled_by_death",
			// "&6Warm-ups have been cancelled due to death.&f");
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
			conf.addDefault("options.messages.limit_list",
					"&6Limit for command &e&command&&6 is &e&limit&&6. You can still use it &e&times&&6 times.&f");
			conf.addDefault("options.messages.interact_blocked_during_warmup",
					"&6You can't do this when command is warming-up!&f");
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
			conf.addDefault("commands.cooldowns.cooldown./spawn", 60);
			conf.addDefault("commands.cooldowns.cooldown./home", 30);
			conf.addDefault("commands.cooldowns.cooldown2./home", 40);
			conf.addDefault("commands.cooldowns.cooldown3./home", 90);
			conf.addDefault("commands.cooldowns.cooldown4./home", 99);
			conf.addDefault("commands.cooldowns.cooldown5./home", 542);
			conf.addDefault("commands.warmups.warmup./warp", 10);
			conf.addDefault("commands.warmups.warmup./warp list", 0);
			conf.addDefault("commands.warmups.warmup./warp arena", 60);
			conf.addDefault("commands.warmups.warmup2./home", 40);
			conf.addDefault("commands.warmups.warmup3./home", 90);
			conf.addDefault("commands.warmups.warmup4./home", 99);
			conf.addDefault("commands.warmups.warmup5./home", 542);
			conf.addDefault("commands.prices.price./spawn", 10);
			conf.addDefault("commands.prices.price./home", 20);
			conf.addDefault("commands.prices.price2./home", 40);
			conf.addDefault("commands.prices.price3./home", 90);
			conf.addDefault("commands.prices.price4./home", 99);
			conf.addDefault("commands.prices.price5./home", 542);
			conf.addDefault("commands.limits.limit./example", 0);
			conf.addDefault("commands.limits.limit2./example", 100);
			conf.addDefault("commands.limits.limit3./command", 50);
			conf.addDefault("commands.limits.limit4./command", 11);
			conf.addDefault("commands.limits.limit5./lol", 2);
			conf.addDefault("commands.links.link./lol", "default");
			conf.addDefault("commands.links.link./home", "default");
			conf.addDefault("commands.links.link./warp", "default");
			conf.addDefault("commands.links.link./yourCommandHere",
					"yourNameHere");
			String[] def = { "/home", "/lol", "/warp" };
			conf.addDefault("commands.links.linkGroups.default",
					Arrays.asList(def));
			String[] def2 = { "/yourCommandHere", "/someCommand",
					"/otherCommand" };
			conf.addDefault("commands.links.linkGroups.yourNameHere",
					Arrays.asList(def2));
			conf.addDefault("commands.aliasses./home", "/warp home");
			conf.addDefault("commands.aliasses./spawn", "/mv spawn");
			conf.save(confFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	static int getCoolDown(String pre) {
		int coolDown = 0;
		pre = pre.toLowerCase();
		coolDown = conf.getInt("commands.cooldowns.cooldown." + pre, coolDown);
		return coolDown;
	}

	public static int getCoolDown2(String pre) {
		int coolDown = 0;
		pre = pre.toLowerCase();
		coolDown = conf.getInt("commands.cooldowns.cooldown2." + pre, coolDown);
		return coolDown;
	}

	public static int getCoolDown3(String pre) {
		int coolDown = 0;
		pre = pre.toLowerCase();
		coolDown = conf.getInt("commands.cooldowns.cooldown3." + pre, coolDown);
		return coolDown;
	}

	public static int getCoolDown4(String pre) {
		int coolDown = 0;
		pre = pre.toLowerCase();
		coolDown = conf.getInt("commands.cooldowns.cooldown4." + pre, coolDown);
		return coolDown;
	}

	public static int getCoolDown5(String pre) {
		int coolDown = 0;
		pre = pre.toLowerCase();
		coolDown = conf.getInt("commands.cooldowns.cooldown5." + pre, coolDown);
		return coolDown;
	}
	
	static void setAddToConfigFile(String coSetnout, String co, int hodnota) {
		co = co.toLowerCase();
		coSetnout = coSetnout.toLowerCase();
		String sekce = null;
		if (coSetnout.contains("cooldown")){
			sekce = "cooldowns";
		} else if (coSetnout.contains("warmup")){
			sekce = "warmups";
		} else if (coSetnout.contains("limit")){
			sekce = "limits";
		} else if (coSetnout.contains("price")){
			sekce = "prices";
		} else {
			return;
		}
		reload();
		conf.set("commands."+sekce+"."+coSetnout+"." + co, hodnota);
		try {
			conf.save(confFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reload();
	}

	public static int getWarmUp(String pre) {
		int warmUp = -1;
		pre = pre.toLowerCase();
		warmUp = conf.getInt("commands.warmups.warmup." + pre, warmUp);
		return warmUp;
	}

	public static int getWarmUp2(String pre) {
		int warmUp = -1;
		pre = pre.toLowerCase();
		warmUp = conf.getInt("commands.warmups.warmup2." + pre, warmUp);
		return warmUp;
	}

	public static int getWarmUp3(String pre) {
		int warmUp = -1;
		pre = pre.toLowerCase();
		warmUp = conf.getInt("commands.warmups.warmup3." + pre, warmUp);
		return warmUp;
	}

	public static int getWarmUp4(String pre) {
		int warmUp = -1;
		pre = pre.toLowerCase();
		warmUp = conf.getInt("commands.warmups.warmup4." + pre, warmUp);
		return warmUp;
	}

	public static int getWarmUp5(String pre) {
		int warmUp = -1;
		pre = pre.toLowerCase();
		warmUp = conf.getInt("commands.warmups.warmup5." + pre, warmUp);
		return warmUp;
	}

	public static int getPrice(String pre) {
		int price = 0;
		pre = pre.toLowerCase();
		price = conf.getInt("commands.prices.price." + pre, price);
		return price;
	}

	public static int getPrice2(String pre) {
		int price = 0;
		pre = pre.toLowerCase();
		price = conf.getInt("commands.prices.price2." + pre, price);
		return price;
	}

	public static int getPrice3(String pre) {
		int price = 0;
		pre = pre.toLowerCase();
		price = conf.getInt("commands.prices.price3." + pre, price);
		return price;
	}

	public static int getPrice4(String pre) {
		int price = 0;
		pre = pre.toLowerCase();
		price = conf.getInt("commands.prices.price4." + pre, price);
		return price;
	}

	public static int getPrice5(String pre) {
		int price = 0;
		pre = pre.toLowerCase();
		price = conf.getInt("commands.prices.price5." + pre, price);
		return price;
	}

	static String getCoolDownMessage() {
		return conf
				.getString(
						"options.messages.cooling_down",
						"&6Wait&e &seconds& seconds&6 before you can use command&e &command& &6again.&f");
	}

	static String getWarmUpCancelledByMoveMessage() {
		return conf.getString("options.messages.warmup_cancelled_by_move",
				"&6Warm-ups have been cancelled due to moving.&f");
	}

	static String getWarmUpCancelledByDamageMessage() {
		return conf.getString("options.messages.warmup_cancelled_by_damage",
				"&6Warm-ups have been cancelled due to receiving damage.&f");
	}

	static String getWarmUpMessage() {
		return conf
				.getString("options.messages.warming_up",
						"&6Wait&e &seconds& seconds&6 before command&e &command& &6has warmed up.&f");
	}

	static String getWarmUpAlreadyStartedMessage() {
		return conf.getString("options.messages.warmup_already_started",
				"&6Warm-Up process for&e &command& &6has already started.&f");
	}

	public static String getPaidForCommandMessage() {
		return conf.getString("options.messages.paid_for_command",
				"Price of &command& was %s and you now have %s");
	}

	public static String getPaidErrorMessage() {
		return conf.getString("options.messages.paid_error",
				"An error has occured: %s");
	}

	public static String getCancelWarmupOnSneakMessage() {
		return conf.getString("options.messages.warmup_cancelled_by_sneak",
				"&6Warm-ups have been cancelled due to sneaking.&f");
	}

	public static String getCancelWarmupOnSprintMessage() {
		return conf.getString("options.messages.warmup_cancelled_by_sprint",
				"&6Warm-ups have been cancelled due to sprinting.&f");
	}

	public static String getCommandBlockedMessage() {
		return conf.getString("options.messages.limit_achieved",
				"&6You cannot use this command anymore!&f");
	}

	static String getUnitSecondsMessage() {
		return conf.getString("options.units.seconds", "seconds");
	}

	static String getUnitMinutesMessage() {
		return conf.getString("options.units.minutes", "minutes");
	}

	static String getUnitHoursMessage() {
		return conf.getString("options.units.hours", "hours");
	}

	static boolean getClearOnRestart() {
		return conf.getBoolean("options.options.clear_on_restart", false);
	}

	public static boolean getCancelWarmUpOnDamage() {
		return conf
				.getBoolean("options.options.cancel_warmup_on_damage", false);
	}

	public static boolean getCancelWarmupOnMove() {
		return conf.getBoolean("options.options.cancel_warmup_on_move", false);
	}

	public static boolean getCancelWarmupOnSprint() {
		return conf
				.getBoolean("options.options.cancel_warmup_on_sprint", false);
	}

	public static boolean getCancelWarmupOnSneak() {
		return conf.getBoolean("options.options.cancel_warmup_on_sneak", false);
	}

	public static boolean getCommandLogging() {
		return conf.getBoolean("options.options.command_logging", false);
	}

	public static int getLimit2(String pre) {
		int limit = -1;
		pre = pre.toLowerCase();
		limit = conf.getInt("commands.limits.limit2." + pre, limit);
		return limit;
	}

	public static int getLimit3(String pre) {
		int limit = -1;
		pre = pre.toLowerCase();
		limit = conf.getInt("commands.limits.limit3." + pre, limit);
		return limit;
	}

	public static int getLimit4(String pre) {
		int limit = -1;
		pre = pre.toLowerCase();
		limit = conf.getInt("commands.limits.limit4." + pre, limit);
		return limit;
	}

	public static int getLimit5(String pre) {
		int limit = -1;
		pre = pre.toLowerCase();
		limit = conf.getInt("commands.limits.limit5." + pre, limit);
		return limit;
	}

	public static int getLimit(String pre) {
		int limit = -1;
		pre = pre.toLowerCase();
		limit = conf.getInt("commands.limits.limit." + pre, limit);
		return limit;
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

	public static boolean getCancelWarmUpOnGameModeChange() {
		return conf.getBoolean(
				"options.options.cancel_warmup_on_gamemode_change", false);
	}

	public static String getCancelWarmupByGameModeChangeMessage() {
		return conf.getString(
				"options.messages.warmup_cancelled_by_gamemode_change",
				"&6Warm-ups have been cancelled due to changing gamemode.&f");
	}

	public static boolean getBlockInteractDuringWarmup() {
		return conf.getBoolean("options.options.block_interact_during_warmup",
				false);
	}

	public static String getInteractBlockedMessage() {
		return conf.getString(
				"options.messages.interact_blocked_during_warmup",
				"&6You can't do this when command is warming-up!&f");
	}

	public static boolean getCleanUsesOnDeath() {
		return conf.getBoolean("options.options.clear_uses_on_death", false);
	}

	public static boolean getCleanCooldownsOnDeath() {
		return conf.getBoolean("options.options.clear_cooldowns_on_death",
				false);
	}
	
	public static String getLimitListMessage(){
		return conf.getString("options.messages.limit_list",
			"&6Limit for command &e&command&&6 is &e&limit&&6. You can still use it &e&times&&6 times.&f");
	}
	
	public static String getLimGrp(Player player){
	String lim;
		if (player.hasPermission(
				"booscooldowns.limit2")) {
			lim = "limit2";
		} else if (player.hasPermission(
				"booscooldowns.limit3")) {
			lim = "limit3";
		} else if (player.hasPermission(
				"booscooldowns.limit4")) {
			lim = "limit4";
		} else if (player.hasPermission(
				"booscooldowns.limit5")) {
			lim = "limit5";
		} else {
			lim = "limit";
		}
	return lim;
	}
	
	public static ConfigurationSection getLimits(Player player){
		String lim = getLimGrp(player);
	ConfigurationSection uses = conf
			.getConfigurationSection("commands.limits." + lim);
	return uses;
	}
	
	public static ConfigurationSection getAliasses(){
		ConfigurationSection aliasses = conf
				.getConfigurationSection("commands.aliasses");
		return aliasses;
	}
	
	public static String getAliass(String message){
		return conf.getString("commands.aliasses." + message);
	}
}
