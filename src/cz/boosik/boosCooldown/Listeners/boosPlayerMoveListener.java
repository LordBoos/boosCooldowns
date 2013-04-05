package cz.boosik.boosCooldown.Listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import util.boosChat;
import cz.boosik.boosCooldown.boosConfigManager;
import cz.boosik.boosCooldown.boosWarmUpManager;

/**
 * @author Jakub
 *
 */
public class boosPlayerMoveListener implements Listener {
	/**
	 * @param player
	 * @return
	 */
	private static boolean hasMoved(Player player) {
		String curworld = player.getWorld().getName();
		String cmdworld = boosWarmUpManager.getPlayerworld().get(player);
		Location curloc = player.getLocation();
		Location cmdloc = boosWarmUpManager.getPlayerloc().get(player);
		if (!curworld.equals(cmdworld)) {
			return true;
		} else if (cmdloc.distanceSquared(curloc) > 2) {
			return true;
		}

		return false;
	}

	private int tempTimer = 0;

	/**
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	private void onPlayerMove(PlayerMoveEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (tempTimer < 20) {
			tempTimer = tempTimer + 1;
			return;
		} else {
			Player player = event.getPlayer();
			if (player != null
					&& !player.hasPermission("booscooldowns.nocancel.move")) {
				if (boosWarmUpManager.hasWarmUps(player) && hasMoved(player)) {
					boosWarmUpManager.clearLocWorld(player);
					boosChat.sendMessageToPlayer(player,
							boosConfigManager.getWarmUpCancelledByMoveMessage());
					boosWarmUpManager.cancelWarmUps(player);
				}
			}
			tempTimer = 0;
		}
	}
}