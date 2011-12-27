package cz.boosik.boosCooldown;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import util.boosChat;

import java.util.logging.Logger;

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
		if (boosConfigManager.getClearOnRestart() == true) {
			boosCoolDownManager.clear();
		} else {
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

}
