package cz.boosik.boosCooldown.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import cz.boosik.boosCooldown.Managers.BoosConfigManager;
import cz.boosik.boosCooldown.Managers.BoosCoolDownManager;

public class BoosPlayerDeathListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onPlayerDeath(final PlayerDeathEvent event) {
        final Entity entity = event.getEntity();
        if (entity != null) {
            final Player player = (Player) entity;
            clearCooldownsOnDeath(player);
            clearUsesOnDeath(player);
            startCooldownsOnDeath(player);
        }
    }

    private void startCooldownsOnDeath(final Player player) {
        if (player != null) {
            if (BoosConfigManager.getStartCooldownsOnDeath()) {
                BoosCoolDownManager.startAllCooldowns(player, "");
            }
        }
    }

    private void clearUsesOnDeath(final Player player) {
        if (player != null
                && player.hasPermission("booscooldowns.clear.uses.death")) {
            if (BoosConfigManager.getCleanUsesOnDeath()) {
                BoosConfigManager.clearSomething("uses", player.getUniqueId());
            }
        }
    }

    private void clearCooldownsOnDeath(final Player player) {
        if (player != null
                && player.hasPermission("booscooldowns.clear.cooldowns.death")) {
            if (BoosConfigManager.getCleanCooldownsOnDeath()) {
                BoosConfigManager.clearSomething("cooldown",
                        player.getUniqueId());
            }
        }
    }
}
