package cz.boosik.boosCooldown;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.entity.Player;

import util.boosChat;

public class boosPriceManager {
	private static Economy economy = boosCoolDown.getEconomy();

	public static boolean payForCommand(Player player, String pre, double price,
			String name) {
		if (economy == null) {
			return true;
		}
		EconomyResponse r = economy.withdrawPlayer(name, price);
		if (r.transactionSuccess()) {
			String msg = String.format(
					boosConfigManager.getPaidForCommandMessage(),
					economy.format(r.amount), economy.format(r.balance));
			msg = msg.replaceAll("&command&", pre);
			boosChat.sendMessageToPlayer(player, msg);
			return true;
		} else {
			String msg = String.format(boosConfigManager.getPaidErrorMessage(),
					r.errorMessage);
			boosChat.sendMessageToPlayer(player, msg);
			return false;
		}
	}
}
