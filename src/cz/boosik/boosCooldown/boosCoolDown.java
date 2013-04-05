package cz.boosik.boosCooldown;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
import cz.boosik.boosCooldown.Listeners.boosEntityDamageListener;
import cz.boosik.boosCooldown.Listeners.boosPlayerDeathListener;
import cz.boosik.boosCooldown.Listeners.boosPlayerGameModeChangeListener;
import cz.boosik.boosCooldown.Listeners.boosPlayerInteractListener;
import cz.boosik.boosCooldown.Listeners.boosPlayerMoveListener;
import cz.boosik.boosCooldown.Listeners.boosPlayerToggleSneakListener;
import cz.boosik.boosCooldown.Listeners.boosPlayerToggleSprintListener;
import cz.boosik.boosCooldown.Listeners.boosSignChangeListener;
import cz.boosik.boosCooldown.Listeners.boosSignInteractListener;

/**
 * @author Jakub
 *
 */
public class boosCoolDown extends JavaPlugin implements Runnable {
	private static final Logger log = Logger.getLogger("Minecraft");
	private static PluginDescriptionFile pdfFile;
	private static Economy economy = null;
	private static boolean usingVault = false;

	/**
	 * @param player
	 * @param command
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
	 * @param player
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

	private PluginManager pm;

	/**
	 * 
	 */
	private void initializeVault() {
		Plugin x = this.getServer().getPluginManager().getPlugin("Vault");
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

	/* (non-Javadoc)
	 * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
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
						Set<String> commands = boosConfigManager
								.getCommands(send);
						for (String comm : commands) {
							int lim = boosConfigManager.getLimit(comm, send);
							boosLimitManager.getLimitListMessages(send, comm, lim);
						}
					} catch (ClassCastException e) {
						log.warning("You cannot use this command from console!");
					}
					return true;
				}
			}
			if (args.length == 2) {
				String jmeno = args[1];
				if (sender.hasPermission("booscooldowns.clearcooldowns")
						&& args[0].equalsIgnoreCase("clearcooldowns")) {
					String co = "cooldown";
					boosConfigManager.clearSomething(co, jmeno);
					boosChat.sendMessageToCommandSender(sender,
							"&6[" + pdfFile.getName() + "]&e"
									+ " cooldowns of player " + jmeno
									+ " cleared");
					return true;
				} else if (sender.hasPermission("booscooldowns.clearuses")
						&& command.equalsIgnoreCase("booscooldowns")
						&& args[0].equalsIgnoreCase("clearuses")) {
					String co = "uses";
					boosConfigManager.clearSomething(co, jmeno);
					boosChat.sendMessageToCommandSender(sender,
							"&6[" + pdfFile.getName() + "]&e"
									+ " uses of player " + jmeno + " cleared");
					return true;
				} else if (sender.hasPermission("booscooldowns.clearwarmups")
						&& command.equalsIgnoreCase("booscooldowns")
						&& args[0].equalsIgnoreCase("clearwarmups")) {
					String co = "warmup";
					boosConfigManager.clearSomething(co, jmeno);
					boosChat.sendMessageToCommandSender(sender,
							"&6[" + pdfFile.getName() + "]&e"
									+ " warmups of player " + jmeno
									+ " cleared");
					return true;
				}
			}
			if (args.length == 3) {
				String jmeno = args[1];
				String command2 = args[2].trim();
				if (sender.hasPermission("booscooldowns.clearcooldowns")
						&& args[0].equalsIgnoreCase("clearcooldowns")) {
					String co = "cooldown";
					boosConfigManager.clearSomething(co, jmeno, command2);
					boosChat.sendMessageToCommandSender(sender,
							"&6[" + pdfFile.getName() + "]&e"
									+ " cooldown for command " + command2
									+ " of player " + jmeno + " cleared");
					return true;
				} else if (sender.hasPermission("booscooldowns.clearuses")
						&& args[0].equalsIgnoreCase("clearuses")) {
					String co = "uses";
					boosConfigManager.clearSomething(co, jmeno, command2);
					boosChat.sendMessageToCommandSender(sender,
							"&6[" + pdfFile.getName() + "]&e"
									+ " uses for command " + command2
									+ " of player " + jmeno + " cleared");
					return true;
				} else if (sender.hasPermission("booscooldowns.clearwarmups")
						&& args[0].equalsIgnoreCase("clearwarmups")) {
					String co = "warmup";
					boosConfigManager.clearSomething(co, jmeno, command2);
					boosChat.sendMessageToCommandSender(sender,
							"&6[" + pdfFile.getName() + "]&e"
									+ " warmups for command " + command2
									+ " of player " + jmeno + " cleared");
					return true;

				}
			}
			if (args.length == 4) {
				if (sender.hasPermission("booscooldowns.set")
						&& args[0].equalsIgnoreCase("set")) {
					String coSetnout = args[1];
					String co = args[2];
					String hodnota = args[3];
					String regex1 = "(\\d+)(,)(\\d+)(,)(\\d+)(\\.)(\\d+)(,)(-?)(\\d+)(,)(CONFUSION|DAMAGE_RESISTANCE|FAST_DIGGING|FIRE_RESISTANCE|HARM|HEAL|HUNGER|INCREASE_DAMAGE|INVISIBILITY|JUMP|NIGHT_VISION|POISON|REGENERATION|SLOW|SLOW_DIGGING|SPEED|WATER_BREATHING|WEAKNESS|WITHER)(,)(\\d+)";
					String regex2 = "(\\d+)(,)(\\d+)(,)(\\d+)(\\.)(\\d+)(,)(-?)(\\d+)";
					if (!hodnota.matches(regex1) && !hodnota.matches(regex2)) {
						boosChat.sendMessageToCommandSender(sender,
								"Invalid syntax!");
						return true;
					}
					if (co.startsWith("/") || co.equals("*")) {
						if (co.contains("_")) {
							co = co.replace("_", " ");
						}
						boosConfigManager.setAddToConfigFile(coSetnout, co,
								hodnota);
						boosChat.sendMessageToCommandSender(sender, "&6["
								+ pdfFile.getName() + "]&e" + " " + co
								+ " in group " + coSetnout + " is now set to "
								+ hodnota);
						return true;
					} else {
						boosChat.sendMessageToCommandSender(sender, "&6["
								+ pdfFile.getName() + "]&e"
								+ " Command has to start with \"/\".");
						return true;
					}
				}

			} else {
				boosChat.sendMessageToCommandSender(
						sender,
						"&6["
								+ pdfFile.getName()
								+ "]&e"
								+ " access denied, you lack required permission to do this!");
			}
		}
		return false;

	}

	/* (non-Javadoc)
	 * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
	 */
	@Override
	public void onDisable() {
		if (boosConfigManager.getClearOnRestart() == true) {
			boosConfigManager.clear();
			log.info("[" + pdfFile.getName() + "]" + " cooldowns cleared!");
		} else {
			boosConfigManager.saveConfusers();
			log.info("[" + pdfFile.getName() + "]" + " cooldowns saved!");
		}
		log.info("[" + pdfFile.getName() + "]" + " version "
				+ pdfFile.getVersion() + " disabled!");
	}

	/* (non-Javadoc)
	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
	 */
	@Override
	public void onEnable() {
		pdfFile = this.getDescription();
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName() + "]" + " version "
				+ pdfFile.getVersion() + " by " + pdfFile.getAuthors()
				+ " is enabled!");
		new boosConfigManager(this);
		boosConfigManager.load();
		boosConfigManager.loadConfusers();
		pm = getServer().getPluginManager();
		registerListeners();
		initializeVault();
		BukkitScheduler scheduler = this.getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, this,
				boosConfigManager.getSaveInterval() * 1200,
				boosConfigManager.getSaveInterval() * 1200);
		if (boosConfigManager.getClearOnRestart()) {
			boosConfigManager.clear();
		}
		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}

	}

	/**
	 * 
	 */
	private void registerListeners() {
		HandlerList.unregisterAll(this);
		pm.registerEvents(new boosCoolDownListener(this), this);
		if (boosConfigManager.getCancelWarmUpOnDamage()) {
			pm.registerEvents(new boosEntityDamageListener(), this);
		}
		if (boosConfigManager.getCleanCooldownsOnDeath()
				|| boosConfigManager.getCleanUsesOnDeath()
				|| boosConfigManager.getStartCooldownsOnDeath()) {
			pm.registerEvents(new boosPlayerDeathListener(), this);
		}
		if (boosConfigManager.getCancelWarmUpOnGameModeChange()) {
			pm.registerEvents(new boosPlayerGameModeChangeListener(), this);
		}
		if (boosConfigManager.getBlockInteractDuringWarmup()) {
			pm.registerEvents(new boosPlayerInteractListener(), this);
		}
		if (boosConfigManager.getCancelWarmupOnMove()) {
			pm.registerEvents(new boosPlayerMoveListener(), this);
		}
		if (boosConfigManager.getCancelWarmupOnSneak()) {
			pm.registerEvents(new boosPlayerToggleSneakListener(), this);
		}
		if (boosConfigManager.getCancelWarmupOnSprint()) {
			pm.registerEvents(new boosPlayerToggleSprintListener(), this);
		}
		if (boosConfigManager.getSignCommands()) {
			pm.registerEvents(new boosSignChangeListener(), this);
			pm.registerEvents(new boosSignInteractListener(this), this);
		}
	}

	/**
	 * 
	 */
	private void reload() {
		boosConfigManager.reload();
		registerListeners();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		boosConfigManager.saveConfusers();
		boosConfigManager.loadConfusers();
		log.info("[boosCooldowns] Config saved!");
	}

	/**
	 * @return
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
}
