package cz.boosik.boosCooldown;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import util.boosChat;

public class BoosXpCostManager {
	private static String msg = "";

	static boolean payXPForCommand(Player player, String regexCommand,
			String originalCommand, int xpPrice) {
		int xp = player.getLevel();
		Boolean trans = false;
		if (xp >= xpPrice) {
			player.setLevel(xp - xpPrice);
			trans = true;
		}
		if (trans) {
			msg = String.format(BoosConfigManager.getPaidXPForCommandMessage(),
					xpPrice);
			msg = msg.replaceAll("&command&", originalCommand);
			boosChat.sendMessageToPlayer(player, msg);
			return true;
		} else {
			return false;
		}
	}

	static void payXPForCommand(PlayerCommandPreprocessEvent event,
			Player player, String regexCommand, String originalCommand,
			int xpPrice) {
		if (xpPrice > 0) {
			if (!player.hasPermission("booscooldowns.noxpcost")
					&& !player.hasPermission("booscooldowns.noxpcost."
							+ originalCommand)) {
				if (payXPForCommand(player, regexCommand, originalCommand,
						xpPrice)) {
					return;
				} else {
					BoosCoolDownManager.cancelCooldown(player, regexCommand);
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	public static boolean has(Player player, int xpPrice) {
		if (xpPrice <= 0){
			return true;
		}
		int xp = player.getLevel();
		if (xp >= xpPrice) {
			return true;
		}
		return false;
	}
}
