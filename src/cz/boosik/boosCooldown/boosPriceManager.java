package cz.boosik.boosCooldown;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import util.boosChat;

/**
 * @author Jakub
 *
 */
public class boosPriceManager {
	private static Economy economy = boosCoolDown.getEconomy();
	private static String msg = "";

	/**
	 * @param player
	 * @param regexCommand
	 * @param originalCommand
	 * @param price
	 * @param name
	 * @return
	 */
	static boolean payForCommand(Player player, String regexCommand,
			String originalCommand, double price, String name) {
		if (economy == null) {
			return true;
		}
		EconomyResponse r = economy.withdrawPlayer(name, price);
		if (r.transactionSuccess()) {
			msg = String.format(boosConfigManager.getPaidForCommandMessage(),
					economy.format(r.amount), economy.format(r.balance));
			msg = msg.replaceAll("&command&", originalCommand);
			boosChat.sendMessageToPlayer(player, msg);
			return true;
		} else {
			if (r.errorMessage.equals("Insufficient funds")) {
				String unit;
				if (price == 1) {
					unit = economy.currencyNameSingular();
				} else {
					unit = economy.currencyNamePlural();
				}
				msg = String.format(
						boosConfigManager.getInsufficientFundsMessage(), (price
								+ " " + unit), economy.format(r.balance));
				msg = msg.replaceAll("&command&", originalCommand);
			} else {
				msg = String.format(boosConfigManager.getPaidErrorMessage(),
						r.errorMessage);
			}
			boosChat.sendMessageToPlayer(player, msg);
			return false;
		}
	}

	/**
	 * @param event
	 * @param player
	 * @param regexCommand
	 * @param originalCommand
	 * @param price
	 */
	static void payForCommand(PlayerCommandPreprocessEvent event,
			Player player, String regexCommand, String originalCommand,
			double price) {
		String name = player.getName();
		if (price > 0) {
			if (!player.hasPermission("booscooldowns.noprice")
					&& !player.hasPermission("booscooldowns.noprice."
							+ originalCommand)) {
				if (payForCommand(player, regexCommand, originalCommand, price,
						name)) {
					return;
				} else {
					boosCoolDownManager.cancelCooldown(player, regexCommand);
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	static void payForCommand2(AsyncPlayerChatEvent event, Player player,
			String regexCommand, String originalCommand, double price) {
		String name = player.getName();
		if (price > 0) {
			if (!player.hasPermission("booscooldowns.noprice")
					&& !player.hasPermission("booscooldowns.noprice."
							+ originalCommand)) {
				if (boosPriceManager.payForCommand(player, regexCommand,
						originalCommand, price, name)) {
					return;
				} else {
					boosCoolDownManager.cancelCooldown(player, regexCommand);
					event.setCancelled(true);
					return;
				}
			}
		}
	}
}
