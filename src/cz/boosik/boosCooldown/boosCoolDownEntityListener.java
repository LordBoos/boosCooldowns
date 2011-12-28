package cz.boosik.boosCooldown;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

import util.boosChat;

public class boosCoolDownEntityListener extends EntityListener {
	private final boosCoolDown plugin;

	public boosCoolDownEntityListener(boosCoolDown instance) {
		plugin = instance;
	}

	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Entity entity = event.getEntity();
		if (entity != null && entity instanceof Player) {
			Player player = (Player) entity;
			if (player != null) {
				if (boosWarmUpManager.hasWarmUps(player)) {
					boosChat.sendMessageToPlayer(player,
							boosConfigManager.getWarmUpCancelledByDamageMessage());
					boosWarmUpManager.cancelWarmUps(player);
				}

			}
		}
	}
}
