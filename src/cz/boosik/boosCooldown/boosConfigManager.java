package cz.boosik.boosCooldown;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

@SuppressWarnings("deprecation")
public class boosConfigManager {

	protected static boosCoolDown bCoolDown;
	protected static Configuration conf;
	protected File confFile;
	static List<String> players = new LinkedList<String>();

	@SuppressWarnings("static-access")
	public boosConfigManager(boosCoolDown boosCoolDown) {
		this.bCoolDown = boosCoolDown;

		File f = new File(boosCoolDown.getDataFolder(), "config.yml");
		conf = null;

		if (f.exists()) {
			conf = new Configuration(f);
			conf.load();

		} else {
			this.confFile = new File(boosCoolDown.getDataFolder(), "config.yml");
			this.conf = new Configuration(confFile);
			conf.setProperty("commands.cooldown./spawn", 60);
			conf.setProperty("commands.cooldown./home", 30);
			conf.setProperty("commands.warmup./give", 60);
			conf.setProperty("commands.warmup./home", 20);
			conf.setProperty("commands.price./spawn", 10);
			conf.setProperty("commands.price./home", 20);
			conf.setProperty("commands.options.cancel_warmup_on_damage", false);
			conf.setProperty("commands.options.cancel_warmup_on_move", false);
			conf.setProperty("commands.options.clear_on_restart", false);
			conf.setProperty("commands.options.unit_seconds", "seconds");
			conf.setProperty("commands.options.unit_minutes", "minutes");
			conf.setProperty("commands.options.unit_hours", "hours");
			conf.setProperty(
					"commands.options.message_warmup_cancelled_by_damage",
					"&6Warm-ups have been cancelled due to receiving damage.&f");
			conf.setProperty(
					"commands.options.message_warmup_cancelled_by_move",
					"&6Warm-ups have been cancelled due to moving.&f");
			conf.setProperty("commands.options.message_cooldown",
					"&6Wait&e &seconds& &unit&&6 before you can use command&e &command& &6again.&f");
			conf.setProperty("commands.options.message_warmup",
					"&6Wait&e &seconds& &unit&&6 before command&e &command& &6has warmed up.&f");
			conf.setProperty("commands.options.message_warmup_alreadystarted",
					"&6Warm-Up process for&e &command& &6has already started.&f");
			conf.setProperty("commands.options.paid_error", "&6An error has occured:&e %s");
			conf.setProperty("commands.options.paid_for_command_message", "&6Price of&e &command& &6was&e %s &6and you now have&e %s");
			conf.save();
		}
	}

	static void load() {
		conf.load();
	}

	static void reload() {
		load();
	}

	static int getCoolDown(Player player, String pre) {
		int coolDown = 0;
		pre = pre.toLowerCase();
		coolDown = conf.getInt("commands.cooldown." + pre, coolDown);
		return coolDown;
	}

	public static int getWarmUp(Player player, String pre) {
		int warmUp = 0;
		pre = pre.toLowerCase();
		warmUp = conf.getInt("commands.warmup." + pre, warmUp);
		return warmUp;
	}
	
	public static int getPrice(Player player, String pre) {
		int price = 0;
		pre = pre.toLowerCase();
		price = conf.getInt("commands.price." + pre, price);
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

	static boolean getCancelWarmUpOnDamage() {
		return conf.getBoolean("commands.options.cancel_warmup_on_damage",
				false);
	}

	public boolean getCancelWarmupOnMove() {
		return conf.getBoolean("commands.options.cancel_warmup_on_move", false);
	}

	public static String getPaidForCommandMessage() {
		return conf.getString("commands.options.paid_for_command_message", "Price of &command& was %s and you now have %s");
	}

	public static String getPaidErrorMessage() {
		return conf.getString("commands.options.paid_error", "An error has occured: %s");
	}

}
