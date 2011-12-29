package cz.boosik.boosCooldown;

import java.util.logging.Logger;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;

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
	public static boolean permissions = false;
	private static Vault vault = null;
	public static Vault getVault() {
		return vault;
	}

	private static boolean usingVault;
	private static boolean usingEconomy;
	private static Economy economy = null;

	public static Economy getEconomy() {
		return economy;
	}

	public boolean isUsingVault() {
		return usingVault;
	}
	
	public static boolean isUsingEconomy() {
		return usingEconomy;
	}

	@SuppressWarnings("static-access")
	public void onEnable() {
		pdfFile = this.getDescription();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener,
				Event.Priority.Lowest, this);

		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName() + "]" + " version "
				+ pdfFile.getVersion() + " by " + pdfFile.getAuthors()
				+ " is enabled!");

		boosConfigManager boosConfigManager = new boosConfigManager(this);
		boosConfigManager.load();
		conf = boosConfigManager.conf;
		boosCoolDownManager boosCoolDownManager = new boosCoolDownManager(this);
		boosCoolDownManager.load();
		if (boosConfigManager.getCancelWarmUpOnDamage()) {
			pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener,
					Event.Priority.Normal, this);
		}
		if (boosConfigManager.getCancelWarmupOnMove()) {
			pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener,
					Event.Priority.Normal, this);
		}
		if (boosConfigManager.getClearOnRestart()) {
			boosCoolDownManager.clear();
		}
		Plugin x = this.getServer().getPluginManager().getPlugin("Vault");
        if(x != null & x instanceof Vault) {
            vault = (Vault) x;
            log.info("[" + pdfFile.getName() + "]" + " found [Vault] searching for economy plugin.");
            usingVault = true;
            if(setupEconomy()){
            	log.info("[" + pdfFile.getName() + "]" + " found [" + economy.getName() +"] plugin, enabling prices support.");
            } else {
            	log.info("[" + pdfFile.getName() + "]" + " economy plugin not found, disabling prices support.");
            }
        } else {
        	log.info("[" + pdfFile.getName() + "]" + " [Vault] not found disabling economy support.");
        	usingVault = false;
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
		if (command.equalsIgnoreCase("boosCoolDown")) {
			boosConfigManager.reload();
			boosChat.sendMessageToCommandSender(sender,
					"&6[" + pdfFile.getName() + "]" + " config reloaded");
			return true;
		}
		return false;
	}

	private boolean setupEconomy(){
		if(usingVault){
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy  = economyProvider.getProvider();
        }
        usingEconomy = true;
        return (economy != null);
    } 	usingEconomy = false;
		return false;
    }
}
