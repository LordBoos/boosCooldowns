package cz.boosik.boosCooldown.Listeners;

import cz.boosik.boosCooldown.BoosConfigManager;
import cz.boosik.boosCooldown.BoosCoolDownManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class BoosPlayerDeathListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onPlayerDeath(PlayerDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity != null) {
            Player player = (Player) entity;
            clearCooldownsOnDeath(player);
            clearUsesOnDeath(player);
            startCooldownsOnDeath(player);
        }
    }

    private void startCooldownsOnDeath(Player player) {
        if (player != null) {
            if (BoosConfigManager.getStartCooldownsOnDeath()) {
                BoosCoolDownManager.startAllCooldowns(player, "");
            }
        }
    }

    private void clearUsesOnDeath(Player player) {
        if (player != null
                && player.hasPermission("booscooldowns.clear.uses.death")) {
            if (BoosConfigManager.getCleanUsesOnDeath()) {
                BoosConfigManager.clearSomething("uses", player.getUniqueId());
            }
        }
    }

    private void clearCooldownsOnDeath(Player player) {
        if (player != null
                && player.hasPermission("booscooldowns.clear.cooldowns.death")) {
            if (BoosConfigManager.getCleanCooldownsOnDeath()) {
                BoosConfigManager.clearSomething("cooldown",
                        player.getUniqueId());
            }
        }
    }
}
