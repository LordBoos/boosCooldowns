package cz.boosik.boosCooldown.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

import cz.boosik.boosCooldown.Managers.BoosConfigManager;
import cz.boosik.boosCooldown.Managers.BoosWarmUpManager;
import util.BoosChat;

public class BoosPlayerGameModeChangeListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Entity entity = event.getPlayer();
        if (entity != null) {
            Player player = (Player) entity;
            if (!player
                    .hasPermission("booscooldowns.nocancel.gamemodechange")) {
                if (BoosWarmUpManager.hasWarmUps(player)) {
                    BoosChat.sendMessageToPlayer(player, BoosConfigManager
                            .getCancelWarmupByGameModeChangeMessage());
                    BoosWarmUpManager.cancelWarmUps(player);
                }

            }
        }
    }
}
