package cz.boosik.boosCooldown.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import util.boosChat;
import cz.boosik.boosCooldown.boosConfigManager;
import cz.boosik.boosCooldown.boosWarmUpManager;

/**
 * @author Jakub
 *
 */
public class boosPlayerToggleSprintListener implements Listener {
	/**
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	private void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		if (player != null
				&& !player.hasPermission("booscooldowns.nocancel.sprint")) {
			if (boosWarmUpManager.hasWarmUps(player)) {
				boosChat.sendMessageToPlayer(player,
						boosConfigManager.getCancelWarmupOnSprintMessage());
				boosWarmUpManager.cancelWarmUps(player);
			}

		}
	}
}