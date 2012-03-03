package cz.boosik.boosCooldown;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class boosConfigManager {

	private static YamlConfiguration conf;
	private static File confFile;
	static List<String> players = new LinkedList<String>();
	
	public boosConfigManager(boosCoolDown boosCoolDown) {
		confFile = new File(boosCoolDown.getDataFolder(), "config.yml");
		conf = new YamlConfiguration();
		conf.options().copyDefaults(true);
		conf.addDefault("commands.options.cancel_warmup_on_damage", false);
		conf.addDefault("commands.options.cancel_warmup_on_move", false);
		conf.addDefault("commands.options.cancel_warmup_on_sneak", false);
		conf.addDefault("commands.options.cancel_warmup_on_sprint", false);
		conf.addDefault("commands.options.clear_on_restart", false);
		conf.addDefault("commands.options.unit_seconds", "seconds");
		conf.addDefault("commands.options.unit_minutes", "minutes");
		conf.addDefault("commands.options.unit_hours", "hours");
		conf.addDefault(
				"commands.options.message_warmup_cancelled_by_damage",
				"&6Warm-ups have been cancelled due to receiving damage.&f");
		conf.addDefault(
				"commands.options.message_warmup_cancelled_by_move",
				"&6Warm-ups have been cancelled due to moving.&f");
		conf.addDefault(
				"commands.options.message_warmup_cancelled_by_sprint",
				"&6Warm-ups have been cancelled due to sprinting.&f");
		conf.addDefault(
				"commands.options.message_warmup_cancelled_by_sneak",
				"&6Warm-ups have been cancelled due to sneaking.&f");
//		conf.addDefault(
//				"commands.options.message_warmup_cancelled_by_death",
//				"&6Warm-ups have been cancelled due to death.&f");
		conf.addDefault("commands.options.message_cooldown",
				"&6Wait&e &seconds& &unit&&6 before you can use command&e &command& &6again.&f");
		conf.addDefault("commands.options.message_warmup",
				"&6Wait&e &seconds& &unit&&6 before command&e &command& &6has warmed up.&f");
		conf.addDefault("commands.options.message_warmup_alreadystarted",
				"&6Warm-Up process for&e &command& &6has already started.&f");
		conf.addDefault("commands.options.paid_error",
				"&6An error has occured:&e %s");
		conf.addDefault("commands.options.paid_for_command_message",
				"&6Price of&e &command& &6was&e %s &6and you now have&e %s");

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
			conf.addDefault("commands.warmups.warmup./give", 60);
			conf.addDefault("commands.warmups.warmup./home", 20);
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
		load();
	}

	static int getCoolDown(Player player, String pre) {
		int coolDown = 0;
		pre = pre.toLowerCase();
		coolDown = conf.getInt("commands.cooldowns.cooldown." + pre, coolDown);
		return coolDown;
	}
	
	public static int getCoolDown2(Player player, String pre) {
		int coolDown = 0;
		pre = pre.toLowerCase();
		coolDown = conf.getInt("commands.cooldowns.cooldown2." + pre, coolDown);
		return coolDown;
	}

	public static int getCoolDown3(Player player, String pre) {
		int coolDown = 0;
		pre = pre.toLowerCase();
		coolDown = conf.getInt("commands.cooldowns.cooldown3." + pre, coolDown);
		return coolDown;
	}

	public static int getCoolDown4(Player player, String pre) {
		int coolDown = 0;
		pre = pre.toLowerCase();
		coolDown = conf.getInt("commands.cooldowns.cooldown4." + pre, coolDown);
		return coolDown;
	}

	public static int getCoolDown5(Player player, String pre) {
		int coolDown = 0;
		pre = pre.toLowerCase();
		coolDown = conf.getInt("commands.cooldowns.cooldown5." + pre, coolDown);
		return coolDown;
	}

	public static int getWarmUp(Player player, String pre) {
		int warmUp = 0;
		pre = pre.toLowerCase();
		warmUp = conf.getInt("commands.warmups.warmup." + pre, warmUp);
		return warmUp;
	}
	
	public static int getWarmUp2(Player player, String pre) {
		int warmUp = 0;
		pre = pre.toLowerCase();
		warmUp = conf.getInt("commands.warmups.warmup2." + pre, warmUp);
		return warmUp;
	}

	public static int getWarmUp3(Player player, String pre) {
		int warmUp = 0;
		pre = pre.toLowerCase();
		warmUp = conf.getInt("commands.warmups.warmup3." + pre, warmUp);
		return warmUp;
	}

	public static int getWarmUp4(Player player, String pre) {
		int warmUp = 0;
		pre = pre.toLowerCase();
		warmUp = conf.getInt("commands.warmups.warmup4." + pre, warmUp);
		return warmUp;
	}

	public static int getWarmUp5(Player player, String pre) {
		int warmUp = 0;
		pre = pre.toLowerCase();
		warmUp = conf.getInt("commands.warmups.warmup5." + pre, warmUp);
		return warmUp;
	}

	public static int getPrice(Player player, String pre) {
		int price = 0;
		pre = pre.toLowerCase();
		price = conf.getInt("commands.prices.price." + pre, price);
		return price;
	}
	
	public static int getPrice2(Player player, String pre) {
		int price = 0;
		pre = pre.toLowerCase();
		price = conf.getInt("commands.prices.price2." + pre, price);
		return price;
	}
	
	public static int getPrice3(Player player, String pre) {
		int price = 0;
		pre = pre.toLowerCase();
		price = conf.getInt("commands.prices.price3." + pre, price);
		return price;
	}
	
	public static int getPrice4(Player player, String pre) {
		int price = 0;
		pre = pre.toLowerCase();
		price = conf.getInt("commands.prices.price4." + pre, price);
		return price;
	}
	
	public static int getPrice5(Player player, String pre) {
		int price = 0;
		pre = pre.toLowerCase();
		price = conf.getInt("commands.prices.price5." + pre, price);
		return price;
	}

	static String getCoolDownMessage() {
		return conf
				.getString(
						"commands.options.message_cooldown",
						"&6Wait&e &seconds& seconds&6 before you can use command&e &command& &6again.&f");
	}

	static String getWarmUpCancelledByMoveMessage() {
		return conf.getString(
				"commands.options.message_warmup_cancelled_by_move",
				"&6Warm-ups have been cancelled due to moving.&f");
	}

	static String getWarmUpCancelledByDamageMessage() {
		return conf.getString(
				"commands.options.message_warmup_cancelled_by_damage",
				"&6Warm-ups have been cancelled due to receiving damage.&f");
	}

	static String getWarmUpMessage() {
		return conf
				.getString("commands.options.message_warmup",
						"&6Wait&e &seconds& seconds&6 before command&e &command& &6has warmed up.&f");
	}

	static String getWarmUpAlreadyStartedMessage() {
		return conf.getString("commands.options.message_warmup_alreadystarted",
				"&6Warm-Up process for&e &command& &6has already started.&f");
	}

	static String getUnitSecondsMessage() {
		return conf.getString("commands.options.unit_seconds", "seconds");
	}

	static String getUnitMinutesMessage() {
		return conf.getString("commands.options.unit_minutes", "minutes");
	}

	static String getUnitHoursMessage() {
		return conf.getString("commands.options.unit_hours", "hours");
	}

	static boolean getClearOnRestart() {
		return conf.getBoolean("commands.options.clear_on_restart", false);
	}

	public static boolean getCancelWarmUpOnDamage() {
		return conf.getBoolean("commands.options.cancel_warmup_on_damage",
				false);
	}

	public static boolean getCancelWarmupOnMove() {
		return conf.getBoolean("commands.options.cancel_warmup_on_move", false);
	}

	public static String getPaidForCommandMessage() {
		return conf.getString("commands.options.paid_for_command_message",
				"Price of &command& was %s and you now have %s");
	}

	public static String getPaidErrorMessage() {
		return conf.getString("commands.options.paid_error",
				"An error has occured: %s");
	}

	public static boolean getCancelWarmupOnSprint() {
		return conf.getBoolean("commands.options.cancel_warmup_on_sprint",
				false);
	}

	public static boolean getCancelWarmupOnSneak() {
		return conf
				.getBoolean("commands.options.cancel_warmup_on_sneak", false);
	}

	public static String getCancelWarmupOnSneakMessage() {
		return conf.getString(
				"commands.options.message_warmup_cancelled_by_sneak",
				"&6Warm-ups have been cancelled due to sneaking.&f");
	}

	public static String getCancelWarmupOnSprintMessage() {
		return conf.getString(
				"commands.options.message_warmup_cancelled_by_sprint",
				"&6Warm-ups have been cancelled due to sprinting.&f");
	}



//	public static String getWarmUpCancelledByDeathMessage() {
//		return conf.getString(
//				"commands.options.message_warmup_cancelled_by_death",
//				"&6Warm-ups have been cancelled due to death.&f");
//	}

}
