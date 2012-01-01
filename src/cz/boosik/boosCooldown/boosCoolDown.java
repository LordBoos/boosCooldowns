package cz.boosik.boosCooldown;

import java.util.logging.Logger;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import util.boosChat;

@SuppressWarnings("deprecation")
public class boosCoolDown extends JavaPlugin {

	private final boosCoolDownPlayerListener playerListener = new boosCoolDownPlayerListener(
			this);
	private final boosCoolDownEntityListener entityListener = new boosCoolDownEntityListener(
			this);
	public static final Logger log = Logger.getLogger("Minecraft");
	public static PluginDescriptionFile pdfFile;
	public static Configuration conf;
	public static Configuration confusers;
	private static Permission permissions = null;
	private static Economy economy = null;
	@SuppressWarnings("unused")
	private static Vault vault = null;
	private static boolean usingVault = false;
	private static boolean usingEconomy = false;
	private static boolean usingPermissions = false;
	private PluginManager pm;
	@SuppressWarnings("static-access")
	public void onEnable() {
		pdfFile = this.getDescription();
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName() + "]" + " version "
				+ pdfFile.getVersion() + " by " + pdfFile.getAuthors()
				+ " is enabled!");
		
		boosConfigManager boosConfigManager = new boosConfigManager(this);
		boosConfigManager.load();
		conf = boosConfigManager.conf;
		boosCoolDownManager boosCoolDownManager = new boosCoolDownManager(this);
		boosCoolDownManager.load();
		pm = getServer().getPluginManager();
		registerEvents();
		initializeVault();
		if (boosConfigManager.getClearOnRestart()) {
			boosCoolDownManager.clear();
		}
		confusers = boosCoolDownManager.confusers;

	}

	public void onDisable() {
		if (boosConfigManager.getClearOnRestart() == true) {
			boosCoolDownManager.clear();
		} else {
		}
		log.info("[" + pdfFile.getName() + "]" + " version "
				+ pdfFile.getVersion() + " disabled!");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command c,
			String commandLabel, String[] args) {
		String command = c.getName().toLowerCase();
		if (usingPermissions) {
			if (permissions.has(sender, "booscooldowns.reload")
					&& command.equalsIgnoreCase("boosCooldowns")) {
				boosConfigManager.reload();
				boosChat.sendMessageToCommandSender(sender,
						"&6[" + pdfFile.getName() + "]" + " config reloaded");
				return true;
			}
		} else {
			if (sender.isOp() && command.equalsIgnoreCase("boosCooldowns")) {
				boosConfigManager.reload();
				boosChat.sendMessageToCommandSender(sender,
						"&6[" + pdfFile.getName() + "]" + " config reloaded");
				return true;
			}
		}
		return false;
	}

	public static Economy getEconomy() {
		return economy;
	}

	public static Permission getPermissions() {
		return permissions;
	}

	public static boolean isUsingVault() {
		return usingVault;
	}

	public static boolean isUsingEconomy() {
		return usingEconomy;
	}

	public static boolean isUsingPermissions() {
		return usingPermissions;
	}

	private boolean setupEconomy() {
		if (usingVault) {
			RegisteredServiceProvider<Economy> economyProvider = getServer()
					.getServicesManager().getRegistration(
							net.milkbowl.vault.economy.Economy.class);
			if (economyProvider != null) {
				economy = economyProvider.getProvider();
			}
			usingEconomy = true;
			return (economy != null);
		}
		usingEconomy = false;
		return false;
	}

	private boolean setupPermissions() {
		if (usingVault) {
			RegisteredServiceProvider<Permission> permissionsProvider = getServer()
					.getServicesManager().getRegistration(
							net.milkbowl.vault.permission.Permission.class);
			if (permissionsProvider != null) {
				permissions = permissionsProvider.getProvider();
			}
			usingPermissions = true;
			return (permissions != null);
		}
		usingPermissions = false;
		return false;
	}
	
	private void registerEvents(){
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener,
				Event.Priority.Lowest, this);
		pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener,
				Event.Priority.Normal, this);
		if (boosConfigManager.getCancelWarmUpOnDamage()) {
			pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener,
					Event.Priority.Normal, this);
		}
		if (boosConfigManager.getCancelWarmupOnMove()) {
			pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener,
					Event.Priority.Normal, this);
		}
		if (boosConfigManager.getCancelWarmupOnSprint()){
			pm.registerEvent(Event.Type.PLAYER_TOGGLE_SPRINT, playerListener, Event.Priority.Normal, this);
		}
		if(boosConfigManager.getCancelWarmupOnSneak()){
			pm.registerEvent(Event.Type.PLAYER_TOGGLE_SNEAK, playerListener, Event.Priority.Normal, this);
		}
	}
	
	private void initializeVault() {
		Plugin x = this.getServer().getPluginManager().getPlugin("Vault");
		if (x != null & x instanceof Vault) {
			vault = (Vault) x;
			log.info("[" + pdfFile.getName() + "]"
					+ " found [Vault] searching for economy plugin.");
			log.info("[" + pdfFile.getName() + "]"
					+ " found [Vault] searching for permissions plugin.");
			usingVault = true;
			if (setupEconomy() && setupPermissions()) {
				log.info("[" + pdfFile.getName() + "]" + " found ["
						+ economy.getName()
						+ "] plugin, enabling prices support.");
				log.info("[" + pdfFile.getName() + "]" + " found ["
						+ permissions.getName()
						+ "] plugin, enabling permissions support.");
			} else if (setupEconomy() && !setupPermissions()) {
				log.info("[" + pdfFile.getName() + "]" + " found ["
						+ economy.getName()
						+ "] plugin, enabling prices support.");
				log.info("["
						+ pdfFile.getName()
						+ "]"
						+ "] permissions pluging not found, disabling permissions support.");
			} else if (!setupEconomy() && setupPermissions()) {
				log.info("["
						+ pdfFile.getName()
						+ "]"
						+ " economy plugin not found, disabling prices support.");
				usingEconomy = false;
				log.info("[" + pdfFile.getName() + "]" + " found ["
						+ permissions.getName()
						+ "] plugin, enabling permissions support.");
			} else {
				log.info("["
						+ pdfFile.getName()
						+ "]"
						+ " economy plugin not found, disabling prices support.");
				log.info("["
						+ pdfFile.getName()
						+ "]"
						+ "] permissions pluging not found, disabling permissions support.");
			}
		} else {
			log.info("["
					+ pdfFile.getName()
					+ "]"
					+ " [Vault] not found disabling economy and permissions support.");
			usingVault = false;
		}
		
	}
}
