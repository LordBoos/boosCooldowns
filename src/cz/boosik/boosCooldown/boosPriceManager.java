package cz.boosik.boosCooldown;

import org.bukkit.entity.Player;

import util.boosChat;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class boosPriceManager {
	private static Economy economy = boosCoolDown.getEconomy();

	public static void payForCommand(Player player, String pre, String message) {
		EconomyResponse r = economy.withdrawPlayer(player.getName(),
				boosConfigManager.getPrice(player, pre));
		if (r.transactionSuccess()) {
			String msg = String.format(
					boosConfigManager.getPaidForCommandMessage(),
					economy.format(r.amount), economy.format(r.balance));
			msg = msg.replaceAll("&command&", pre);
			boosChat.sendMessageToPlayer(player, msg);
		} else {
			String msg = String.format(boosConfigManager.getPaidErrorMessage(),
					r.errorMessage);
			boosChat.sendMessageToPlayer(player, msg);
		}
	}
}
