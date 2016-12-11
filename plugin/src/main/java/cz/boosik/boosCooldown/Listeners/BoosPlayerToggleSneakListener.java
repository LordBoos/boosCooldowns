package cz.boosik.boosCooldown.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import cz.boosik.boosCooldown.Managers.BoosConfigManager;
import cz.boosik.boosCooldown.Managers.BoosWarmUpManager;
import util.BoosChat;

public class BoosPlayerToggleSneakListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (player != null
                && !player.hasPermission("booscooldowns.nocancel.sneak")) {
            if (BoosWarmUpManager.hasWarmUps(player)) {
                BoosChat.sendMessageToPlayer(player,
                        BoosConfigManager.getCancelWarmupOnSneakMessage());
                BoosWarmUpManager.cancelWarmUps(player);
            }

        }
    }
}