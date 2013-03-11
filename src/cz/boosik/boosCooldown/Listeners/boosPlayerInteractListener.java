package cz.boosik.boosCooldown.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import util.boosChat;
import cz.boosik.boosCooldown.boosConfigManager;
import cz.boosik.boosCooldown.boosWarmUpManager;

public class boosPlayerInteractListener implements Listener {
	@EventHandler(priority = EventPriority.NORMAL)
	private void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled())
			return;

		Entity entity = event.getPlayer();
		if (entity != null && entity instanceof Player) {
			Player player = (Player) entity;
			if (player != null
					&& !player
							.hasPermission("booscooldowns.dontblock.interact")) {
				if (boosWarmUpManager.hasWarmUps(player)) {
					if (event.getClickedBlock().getType().name()
							.equals("CHEST")
							|| event.getClickedBlock().getType().name()
									.equals("FURNACE")
							|| event.getClickedBlock().getType().name()
									.equals("BURNING_FURNACE")
							|| event.getClickedBlock().getType().name()
									.equals("WORKBENCH")
							|| event.getClickedBlock().getType().name()
									.equals("DISPENSER")
							|| event.getClickedBlock().getType().name()
									.equals("JUKEBOX")
							|| event.getClickedBlock().getType().name()
									.equals("LOCKED_CHEST")
							|| event.getClickedBlock().getType().name()
									.equals("ENCHANTMENT_TABLE")
							|| event.getClickedBlock().getType().name()
									.equals("BREWING_STAND")
							|| event.getClickedBlock().getType().name()
									.equals("CAULDRON")
							|| event.getClickedBlock().getType().name()
									.equals("STORAGE_MINECART")) {
						event.setCancelled(true);
						boosChat.sendMessageToPlayer(player,
								boosConfigManager.getInteractBlockedMessage());
					}
				}

			}
		}
	}
}
