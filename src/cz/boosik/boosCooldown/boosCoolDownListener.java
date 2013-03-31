package cz.boosik.boosCooldown;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import util.boosChat;

public class boosCoolDownListener<a> implements Listener {
	private final boosCoolDown plugin;
	private boolean blocked = false;
	public static ConcurrentHashMap<Player, Location> playerloc = new ConcurrentHashMap<Player, Location>();
	public static ConcurrentHashMap<Player, String> playerworld = new ConcurrentHashMap<Player, String>();

	public static void clearLocWorld(Player player) {
		boosCoolDownListener.playerloc.remove(player);
		boosCoolDownListener.playerworld.remove(player);
	}

	public boosCoolDownListener(boosCoolDown instance) {
		plugin = instance;
	}

	private boolean blocked(Player player, String pre, int limit) {
		int uses = boosCoolDownManager.getUses(player, pre);
		if (player.hasPermission("booscooldowns.nolimit")
				|| player.hasPermission("booscooldowns.nolimit." + pre)) {
		} else {
			if (limit == -1) {
				return false;
			} else if (limit <= uses) {
				return true;
			}
		}
		return false;
	}

	// Returns true if the command is on cooldown, false otherwise
	private void checkCooldown(PlayerCommandPreprocessEvent event,
			Player player, String pre, String message, int warmUpSeconds,
			int cooldownTime, double price) {
		if (!blocked) {
			if (warmUpSeconds > 0) {
				if (!player.hasPermission("booscooldowns.nowarmup")
						&& !player.hasPermission("booscooldowns.nowarmup."
								+ message)) {
					start(event, player, message, warmUpSeconds, cooldownTime);
				}
			} else {
				if (boosCoolDownManager.coolDown(player, message, cooldownTime)) {
					event.setCancelled(true);
				}
			}
			if (!event.isCancelled()) {
				payForCommand(event, player, message, price);
			}
		} else {
			event.setCancelled(true);
			String msg = String.format(boosConfigManager
					.getCommandBlockedMessage());
			boosChat.sendMessageToPlayer(player, msg);
		}
		if (!event.isCancelled()) {
			boosCoolDownManager.setUses(player, pre, message);
			if (boosConfigManager.getCommandLogging()) {
				boosCoolDown.commandLogger(player.getName(), message);
			}
		}
	}

	private boolean isPluginOnForPlayer(Player player) {
		boolean on;
		if (player.isOp()) {
			on = false;
		}
		if (player.hasPermission("booscooldowns.exception")) {
			on = false;
		} else if (player.isOp()) {
			on = false;
		} else {
			on = true;
		}
		return on;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();
		String message = event.getMessage().trim().replaceAll(" +", " ");
		String confCmd = "";
		Set<String> aliases = boosConfigManager.getAliases();
		Set<String> warmups = boosConfigManager.getWarmups(player);
		Set<String> cooldowns = boosConfigManager.getCooldowns(player);
		Set<String> limits = boosConfigManager.getLimits(player);
		Set<String> prices = boosConfigManager.getPrices(player);
		boolean on = true;
		boolean used = false;
		int warmupTime = 0;
		double price = 0;
		int limit = -1;
		int cooldownTime = 0;
		on = isPluginOnForPlayer(player);
		try {
			if (aliases.contains(message)) {
				message = boosConfigManager.getAlias(message);
				event.setMessage(message);
			}
		} catch (NullPointerException e) {
			boosCoolDown
					.getLog()
					.warning(
							"Aliases section in config.yml is missing! Please delete your config.yml, restart server and set it again!");
		}
		if (on) {
			if (boosConfigManager.getWarmupEnabled()) {
				for (String warmup : warmups) {
					String warmup2 = warmup.replace("*", ".+");
					if (message.matches(warmup2)) {
						warmupTime = boosConfigManager
								.getWarmUp(warmup, player);
						boosCoolDown.log.info("Regex: " + warmup + "Command: "
								+ message);
						if (warmupTime > 0) {
							confCmd = warmup;
							playerloc.put(player, player.getLocation());
							playerworld
									.put(player, player.getWorld().getName());
						}
					}
				}
			}
			if (boosConfigManager.getCooldownEnabled()) {
				for (String cooldown : cooldowns) {
					String cooldown2 = cooldown.replace("*", ".+");
					if (message.matches(cooldown2)) {
						cooldownTime = boosConfigManager.getCoolDown(cooldown,
								player);
						if (cooldownTime > 0 && confCmd.equals("")) {
							confCmd = cooldown;
						}
					}
				}
			}
			if (boosConfigManager.getPriceEnabled()) {
				for (String pric : prices) {
					String pric2 = pric.replace("*", ".+");
					if (message.matches(pric2)) {
						price = boosConfigManager.getPrice(pric, player);
						if (price > 0 && confCmd.equals("")) {
							confCmd = pric;
						}
					}
				}
			}
			if (boosConfigManager.getLimitEnabled()) {
				for (String lim : limits) {
					String lim2 = lim.replace("*", ".+");
					if (message.matches(lim2)) {
						limit = boosConfigManager.getLimit(lim, player);
						if (limit > -1 && confCmd.equals("")) {
							confCmd = lim;
						}
					}
				}
			}
			blocked = blocked(player, message, limit);
			this.checkCooldown(event, player, confCmd, message, warmupTime,
					cooldownTime, price);
			used = true;
		}

		if (!used) {
			blocked = blocked(player, message, limit);
			this.checkCooldown(event, player, confCmd, message, warmupTime,
					cooldownTime, price);
			used = false;
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	private void onPlayerChat(AsyncPlayerChatEvent event) {
		String chatMessage = event.getMessage();
		String temp = "globalchat";
		double price = 0;
		Player player = event.getPlayer();
		int cooldownTime = boosConfigManager.getCoolDown(temp, player);
		if (chatMessage.startsWith("!")) {
			if (!boosCoolDownManager
					.checkCoolDownOK(player, temp, cooldownTime)) {
				event.setCancelled(true);
				return;
			} else {
				if (boosCoolDownManager.coolDown(player, temp, cooldownTime)) {
					event.setCancelled(true);
					return;
				}
			}
			price = boosConfigManager.getPrice(temp, player);
			payForCommand2(event, player, temp, price);
		}
	}

	private void payForCommand(PlayerCommandPreprocessEvent event,
			Player player, String pre, double price) {
		String name = player.getName();
		if (price > 0) {
			if (!player.hasPermission("booscooldowns.noprice")
					&& !player.hasPermission("booscooldowns.noprice." + pre)) {
				if (boosPriceManager.payForCommand(player, pre, price, name)) {
					return;
				} else {
					boosCoolDownManager.cancelCooldown(player, pre);
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	private void payForCommand2(AsyncPlayerChatEvent event, Player player,
			String pre, double price) {
		String name = player.getName();
		if (price > 0) {
			if (!player.hasPermission("booscooldowns.noprice")
					&& !player.hasPermission("booscooldowns.noprice." + pre)) {
				if (boosPriceManager.payForCommand(player, pre, price, name)) {
					return;
				} else {
					boosCoolDownManager.cancelCooldown(player, pre);
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	private void start(PlayerCommandPreprocessEvent event, Player player,
			String pre, int warmUpSeconds, int cooldownTime) {
		if (!boosCoolDownManager.checkWarmUpOK(player, pre)) {
			if (boosCoolDownManager.checkCoolDownOK(player, pre, cooldownTime)) {
				boosWarmUpManager.startWarmUp(this.plugin, player, pre,
						warmUpSeconds);
				event.setCancelled(true);
				return;
			} else {
				event.setCancelled(true);
				return;
			}
		} else {
			if (boosCoolDownManager.coolDown(player, pre, cooldownTime)) {
				event.setCancelled(true);
				return;
			} else {
				boosCoolDownManager.removeWarmUpOK(player, pre);
				return;
			}
		}
	}
}