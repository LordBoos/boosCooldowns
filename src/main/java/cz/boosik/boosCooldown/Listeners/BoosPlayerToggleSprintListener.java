package cz.boosik.boosCooldown.Listeners;

import cz.boosik.boosCooldown.BoosConfigManager;
import cz.boosik.boosCooldown.BoosWarmUpManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import util.boosChat;

public class BoosPlayerToggleSprintListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        if (player != null
                && !player.hasPermission("booscooldowns.nocancel.sprint")) {
            if (BoosWarmUpManager.hasWarmUps(player)) {
                boosChat.sendMessageToPlayer(player,
                        BoosConfigManager.getCancelWarmupOnSprintMessage());
                BoosWarmUpManager.cancelWarmUps(player);
            }

        }
    }
}