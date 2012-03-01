package cz.boosik.boosCooldown;

import javax.xml.bind.Marshaller.Listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import util.boosChat;

public class boosCoolDownEntityListener extends Listener {
	@SuppressWarnings("unused")
	private final boosCoolDown plugin;

	public boosCoolDownEntityListener(boosCoolDown instance) {
		plugin = instance;
	}

	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Entity entity = event.getEntity();
		if (entity != null && entity instanceof Player) {
			Player player = (Player) entity;
			if (boosCoolDown.isUsingPermissions()) {
				if (player != null
						&& !boosCoolDown.getPermissions().has(player,
								"booscooldowns.nocancel.damage")) {
					if (boosWarmUpManager.hasWarmUps(player)) {
						boosChat.sendMessageToPlayer(player, boosConfigManager
								.getWarmUpCancelledByDamageMessage());
						boosWarmUpManager.cancelWarmUps(player);
					}

				}
			} else {
				if (player != null) {
					if (boosWarmUpManager.hasWarmUps(player)) {
						boosChat.sendMessageToPlayer(player, boosConfigManager
								.getWarmUpCancelledByDamageMessage());
						boosWarmUpManager.cancelWarmUps(player);
					}

				}
			}
		}
	}

}
