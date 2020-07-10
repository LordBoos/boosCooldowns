package cz.boosik.boosCooldown.Managers;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import util.BoosChat;

public class BoosXpCostManager {

    private static boolean payXPForCommand(
            final Player player,
            final String originalCommand, final int xpPrice) {
        final int xp = player.getLevel();
        Boolean trans = false;
        if (xp >= xpPrice) {
            player.setLevel(xp - xpPrice);
            trans = true;
        }
        if (trans) {
            String msg = String.format(BoosConfigManager.getPaidXPForCommandMessage(),
                    xpPrice);
            msg = msg.replaceAll("&command&", originalCommand);
            BoosChat.sendMessageToPlayer(player, msg);
            return true;
        } else {
            return false;
        }
    }

    public static void payXPForCommand(
            final PlayerCommandPreprocessEvent event,
            final Player player, final String regexCommand, final String originalCommand,
            final int xpPrice) {
        if (xpPrice > 0) {
            if (!player.hasPermission("booscooldowns.noxpcost")
                    && !player.hasPermission("booscooldowns.noxpcost."
                    + originalCommand)) {
                if (!payXPForCommand(player, originalCommand,
                        xpPrice)) {
                    BoosCoolDownManager.cancelCooldown(player, regexCommand);
                    event.setCancelled(true);
                }
            }
        }
    }

    public static boolean has(final Player player, final int xpPrice) {
        if (xpPrice <= 0) {
            return true;
        }
        final int xp = player.getLevel();
        return xp >= xpPrice;
    }
}
