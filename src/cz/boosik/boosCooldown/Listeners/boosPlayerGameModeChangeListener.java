package cz.boosik.boosCooldown.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

import util.boosChat;
import cz.boosik.boosCooldown.boosConfigManager;
import cz.boosik.boosCooldown.boosWarmUpManager;

public class boosPlayerGameModeChangeListener implements Listener {
	@EventHandler(priority = EventPriority.NORMAL)
	private void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
		if (event.isCancelled())
			return;

		Entity entity = event.getPlayer();
		if (entity != null && entity instanceof Player) {
			Player player = (Player) entity;
			if (player != null
					&& !player
							.hasPermission("booscooldowns.nocancel.gamemodechange")) {
				if (boosWarmUpManager.hasWarmUps(player)) {
					boosChat.sendMessageToPlayer(player, boosConfigManager
							.getCancelWarmupByGameModeChangeMessage());
					boosWarmUpManager.cancelWarmUps(player);
				}

			}
		}
	}
}
