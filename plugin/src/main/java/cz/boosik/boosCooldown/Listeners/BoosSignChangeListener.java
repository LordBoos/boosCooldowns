package cz.boosik.boosCooldown.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import cz.boosik.boosCooldown.Managers.BoosConfigManager;
import util.BoosChat;

public class BoosSignChangeListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String line1 = event.getLine(0);
        String line2 = event.getLine(1);
        if (line1.equals("[boosCooldowns]")) {
            if (line2.equals("player")
                    && !player
                    .hasPermission("booscooldowns.signs.player.place")) {
                BoosChat.sendMessageToPlayer(player,
                        BoosConfigManager.getCannotCreateSignMessage());
                event.getBlock().breakNaturally();
                event.setCancelled(true);
            }
            if (line2.equals("server")
                    && !player
                    .hasPermission("booscooldowns.signs.server.place")) {
                BoosChat.sendMessageToPlayer(player,
                        BoosConfigManager.getCannotCreateSignMessage());
                event.getBlock().breakNaturally();
                event.setCancelled(true);
            }
        }
    }
}
