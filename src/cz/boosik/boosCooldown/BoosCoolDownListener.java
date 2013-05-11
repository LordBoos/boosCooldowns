package cz.boosik.boosCooldown;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import util.boosChat;

/**
 * Hlavní posluchaè, kterı naslouchá události pouití pøíkazu hráèem.
 * Kontroluje, jestli jsou pro pøíkaz nastaveny omezení a na základì tohoto
 * spouští èasovaèe a volá metody spojené s poplatky a limity.
 * 
 * @author Jakub Koláø
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
	 * Metoda zkontroluje pomocí volání dalších metod, jestli pøikaz kterı hráè
	 * pouil je nìjakım zpùsobem omezenı a na základì toho je buï událost
	 * pouití pøíkazu stornována, nebo ne.
	 * 
	 * @param event
	 *            událost PlayerCommandPreprocessEvent
	 * @param player
	 *            hráè kterı spustil tuto událost
	 * @param regexCommad
	 *            pøíkaz z konfiguraèního souboru, kterı vyhovuje originálnímu
	 *            pøíkazu
	 * @param originalCommand
	 *            originální pøíkaz kterı hráè pouil
	 * @param warmupTime
	 *            warmup doba nastavená pro regexCommand
	 * @param cooldownTime
	 *            cooldown doba nastavená pro regexCommand
	 * @param price
	 *            cena nastavená pro regexCommand
	 * @param limit
	 *            limit nastavenı pro regexCommand
	 */
	private void checkRestrictions(PlayerCommandPreprocessEvent event,
			Player player, String regexCommad, String originalCommand,
			int warmupTime, int cooldownTime, double price, int limit) {
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
			} else {
				if (BoosCoolDownManager.coolDown(player, regexCommad,
						originalCommand, cooldownTime)) {
					event.setCancelled(true);
				}
			}
			if (!event.isCancelled()) {
				BoosPriceManager.payForCommand(event, player, regexCommad,
						originalCommand, price);
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
	 * Posluchaè, kterı naslouchá události pouití pøíkazu a spouští se ještì
	 * pøed tím, ne je vykonán efekt tohto pøíkazu. Metoda zjišuje, jestli
	 * pøíkaz není alias jiného pøíkazu a také jestli se pøíkaz kterı hráè
	 * pouil shoduje s pøíkazem nastavenım v konfiguraci. Pokud se shoduje, pak
	 * jsou naèteny informace o warmup dobì, cooldown dobì, poplatku a limitu.
	 * Tyto hodnoty jsou poté pøedány metodì checkRestrictions();.
	 * 
	 * @param event
	 *            událost PlayerCommandPreprocessEvent
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	private void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();
		String originalCommand = event.getMessage().trim()
				.replaceAll(" +", " ").toLowerCase();
		String regexCommad = "";
		Set<String> aliases = BoosConfigManager.getAliases();
		Set<String> commands = BoosConfigManager.getCommands(player);
		boolean on = true;
		int warmupTime = 0;
		double price = 0;
		int limit = -1;
		int cooldownTime = 0;
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
		} catch (NullPointerException e) {
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
					if (BoosConfigManager.getLimitEnabled()) {
						limit = BoosConfigManager.getLimit(regexCommad, player);
					}
					break;
				}
			}
			this.checkRestrictions(event, player, regexCommad, originalCommand,
					warmupTime, cooldownTime, price, limit);
		}
	}

	/**
	 * Metoda spouští warmup a cooldown èasovaèe, pøípadnì je ukonèuje, pokud
	 * ji tyto èasovaèe skonèili.
	 * 
	 * @param event
	 *            událost PlayerCommandPreprocessEvent
	 * @param player
	 *            hráè kterı spustil tuto událost
	 * @param regexCommad
	 *            pøíkaz z konfiguraèního souboru, kterı vyhovuje originálnímu
	 *            pøíkazu
	 * @param originalCommand
	 *            originální pøíkaz kterı hráè pouil
	 * @param warmupTime
	 *            warmup doba nastavená pro regexCommand
	 * @param cooldownTime
	 *            cooldown doba nastavená pro regexCommand
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