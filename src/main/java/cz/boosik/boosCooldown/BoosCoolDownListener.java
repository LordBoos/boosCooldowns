package cz.boosik.boosCooldown;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import util.boosChat;

/**
 * Hlavn� poslucha�, kter� naslouch� ud�losti pou�it� p��kazu
 * hr��em. Kontroluje, jestli jsou pro p��kaz nastaveny omezen� a na
 * z�klad� tohoto spou�t� �asova�e a vol� metody spojen� s
 * poplatky a limity.
 * 
 * @author Jakub Kol��
 * 
 */
public class BoosCoolDownListener implements Listener {
	private static BoosCoolDown plugin;

	/**
	 * @param instance
	 */
	public BoosCoolDownListener(BoosCoolDown instance) {
		plugin = instance;
	}

	/**
	 * Metoda zkontroluje pomoc� vol�n� dal��ch metod, jestli p�ikaz
	 * kter� hr�� pou�il je n�jak�m zp�sobem omezen� a na
	 * z�klad� toho je bu� ud�lost pou�it� p��kazu stornov�na,
	 * nebo ne.
	 * 
	 * @param event
	 *            ud�lost PlayerCommandPreprocessEvent
	 * @param player
	 *            hr�� kter� spustil tuto ud�lost
	 * @param regexCommad
	 *            p��kaz z konfigura�n�ho souboru, kter� vyhovuje
	 *            origin�ln�mu p��kazu
	 * @param originalCommand
	 *            origin�ln� p��kaz kter� hr�� pou�il
	 * @param warmupTime
	 *            warmup doba nastaven� pro regexCommand
	 * @param cooldownTime
	 *            cooldown doba nastaven� pro regexCommand
	 * @param price
	 *            cena nastaven� pro regexCommand
	 * @param limit
	 *            limit nastaven� pro regexCommand
	 */
	private void checkRestrictions(PlayerCommandPreprocessEvent event,
			Player player, String regexCommad, String originalCommand,
			int warmupTime, int cooldownTime, double price, String item,
			int count, int limit, int xpPrice) {
		boolean blocked = BoosLimitManager.blocked(player, regexCommad,
				originalCommand, limit);
		if (!blocked) {
			if (warmupTime > 0) {
				if (!player.hasPermission("booscooldowns.nowarmup")
						&& !player.hasPermission("booscooldowns.nowarmup."
								+ originalCommand)) {
					start(event, player, regexCommad, originalCommand,
							warmupTime, cooldownTime);
				}
			} else if (BoosPriceManager.has(player, price)
					& BoosItemCostManager.has(player, item, count)
					& BoosXpCostManager.has(player, xpPrice)) {
				if (BoosCoolDownManager.coolDown(player, regexCommad,
						originalCommand, cooldownTime)) {
					event.setCancelled(true);
				}
			}
			if (BoosPriceManager.has(player, price)
					& BoosItemCostManager.has(player, item, count)
					& BoosXpCostManager.has(player, xpPrice)) {
				if (!event.isCancelled()) {
					BoosPriceManager.payForCommand(event, player, regexCommad,
							originalCommand, price);
				}
				if (!event.isCancelled()) {
					BoosItemCostManager.payItemForCommand(event, player,
							regexCommad, originalCommand, item, count);
				}
				if (!event.isCancelled()) {
					BoosXpCostManager.payXPForCommand(event, player,
							regexCommad, originalCommand, xpPrice);
				}
			} else {
				if (!BoosPriceManager.has(player, price)
						& !BoosWarmUpManager.isWarmUpProcess(player,
								regexCommad)) {
					String unit;
					String msg = "";
					if (price == 1) {
						unit = BoosCoolDown.getEconomy().currencyNameSingular();
					} else {
						unit = BoosCoolDown.getEconomy().currencyNamePlural();
					}
					msg = String.format(
							BoosConfigManager.getInsufficientFundsMessage(),
							(price + " " + unit),
							BoosCoolDown.getEconomy().format(
									BoosCoolDown.getEconomy().getBalance(
											player)));
					msg = msg.replaceAll("&command&", originalCommand);
					boosChat.sendMessageToPlayer(player, msg);
				}
				if (!BoosItemCostManager.has(player, item, count)
						& !BoosWarmUpManager.isWarmUpProcess(player,
								regexCommad)) {
					String msg = "";
					msg = String.format(
							BoosConfigManager.getInsufficientItemsMessage(),
							(count + " " + item));
					msg = msg.replaceAll("&command&", originalCommand);
					boosChat.sendMessageToPlayer(player, msg);
				}
				if (!BoosXpCostManager.has(player, xpPrice)
						& !BoosWarmUpManager.isWarmUpProcess(player,
								regexCommad)) {
					String msg = "";
					msg = String.format(
							BoosConfigManager.getInsufficientXpMessage(),
							(xpPrice));
					msg = msg.replaceAll("&command&", originalCommand);
					boosChat.sendMessageToPlayer(player, msg);
				}
				event.setCancelled(true);
			}
			if (!event.isCancelled()) {
				String msg = String.format(BoosConfigManager.getMessage(
						regexCommad, player));
				if (!msg.equals("")) {
					boosChat.sendMessageToPlayer(player, msg);
				}
			}
		} else {
			event.setCancelled(true);
			String msg = String.format(BoosConfigManager
					.getCommandBlockedMessage());
			boosChat.sendMessageToPlayer(player, msg);
		}
		if (!event.isCancelled()) {
			BoosLimitManager.setUses(player, regexCommad, originalCommand);
			if (BoosConfigManager.getCommandLogging()) {
				BoosCoolDown.commandLogger(player.getName(), originalCommand);
			}
		}
	}

	/**
	 * Poslucha�, kter� naslouch� ud�losti pou�it� p��kazu a
	 * spou�t� se je�t� p�ed t�m, ne� je vykon�n efekt tohto
	 * p��kazu. Metoda zji��uje, jestli p��kaz nen� alias jin�ho
	 * p��kazu a tak� jestli se p��kaz kter� hr�� pou�il
	 * shoduje s p��kazem nastaven�m v konfiguraci. Pokud se shoduje, pak
	 * jsou na�teny informace o warmup dob�, cooldown dob�, poplatku a
	 * limitu. Tyto hodnoty jsou pot� p�ed�ny metod�
	 * checkRestrictions();.
	 * 
	 * @param event
	 *            ud�lost PlayerCommandPreprocessEvent
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	private void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String originalCommand = event.getMessage().replace("\\", "\\\\");
		originalCommand = originalCommand.replace("$", "S");
		originalCommand = originalCommand.trim().replaceAll(" +", " ")
				.toLowerCase();
		String regexCommad = "";
		Set<String> aliases = null;
		try {
			aliases = BoosConfigManager.getAliases();
		} catch (Exception e1) {
			BoosCoolDown
			.getLog()
			.warning(
					"Aliases section in config.yml is missing! Please delete your config.yml, restart server and set it again!");
		}
		Set<String> commands = BoosConfigManager.getCommands(player);
		boolean on = true;
		String item = "";
		int count = 0;
		int warmupTime = 0;
		double price = 0;
		int limit = -1;
		int cooldownTime = 0;
		int xpPrice = 0;
		on = BoosCoolDown.isPluginOnForPlayer(player);
		try {
			if (aliases.contains(originalCommand)) {
				originalCommand = BoosConfigManager.getAlias(originalCommand);
				if (originalCommand.contains("$player")) {
					originalCommand.replaceAll("$player", player.getName());
				}
				if (originalCommand.contains("$world")) {
					originalCommand.replaceAll("$world", player.getWorld()
							.getName());
				}
				event.setMessage(originalCommand);
			}
		} catch (Exception e) {
			BoosCoolDown
					.getLog()
					.warning(
							"Aliases section in config.yml is missing! Please delete your config.yml, restart server and set it again!");
		}
		if (on) {
			for (String group : commands) {
				String group2 = group.replace("*", ".+");
				if (originalCommand.matches(group2)) {
					regexCommad = group;
					if (BoosConfigManager.getWarmupEnabled()) {
						warmupTime = BoosConfigManager.getWarmUp(regexCommad,
								player);
					}
					if (BoosConfigManager.getCooldownEnabled()) {
						cooldownTime = BoosConfigManager.getCoolDown(
								regexCommad, player);
					}
					if (BoosConfigManager.getPriceEnabled()) {
						price = BoosConfigManager.getPrice(regexCommad, player);
					}
					if (BoosConfigManager.getXpPriceEnabled()) {
						xpPrice = BoosConfigManager.getXpPrice(regexCommad,
								player);
					}
					if (BoosConfigManager.getItemCostEnabled()) {
						item = BoosConfigManager.getItemCostItem(regexCommad,
								player);
						count = BoosConfigManager.getItemCostCount(regexCommad,
								player);
					}
					if (BoosConfigManager.getLimitEnabled()) {
						limit = BoosConfigManager.getLimit(regexCommad, player);
					}
					break;
				}
			}
			this.checkRestrictions(event, player, regexCommad, originalCommand,
					warmupTime, cooldownTime, price, item, count, limit,
					xpPrice);
		}
	}

	/**
	 * Metoda spou�t� warmup a cooldown �asova�e, p��padn� je
	 * ukon�uje, pokud ji� tyto �asova�e skon�ili.
	 * 
	 * @param event
	 *            ud�lost PlayerCommandPreprocessEvent
	 * @param player
	 *            hr�� kter� spustil tuto ud�lost
	 * @param regexCommad
	 *            p��kaz z konfigura�n�ho souboru, kter� vyhovuje
	 *            origin�ln�mu p��kazu
	 * @param originalCommand
	 *            origin�ln� p��kaz kter� hr�� pou�il
	 * @param warmupTime
	 *            warmup doba nastaven� pro regexCommand
	 * @param cooldownTime
	 *            cooldown doba nastaven� pro regexCommand
	 */
	private void start(PlayerCommandPreprocessEvent event, Player player,
			String regexCommad, String originalCommand, int warmupTime,
			int cooldownTime) {
		if (!BoosWarmUpManager.checkWarmUpOK(player, regexCommad)) {
			if (BoosCoolDownManager.checkCoolDownOK(player, regexCommad,
					originalCommand, cooldownTime)) {
				BoosWarmUpManager.startWarmUp(plugin, player, regexCommad,
						originalCommand, warmupTime);
				event.setCancelled(true);
				return;
			} else {
				event.setCancelled(true);
				return;
			}
		} else {
			if (BoosCoolDownManager.coolDown(player, regexCommad,
					originalCommand, cooldownTime)) {
				event.setCancelled(true);
				return;
			} else {
				BoosWarmUpManager.removeWarmUpOK(player, regexCommad);
				return;
			}
		}
	}
}