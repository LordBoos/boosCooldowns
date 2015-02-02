package cz.boosik.boosCooldown;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import util.boosChat;

class BoosPriceManager {
    private static final Economy economy = BoosCoolDown.getEconomy();

    private static boolean payForCommand(Player player,
                                         String originalCommand, double price) {
        if (economy == null) {
            return true;
        }
        EconomyResponse r = economy.withdrawPlayer(player, price);
        String msg = "";
        if (r.transactionSuccess()) {
            msg = String.format(BoosConfigManager.getPaidForCommandMessage(),
                    economy.format(r.amount), economy.format(r.balance));
            msg = msg.replaceAll("&command&", originalCommand);
            boosChat.sendMessageToPlayer(player, msg);
            return true;
        } else {
                msg = String.format(BoosConfigManager.getPaidErrorMessage(),
                        r.errorMessage);
            boosChat.sendMessageToPlayer(player, msg);
            return false;
        }
    }

    static void payForCommand(PlayerCommandPreprocessEvent event,
                              Player player, String regexCommand, String originalCommand,
                              double price) {
        if (price > 0) {
            if (!player.hasPermission("booscooldowns.noprice")
                    && !player.hasPermission("booscooldowns.noprice."
                    + originalCommand)) {
                if (!payForCommand(player, originalCommand, price)) {
                    BoosCoolDownManager.cancelCooldown(player, regexCommand);
                    event.setCancelled(true);
                }
            }
        }
    }

    public static boolean has(Player player, double price) {
        return economy == null || price <= 0 || economy.has(player, price);
    }
}
