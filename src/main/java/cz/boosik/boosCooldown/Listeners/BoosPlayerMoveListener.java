package cz.boosik.boosCooldown.Listeners;

import cz.boosik.boosCooldown.BoosConfigManager;
import cz.boosik.boosCooldown.BoosWarmUpManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import util.boosChat;

public class BoosPlayerMoveListener implements Listener {
    private int tempTimer = 0;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onPlayerMove(PlayerMoveEvent event) {

        if (tempTimer < 10) {
            tempTimer = tempTimer + 1;
        } else {
            Player player = event.getPlayer();
            if (player != null
                    && !player.hasPermission("booscooldowns.nocancel.move")) {
                if (BoosWarmUpManager.hasWarmUps(player) && (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ() || event.getFrom().getY() != event.getTo().getY())) {
                    BoosWarmUpManager.clearLocWorld(player);
                    boosChat.sendMessageToPlayer(player,
                            BoosConfigManager.getWarmUpCancelledByMoveMessage());
                    BoosWarmUpManager.cancelWarmUps(player);
                }
            }
            tempTimer = 0;
        }
    }
}