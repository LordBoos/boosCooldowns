package cz.boosik.boosCooldown.Listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import util.boosChat;
import cz.boosik.boosCooldown.boosConfigManager;
import cz.boosik.boosCooldown.boosCoolDownListener;
import cz.boosik.boosCooldown.boosWarmUpManager;

public class boosPlayerMoveListener implements Listener {
	private static boolean hasMoved(Player player) {
		String curworld = player.getWorld().getName();
		String cmdworld = boosCoolDownListener.playerworld.get(player);
		Location curloc = player.getLocation();
		Location cmdloc = boosCoolDownListener.playerloc.get(player);
		if (!curworld.equals(cmdworld)) {
			return true;
		} else if (cmdloc.distanceSquared(curloc) > 2) {
			return true;
		}

		return false;
	}

	int tempTimer = 0;

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
					boosCoolDownListener.clearLocWorld(player);
					boosChat.sendMessageToPlayer(player,
							boosConfigManager.getWarmUpCancelledByMoveMessage());
					boosWarmUpManager.cancelWarmUps(player);
				}
			}
			tempTimer = 0;
		}
	}
}