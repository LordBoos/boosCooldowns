package cz.boosik.boosCooldown.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import cz.boosik.boosCooldown.Managers.BoosConfigManager;
import cz.boosik.boosCooldown.Managers.BoosWarmUpManager;
import util.BoosChat;

public class BoosEntityDamageListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onEntityDamage(final EntityDamageEvent event) {
        final Entity entity = event.getEntity();
        if (entity != null && entity instanceof Player) {
            final Player player = (Player) entity;
            if (!player.hasPermission("booscooldowns.nocancel.damage")) {
                if (BoosWarmUpManager.hasWarmUps(player)) {
                    BoosChat.sendMessageToPlayer(player, BoosConfigManager
                            .getWarmUpCancelledByDamageMessage());
                    BoosWarmUpManager.cancelWarmUps(player);
                }

            }
        }
    }
}
