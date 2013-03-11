package cz.boosik.boosCooldown.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import util.boosChat;
import cz.boosik.boosCooldown.boosConfigManager;
import cz.boosik.boosCooldown.boosWarmUpManager;

public class boosPlayerToggleSneakListener implements Listener {
	@EventHandler(priority = EventPriority.NORMAL)
	private void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		if (player != null
				&& !player.hasPermission("booscooldowns.nocancel.sneak")) {
			if (boosWarmUpManager.hasWarmUps(player)) {
				boosChat.sendMessageToPlayer(player,
						boosConfigManager.getCancelWarmupOnSneakMessage());
				boosWarmUpManager.cancelWarmUps(player);
			}

		}
	}
}