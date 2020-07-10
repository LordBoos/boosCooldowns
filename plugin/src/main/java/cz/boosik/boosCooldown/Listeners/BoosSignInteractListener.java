package cz.boosik.boosCooldown.Listeners;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import cz.boosik.boosCooldown.BoosCoolDown;
import cz.boosik.boosCooldown.Managers.BoosConfigManager;
import util.BoosChat;

public class BoosSignInteractListener implements Listener {
    private final BoosCoolDown plugin;

    public BoosSignInteractListener(final BoosCoolDown instance) {
        plugin = instance;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onSignInteract(final PlayerInteractEvent event) {
        String msg;
        if (event.isCancelled()) {
            return;
        }

        if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock().getType() == Material.LEGACY_SIGN
                    || event.getClickedBlock().getType() == Material.LEGACY_SIGN_POST
                    || event.getClickedBlock().getType() == Material.LEGACY_WALL_SIGN

                    || event.getClickedBlock().getType() == Material.ACACIA_SIGN
                    || event.getClickedBlock().getType() == Material.ACACIA_WALL_SIGN

                    || event.getClickedBlock().getType() == Material.DARK_OAK_SIGN
                    || event.getClickedBlock().getType() == Material.DARK_OAK_WALL_SIGN

                    || event.getClickedBlock().getType() == Material.JUNGLE_SIGN
                    || event.getClickedBlock().getType() == Material.JUNGLE_WALL_SIGN

                    || event.getClickedBlock().getType() == Material.OAK_SIGN
                    || event.getClickedBlock().getType() == Material.OAK_WALL_SIGN

                    || event.getClickedBlock().getType() == Material.SPRUCE_SIGN
                    || event.getClickedBlock().getType() == Material.SPRUCE_WALL_SIGN

                    || event.getClickedBlock().getType() == Material.BIRCH_SIGN
                    || event.getClickedBlock().getType() == Material.BIRCH_WALL_SIGN) {
                final Sign s = (Sign) event.getClickedBlock().getState();
                final String line1 = s.getLine(0);
                final String line2 = s.getLine(1);
                final String line3 = s.getLine(2);
                final String line4 = s.getLine(3);
                final Player player = event.getPlayer();
                if (line1.equals("[boosCooldowns]")) {
                    if (line2.equals("player")
                            && player
                            .hasPermission("booscooldowns.signs.player.use")) {
                        msg = line3;
                        if (line3.endsWith("+") || !line4.isEmpty()) {
                            msg = line3.substring(0, line3.length() - 1) + " "
                                    + line4;
                        }
                        event.getPlayer().chat(msg);
                    } else if (line2.equals("server")
                            && player
                            .hasPermission("booscooldowns.signs.server.use")) {
                        msg = line3;
                        if (line3.endsWith("+") || !line4.isEmpty()) {
                            msg = line3.substring(0, line3.length() - 1) + " "
                                    + line4;
                        }
                        plugin.getServer().dispatchCommand(
                                plugin.getServer().getConsoleSender(), msg);
                    } else {
                        BoosChat.sendMessageToPlayer(player,
                                BoosConfigManager.getCannotUseSignMessage());
                    }
                }
            }
        }
    }
}
