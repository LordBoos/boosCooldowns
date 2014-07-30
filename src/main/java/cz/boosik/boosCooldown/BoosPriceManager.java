package cz.boosik.boosCooldown;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import util.boosChat;

/**
 * T��da obsahuje ve�ker� metody pot�ebn� k ��zen� poplatk�
 * pomoc� v�c� za p��kazy.
 * 
 * @author Jakub Kol��
 * 
 */
public class BoosPriceManager {
	private static Economy economy = BoosCoolDown.getEconomy();
	private static String msg = "";

	/**
	 * Metoda zaji��uje funkci platby za p��kaz. Vrac� hodnotu v
	 * z�vislosti na �sp�nosti platby.
	 * 
	 * @param player
	 *            specifikovan� hr��
	 * @param regexCommand
	 *            p��kaz z konfigurace vyhovuj�c� origin�ln�mu
	 *            p��kazu
	 * @param originalCommand
	 *            origin�ln� p��kaz pou�it� hr��em
	 * @param price
	 *            cena pou�it� p��kazu
	 * @param name
	 *            jm�no specifick�ho hr��e
	 * @return true pokud byl �sp�n� zaplacen poplatek, nebo pokud nebyl
	 *         nalezen ekonomick� plugin; false pokud do�lo k chyb� nebo
	 *         hr�� nem�l dostatek financ�
	 */
	static boolean payForCommand(Player player, String regexCommand,
			String originalCommand, double price, String name) {
		if (economy == null) {
			return true;
		}
		EconomyResponse r = economy.withdrawPlayer(player, price);
		if (r.transactionSuccess()) {
			msg = String.format(BoosConfigManager.getPaidForCommandMessage(),
					economy.format(r.amount), economy.format(r.balance));
			msg = msg.replaceAll("&command&", originalCommand);
			boosChat.sendMessageToPlayer(player, msg);
			return true;
		} else {
			if (r.errorMessage.equals("Insufficient funds")) {
				// String unit;
				// if (price == 1) {
				// unit = economy.currencyNameSingular();
				// } else {
				// unit = economy.currencyNamePlural();
				// }
				// msg = String.format(
				// BoosConfigManager.getInsufficientFundsMessage(), (price
				// + " " + unit), economy.format(r.balance));
				// msg = msg.replaceAll("&command&", originalCommand);
			} else {
				msg = String.format(BoosConfigManager.getPaidErrorMessage(),
						r.errorMessage);
			}
			boosChat.sendMessageToPlayer(player, msg);
			return false;
		}
	}

	/**
	 * Metoda ukon�uje/neukon�uje ud�lost pou�it� p��kazu v
	 * z�vislosti na tom, jakou hodnotu vr�tila metoda payForCommand(Player
	 * player, String regexCommand, String originalCommand, double price, String
	 * name);.
	 * 
	 * @param event
	 *            ud�lost PlayerCommandPreprocessEvent
	 * @param player
	 *            specifick� hr��
	 * @param regexCommand
	 *            p��kaz z konfigurace vyhovuj�c� origin�ln�mu
	 *            p��kazu
	 * @param originalCommand
	 *            origin�ln� p��kaz pou�it� hr��em
	 * @param price
	 *            cena pou�it� p��kazu
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
					BoosCoolDownManager.cancelCooldown(player, regexCommand);
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	public static boolean has(Player player, double price) {
		if (economy == null) {
			return true;
		} else {
			return economy.has(player, price);
		}
	}
}
