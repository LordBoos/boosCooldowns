package cz.boosik.boosCooldown;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.mcstats.MetricsLite;

import util.boosChat;
import cz.boosik.boosCooldown.Listeners.BoosEntityDamageListener;
import cz.boosik.boosCooldown.Listeners.BoosPlayerDeathListener;
import cz.boosik.boosCooldown.Listeners.BoosPlayerGameModeChangeListener;
import cz.boosik.boosCooldown.Listeners.BoosPlayerInteractListener;
import cz.boosik.boosCooldown.Listeners.BoosPlayerMoveListener;
import cz.boosik.boosCooldown.Listeners.BoosPlayerToggleSneakListener;
import cz.boosik.boosCooldown.Listeners.BoosPlayerToggleSprintListener;
import cz.boosik.boosCooldown.Listeners.BoosSignChangeListener;
import cz.boosik.boosCooldown.Listeners.BoosSignInteractListener;

/**
 * Hlavn� t��da pluginu. T��da je potomkem JavaPlugin a implementuje Runnable.
 * Tato t��da se star� o ve�ker� inicializace p�i spu�t�n� a ukon�en� pluginu.
 * Toto zahrnuje zji�t�n�, zda je k dispozici plugin Vault a ekonomick� plugin,
 * registraci poslucha�� a tak� se star� o funkce v�ech kontroln�ch a
 * konfigura�n�ch p��kaz�. Periodicky tak� ukl�d� soubor datab�ze v intervalu
 * nastaven�m v konfiguraci.
 * 
 * @author Jakub Kol��
 * 
 */
public class BoosCoolDown extends JavaPlugin implements Runnable {
	private static final Logger log = Logger.getLogger("Minecraft");
	private static PluginDescriptionFile pdfFile;
	private static Economy economy = null;
	private static boolean usingVault = false;
	private PluginManager pm;
	private static BoosCoolDown instance = null;

	/**
	 * Metoda odes�l� zpr�vy o pou�it� p��kaz� do konzole serveru.
	 * 
	 * @param player
	 *            jm�no hr��e kter� vykonal p��kaz
	 * @param command
	 *            vykonan� p��kaz
	 */
	public static void commandLogger(String player, String command) {
		log.info("[" + "boosLogger" + "] " + player + " used command "
				+ command);
	}

	/**
	 * @return
	 */
	public static Economy getEconomy() {
		return economy;
	}

	/**
	 * @return
	 */
	public static Logger getLog() {
		return log;
	}

	/**
	 * Metoda zji��uje na z�klad� opr�vn�n� jestli je plugin aktivn� pro dan�ho
	 * hr��e nebo ne.
	 * 
	 * @param player
	 *            specifikovan� hr��
	 * @return
	 */
	static boolean isPluginOnForPlayer(Player player) {
		boolean on;
		if (player.hasPermission("booscooldowns.exception")) {
			on = false;
		} else if (player.isOp()) {
			on = false;
		} else {
			on = true;
		}
		return on;
	}

	/**
	 * Metoda zji��uje jestli je dostupn� plugin Vault a na z�klad� toho vol�
	 * metodu pro nastaven� ekonomick�ho pluginu a informuje o tom u�ivatele
	 * pomoc� konzole serveru.
	 */
	private void initializeVault() {
		Plugin x = pm.getPlugin("Vault");
		if (x != null & x instanceof Vault) {
			log.info("[" + pdfFile.getName() + "]"
					+ " found [Vault] searching for economy plugin.");
			usingVault = true;
			if (setupEconomy()) {
				log.info("[" + pdfFile.getName() + "]" + " found ["
						+ economy.getName()
						+ "] plugin, enabling prices support.");
			} else {
				log.info("["
						+ pdfFile.getName()
						+ "]"
						+ " economy plugin not found, disabling prices support.");
			}
		} else {
			log.info("[" + pdfFile.getName() + "]"
					+ " [Vault] not found disabling economy support.");
			usingVault = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender
	 * , org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command c,
			String commandLabel, String[] args) {
		String command = c.getName().toLowerCase();
		if (command.equalsIgnoreCase("booscooldowns")) {
			if (args.length == 1) {
				if (sender.hasPermission("booscooldowns.reload")
						&& args[0].equalsIgnoreCase("reload")) {
					reload();
					boosChat.sendMessageToCommandSender(sender,
							"&6[" + pdfFile.getName() + "]&e"
									+ " config reloaded");
					return true;
				}
				if (sender.hasPermission("booscooldowns.list.limits")
						&& args[0].equalsIgnoreCase("limits")) {
					try {
						Player send = (Player) sender;
						Set<String> commands = BoosConfigManager
								.getCommands(send);
						for (String comm : commands) {
							int lim = BoosConfigManager.getLimit(comm, send);
							BoosLimitManager.getLimitListMessages(send, comm,
									lim);
						}
					} catch (ClassCastException e) {
						log.warning("You cannot use this command from console!");
					}
					return true;
				}
				else if (sender.hasPermission("booscooldowns.globalreset")
						&& args[0].equalsIgnoreCase("startglobalreset")) {
					BoosLimitManager.setGlobalLimitResetDate();
					startLimitResetTimersGlobal();
					return true;
				}
			}
			else if (args.length == 2) {
				String jmeno = args[1];
				Player player = Bukkit.getPlayerExact(jmeno);
				UUID uuid = player.getUniqueId();
				if (sender.hasPermission("booscooldowns.clearcooldowns")
						&& args[0].equalsIgnoreCase("clearcooldowns")) {
					String co = "cooldown";
					BoosConfigManager.clearSomething(co, uuid);
					boosChat.sendMessageToCommandSender(sender,
							"&6[" + pdfFile.getName() + "]&e"
									+ " cooldowns of player " + jmeno
									+ " cleared");
					return true;
				} else if (sender.hasPermission("booscooldowns.clearuses")
						&& command.equalsIgnoreCase("booscooldowns")
						&& args[0].equalsIgnoreCase("clearuses")) {
					String co = "uses";
					BoosConfigManager.clearSomething(co, uuid);
					boosChat.sendMessageToCommandSender(sender,
							"&6[" + pdfFile.getName() + "]&e"
									+ " uses of player " + jmeno + " cleared");
					return true;
				} else if (sender.hasPermission("booscooldowns.clearwarmups")
						&& command.equalsIgnoreCase("booscooldowns")
						&& args[0].equalsIgnoreCase("clearwarmups")) {
					String co = "warmup";
					BoosConfigManager.clearSomething(co, uuid);
					boosChat.sendMessageToCommandSender(sender,
							"&6[" + pdfFile.getName() + "]&e"
									+ " warmups of player " + jmeno
									+ " cleared");
					return true;
				}
			}
			else if (args.length == 3) {
				String jmeno = args[1];
				Player player = Bukkit.getPlayerExact(jmeno);
				UUID uuid = player.getUniqueId();
				String command2 = args[2].trim();
				if (sender.hasPermission("booscooldowns.clearcooldowns")
						&& args[0].equalsIgnoreCase("clearcooldowns")) {
					String co = "cooldown";
					BoosConfigManager.clearSomething(co, uuid, command2);
					boosChat.sendMessageToCommandSender(sender,
							"&6[" + pdfFile.getName() + "]&e"
									+ " cooldown for command " + command2
									+ " of player " + uuid + " cleared");
					return true;
				} else if (sender.hasPermission("booscooldowns.clearuses")
						&& args[0].equalsIgnoreCase("clearuses")) {
					String co = "uses";
					BoosConfigManager.clearSomething(co, uuid, command2);
					boosChat.sendMessageToCommandSender(sender,
							"&6[" + pdfFile.getName() + "]&e"
									+ " uses for command " + command2
									+ " of player " + jmeno + " cleared");
					return true;
				} else if (sender.hasPermission("booscooldowns.clearwarmups")
						&& args[0].equalsIgnoreCase("clearwarmups")) {
					String co = "warmup";
					BoosConfigManager.clearSomething(co, uuid, command2);
					boosChat.sendMessageToCommandSender(sender,
							"&6[" + pdfFile.getName() + "]&e"
									+ " warmups for command " + command2
									+ " of player " + jmeno + " cleared");
					return true;

				}
			}
			else if (args.length == 4) {
				if (sender.hasPermission("booscooldowns.set")
						&& args[0].equalsIgnoreCase("set")) {
					String what = args[1];
					String comm = args[2];
					String value = args[3];
					String group = "default";
					if (comm.startsWith("/") || comm.equals("*")) {
						if (comm.contains("_")) {
							comm = comm.replace("_", " ");
						}
						BoosConfigManager.setAddToConfigFile(group, comm, what,
								value);
						boosChat.sendMessageToCommandSender(sender, "&6["
								+ pdfFile.getName() + "]&e " + what
								+ " for command" + comm + " in group " + group
								+ " is now set to " + value);
						return true;
					} else {
						boosChat.sendMessageToCommandSender(sender, "&6["
								+ pdfFile.getName() + "]&e"
								+ " Command has to start with \"/\".");
						return true;
					}
				}

			}
			else if (args.length == 5) {
				if (sender.hasPermission("booscooldowns.set")
						&& args[0].equalsIgnoreCase("set")) {
					String what = args[1];
					String comm = args[2];
					String value = args[3];
					String group = args[4];
					if (comm.startsWith("/") || comm.equals("*")) {
						if (comm.contains("_")) {
							comm = comm.replace("_", " ");
						}
						BoosConfigManager.setAddToConfigFile(group, comm, what,
								value);
						boosChat.sendMessageToCommandSender(sender, "&6["
								+ pdfFile.getName() + "]&e " + what
								+ " for command" + comm + " in group " + group
								+ " is now set to " + value);
						return true;
					} else {
						boosChat.sendMessageToCommandSender(sender, "&6["
								+ pdfFile.getName() + "]&e"
								+ " Command has to start with \"/\".");
						return true;
					}
				}

			} else {
//				boosChat.sendMessageToCommandSender(sender,
//						"&6[" + pdfFile.getName() + "]&e"
//								+ " Invalid command or access denied!");
				return false;
			}
		}
		return false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
	 */
	@Override
	public void onDisable() {
		if (BoosConfigManager.getClearOnRestart()) {
			BoosConfigManager.clear();
			log.info("[" + pdfFile.getName() + "]" + " cooldowns cleared!");
		} else {
			BoosConfigManager.saveConfusers();
			log.info("[" + pdfFile.getName() + "]" + " cooldowns saved!");
		}
		log.info("[" + pdfFile.getName() + "]" + " version "
				+ pdfFile.getVersion() + " disabled!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
	 */
	@Override
	public void onEnable() {
		pdfFile = this.getDescription();
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName() + "]" + " version "
				+ pdfFile.getVersion() + " by " + pdfFile.getAuthors()
				+ " is enabled!");
		this.saveDefaultConfig();
		new BoosConfigManager(this);
		BoosConfigManager.load();
		BoosConfigManager.loadConfusers();
		pm = getServer().getPluginManager();
		registerListeners();
		initializeVault();
		BukkitScheduler scheduler = this.getServer().getScheduler();
		startLimitResetTimersGlobal();
		if (BoosConfigManager.getAutoSave()) {
			scheduler.scheduleSyncRepeatingTask(this, this,
					BoosConfigManager.getSaveInterval() * 1200,
					BoosConfigManager.getSaveInterval() * 1200);
		}

		if (BoosConfigManager.getClearOnRestart()) {
			BoosConfigManager.clear();
		}
		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}

	}

	static void startLimitResetTimersGlobal() {
		YamlConfiguration confusers = BoosConfigManager.getConfusers();
		ConfigurationSection global = confusers
				.getConfigurationSection("global");
		if (global != null) {
			Set<String> globalKeys = global.getKeys(false);
			BukkitScheduler scheduler = Bukkit.getScheduler();
			for (String key : globalKeys) {
				String confTime = confusers.getString("global." + key
						+ ".reset");
				long limitResetDelay = BoosConfigManager
						.getLimitResetDelayGlobal(key);
				Date endDate = getTime(confTime);
				Date startDate = getCurrTime();
				Calendar calcurrTime = Calendar.getInstance();
				calcurrTime.setTime(startDate);
				Calendar callastTime = Calendar.getInstance();
				callastTime.setTime(endDate);
				long time = secondsBetween(calcurrTime, callastTime,
						limitResetDelay);
				if (limitResetDelay != -65535) {
					if (time <= 0) {
						time = 1;
					}
					BoosCoolDown.getLog().info(
							"[boosCooldowns] Starting timer for " + time
									+ " seconds to reset limits for command "
									+ key);
					scheduler.scheduleSyncDelayedTask(Bukkit.getPluginManager()
							.getPlugin("boosCooldowns"),
							new BoosGlobalLimitResetRunnable(key), time * 20);
				} else {
					BoosCoolDown.getLog().info(
							"[boosCooldowns] Stoping timer to reset limits for command "
									+ key);
				}
			}
		}
	}

	static void startLimitResetTimerGlobal(String key) {
		YamlConfiguration confusers = BoosConfigManager.getConfusers();
		BukkitScheduler scheduler = Bukkit.getScheduler();
		String confTime = confusers.getString("global." + key + ".reset");
		long limitResetDelay = BoosConfigManager.getLimitResetDelayGlobal(key);
		Date endDate = getTime(confTime);
		Date startDate = getCurrTime();
		Calendar calcurrTime = Calendar.getInstance();
		calcurrTime.setTime(startDate);
		Calendar callastTime = Calendar.getInstance();
		callastTime.setTime(endDate);
		long time = secondsBetween(calcurrTime, callastTime, limitResetDelay);
		if (limitResetDelay != -65535) {
			if (time <= 0) {
				time = 1;
			}
			BoosCoolDown.getLog().info(
					"[boosCooldowns] Starting timer for " + time
							+ " seconds to reset limits for command " + key);
			scheduler.scheduleSyncDelayedTask(Bukkit.getPluginManager()
					.getPlugin("boosCooldowns"),
					new BoosGlobalLimitResetRunnable(key), time * 20);
		} else {
			BoosCoolDown.getLog().info(
					"[boosCooldowns] Stoping timer to reset limits for command "
							+ key);
		}
	}

	static long secondsBetween(Calendar startDate, Calendar endDate,
			long limitResetDelay) {
		long secondsBetween = 0;
		secondsBetween = ((endDate.getTimeInMillis() - startDate
				.getTimeInMillis()) / 1000) + limitResetDelay;
		return secondsBetween;
	}

	static Date getCurrTime() {
		String currTime = "";
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		currTime = sdf.format(cal.getTime());
		Date time = null;
		try {
			time = sdf.parse(currTime);
			return time;
		} catch (ParseException e) {
			return null;
		}
	}

	static Date getTime(String confTime) {
		if (confTime != null && !confTime.equals("")) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			Date lastDate = null;

			try {
				lastDate = sdf.parse(confTime);
				return lastDate;
			} catch (ParseException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Metoda registruje poslucha�e v PluginManageru na z�klad� konfigurace
	 * pluginu. Registroj� se v�dy jen nezbytn� poslucha�i. Poslucha�i pro
	 * vypnut� funkce nejsou registrov�ni.
	 */
	private void registerListeners() {
		HandlerList.unregisterAll(this);
		pm.registerEvents(new BoosCoolDownListener(this), this);
		if (BoosConfigManager.getCancelWarmUpOnDamage()) {
			pm.registerEvents(new BoosEntityDamageListener(), this);
		}
		if (BoosConfigManager.getCleanCooldownsOnDeath()
				|| BoosConfigManager.getCleanUsesOnDeath()
				|| BoosConfigManager.getStartCooldownsOnDeath()) {
			pm.registerEvents(new BoosPlayerDeathListener(), this);
		}
		if (BoosConfigManager.getCancelWarmUpOnGameModeChange()) {
			pm.registerEvents(new BoosPlayerGameModeChangeListener(), this);
		}
		if (BoosConfigManager.getBlockInteractDuringWarmup()) {
			pm.registerEvents(new BoosPlayerInteractListener(), this);
		}
		if (BoosConfigManager.getCancelWarmupOnMove()) {
			pm.registerEvents(new BoosPlayerMoveListener(), this);
		}
		if (BoosConfigManager.getCancelWarmupOnSneak()) {
			pm.registerEvents(new BoosPlayerToggleSneakListener(), this);
		}
		if (BoosConfigManager.getCancelWarmupOnSprint()) {
			pm.registerEvents(new BoosPlayerToggleSprintListener(), this);
		}
		if (BoosConfigManager.getSignCommands()) {
			pm.registerEvents(new BoosSignChangeListener(), this);
			pm.registerEvents(new BoosSignInteractListener(this), this);
		}
	}

	/**
	 * Metoda vol� metodu pro znovuna�ten� konfigura�n�ho souboru a metodu pro
	 * registraci poslucha��.
	 */
	private void reload() {
		BoosConfigManager.reload();
		registerListeners();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		BoosConfigManager.saveConfusers();
		BoosConfigManager.loadConfusers();
		log.info("[boosCooldowns] Config saved!");
	}

	/**
	 * Metoda vrac� hodnotu true, pokud je na serveru dostupn� Ekonomick� plugin
	 * kompatibiln� s pluginem Vault.
	 * 
	 * @return true pokud je dostupn� ekonomick� plugin kompatibiln� s pluginem
	 *         Vault, jinak vrac� hodnotu false
	 */
	private boolean setupEconomy() {
		if (usingVault) {
			RegisteredServiceProvider<Economy> economyProvider = getServer()
					.getServicesManager().getRegistration(
							net.milkbowl.vault.economy.Economy.class);
			if (economyProvider != null) {
				economy = economyProvider.getProvider();
			}
			return (economy != null);
		}
		return false;
	}

	public static BoosCoolDown getInstance() {
		if (instance == null) {
			instance = new BoosCoolDown();
		}
		return instance;
	}
}
