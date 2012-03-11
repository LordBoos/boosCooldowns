package cz.boosik.boosCooldown;

import java.util.WeakHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import util.boosChat;

public class boosCoolDownListener implements Listener {
	private final boosCoolDown plugin;
	private static WeakHashMap<Player, Location> playerloc = new WeakHashMap<Player, Location>();
	private static WeakHashMap<Player, String> playerworld = new WeakHashMap<Player, String>();

	public boosCoolDownListener(boosCoolDown instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled()) {
			return;
		}
		String message = event.getMessage();
		Player player = event.getPlayer();
		boolean on = true;

		if (player.isOp()) {
			on = false;
		}
		if (boosCoolDown.isUsingPermissions()
				&& boosCoolDown.getPermissions().has(player,
						"booscooldowns.exception")) {
			on = false;
		} else if (player.isOp()) {
			on = false;
		} else {
			on = true;
		}

		if (on) {
			playerloc.put(player, player.getLocation());
			playerworld.put(player, player.getWorld().getName());
			int i = message.indexOf(' ');
			if (i < 0) {
				i = message.length();
			}

			String preCommand = message.substring(0, i);
			String messageCommand = message.substring(i, message.length());
			boolean used = false;
			int preSubCheck = 0;

			if (!used && messageCommand.length() > 1) {
				int j = messageCommand.indexOf(' ', 1);
				if (j < 0) {
					j = messageCommand.length();
				}

				String preSub = messageCommand.substring(1, j);
				String messageSub = messageCommand.substring(j,
						messageCommand.length());
				preSub = preCommand + ' ' + preSub;
				if (boosCoolDown.isUsingPermissions()) {
					if (boosCoolDown.getPermissions().has(player,
							"booscooldowns.warmup2")) {
						preSubCheck = boosConfigManager.getWarmUp2(player,
								preSub);
					} else if (boosCoolDown.getPermissions().has(player,
							"booscooldowns.warmup3")) {
						preSubCheck = boosConfigManager.getWarmUp3(player,
								preSub);
					} else if (boosCoolDown.getPermissions().has(player,
							"booscooldowns.warmup4")) {
						preSubCheck = boosConfigManager.getWarmUp4(player,
								preSub);
					} else if (boosCoolDown.getPermissions().has(player,
							"booscooldowns.warmup5")) {
						preSubCheck = boosConfigManager.getWarmUp5(player,
								preSub);
					} else {
						preSubCheck = boosConfigManager.getWarmUp(player,
								preSub);
					}
				} else {
					preSubCheck = boosConfigManager.getWarmUp(player, preSub);
				}
				if (preSubCheck > 0) {
					this.checkCooldown(event, player, preSub,
							messageSub);
					used = true;
				} else {
					this.checkCooldown(event, player, preCommand,
							messageCommand);
					used = true;
				}
			}
			if (!used) {
				this.checkCooldown(event, player, preCommand,
						messageCommand);
				used = false;
			}
		}
	}

	// Returns true if the command is on cooldown, false otherwise
	private boolean checkCooldown(PlayerCommandPreprocessEvent event,
			Player player, String pre, String message) {
		int warmUpSeconds = 0;
		if (boosCoolDown.isUsingPermissions()) {
			if (!boosCoolDown.getPermissions().has(player,
					"booscooldowns.warmup2")
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.warmup3")
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.warmup4")
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.warmup5")) {
				warmUpSeconds = boosConfigManager.getWarmUp(player, pre);
			}
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.warmup2")) {
				warmUpSeconds = boosConfigManager.getWarmUp2(player, pre);
			}
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.warmup3")) {
				warmUpSeconds = boosConfigManager.getWarmUp3(player, pre);
			}
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.warmup4")) {
				warmUpSeconds = boosConfigManager.getWarmUp4(player, pre);
			}
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.warmup5")) {
				warmUpSeconds = boosConfigManager.getWarmUp5(player, pre);
			}
		} else {
			warmUpSeconds = boosConfigManager.getWarmUp(player, pre);
		}
		if (boosCoolDown.isUsingPermissions()) {
			if (warmUpSeconds > 0
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.nowarmup")
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.nowarmup." + pre)) {
				if (!boosCoolDownManager.checkWarmUpOK(player, pre, message)) {
					if (boosCoolDownManager.checkCoolDownOK(player, pre,
							message)) {
						boosWarmUpManager.startWarmUp(this.plugin, player, pre,
								message, warmUpSeconds);
						event.setCancelled(true);
						return true;
					} else {
						event.setCancelled(true);
						return true;
					}
				} else {
					if (boosCoolDownManager.coolDown(player, pre, message)) {
						event.setCancelled(true);
						return true;
					} else {
						boosCoolDownManager
								.removeWarmUpOK(player, pre, message);
					}
				}
			} else {
				if (boosCoolDownManager.coolDown(player, pre, message)) {
					event.setCancelled(true);
					return true;
				}
			}
		} else {
			if (warmUpSeconds > 0) {
				if (!boosCoolDownManager.checkWarmUpOK(player, pre, message)) {
					if (boosCoolDownManager.checkCoolDownOK(player, pre,
							message)) {
						boosWarmUpManager.startWarmUp(this.plugin, player, pre,
								message, warmUpSeconds);
						event.setCancelled(true);
						return true;
					} else {
						event.setCancelled(true);
						return true;
					}
				} else {
					if (boosCoolDownManager.coolDown(player, pre, message)) {
						event.setCancelled(true);
						return true;
					} else {
						boosCoolDownManager
								.removeWarmUpOK(player, pre, message);
					}
				}
			} else {
				if (boosCoolDownManager.coolDown(player, pre, message)) {
					event.setCancelled(true);
					return true;
				}
			}
		}
		if (boosCoolDown.isUsingEconomy()) {
			if (boosConfigManager.getPrice(player, pre) > 0) {
				if (!boosCoolDown.getPermissions().has(player,
						"booscooldowns.noprice")
						&& !boosCoolDown.getPermissions().has(player,
								"booscooldowns.noprice." + pre)) {
					if (boosCoolDown.getEconomy().getBalance(player.getName()) >= boosConfigManager
							.getPrice(player, pre)) {
						boosPriceManager.payForCommand(player, pre, message);
					} else {
						boosPriceManager.payForCommand(player, pre, message);
						boosCoolDownManager.cancelCooldown(player, pre);
						event.setCancelled(true);
						return true;
					}
				}
			}
		}
		return false;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerMove(PlayerMoveEvent event) {
		if (!boosConfigManager.getCancelWarmupOnMove())
			return;

		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		if (boosCoolDown.isUsingPermissions()) {
			if (player != null
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.nocancel.move")) {
				if (boosWarmUpManager.hasWarmUps(player) && hasMoved(player)) {
					clearLocWorld(player);
					boosChat.sendMessageToPlayer(player,
							boosConfigManager.getWarmUpCancelledByMoveMessage());
					boosWarmUpManager.cancelWarmUps(player);
				}

			}
		} else {
			if (player != null) {
				if (boosWarmUpManager.hasWarmUps(player) && hasMoved(player)) {
					clearLocWorld(player);
					boosChat.sendMessageToPlayer(player,
							boosConfigManager.getWarmUpCancelledByMoveMessage());
					boosWarmUpManager.cancelWarmUps(player);
				}

			}
		}
	}

	public static boolean hasMoved(Player player) {
		String curworld = player.getWorld().getName();
		String cmdworld = playerworld.get(player);
		Location curloc = player.getLocation();
		Location cmdloc = playerloc.get(player);
		if (!curworld.equals(cmdworld)) {
			return true;
		} else if (cmdloc.distanceSquared(curloc) > 2) {
			return true;
		}

		return false;
	}

	public static void clearLocWorld(Player player) {
		playerloc.remove(player);
		playerworld.remove(player);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		if (!boosConfigManager.getCancelWarmupOnSneak())
			return;

		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		if (boosCoolDown.isUsingPermissions()) {
			if (player != null
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.nocancel.sneak")) {
				if (boosWarmUpManager.hasWarmUps(player)) {
					boosChat.sendMessageToPlayer(player,
							boosConfigManager.getCancelWarmupOnSneakMessage());
					boosWarmUpManager.cancelWarmUps(player);
				}

			}
		} else {
			if (player != null) {
				if (boosWarmUpManager.hasWarmUps(player)) {
					boosChat.sendMessageToPlayer(player,
							boosConfigManager.getCancelWarmupOnSneakMessage());
					boosWarmUpManager.cancelWarmUps(player);
				}

			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
		if (!boosConfigManager.getCancelWarmupOnSprint())
			return;

		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		if (boosCoolDown.isUsingPermissions()) {
			if (player != null
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.nocancel.sprint")) {
				if (boosWarmUpManager.hasWarmUps(player)) {
					boosChat.sendMessageToPlayer(player,
							boosConfigManager.getCancelWarmupOnSprintMessage());
					boosWarmUpManager.cancelWarmUps(player);
				}

			}
		} else {
			if (player != null) {
				if (boosWarmUpManager.hasWarmUps(player)) {
					boosChat.sendMessageToPlayer(player,
							boosConfigManager.getCancelWarmupOnSprintMessage());
					boosWarmUpManager.cancelWarmUps(player);
				}

			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamage(EntityDamageEvent event) {
		if (!boosConfigManager.getCancelWarmUpOnDamage())
			return;

		if (event.isCancelled())
			return;

		Entity entity = event.getEntity();
		if (entity != null && entity instanceof Player) {
			Player player = (Player) entity;
			if (boosCoolDown.isUsingPermissions()) {
				if (player != null
						&& !boosCoolDown.getPermissions().has(player,
								"booscooldowns.nocancel.damage")) {
					if (boosWarmUpManager.hasWarmUps(player)) {
						boosChat.sendMessageToPlayer(player, boosConfigManager
								.getWarmUpCancelledByDamageMessage());
						boosWarmUpManager.cancelWarmUps(player);
					}

				}
			} else {
				if (player != null) {
					if (boosWarmUpManager.hasWarmUps(player)) {
						boosChat.sendMessageToPlayer(player, boosConfigManager
								.getWarmUpCancelledByDamageMessage());
						boosWarmUpManager.cancelWarmUps(player);
					}

				}
			}
		}
	}
}