package cz.boosik.boosCooldown;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import util.boosChat;

public class boosCoolDownListener implements Listener {
	private final boosCoolDown plugin;
	private boolean blocked = false;
	private static ConcurrentHashMap<Player, Location> playerloc = new ConcurrentHashMap<Player, Location>();
	private static ConcurrentHashMap<Player, String> playerworld = new ConcurrentHashMap<Player, String>();

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
		on = isPluginOnForPlayer(player);

		if (on) {
			playerloc.put(player, player.getLocation());
			playerworld.put(player, player.getWorld().getName());
			int i = message.lastIndexOf(' ');
			if (i < 0) {
				i = message.length();
			}

			String preCommand = message.substring(0, i);
			String messageCommand = message.substring(i, message.length());
			boolean used = false;
			int preSubCheck = -1;
			if (!used && messageCommand.length() > 1) {
				int j = messageCommand.indexOf(' ', 1);
				if (j < 0) {
					j = messageCommand.length();
				}

				String preSub = messageCommand.substring(1, j);
				String messageSub = messageCommand.substring(j,
						messageCommand.length());
				preSub = preCommand + ' ' + preSub;
				preSubCheck = preSubCheck(player, preSub);
				if (preCDCheck(player, preSub) > 0) {
					preSubCheck = 0;
				}
				if (prePriceCheck(player, preSub) > 0) {
					preSubCheck = 0;
				}
				if (preLimitCheck(player, preSub) > 0) {
					preSubCheck = 0;
				}
				if (preSubCheck >= 0) {
					blocked = blocked(player, preSub, messageSub);
					this.checkCooldown(event, player, preSub, messageSub);
					used = true;
				} else {
					blocked = blocked(player, preCommand, messageCommand);
					this.checkCooldown(event, player, preCommand,
							messageCommand);
					used = true;
				}
			}
			if (!used) {
				blocked = blocked(player, preCommand, messageCommand);
				this.checkCooldown(event, player, preCommand, messageCommand);
				used = false;
			}
		}
	}

	private int preSubCheck(Player player, String preSub) {
		int preSubCheck;
		if (boosCoolDown.isUsingPermissions()) {
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.warmup2")) {
				preSubCheck = boosConfigManager.getWarmUp2(player, preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.warmup3")) {
				preSubCheck = boosConfigManager.getWarmUp3(player, preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.warmup4")) {
				preSubCheck = boosConfigManager.getWarmUp4(player, preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.warmup5")) {
				preSubCheck = boosConfigManager.getWarmUp5(player, preSub);
			} else {
				preSubCheck = boosConfigManager.getWarmUp(player, preSub);
			}
		} else {
			preSubCheck = boosConfigManager.getWarmUp(player, preSub);
		}
		return preSubCheck;
	}
	
	private int preLimitCheck(Player player, String preSub) {
		int preLimitCheck;
		if (boosCoolDown.isUsingPermissions()) {
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.limit2")) {
				preLimitCheck = boosConfigManager.getLimit2(player, preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.limit3")) {
				preLimitCheck = boosConfigManager.getLimit3(player, preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.limit4")) {
				preLimitCheck = boosConfigManager.getLimit4(player, preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.limit5")) {
				preLimitCheck = boosConfigManager.getLimit5(player, preSub);
			} else {
				preLimitCheck = boosConfigManager.getLimit(player, preSub);
			}
		} else {
			preLimitCheck = boosConfigManager.getLimit(player, preSub);
		}
		return preLimitCheck;
	}

	private int preCDCheck(Player player, String preSub) {
		int preCDCheck;
		if (boosCoolDown.isUsingPermissions()) {
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown2")) {
				preCDCheck = boosConfigManager.getCoolDown2(player, preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown3")) {
				preCDCheck = boosConfigManager.getCoolDown3(player, preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown4")) {
				preCDCheck = boosConfigManager.getCoolDown4(player, preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown5")) {
				preCDCheck = boosConfigManager.getCoolDown5(player, preSub);
			} else {
				preCDCheck = boosConfigManager.getCoolDown(player, preSub);
			}
		} else {
			preCDCheck = boosConfigManager.getCoolDown(player, preSub);
		}
		return preCDCheck;
	}

	private int prePriceCheck(Player player, String preSub) {
		int prePriceCheck;
		if (boosCoolDown.isUsingPermissions()) {
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown2")) {
				prePriceCheck = boosConfigManager.getPrice2(player, preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown3")) {
				prePriceCheck = boosConfigManager.getPrice3(player, preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown4")) {
				prePriceCheck = boosConfigManager.getPrice4(player, preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown5")) {
				prePriceCheck = boosConfigManager.getPrice5(player, preSub);
			} else {
				prePriceCheck = boosConfigManager.getPrice(player, preSub);
			}
		} else {
			prePriceCheck = boosConfigManager.getPrice(player, preSub);
		}
		return prePriceCheck;
	}

	private boolean blocked(Player player, String pre, String msg) {
		boolean blocked = false;
		int limit = -1;
		int uses = boosCoolDownManager.getUses(player, pre);
		if (boosCoolDown.isUsingPermissions()) {
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.nolimit")
					|| boosCoolDown.getPermissions().has(player,
							"booscooldowns.nolimit." + pre)) {
			} else {
				if (boosCoolDown.getPermissions().has(player,
						"booscooldowns.limit2")) {
					limit = boosConfigManager.getLimit2(player, pre);
					if (limit == -1) {
						blocked = false;
					} else if (limit <= uses) {
						blocked = true;
					}
				} else if (boosCoolDown.getPermissions().has(player,
						"booscooldowns.limit3")) {
					limit = boosConfigManager.getLimit3(player, pre);
					if (limit == -1) {
						blocked = false;
					} else if (limit <= uses) {
						blocked = true;
					}
				} else if (boosCoolDown.getPermissions().has(player,
						"booscooldowns.limit4")) {
					limit = boosConfigManager.getLimit4(player, pre);
					if (limit == -1) {
						blocked = false;
					} else if (limit <= uses) {
						blocked = true;
					}
				} else if (boosCoolDown.getPermissions().has(player,
						"booscooldowns.limit5")) {
					limit = boosConfigManager.getLimit5(player, pre);
					if (limit == -1) {
						blocked = false;
					} else if (limit <= uses) {
						blocked = true;
					}
				} else {
					limit = boosConfigManager.getLimit(player, pre);
					if (limit == -1) {
						blocked = false;
					} else if (limit <= uses) {
						blocked = true;
					}
				}
			}
		} else {
			limit = boosConfigManager.getLimit(player, pre);
			if (limit == -1) {
				blocked = false;
			} else if (limit <= uses) {
				blocked = true;
			}
		}
		return blocked;
	}

	private boolean isPluginOnForPlayer(Player player) {
		boolean on;
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
		return on;
	}

	// Returns true if the command is on cooldown, false otherwise
	private boolean checkCooldown(PlayerCommandPreprocessEvent event,
			Player player, String pre, String message) {
		if (!blocked) {
			int warmUpSeconds = 0;
			warmUpSeconds = getWarmupGroup(player, pre, warmUpSeconds);
			if (boosCoolDown.isUsingPermissions()) {
				if (warmUpSeconds > 0
						&& !boosCoolDown.getPermissions().has(player,
								"booscooldowns.nowarmup")
						&& !boosCoolDown.getPermissions().has(player,
								"booscooldowns.nowarmup." + pre)) {
					if (!boosCoolDownManager
							.checkWarmUpOK(player, pre, message)) {
						if (boosCoolDownManager.checkCoolDownOK(player, pre,
								message)) {
							boosWarmUpManager.startWarmUp(this.plugin, player,
									pre, message, warmUpSeconds);
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
							boosCoolDownManager.removeWarmUpOK(player, pre,
									message);
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
					if (!boosCoolDownManager
							.checkWarmUpOK(player, pre, message)) {
						if (boosCoolDownManager.checkCoolDownOK(player, pre,
								message)) {
							boosWarmUpManager.startWarmUp(this.plugin, player,
									pre, message, warmUpSeconds);
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
							boosCoolDownManager.removeWarmUpOK(player, pre,
									message);
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
						if (boosCoolDown.getEconomy().getBalance(
								player.getName()) >= boosConfigManager
								.getPrice(player, pre)) {
							boosPriceManager
									.payForCommand(player, pre, message);
							if (boosConfigManager.getCommandLogging()) {
								boosCoolDown.commandLogger(player.getName(),
										message);
							}
						} else {
							boosPriceManager
									.payForCommand(player, pre, message);
							boosCoolDownManager.cancelCooldown(player, pre);
							event.setCancelled(true);
							return true;
						}
					}
				}
			}
		} else {
			event.setCancelled(true);
			String msg = String.format(boosConfigManager
					.getCommandBlockedMessage());
			boosChat.sendMessageToPlayer(player, msg);
			return false;
		}
		if (!event.isCancelled()) {
			boosCoolDownManager.setUses(player, pre, message);
			if (boosConfigManager.getCommandLogging()) {
				boosCoolDown.commandLogger(player.getName(), pre + message);
			}
		}
		return false;
	}

	private int getWarmupGroup(Player player, String pre, int warmUpSeconds) {
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
		return warmUpSeconds;
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

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!boosConfigManager.getBlockInteractDuringWarmup())
			return;

		if (event.isCancelled())
			return;

		Entity entity = event.getPlayer();
		if (entity != null && entity instanceof Player) {
			Player player = (Player) entity;
			if (boosCoolDown.isUsingPermissions()) {
				if (player != null
						&& !boosCoolDown.getPermissions().has(player,
								"booscooldowns.dontblock.interact")) {
					if (boosWarmUpManager.hasWarmUps(player)) {
						if (event.getClickedBlock().getType().equals("CHEST")
								|| event.getClickedBlock().getType()
										.equals("FURNACE")
								|| event.getClickedBlock().getType()
										.equals("BURNING_FURNACE")
								|| event.getClickedBlock().getType()
										.equals("WORKBENCH")
								|| event.getClickedBlock().getType()
										.equals("DISPENSER")
								|| event.getClickedBlock().getType()
										.equals("JUKEBOX")
								|| event.getClickedBlock().getType()
										.equals("LOCKED_CHEST")
								|| event.getClickedBlock().getType()
										.equals("ENCHANTMENT_TABLE")
								|| event.getClickedBlock().getType()
										.equals("BREWING_STAND")
								|| event.getClickedBlock().getType()
										.equals("CAULDRON")
								|| event.getClickedBlock().getType()
										.equals("STORAGE_MINECART")) {
							event.setCancelled(true);
							boosChat.sendMessageToPlayer(player,
									boosConfigManager
											.getInteractBlockedMessage());
						}
					}

				}
			} else {
				if (player != null) {
					if (boosWarmUpManager.hasWarmUps(player)) {
						if (event.getClickedBlock().getType().equals("CHEST")
								|| event.getClickedBlock().getType()
										.equals("FURNACE")
								|| event.getClickedBlock().getType()
										.equals("BURNING_FURNACE")
								|| event.getClickedBlock().getType()
										.equals("WORKBENCH")
								|| event.getClickedBlock().getType()
										.equals("DISPENSER")
								|| event.getClickedBlock().getType()
										.equals("JUKEBOX")
								|| event.getClickedBlock().getType()
										.equals("LOCKED_CHEST")
								|| event.getClickedBlock().getType()
										.equals("ENCHANTMENT_TABLE")
								|| event.getClickedBlock().getType()
										.equals("BREWING_STAND")
								|| event.getClickedBlock().getType()
										.equals("CAULDRON")
								|| event.getClickedBlock().getType()
										.equals("STORAGE_MINECART")) {
							event.setCancelled(true);
							boosChat.sendMessageToPlayer(player,
									boosConfigManager
											.getInteractBlockedMessage());
						}
					}

				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
		if (!boosConfigManager.getCancelWarmUpOnGameModeChange())
			return;

		if (event.isCancelled())
			return;

		Entity entity = event.getPlayer();
		if (entity != null && entity instanceof Player) {
			Player player = (Player) entity;
			if (boosCoolDown.isUsingPermissions()) {
				if (player != null
						&& !boosCoolDown.getPermissions().has(player,
								"booscooldowns.nocancel.gamemodechange")) {
					if (boosWarmUpManager.hasWarmUps(player)) {
						boosChat.sendMessageToPlayer(player, boosConfigManager
								.getCancelWarmupByGameModeChangeMessage());
						boosWarmUpManager.cancelWarmUps(player);
					}

				}
			} else {
				if (player != null) {
					if (boosWarmUpManager.hasWarmUps(player)) {
						boosChat.sendMessageToPlayer(player, boosConfigManager
								.getCancelWarmupByGameModeChangeMessage());
						boosWarmUpManager.cancelWarmUps(player);
					}

				}
			}
		}
	}
}