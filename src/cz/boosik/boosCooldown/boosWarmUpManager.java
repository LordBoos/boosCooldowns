package cz.boosik.boosCooldown;

import java.util.Iterator;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import util.boosChat;

/**
 * @author Jakub
 *
 */
/**
 * @author Jakub
 *
 */
public class boosWarmUpManager {

	private static ConcurrentHashMap<String, boosWarmUpTimer> playercommands = new ConcurrentHashMap<String, boosWarmUpTimer>();
	private static ConcurrentHashMap<Player, Location> playerloc = new ConcurrentHashMap<Player, Location>();
	private static ConcurrentHashMap<Player, String> playerworld = new ConcurrentHashMap<Player, String>();

	private static Timer scheduler;

	/**
	 * @param player
	 * @param regexCommand
	 * @param warmUpSeconds
	 */
	static void applyPotionEffect(Player player, String regexCommand,
			int warmUpSeconds) {
		String potion = boosConfigManager.getPotionEffect(regexCommand, player);
		if (potion.equals("")) {
			return;
		}
		int potionStrength = boosConfigManager.getPotionEffectStrength(
				regexCommand, player);
		if (potionStrength == 0) {
			return;
		}
		PotionEffectType effect = PotionEffectType.getByName(potion);
		player.addPotionEffect(
				effect.createEffect(warmUpSeconds * 40, potionStrength - 1),
				true);
	}

	/**
	 * @param player
	 */
	public static void cancelWarmUps(Player player) {
		Iterator<String> iter = playercommands.keySet().iterator();
		while (iter.hasNext()) {
			if (iter.next().startsWith(player.getName() + "@")) {
				killTimer(player);
				iter.remove();
			}
		}
	}

	/**
	 * @param player
	 */
	public static void clearLocWorld(Player player) {
		boosWarmUpManager.playerloc.remove(player);
		boosWarmUpManager.playerworld.remove(player);
	}

	/**
	 * @param player
	 * @return
	 */
	public static boolean hasWarmUps(Player player) {
		for (String key : playercommands.keySet()) {
			if (key.startsWith(player.getName() + "@")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param player
	 * @param regexCommand
	 * @return
	 */
	static boolean checkWarmUpOK(Player player, String regexCommand) {
		int pre2 = regexCommand.toLowerCase().hashCode();
		int ok = 0;
		ok = boosConfigManager.getConfusers().getInt(
				"users." + player.getName().toLowerCase().hashCode()
						+ ".warmup." + pre2, ok);
		if (ok == 1) {
			return true;
		}
		return false;
	}

	/**
	 * @param player
	 * @param regexCommand
	 * @return
	 */
	static boolean isWarmUpProcess(Player player, String regexCommand) {
		regexCommand = regexCommand.toLowerCase();
		if (playercommands.containsKey(player.getName() + "@" + regexCommand)) {
			return true;
		}
		return false;
	}

	/**
	 * @param player
	 */
	static void killTimer(Player player) {
		for (String key : playercommands.keySet()) {
			if (key.startsWith(player.getName() + "@")) {
				playercommands.get(key).cancel();
			}
		}
	}

	/**
	 * @param player
	 * @param regexCommand
	 */
	static void removeWarmUp(Player player, String regexCommand) {
		int pre2 = regexCommand.toLowerCase().hashCode();
		boosConfigManager.getConfusers().set(
				"users." + player.getName().toLowerCase().hashCode()
						+ ".warmup." + pre2, null);
	}

	/**
	 * @param player
	 * @param regexCommand
	 */
	static void removeWarmUpOK(Player player, String regexCommand) {
		int pre2 = regexCommand.toLowerCase().hashCode();
		boosConfigManager.getConfusers().set(
				"users." + player.getName().toLowerCase().hashCode()
						+ ".warmup." + pre2, null);
	}

	/**
	 * @param tag
	 */
	static void removeWarmUpProcess(String tag) {
		boosWarmUpManager.playercommands.remove(tag);
	}

	/**
	 * @param player
	 * @param regexCommand
	 */
	static void setWarmUpOK(Player player, String regexCommand) {
		int pre2 = regexCommand.toLowerCase().hashCode();
		boosConfigManager.getConfusers().set(
				"users." + player.getName().toLowerCase().hashCode()
						+ ".warmup." + pre2, 1);
	}

	/**
	 * @param bCoolDown
	 * @param player
	 * @param regexCommand
	 * @param originalCommand
	 * @param warmUpSeconds
	 */
	static void startWarmUp(boosCoolDown bCoolDown, Player player,
			String regexCommand, String originalCommand, int warmUpSeconds) {
		regexCommand = regexCommand.toLowerCase();
		long warmUpMinutes = Math.round(warmUpSeconds / 60);
		long warmUpHours = Math.round(warmUpMinutes / 60);
		if (!isWarmUpProcess(player, regexCommand)) {
			boosWarmUpManager.removeWarmUpOK(player, regexCommand);
			String msg = boosConfigManager.getWarmUpMessage();
			msg = msg.replaceAll("&command&", originalCommand);
			if (warmUpSeconds >= 60 && 3600 >= warmUpSeconds) {
				msg = msg.replaceAll("&seconds&", Long.toString(warmUpMinutes));
				msg = msg.replaceAll("&unit&",
						boosConfigManager.getUnitMinutesMessage());
			} else if (warmUpMinutes >= 60) {
				msg = msg.replaceAll("&seconds&", Long.toString(warmUpHours));
				msg = msg.replaceAll("&unit&",
						boosConfigManager.getUnitHoursMessage());
			} else {
				msg = msg.replaceAll("&seconds&", Long.toString(warmUpSeconds));
				msg = msg.replaceAll("&unit&",
						boosConfigManager.getUnitSecondsMessage());
			}
			boosChat.sendMessageToPlayer(player, msg);

			scheduler = new Timer();
			boosWarmUpTimer scheduleMe = new boosWarmUpTimer(bCoolDown,
					scheduler, player, regexCommand, originalCommand);
			playercommands.put(player.getName() + "@" + regexCommand,
					scheduleMe);
			scheduler.schedule(scheduleMe, warmUpSeconds * 1000);
			applyPotionEffect(player, regexCommand, warmUpSeconds);
		} else {
			String msg = boosConfigManager.getWarmUpAlreadyStartedMessage();
			msg = msg.replaceAll("&command&", originalCommand);
			boosChat.sendMessageToPlayer(player, msg);
		}
	}

	/**
	 * @return
	 */
	public static ConcurrentHashMap<Player, String> getPlayerworld() {
		return playerworld;
	}

	/**
	 * @return
	 */
	public static ConcurrentHashMap<Player, Location> getPlayerloc() {
		return playerloc;
	}
}
