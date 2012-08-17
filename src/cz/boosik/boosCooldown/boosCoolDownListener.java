package cz.boosik.boosCooldown;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import util.boosChat;

public class boosCoolDownListener<a> implements Listener {
	private final boosCoolDown plugin;
	private boolean blocked = false;
	private static ConcurrentHashMap<Player, Location> playerloc = new ConcurrentHashMap<Player, Location>();
	private static ConcurrentHashMap<Player, String> playerworld = new ConcurrentHashMap<Player, String>();

	public boosCoolDownListener(boosCoolDown instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled()) {
			return;
		}
		String message = event.getMessage();
		message = message.trim().replaceAll(" +", " ");
		Player player = event.getPlayer();
		boolean on = true;
		on = isPluginOnForPlayer(player);

		if (on) {
			boolean used = false;
			String messageCommand = "";
			String preSub = "";
			String preSub2 = "";
			String preSub3 = "";
			String messageSub = "";
			String messageSub2 = "";
			String messageSub3 = "";
			int preSubCheck = -1;
			int preSubCheck2 = -1;
			int preSubCheck3 = -1;
			int price = 0;
			int limit = 0;
			int cd = 0;
			playerloc.put(player, player.getLocation());
			playerworld.put(player, player.getWorld().getName());
			String[] splitCommand;
			splitCommand = message.split(" ");
			String preCommand = splitCommand[0];
			if (splitCommand.length > 1) {
				for (int i = 1; i < splitCommand.length; i++) {
					messageCommand = messageCommand + " " + splitCommand[i];
				}
			}
			if (splitCommand.length > 1) {
				preSub = splitCommand[0] + " " + splitCommand[1];
				for (int i = 2; i < splitCommand.length; i++) {
					messageSub = messageSub + " " + splitCommand[i];
				}
			}
			if (splitCommand.length > 2) {
				preSub2 = splitCommand[0] + " " + splitCommand[1] + " "
						+ splitCommand[2];
				for (int i = 3; i < splitCommand.length; i++) {
					messageSub2 = messageSub2 + " " + splitCommand[i];
				}
			}
			if (splitCommand.length > 3) {
				preSub3 = splitCommand[0] + " " + splitCommand[1] + " "
						+ splitCommand[2] + " " + splitCommand[3];
				for (int i = 4; i < splitCommand.length; i++) {
					messageSub3 = messageSub3 + " " + splitCommand[i];
				}
			}
			if (preSub3.length() > 0) {
				if (preSub3 != null) {
					preSubCheck3 = preSubCheck(player, preSub3);
					if (preSubCheck3 < 0) {
						price = prePriceCheck(player, preSub3);
						cd = preCDCheck(player, preSub3);
						limit = preLimitCheck(player, preSub3);
						if (cd > 0) {
							preSubCheck3 = 0;
						} else if (price > 0) {
							preSubCheck3 = 0;
						} else if (limit > 0) {
							preSubCheck3 = 0;
						}
					}
				}
			}
			if (preSub2.length() > 0) {
				if (preSub2 != null && preSubCheck3 < 0) {
					preSubCheck2 = preSubCheck(player, preSub2);
					if (preSubCheck2 < 0) {
						price = prePriceCheck(player, preSub2);
						cd = preCDCheck(player, preSub2);
						limit = preLimitCheck(player, preSub2);
						if (cd > 0) {
							preSubCheck2 = 0;
						} else if (price > 0) {
							preSubCheck2 = 0;
						} else if (limit > 0) {
							preSubCheck2 = 0;
						}
					}
				}
			}
			if (preSub.length() > 0) {
				if (preSub.length() < 1 || preSub != null && preSubCheck2 < 0) {
					preSubCheck = preSubCheck(player, preSub);
					if (preSubCheck < 0) {
						price = prePriceCheck(player, preSub);
						cd = preCDCheck(player, preSub);
						limit = preLimitCheck(player, preSub);
						if (cd > 0) {
							preSubCheck = 0;
						} else if (price > 0) {
							preSubCheck = 0;
						} else if (limit > 0) {
							preSubCheck = 0;
						}
					}
				}
			}
			if (preSubCheck3 >= 0) {
				blocked = blocked(player, preSub3, messageSub3);
				this.checkCooldown(event, player, preSub3, messageSub3,
						preSubCheck3, price);
				used = true;
			} else if (preSubCheck2 >= 0) {
				blocked = blocked(player, preSub2, messageSub2);
				this.checkCooldown(event, player, preSub2, messageSub2,
						preSubCheck2, price);
				used = true;
			} else if (preSubCheck >= 0) {
				blocked = blocked(player, preSub, messageSub);
				this.checkCooldown(event, player, preSub, messageSub,
						preSubCheck, price);
				used = true;
			} else {
				blocked = blocked(player, preCommand, messageCommand);
				int preCmdCheck = preSubCheck(player, preCommand);
				price = prePriceCheck(player, preCommand);
				this.checkCooldown(event, player, preCommand, messageCommand,
						preCmdCheck, price);
				used = true;
			}

			if (!used) {
				blocked = blocked(player, preCommand, messageCommand);
				int preCmdCheck = preSubCheck(player, preCommand);
				price = prePriceCheck(player, preCommand);
				this.checkCooldown(event, player, preCommand, messageCommand,
						preCmdCheck, price);
				used = false;
			}
		}
	}

	private int preSubCheck(Player player, String preSub) {
		if (boosCoolDown.isUsingPermissions()) {
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.warmup2")) {
				return boosConfigManager.getWarmUp2(preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.warmup3")) {
				return boosConfigManager.getWarmUp3(preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.warmup4")) {
				return boosConfigManager.getWarmUp4(preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.warmup5")) {
				return boosConfigManager.getWarmUp5(preSub);
			} else {
				return boosConfigManager.getWarmUp(preSub);
			}
		} else {
			return boosConfigManager.getWarmUp(preSub);
		}
	}

	private int preLimitCheck(Player player, String preSub) {
		if (boosCoolDown.isUsingPermissions()) {
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.limit2")) {
				return boosConfigManager.getLimit2(preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.limit3")) {
				return boosConfigManager.getLimit3(preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.limit4")) {
				return boosConfigManager.getLimit4(preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.limit5")) {
				return boosConfigManager.getLimit5(preSub);
			} else {
				return boosConfigManager.getLimit(preSub);
			}
		} else {
			return boosConfigManager.getLimit(preSub);
		}
	}

	private int preCDCheck(Player player, String preSub) {
		if (boosCoolDown.isUsingPermissions()) {
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown2")) {
				return boosConfigManager.getCoolDown2(preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown3")) {
				return boosConfigManager.getCoolDown3(preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown4")) {
				return boosConfigManager.getCoolDown4(preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown5")) {
				return boosConfigManager.getCoolDown5(preSub);
			} else {
				return boosConfigManager.getCoolDown(preSub);
			}
		} else {
			return boosConfigManager.getCoolDown(preSub);
		}
	}

	public int prePriceCheck(Player player, String preSub) {
		if (boosCoolDown.isUsingPermissions()) {
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown2")) {
				return boosConfigManager.getPrice2(preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown3")) {
				return boosConfigManager.getPrice3(preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown4")) {
				return boosConfigManager.getPrice4(preSub);
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown5")) {
				return boosConfigManager.getPrice5(preSub);
			} else {
				return boosConfigManager.getPrice(preSub);
			}
		} else {
			return boosConfigManager.getPrice(preSub);
		}
	}

	private boolean blocked(Player player, String pre, String msg) {
		int limit = -1;
		int uses = boosCoolDownManager.getUses(player, pre, msg);
		if (boosCoolDown.isUsingPermissions()) {
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.nolimit")
					|| boosCoolDown.getPermissions().has(player,
							"booscooldowns.nolimit." + pre)) {
			} else {
				if (boosCoolDown.getPermissions().has(player,
						"booscooldowns.limit2")) {
					limit = boosConfigManager.getLimit2(pre);
					if (limit == -1) {
						return false;
					} else if (limit <= uses) {
						return true;
					}
				} else if (boosCoolDown.getPermissions().has(player,
						"booscooldowns.limit3")) {
					limit = boosConfigManager.getLimit3(pre);
					if (limit == -1) {
						return false;
					} else if (limit <= uses) {
						return true;
					}
				} else if (boosCoolDown.getPermissions().has(player,
						"booscooldowns.limit4")) {
					limit = boosConfigManager.getLimit4(pre);
					if (limit == -1) {
						return false;
					} else if (limit <= uses) {
						return true;
					}
				} else if (boosCoolDown.getPermissions().has(player,
						"booscooldowns.limit5")) {
					limit = boosConfigManager.getLimit5(pre);
					if (limit == -1) {
						return false;
					} else if (limit <= uses) {
						return true;
					}
				} else {
					limit = boosConfigManager.getLimit(pre);
					if (limit == -1) {
						return false;
					} else if (limit <= uses) {
						return true;
					}
				}
			}
		} else {
			limit = boosConfigManager.getLimit(pre);
			if (limit == -1) {
				return false;
			} else if (limit <= uses) {
				return true;
			}
		}
		return false;
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
	private void checkCooldown(PlayerCommandPreprocessEvent event,
			Player player, String pre, String message, int warmUpSeconds,
			int price) {
		if (!blocked) {
			// int warmUpSeconds = 0;
			// warmUpSeconds = preSubCheck(player, pre);
			if (boosCoolDown.isUsingPermissions()) {
				if (warmUpSeconds > 0) {
					if (!boosCoolDown.getPermissions().has(player,
							"booscooldowns.nowarmup")
							&& !boosCoolDown.getPermissions().has(player,
									"booscooldowns.nowarmup." + pre)) {
						start(event, player, pre, message, warmUpSeconds);
					}
				} else {
					if (boosCoolDownManager.coolDown(player, pre)) {
						event.setCancelled(true);
					}
				}
			} else {
				if (warmUpSeconds > 0) {
					start(event, player, pre, message, warmUpSeconds);
				} else {
					if (boosCoolDownManager.coolDown(player, pre)) {
						event.setCancelled(true);
					}
				}
			}
			if (!event.isCancelled() && boosCoolDown.isUsingEconomy()) {
				payForCommand(event, player, pre, message, price);
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
				boosCoolDown.commandLogger(player.getName(), pre + message);
			}
		}
	}

	private void payForCommand(PlayerCommandPreprocessEvent event,
			Player player, String pre, String message, int price) {
		String name = player.getName();
		if (price > 0) {
			if (!boosCoolDown.getPermissions().has(player,
					"booscooldowns.noprice")
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.noprice." + pre)) {
				if (boosPriceManager.payForCommand(player, pre, price, name)) {
					return;
				} else {
					// boosPriceManager.payForCommand(player, pre, price, name);
					boosCoolDownManager.cancelCooldown(player, pre);
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	private void start(PlayerCommandPreprocessEvent event, Player player,
			String pre, String message, int warmUpSeconds) {
		if (!boosCoolDownManager.checkWarmUpOK(player, pre, message)) {
			if (boosCoolDownManager.checkCoolDownOK(player, pre, message)) {
				boosWarmUpManager.startWarmUp(this.plugin, player, pre,
						message, warmUpSeconds);
				event.setCancelled(true);
				return;
			} else {
				event.setCancelled(true);
				return;
			}
		} else {
			if (boosCoolDownManager.coolDown(player, pre)) {
				event.setCancelled(true);
				return;
			} else {
				boosCoolDownManager.removeWarmUpOK(player, pre, message);
				return;
			}
		}
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
						if (event.getClickedBlock().getType().name()
								.equals("CHEST")
								|| event.getClickedBlock().getType().name()
										.equals("FURNACE")
								|| event.getClickedBlock().getType().name()
										.equals("BURNING_FURNACE")
								|| event.getClickedBlock().getType().name()
										.equals("WORKBENCH")
								|| event.getClickedBlock().getType().name()
										.equals("DISPENSER")
								|| event.getClickedBlock().getType().name()
										.equals("JUKEBOX")
								|| event.getClickedBlock().getType().name()
										.equals("LOCKED_CHEST")
								|| event.getClickedBlock().getType().name()
										.equals("ENCHANTMENT_TABLE")
								|| event.getClickedBlock().getType().name()
										.equals("BREWING_STAND")
								|| event.getClickedBlock().getType().name()
										.equals("CAULDRON")
								|| event.getClickedBlock().getType().name()
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
						if (event.getClickedBlock().getType().name()
								.equals("CHEST")
								|| event.getClickedBlock().getType().name()
										.equals("FURNACE")
								|| event.getClickedBlock().getType().name()
										.equals("BURNING_FURNACE")
								|| event.getClickedBlock().getType().name()
										.equals("WORKBENCH")
								|| event.getClickedBlock().getType().name()
										.equals("DISPENSER")
								|| event.getClickedBlock().getType().name()
										.equals("JUKEBOX")
								|| event.getClickedBlock().getType().name()
										.equals("LOCKED_CHEST")
								|| event.getClickedBlock().getType().name()
										.equals("ENCHANTMENT_TABLE")
								|| event.getClickedBlock().getType().name()
										.equals("BREWING_STAND")
								|| event.getClickedBlock().getType().name()
										.equals("CAULDRON")
								|| event.getClickedBlock().getType().name()
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

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (!boosConfigManager.getCleanCooldownsOnDeath()
				&& !boosConfigManager.getCleanUsesOnDeath())
			return;
		Entity entity = event.getEntity();
		if (entity != null && entity instanceof Player) {
			Player player = (Player) entity;
			if (boosCoolDown.isUsingPermissions()) {
				if (player != null
						&& boosCoolDown.getPermissions().has(player,
								"booscooldowns.clear.cooldowns.death")) {
					if (boosConfigManager.getCleanCooldownsOnDeath()) {
						boosCoolDownManager.clearSomething("cooldown", player
								.getName().toLowerCase());
					}
				}
				if (player != null
						&& boosCoolDown.getPermissions().has(player,
								"booscooldowns.clear.uses.death")) {
					if (boosConfigManager.getCleanUsesOnDeath()) {
						boosCoolDownManager.clearSomething("uses", player
								.getName().toLowerCase());
					}
				}
			} else {
				if (player != null) {
					if (boosConfigManager.getCleanCooldownsOnDeath()) {
						boosCoolDownManager.clearSomething("cooldown", player
								.getName().toLowerCase());
					}
					if (boosConfigManager.getCleanUsesOnDeath()) {
						boosCoolDownManager.clearSomething("uses", player
								.getName().toLowerCase());
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		String chatMessage = event.getMessage();
		if (chatMessage.startsWith("!")) {
			String temp = "globalchat";
			if (!boosCoolDownManager.checkCoolDownOK(event.getPlayer(), temp,
					chatMessage)) {
				event.setCancelled(true);
				return;
			} else {
				if (boosCoolDownManager.coolDown(event.getPlayer(), temp)) {
					event.setCancelled(true);
					return;
				} else {
					return;
				}
			}
		}
	}
}