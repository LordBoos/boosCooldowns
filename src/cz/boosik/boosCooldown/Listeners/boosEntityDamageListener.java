package cz.boosik.boosCooldown.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import util.boosChat;
import cz.boosik.boosCooldown.boosConfigManager;
import cz.boosik.boosCooldown.boosWarmUpManager;

/**
 * @author Jakub
 *
 */
public class boosEntityDamageListener implements Listener {
	/**
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	private void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled())
			return;

		Entity entity = event.getEntity();
		if (entity != null && entity instanceof Player) {
			Player player = (Player) entity;
			if (player != null
					&& !player.hasPermission("booscooldowns.nocancel.damage")) {
				if (boosWarmUpManager.hasWarmUps(player)) {
					boosChat.sendMessageToPlayer(player, boosConfigManager
							.getWarmUpCancelledByDamageMessage());
					boosWarmUpManager.cancelWarmUps(player);
				}

			}
		}
	}
}
