package cz.boosik.boosCooldown;

import cz.boosik.boosCooldown.Managers.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import util.boosChat;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class BoosCoolDownListener implements Listener {
    private static BoosCoolDown plugin;

    public BoosCoolDownListener(BoosCoolDown instance) {
        plugin = instance;
    }

    private void checkRestrictions(PlayerCommandPreprocessEvent event,
                                   Player player, String regexCommad, String originalCommand,
                                   int warmupTime, int cooldownTime, double price, String item,
                                   int count, int limit, int xpPrice) {
        boolean blocked = false;
        String perm = BoosConfigManager.getPermission(player, regexCommad);
        if (!(perm == null)) {
            if (!player.hasPermission(perm)) {
                String msg = BoosConfigManager.getPermissionMessage(player, regexCommad);
                if (!(msg == null)){
                    boosChat.sendMessageToPlayer(player, msg);
                }
                event.setCancelled(true);
            }
        }
        if (limit != -1) {
            blocked = BoosLimitManager.blocked(player, regexCommad,
                    originalCommand, limit);
        }
        if (!blocked) {
            if (warmupTime > 0) {
                if (!player.hasPermission("booscooldowns.nowarmup")
                        && !player.hasPermission("booscooldowns.nowarmup."
                        + originalCommand)) {
                    start(event, player, regexCommad, originalCommand,
                            warmupTime, cooldownTime);
                }
            } else if (BoosPriceManager.has(player, price)
                    & BoosItemCostManager.has(player, item, count)
                    & BoosXpCostManager.has(player, xpPrice)) {
                if (BoosCoolDownManager.coolDown(player, regexCommad,
                        originalCommand, cooldownTime)) {
                    event.setCancelled(true);
                }
            }
            if (BoosPriceManager.has(player, price)
                    & BoosItemCostManager.has(player, item, count)
                    & BoosXpCostManager.has(player, xpPrice)) {
                if (!event.isCancelled()) {
                    BoosPriceManager.payForCommand(event, player, regexCommad,
                            originalCommand, price);
                }
                if (!event.isCancelled()) {
                    BoosItemCostManager.payItemForCommand(event, player,
                            regexCommad, originalCommand, item, count);
                }
                if (!event.isCancelled()) {
                    BoosXpCostManager.payXPForCommand(event, player,
                            regexCommad, originalCommand, xpPrice);
                }
            } else {
                if (!BoosPriceManager.has(player, price)
                        & !BoosWarmUpManager.isWarmUpProcess(player,
                        regexCommad)) {
                    String unit;
                    String msg = "";
                    if (price == 1) {
                        unit = BoosCoolDown.getEconomy().currencyNameSingular();
                    } else {
                        unit = BoosCoolDown.getEconomy().currencyNamePlural();
                    }
                    msg = String.format(
                            BoosConfigManager.getInsufficientFundsMessage(),
                            (price + " " + unit),
                            BoosCoolDown.getEconomy().format(
                                    BoosCoolDown.getEconomy()
                                            .getBalance(player)));
                    msg = msg.replaceAll("&command&", originalCommand);
                    boosChat.sendMessageToPlayer(player, msg);
                }
                if (!BoosItemCostManager.has(player, item, count)
                        & !BoosWarmUpManager.isWarmUpProcess(player,
                        regexCommad)) {
                    String msg = "";
                    msg = String.format(
                            BoosConfigManager.getInsufficientItemsMessage(),
                            (count + " " + item));
                    msg = msg.replaceAll("&command&", originalCommand);
                    boosChat.sendMessageToPlayer(player, msg);
                }
                if (!BoosXpCostManager.has(player, xpPrice)
                        & !BoosWarmUpManager.isWarmUpProcess(player,
                        regexCommad)) {
                    String msg = "";
                    msg = String.format(
                            BoosConfigManager.getInsufficientXpMessage(),
                            (xpPrice));
                    msg = msg.replaceAll("&command&", originalCommand);
                    boosChat.sendMessageToPlayer(player, msg);
                }
                event.setCancelled(true);
            }
            if (!event.isCancelled()) {
                String msg = String.format(BoosConfigManager.getMessage(
                        regexCommad, player));
                if (!msg.equals("")) {
                    boosChat.sendMessageToPlayer(player, msg);
                }
            }
        } else {
            event.setCancelled(true);
        }
        if (!event.isCancelled()) {
            List<String> linkGroup = BoosConfigManager.getSharedLimits(
                    regexCommad, player);
                if (linkGroup.isEmpty()) {
                    BoosLimitManager.setUses(player, regexCommad);
                } else {
                    BoosLimitManager.setUses(player, regexCommad);
                    for (String a : linkGroup) {
                        BoosLimitManager.setUses(player, a);
                    }
                }
            if (BoosConfigManager.getCommandLogging()) {
                BoosCoolDown.commandLogger(player.getName(), originalCommand);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (event.getMessage().contains(":")) {
            Pattern p = Pattern.compile("^/([a-zA-Z0-9_]+):");
            Matcher m = p.matcher(event.getMessage());
            if (m.find()) {
                {
                    boosChat.sendMessageToPlayer(player, BoosConfigManager
                            .getInvalidCommandSyntaxMessage());
                    event.setCancelled(true);
                    return;
                }
            }
        }
        String originalCommand = event.getMessage().replace("\\", "\\\\");
        originalCommand = originalCommand.replace("$", "S");
        originalCommand = originalCommand.trim().replaceAll(" +", " ");
        String regexCommad = "";
        Set<String> aliases = BoosConfigManager.getAliases();
        Set<String> commands = BoosConfigManager.getCommands(player);
        boolean on;
        String item = "";
        int count = 0;
        int warmupTime = 0;
        double price = 0;
        int limit = -1;
        int cooldownTime = 0;
        int xpPrice = 0;
        on = BoosCoolDown.isPluginOnForPlayer(player);
        if (aliases != null) {
            originalCommand = BoosAliasManager.checkCommandAlias(
                    originalCommand, aliases, player);
            event.setMessage(originalCommand);
        }
        if (on && commands != null) {
            for (String group : commands) {
                String group2 = group.replace("*", ".+");
                if (originalCommand.matches("(?i)" + group2)) {
                    regexCommad = group;
                    if (BoosConfigManager.getWarmupEnabled()) {
                        warmupTime = BoosConfigManager.getWarmUp(regexCommad,
                                player);
                    }
                    if (BoosConfigManager.getCooldownEnabled()) {
                        cooldownTime = BoosConfigManager.getCoolDown(
                                regexCommad, player);
                    }
                    if (BoosConfigManager.getPriceEnabled()) {
                        price = BoosConfigManager.getPrice(regexCommad, player);
                    }
                    if (BoosConfigManager.getXpPriceEnabled()) {
                        xpPrice = BoosConfigManager.getXpPrice(regexCommad,
                                player);
                    }
                    if (BoosConfigManager.getItemCostEnabled()) {
                        item = BoosConfigManager.getItemCostItem(regexCommad,
                                player);
                        count = BoosConfigManager.getItemCostCount(regexCommad,
                                player);
                    }
                    if (BoosConfigManager.getLimitEnabled()) {
                        limit = BoosConfigManager.getLimit(regexCommad, player);
                    }
                    break;
                }
            }
            this.checkRestrictions(event, player, regexCommad, originalCommand,
                    warmupTime, cooldownTime, price, item, count, limit,
                    xpPrice);
        }
    }

    private void start(PlayerCommandPreprocessEvent event, Player player,
                       String regexCommad, String originalCommand, int warmupTime,
                       int cooldownTime) {
        if (!BoosWarmUpManager.checkWarmUpOK(player, regexCommad)) {
            if (BoosCoolDownManager.checkCoolDownOK(player, regexCommad,
                    originalCommand, cooldownTime)) {
                BoosWarmUpManager.startWarmUp(plugin, player, regexCommad,
                        originalCommand, warmupTime);
                event.setCancelled(true);
            } else {
                event.setCancelled(true);
            }
        } else if (BoosCoolDownManager.coolDown(player, regexCommad,
                originalCommand, cooldownTime)) {
            event.setCancelled(true);
        } else {
            BoosWarmUpManager.removeWarmUpOK(player, regexCommad);
        }
    }
}