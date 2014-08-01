package cz.boosik.boosCooldown.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import cz.boosik.boosCooldown.BoosConfigManager;
import cz.boosik.boosCooldown.BoosCoolDownManager;

/**
 * Poslucha� naslouchaj�c� ud�losti, kter� se spou�t� v okam�iku kdy hr�� zem�e.
 * V z�vislosti na konfiguraci pluginu a opr�vn�n�ch hr��e mohou nastat t�i
 * r�zn� aktivity. Cooldown �asova�e mohou b�t po smrti vymaz�ny, nebo naopak
 * mohou b�t znovu spu�t�ny ve�ker� cooldown �asova�e pro ve�ker� nastaven�
 * p��kazy. Tak� mohou b�t vymaz�ny z�znamy o pou�it�ch p��kazu a hr�� bude op�t
 * schopen pou��vat limitovan� p��kazy a� po hodnotu limitu.
 * 
 * @author Jakub Kol��
 * 
 */
public class BoosPlayerDeathListener implements Listener {
	/**
	 * Metoda zji��uje jestli je entita kter� spustila tuto ud�lost hr�� a
	 * jestli nen� null. Na z�klad� toho spou�t� dal�� metody.
	 * 
	 * @param event
	 *            ud�lost PlayerDeathEvent
	 */
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	private void onPlayerDeath(PlayerDeathEvent event) {
		Entity entity = event.getEntity();
		if (entity != null && entity instanceof Player) {
			Player player = (Player) entity;
			clearCooldownsOnDeath(player);
			clearUsesOnDeath(player);
			startCooldownsOnDeath(player);
		}
	}

	/**
	 * Na z�klad� konfigurace metoda spou�t� v�echny cooldown �asova�e
	 * specifikovan�ho hr��e t�m �e spust� medotu startAllCooldowns();.
	 * 
	 * @param player
	 *            hr��, kter� spustil ud�lost PlayerDeathEvent
	 */
	private void startCooldownsOnDeath(Player player) {
		if (player != null) {
			if (BoosConfigManager.getStartCooldownsOnDeath()) {
				BoosCoolDownManager.startAllCooldowns(player, "");
			}
		}
	}

	/**
	 * Na z�klad� konfigurace a toho jestli hr�� disponuje opr�vn�n�m
	 * booscooldowns.clear.uses.death metoda vyma�e v�echny z�znamy o spu�t�n�
	 * v�ech p��kaz� specifikovan�ho hr��e t�m �e spust� metodu
	 * clearSomething();.
	 * 
	 * @param player
	 *            hr��, kter� spustil ud�lost PlayerDeathEvent
	 */
	private void clearUsesOnDeath(Player player) {
		if (player != null
				&& player.hasPermission("booscooldowns.clear.uses.death")) {
			if (BoosConfigManager.getCleanUsesOnDeath()) {
				BoosConfigManager.clearSomething("uses", player.getUniqueId());
			}
		}
	}

	/**
	 * Na z�klad� konfigurace a toho jestli hr�� disponuje opr�vn�n�m
	 * booscooldowns.clear.cooldowns.death metoda vyma�e v�echny cooldown
	 * �asova�e v�ech p��kaz� specifikovan�ho hr��e t�m �e spust� metodu
	 * clearSomething();.
	 * 
	 * @param player
	 *            hr��, kter� spustil ud�lost PlayerDeathEvent
	 */
	private void clearCooldownsOnDeath(Player player) {
		if (player != null
				&& player.hasPermission("booscooldowns.clear.cooldowns.death")) {
			if (BoosConfigManager.getCleanCooldownsOnDeath()) {
				BoosConfigManager.clearSomething("cooldown",
						player.getUniqueId());
			}
		}
	}
}
