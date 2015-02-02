package cz.boosik.boosCooldown.Listeners;

import cz.boosik.boosCooldown.BoosConfigManager;
import cz.boosik.boosCooldown.BoosWarmUpManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import util.boosChat;

public class BoosEntityDamageListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity != null && entity instanceof Player) {
            Player player = (Player) entity;
            if (!player.hasPermission("booscooldowns.nocancel.damage")) {
                if (BoosWarmUpManager.hasWarmUps(player)) {
                    boosChat.sendMessageToPlayer(player, BoosConfigManager
                            .getWarmUpCancelledByDamageMessage());
                    BoosWarmUpManager.cancelWarmUps(player);
                }

            }
        }
    }
}
