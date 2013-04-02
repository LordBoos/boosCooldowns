package cz.boosik.boosCooldown;

import java.util.Set;

import org.bukkit.entity.Player;

import util.boosChat;

public class boosLimitManager {
	static boolean blocked(Player player, String regexCommand,
			String originalCommand, int limit) {
		int uses = getUses(player, regexCommand);
		if (player.hasPermission("booscooldowns.nolimit")
				|| player.hasPermission("booscooldowns.nolimit."
						+ originalCommand)) {
		} else {
			if (limit == -1) {
				return false;
			} else if (limit <= uses) {
				return true;
			}
		}
		return false;
	}

	static void getLimits(Player player) {
		int usesNum = 0;
		int limitNum = 0;
		int num;
		String message;
		Set<String> uses = boosConfigManager.getLimits(player);
		if (uses != null) {
			for (String key : uses) {
				usesNum = boosConfigManager.getConfusers().getInt(
						"users." + player.getName().toLowerCase().hashCode()
								+ ".uses." + key, usesNum);
				limitNum = boosConfigManager.getLimit(key, player);
				num = limitNum - usesNum;
				if (num < 0) {
					num = 0;
				}
				message = boosConfigManager.getLimitListMessage();
				message = message.replaceAll("&command&", key);
				message = message.replaceAll("&limit&",
						String.valueOf(limitNum));
				message = message.replaceAll("&times&", String.valueOf(num));
				boosChat.sendMessageToPlayer(player, message);
			}
		}
	}

	static int getUses(Player player, String regexCommand) {
		int regexCommand2 = regexCommand.toLowerCase().hashCode();
		int uses = 0;
		uses = boosConfigManager.getConfusers().getInt(
				"users." + player.getName().toLowerCase().hashCode() + ".uses."
						+ regexCommand2, uses);
		return uses;
	}

	static void setUses(Player player, String regexCommand,
			String originalCommand) {
		if (boosConfigManager.getLimitsEnabled()) {
			if (boosConfigManager.getLimits(player).contains(regexCommand)) {
				boosCoolDown.log.info("regexCommand: " + regexCommand
						+ "  originalCommand: " + originalCommand);
				int regexCommand2 = regexCommand.toLowerCase().hashCode();
				int uses = getUses(player, regexCommand);
				boosCoolDown.log.info("Uses: " + uses);
				uses = uses + 1;
				try {
					boosConfigManager.getConfusers().set(
							"users."
									+ player.getName().toLowerCase().hashCode()
									+ ".uses." + regexCommand2, uses);
				} catch (IllegalArgumentException e) {
					boosCoolDown.log.warning("Player " + player.getName()
							+ " used empty command and caused this error!");
				}
			} else {
				return;
			}
		}
	}

}
