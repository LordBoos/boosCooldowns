package cz.boosik.boosCooldown.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import cz.boosik.boosCooldown.Managers.BoosConfigManager;
import cz.boosik.boosCooldown.Managers.BoosWarmUpManager;
import util.BoosChat;

public class BoosPlayerInteractListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onPlayerInteract(PlayerInteractEvent event) {
        Entity entity = event.getPlayer();
        if (entity != null) {
            Player player = (Player) entity;
            if (!player
                    .hasPermission("booscooldowns.dontblock.interact")) {
                if (BoosWarmUpManager.hasWarmUps(player)) {
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
                            .equals("STORAGE_MINECART")
                            || event.getClickedBlock().getType().name()
                            .equals("TRAPPED_CHEST")
                            || event.getClickedBlock().getType().name()
                            .equals("DROPPER")
                            || event.getClickedBlock().getType().name()
                            .equals("HOPPER")) {
                        event.setCancelled(true);
                        BoosChat.sendMessageToPlayer(player,
                                BoosConfigManager.getInteractBlockedMessage());
                    }
                }

            }
        }
    }
}
