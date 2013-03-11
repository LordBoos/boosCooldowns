package cz.boosik.boosCooldown.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import cz.boosik.boosCooldown.boosConfigManager;
import cz.boosik.boosCooldown.boosCoolDownManager;

public class boosPlayerDeathListener implements Listener {
	@EventHandler(priority = EventPriority.NORMAL)
	private void onPlayerDeath(PlayerDeathEvent event) {
		Entity entity = event.getEntity();
		if (entity != null && entity instanceof Player) {
			Player player = (Player) entity;
			if (player != null
					&& player
							.hasPermission("booscooldowns.clear.cooldowns.death")) {
				if (boosConfigManager.getCleanCooldownsOnDeath()) {
					boosCoolDownManager.clearSomething("cooldown", player
							.getName().toLowerCase());
				}
			}
			if (player != null
					&& player.hasPermission("booscooldowns.clear.uses.death")) {
				if (boosConfigManager.getCleanUsesOnDeath()) {
					boosCoolDownManager.clearSomething("uses", player.getName()
							.toLowerCase());
				}
			}
			if (player != null) {
				if (boosConfigManager.getStartCooldownsOnDeath()) {
					boosCoolDownManager.startAllCooldowns(player);
				}
			}
		}
	}
}
