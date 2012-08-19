package cz.boosik.boosCooldown;

import java.util.Iterator;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import util.boosChat;

public class boosWarmUpManager {

	private static ConcurrentHashMap<String, boosWarmUpTimer> playercommands = new ConcurrentHashMap<String, boosWarmUpTimer>();

	static Timer scheduler;

	public static void startWarmUp(boosCoolDown bCoolDown, Player player,
			String pre, String message, int warmUpSeconds) {
		pre = pre.toLowerCase();
		long warmUpMinutes = Math.round(warmUpSeconds / 60);
		long warmUpHours = Math.round(warmUpMinutes / 60);
		if (!isWarmUpProcess(player, pre, message)) {
			boosCoolDownManager.removeWarmUpOK(player, pre, message);
			String msg = boosConfigManager.getWarmUpMessage();
			msg = msg.replaceAll("&command&", pre);
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
					scheduler, player, pre, message);
			playercommands.put(player.getName() + "@" + pre, scheduleMe);
			scheduler.schedule(scheduleMe, warmUpSeconds * 1000);
		} else {
			String msg = boosConfigManager.getWarmUpAlreadyStartedMessage();
			msg = msg.replaceAll("&command&", pre);
			boosChat.sendMessageToPlayer(player, msg);
		}
	}

	public static boolean isWarmUpProcess(Player player, String pre,
			String message) {
		pre = pre.toLowerCase();
		if (playercommands.containsKey(player.getName() + "@" + pre)) {
			return true;
		}
		return false;
	}

	public static void removeWarmUpProcess(String tag) {
		boosWarmUpManager.playercommands.remove(tag);
	}

//	public static void cancelWarmUps(Player player) {
//		for (String key : playercommands.keySet()) {
//			if (key.startsWith(player.getName() + "@")) {
//				removeWarmUpProcess(key);
//			}
//		}
//	}

	public static void cancelWarmUps(Player player) {
		Iterator<String> iter = playercommands.keySet().iterator();
		while (iter.hasNext()) {
			if (iter.next().startsWith(player.getName() + "@")) {
				killTimer(player);
				iter.remove();
			}
		}
	}
	public static void killTimer(Player player) {
	for (String key : playercommands.keySet()) {
		if (key.startsWith(player.getName() + "@")) {
			playercommands.get(key).cancel();
		}
	}
	}
	
	public static boolean hasWarmUps(Player player) {
		for (String key : playercommands.keySet()) {
			if (key.startsWith(player.getName() + "@")) {
				return true;
			}
		}
		return false;
	}
}
