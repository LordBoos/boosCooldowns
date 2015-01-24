package cz.boosik.boosCooldown;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import util.boosChat;

/**
 * T��da obsahuje ve�ker� metody pot�ebn� k ��zen� poplatk� za p��kazy.
 * 
 * @author Jakub Kol��
 * 
 */
public class BoosItemCostManager {
	private static String msg = "";

	/**
	 * Metoda zaji��uje funkci platby za p��kaz. Vrac� hodnotu v z�vislosti na
	 * �sp�nosti platby.
	 * 
	 * @param player
	 *            specifikovan� hr��
	 * @param regexCommand
	 *            p��kaz z konfigurace vyhovuj�c� origin�ln�mu p��kazu
	 * @param originalCommand
	 *            origin�ln� p��kaz pou�it� hr��em
	 * @param item
	 * @param price
	 *            cena pou�it� p��kazu
	 * @param name
	 *            jm�no specifick�ho hr��e
	 * @return true pokud byl �sp�n� zaplacen poplatek, nebo pokud nebyl nalezen
	 *         ekonomick� plugin; false pokud do�lo k chyb� nebo hr�� nem�l
	 *         dostatek financ�
	 */
	static boolean payItemForCommand(Player player, String regexCommand,
			String originalCommand, String item, int count, String name) {
		Material material = Material.getMaterial(item);
		Inventory inventory = player.getInventory();
		Boolean trans = false;
		if (inventory.contains(material, count)) {
			ItemStack itemstack = new ItemStack(material, count);
			inventory.removeItem(itemstack);
			trans = true;
		}
		if (trans) {
			msg = String.format(
					BoosConfigManager.getPaidItemsForCommandMessage(), count
							+ " " + item);
			msg = msg.replaceAll("&command&", originalCommand);
			boosChat.sendMessageToPlayer(player, msg);
			return true;
		} else {
			// msg = String.format(
			// BoosConfigManager.getInsufficientItemsMessage(), (count
			// + " " + item));
			// msg = msg.replaceAll("&command&", originalCommand);
			// boosChat.sendMessageToPlayer(player, msg);
			return false;
		}
	}

	/**
	 * Metoda ukon�uje/neukon�uje ud�lost pou�it� p��kazu v z�vislosti na tom,
	 * jakou hodnotu vr�tila metoda payForCommand(Player player, String
	 * regexCommand, String originalCommand, double price, String name);.
	 * 
	 * @param event
	 *            ud�lost PlayerCommandPreprocessEvent
	 * @param player
	 *            specifick� hr��
	 * @param regexCommand
	 *            p��kaz z konfigurace vyhovuj�c� origin�ln�mu p��kazu
	 * @param originalCommand
	 *            origin�ln� p��kaz pou�it� hr��em
	 * @param item
	 * @param price
	 *            cena pou�it� p��kazu
	 */
	static void payItemForCommand(PlayerCommandPreprocessEvent event,
			Player player, String regexCommand, String originalCommand,
			String item, int count) {
		String name = player.getName();
		if (count > 0) {
			if (!player.hasPermission("booscooldowns.noitemcost")
					&& !player.hasPermission("booscooldowns.noitemcost."
							+ originalCommand)) {
				if (payItemForCommand(player, regexCommand, originalCommand,
						item, count, name)) {
					return;
				} else {
					BoosCoolDownManager.cancelCooldown(player, regexCommand);
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	public static boolean has(Player player, String item, int count) {
		if (item.equals("")) {
			return true;
		}
		if (count <= 0) {
			return true;
		}
		Material material = Material.getMaterial(item);
		Inventory inventory = player.getInventory();
		if (inventory.contains(material, count)) {
			return true;
		}
		return false;
	}
}
