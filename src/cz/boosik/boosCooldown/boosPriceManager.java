package cz.boosik.boosCooldown;

import org.bukkit.entity.Player;

import util.boosChat;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class boosPriceManager {
	private static Economy economy = boosCoolDown.getEconomy();

	public static void payForCommand(Player player, String pre) {
		EconomyResponse r = null;
		r = getPriceGroup(player, pre, r);
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

	private static EconomyResponse getPriceGroup(Player player, String pre,
			EconomyResponse r) {
		if (boosCoolDown.isUsingPermissions()) {
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.price2")) {
				r = economy.withdrawPlayer(player.getName(),
						boosConfigManager.getPrice2(pre));
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.price3")) {
				r = economy.withdrawPlayer(player.getName(),
						boosConfigManager.getPrice3(pre));
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.price4")) {
				r = economy.withdrawPlayer(player.getName(),
						boosConfigManager.getPrice4(pre));
			} else if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.price5")) {
				r = economy.withdrawPlayer(player.getName(),
						boosConfigManager.getPrice5(pre));
			} else{
				r = economy.withdrawPlayer(player.getName(),
						boosConfigManager.getPrice(pre));
			}
		} else {
			r = economy.withdrawPlayer(player.getName(),
					boosConfigManager.getPrice(pre));
		}
		return r;
	}
}
