package cz.boosik.boosCooldown;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import util.boosChat;

/**
 * @author Jakub
 *
 */
public class boosCoolDownListener implements Listener {
	private static boosCoolDown plugin;

	/**
	 * @param instance
	 */
	public boosCoolDownListener(boosCoolDown instance) {
		plugin = instance;
	}

	/**
	 * @param event
	 * @param player
	 * @param regexCommad
	 * @param originalCommand
	 * @param warmupTime
	 * @param cooldownTime
	 * @param price
	 * @param limit
	 */
	private void checkRestrictions(PlayerCommandPreprocessEvent event,
			Player player, String regexCommad, String originalCommand,
			int warmupTime, int cooldownTime, double price, int limit) {
		boolean blocked = boosLimitManager.blocked(player, regexCommad,
				originalCommand, limit);
		if (!blocked) {
			if (warmupTime > 0) {
				if (!player.hasPermission("booscooldowns.nowarmup")
						&& !player.hasPermission("booscooldowns.nowarmup."
								+ originalCommand)) {
					start(event, player, regexCommad, originalCommand,
							warmupTime, cooldownTime);
				}
			} else {
				if (boosCoolDownManager.coolDown(player, regexCommad,
						originalCommand, cooldownTime)) {
					event.setCancelled(true);
				}
			}
			if (!event.isCancelled()) {
				boosPriceManager.payForCommand(event, player, regexCommad,
						originalCommand, price);
			}
		} else {
			event.setCancelled(true);
			String msg = String.format(boosConfigManager
					.getCommandBlockedMessage());
			boosChat.sendMessageToPlayer(player, msg);
		}
		if (!event.isCancelled()) {
			boosLimitManager.setUses(player, regexCommad, originalCommand);
			if (boosConfigManager.getCommandLogging()) {
				boosCoolDown.commandLogger(player.getName(), originalCommand);
			}
		}
	}

	/**
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	private void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();
		String originalCommand = event.getMessage().trim()
				.replaceAll(" +", " ").toLowerCase();
		String regexCommad = "";
		Set<String> aliases = boosConfigManager.getAliases();
		Set<String> commands = boosConfigManager.getCommands(player);
		boolean on = true;
		int warmupTime = 0;
		double price = 0;
		int limit = -1;
		int cooldownTime = 0;
		on = boosCoolDown.isPluginOnForPlayer(player);
		try {
			if (aliases.contains(originalCommand)) {
				originalCommand = boosConfigManager.getAlias(originalCommand);
				event.setMessage(originalCommand);
			}
		} catch (NullPointerException e) {
			boosCoolDown
					.getLog()
					.warning(
							"Aliases section in config.yml is missing! Please delete your config.yml, restart server and set it again!");
		}
		if (on) {
			for (String group : commands) {
				String group2 = group.replace("*", ".+");
				if (originalCommand.matches(group2)) {
					regexCommad = group;
					if (boosConfigManager.getWarmupEnabled()) {
						warmupTime = boosConfigManager.getWarmUp(regexCommad,
								player);
					}
					if (boosConfigManager.getCooldownEnabled()) {
						cooldownTime = boosConfigManager.getCoolDown(
								regexCommad, player);
					}
					if (boosConfigManager.getPriceEnabled()) {
						price = boosConfigManager.getPrice(regexCommad, player);
					}
					if (boosConfigManager.getLimitEnabled()) {
						limit = boosConfigManager.getLimit(regexCommad, player);
					}
					break;
				}
			}
			this.checkRestrictions(event, player, regexCommad, originalCommand,
					warmupTime, cooldownTime, price, limit);
		}
	}

	/**
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOW)
	private void onPlayerChat(AsyncPlayerChatEvent event) {
		String chatMessage = event.getMessage();
		String temp = "globalchat";
		double price = 0;
		Player player = event.getPlayer();
		if (!boosConfigManager.getCommands(player).contains("globalchat")) {
			return;
		}
		int cooldownTime = boosConfigManager.getCoolDown(temp, player);
		if (chatMessage.startsWith("!")) {
			if (!boosCoolDownManager.checkCoolDownOK(player, temp, temp,
					cooldownTime)) {
				event.setCancelled(true);
				return;
			} else {
				if (boosCoolDownManager.coolDown(player, temp, temp,
						cooldownTime)) {
					event.setCancelled(true);
					return;
				}
			}
			price = boosConfigManager.getPrice(temp, player);
			boosPriceManager.payForCommand2(event, player, temp, temp, price);
		}
	}

	/**
	 * @param event
	 * @param player
	 * @param regexCommad
	 * @param originalCommand
	 * @param warmupTime
	 * @param cooldownTime
	 */
	private void start(PlayerCommandPreprocessEvent event, Player player,
			String regexCommad, String originalCommand, int warmupTime,
			int cooldownTime) {
		if (!boosWarmUpManager.checkWarmUpOK(player, regexCommad)) {
			if (boosCoolDownManager.checkCoolDownOK(player, regexCommad,
					originalCommand, cooldownTime)) {
				boosWarmUpManager.startWarmUp(plugin, player, regexCommad,
						originalCommand, warmupTime);
				event.setCancelled(true);
				return;
			} else {
				event.setCancelled(true);
				return;
			}
		} else {
			if (boosCoolDownManager.coolDown(player, regexCommad,
					originalCommand, cooldownTime)) {
				event.setCancelled(true);
				return;
			} else {
				boosWarmUpManager.removeWarmUpOK(player, regexCommad);
				return;
			}
		}
	}
}