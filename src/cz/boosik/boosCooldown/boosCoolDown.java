package cz.boosik.boosCooldown;

import java.util.logging.Logger;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import util.boosChat;

public class boosCoolDown extends JavaPlugin {
	public static final Logger log = Logger.getLogger("Minecraft");
	public static PluginDescriptionFile pdfFile;
	private static Permission permissions = null;
	private static Economy economy = null;
	private static boolean usingVault = false;
	private static boolean usingEconomy = false;
	private static boolean usingPermissions = false;
	private PluginManager pm;

	public void onEnable() {
		pdfFile = this.getDescription();
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName() + "]" + " version "
				+ pdfFile.getVersion() + " by " + pdfFile.getAuthors()
				+ " is enabled!");

		new boosConfigManager(this);
		boosConfigManager.load();
		new boosCoolDownManager(this);
		boosCoolDownManager.load();
		pm = getServer().getPluginManager();
		pm.registerEvents(new boosCoolDownListener(this), this);
		initializeVault();
		if (boosConfigManager.getClearOnRestart()) {
			boosCoolDownManager.clear();
		}

	}

	public void onDisable() {
		if (boosConfigManager.getClearOnRestart() == true) {
			boosCoolDownManager.clear();
			log.info("[" + pdfFile.getName() + "]" + " cooldowns cleared!");
		} else {
			boosCoolDownManager.save();
			log.info("[" + pdfFile.getName() + "]" + " cooldowns saved!");
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
		} else if (sender.isOp() && command.equalsIgnoreCase("boosCooldowns")) {
			boosConfigManager.reload();
			boosChat.sendMessageToCommandSender(sender,
					"&6[" + pdfFile.getName() + "]" + " config reloaded");
			return true;
		} else {
			boosChat.sendMessageToCommandSender(
					sender,
					"&6["
							+ pdfFile.getName()
							+ "]"
							+ " access denied, you lack required permission to do this!");
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

	public static void commandLogger(String player, String command) {
		log.info("[" + "boosLogger" + "] " + player + " used command "
				+ command);
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

	private void initializeVault() {
		Plugin x = this.getServer().getPluginManager().getPlugin("Vault");
		if (x != null & x instanceof Vault) {
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
