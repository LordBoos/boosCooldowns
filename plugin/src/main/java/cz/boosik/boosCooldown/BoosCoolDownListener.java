package cz.boosik.boosCooldown;

import static cz.boosik.boosCooldown.Managers.BoosItemCostManager.getItemStackJson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.coloredcarrot.mcapi.json.JSON;
import com.coloredcarrot.mcapi.json.JSONClickAction;
import com.coloredcarrot.mcapi.json.JSONColor;
import com.coloredcarrot.mcapi.json.JSONComponent;
import com.coloredcarrot.mcapi.json.JSONHoverAction;
import cz.boosik.boosCooldown.Managers.BoosAliasManager;
import cz.boosik.boosCooldown.Managers.BoosConfigManager;
import cz.boosik.boosCooldown.Managers.BoosCoolDownManager;
import cz.boosik.boosCooldown.Managers.BoosItemCostManager;
import cz.boosik.boosCooldown.Managers.BoosLimitManager;
import cz.boosik.boosCooldown.Managers.BoosPlayerPointsManager;
import cz.boosik.boosCooldown.Managers.BoosPriceManager;
import cz.boosik.boosCooldown.Managers.BoosWarmUpManager;
import cz.boosik.boosCooldown.Managers.BoosXpCostManager;
import util.BoosChat;

public class BoosCoolDownListener implements Listener {
    public static Map<String, Boolean> commandQueue = new ConcurrentHashMap<>();
    private static BoosCoolDown plugin;

    BoosCoolDownListener(final BoosCoolDown instance) {
        plugin = instance;
    }

    private void checkRestrictions(
            final PlayerCommandPreprocessEvent event,
            final Player player, final String regexCommad, final String originalCommand,
            final int warmupTime, final int cooldownTime, final double price, final String item,
            final int count, final String name, final List<String> lore, final List<String> enchants, final int limit, final int xpPrice,
            final int xpRequirement, final int playerPoints) {
        boolean blocked = false;
        final String perm = BoosConfigManager.getPermission(player, regexCommad);
        if (!(perm == null)) {
            if (!player.hasPermission(perm)) {
                final String msg = BoosConfigManager.getPermissionMessage(player, regexCommad);
                if (!(msg == null)) {
                    BoosChat.sendMessageToPlayer(player, msg);
                }
                event.setCancelled(true);
            }
        }
        if (limit != -1) {
            blocked = BoosLimitManager.blocked(player, regexCommad,
                    originalCommand, limit);
        }
        if (!blocked && !event.isCancelled()) {
            if (warmupTime > 0) {
                if (!player.hasPermission("booscooldowns.nowarmup")
                        && !player.hasPermission("booscooldowns.nowarmup."
                        + originalCommand)) {
                    start(event, player, regexCommad, originalCommand,
                            warmupTime, cooldownTime);
                }
            } else if (BoosPriceManager.has(player, price)
                    & BoosItemCostManager.has(player, item, count, name, lore, enchants)
                    & BoosXpCostManager.has(player, xpPrice)
                    & BoosXpCostManager.has(player, xpRequirement)
                    & BoosPlayerPointsManager.has(player, playerPoints)) {
                if (BoosCoolDownManager.coolDown(player, regexCommad,
                        originalCommand, cooldownTime)) {
                    event.setCancelled(true);
                }
            }
            if (BoosPriceManager.has(player, price)
                    & BoosItemCostManager.has(player, item, count, name, lore, enchants)
                    & BoosXpCostManager.has(player, xpPrice)
                    & BoosXpCostManager.has(player, xpRequirement)
                    & BoosPlayerPointsManager.has(player, playerPoints)) {
                if (!event.isCancelled()) {
                    BoosPriceManager.payForCommand(event, player, regexCommad,
                            originalCommand, price);
                }
                if (!event.isCancelled()) {
                    BoosItemCostManager.payItemForCommand(event, player,
                            regexCommad, originalCommand, item, count, name, lore, enchants);
                }
                if (!event.isCancelled()) {
                    BoosXpCostManager.payXPForCommand(event, player,
                            regexCommad, originalCommand, xpPrice);
                }
                if (!event.isCancelled()) {
                    BoosPlayerPointsManager.payForCommand(event, player,
                            regexCommad, originalCommand, playerPoints);
                }
            } else {
                final boolean warmupInProgress = BoosWarmUpManager.isWarmUpProcess(player, regexCommad);
                final boolean cooldownInProgress = BoosCoolDownManager.isCoolingdown(player, regexCommad, cooldownTime);
                if (!BoosPriceManager.has(player, price)
                        && !warmupInProgress && !cooldownInProgress) {
                    String msg;
                    msg = String.format(
                            BoosConfigManager.getInsufficientFundsMessage(),
                            BoosCoolDown.getEconomy().format(price),
                            BoosCoolDown.getEconomy().format(
                                    BoosCoolDown.getEconomy()
                                            .getBalance(player)));
                    msg = msg.replaceAll("&command&", originalCommand);
                    BoosChat.sendMessageToPlayer(player, msg);
                }
                if (!BoosItemCostManager.has(player, item, count, name, lore, enchants)
                        && !warmupInProgress && !cooldownInProgress) {
                    String msg;
                    msg = BoosConfigManager.getInsufficientItemsMessage();
                    final JSON json = getItemStackJson(1, item, count, name, lore, enchants);
                    msg = msg.replaceAll("&command&", originalCommand);
                    BoosChat.sendMessageToPlayer(player, msg);
                    json.send(player);
                }
                if (!BoosXpCostManager.has(player, xpRequirement)
                        && !warmupInProgress && !cooldownInProgress) {
                    String msg;
                    msg = String.format(
                            BoosConfigManager.getInsufficientXpRequirementMessage(),
                            xpRequirement);
                    msg = msg.replaceAll("&command&", originalCommand);
                    BoosChat.sendMessageToPlayer(player, msg);
                }
                if (!BoosXpCostManager.has(player, xpPrice)
                        && !warmupInProgress && !cooldownInProgress) {
                    String msg;
                    msg = String.format(
                            BoosConfigManager.getInsufficientXpMessage(),
                            xpPrice);
                    msg = msg.replaceAll("&command&", originalCommand);
                    BoosChat.sendMessageToPlayer(player, msg);
                }
                if (!BoosPlayerPointsManager.has(player, playerPoints)
                        && !warmupInProgress && !cooldownInProgress) {
                    String msg;
                    msg = String.format(
                            BoosConfigManager.getInsufficientPlayerPointsMessage(),
                            playerPoints, BoosCoolDown.getPlayerPoints().getAPI().look(player.getUniqueId()));
                    msg = msg.replaceAll("&command&", originalCommand);
                    BoosChat.sendMessageToPlayer(player, msg);
                }
                event.setCancelled(true);
            }
            if (!event.isCancelled()) {
                final String msg = BoosConfigManager.getMessage(
                        regexCommad, player);
                if (!msg.equals("")) {
                    BoosChat.sendMessageToPlayer(player, msg);
                }
            }
        } else {
            event.setCancelled(true);
        }
        if (!event.isCancelled()) {
            final List<String> linkGroup = BoosConfigManager.getSharedLimits(
                    regexCommad, player);
            if (linkGroup.isEmpty()) {
                BoosLimitManager.setUses(player, regexCommad);
            } else {
                BoosLimitManager.setUses(player, regexCommad);
                for (final String a : linkGroup) {
                    BoosLimitManager.setUses(player, a);
                }
            }
            if (BoosConfigManager.getCommandLogging()) {
                BoosCoolDown.commandLogger(player.getName(), originalCommand);
            }
        }
        if (BoosConfigManager.getCancelCommand(regexCommad, player)) {
            event.setCancelled(true);
        }
        for (final String key : commandQueue.keySet()) {
            if (key.startsWith(String.valueOf(player.getUniqueId()))) {
                commandQueue.remove(key);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        if (BoosConfigManager.getSyntaxBlocker() && !player.isOp() && !player.hasPermission("booscooldowns.syntaxblockerexception")) {
            if (event.getMessage().contains(":")) {
                final Pattern p = Pattern.compile("^/([a-zA-Z0-9_]+):");
                final Matcher m = p.matcher(event.getMessage());
                if (m.find()) {
                    {
                        BoosChat.sendMessageToPlayer(player, BoosConfigManager
                                .getInvalidCommandSyntaxMessage());
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
        if (BoosConfigManager.getConfirmCommandEnabled(player)) {
            for (final String key : commandQueue.keySet()) {
                final String[] keyList = key.split("@");
                if (keyList[0].equals(String.valueOf(uuid))) {
                    if (event.getMessage().contains(BoosConfigManager.getConfirmCommandMessage())) {
                        commandQueue.put(key, true);
                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                player.chat(keyList[1]);
                            }
                        });
                        event.setCancelled(true);
                        return;
                    }
                    if (!keyList[1].equals(event.getMessage())) {
                        commandQueue.remove(key);
                        String commandCancelMessage = BoosConfigManager.getCommandCanceledMessage();
                        commandCancelMessage = commandCancelMessage.replace("&command&", keyList[1]);
                        BoosChat.sendMessageToPlayer(player, commandCancelMessage);
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        String originalCommand = event.getMessage().replace("\\", "\\\\");
        originalCommand = originalCommand.replace("$", "SdollarS");
        originalCommand = originalCommand.trim().replaceAll(" +", " ");
        String regexCommad = "";
        final Set<String> aliases = BoosConfigManager.getAliases();
        final Set<String> commands = BoosConfigManager.getCommands(player);
        final boolean on;
        String item = "";
        String name = "";
        List<String> lore = new ArrayList<>();
        List<String> enchants = new ArrayList<>();
        int count = 0;
        int warmupTime = 0;
        double price = 0;
        int limit = -1;
        int cooldownTime = 0;
        int xpPrice = 0;
        int xpRequirement = 0;
        int playerPoints = 0;
        on = BoosCoolDown.isPluginOnForPlayer(player);
        if (aliases != null) {
            originalCommand = BoosAliasManager.checkCommandAlias(
                    originalCommand, aliases, player);
            event.setMessage(originalCommand);
        }
        if (on && commands != null) {
            regexCommad = getRegexCommand(originalCommand, commands);
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
                xpRequirement = BoosConfigManager.getXpRequirement(regexCommad, player);
            }
            if (BoosConfigManager.getPlayerPointsEnabled()) {
                playerPoints = BoosConfigManager.getPlayerPointsPrice(regexCommad,
                        player);
            }
            if (BoosConfigManager.getItemCostEnabled()) {
                item = BoosConfigManager.getItemCostItem(regexCommad,
                        player);
                name = BoosConfigManager.getItemCostName(regexCommad,
                        player);
                lore = BoosConfigManager.getItemCostLore(regexCommad,
                        player);
                count = BoosConfigManager.getItemCostCount(regexCommad,
                        player);
                enchants = BoosConfigManager.getItemCostEnchants(regexCommad,
                        player);
            }
            if (BoosConfigManager.getLimitEnabled()) {
                limit = BoosConfigManager.getLimit(regexCommad, player);
            }
            if (!BoosConfigManager.getConfirmCommandEnabled(player) || (commandQueue
                    .containsKey(uuid + "@" + originalCommand) && commandQueue.get(uuid + "@" + originalCommand))) {
                this.checkRestrictions(event, player, regexCommad, originalCommand,
                        warmupTime, cooldownTime, price, item, count, name, lore, enchants, limit,
                        xpPrice, xpRequirement, playerPoints);
            } else {
                if ((price > 0 || xpPrice > 0 || count > 0 || limit > 0 || playerPoints > 0) && !BoosWarmUpManager.isWarmUpProcess(player,
                        regexCommad) && !BoosCoolDownManager.isCoolingdown(player, regexCommad, cooldownTime)) {
                    if (BoosConfigManager.getConfirmCommandEnabled(player)) {
                        commandQueue.put(uuid + "@" + originalCommand, false);
                        String questionMessage = BoosConfigManager.getQuestionMessage();
                        questionMessage = questionMessage.replace("&command&", originalCommand);
                        BoosChat.sendMessageToPlayer(player, questionMessage);
                    }
                    if (BoosCoolDown.getEconomy() != null) {
                        if (BoosConfigManager.getPriceEnabled()) {
                            if (price > 0) {
                                String priceMessage = BoosConfigManager.getItsPriceMessage();
                                priceMessage = priceMessage.replace("&price&", BoosCoolDown.getEconomy().format(price))
                                        .replace("&balance&", BoosCoolDown.getEconomy().format(BoosCoolDown.getEconomy().getBalance(player)));
                                BoosChat.sendMessageToPlayer(player, "    " + priceMessage);
                            }
                        }
                    }
                    if (BoosCoolDown.getPlayerPoints() != null) {
                        if (BoosConfigManager.getPlayerPointsEnabled()) {
                            if (playerPoints > 0) {
                                String playerPointsMessage = BoosConfigManager.getItsPlayerPointsPriceMessage();
                                playerPointsMessage = playerPointsMessage.replace("&ppprice&", String.valueOf(playerPoints))
                                        .replace("&ppbalance&",
                                                String.valueOf(BoosCoolDown.getPlayerPoints().getAPI().look(player.getUniqueId())));
                                BoosChat.sendMessageToPlayer(player, "    " + playerPointsMessage);
                            }
                        }
                    }
                    if (xpPrice > 0) {
                        String xpMessage = BoosConfigManager.getItsXpPriceMessage();
                        xpMessage = xpMessage.replace("&xpprice&", String.valueOf(xpPrice));
                        BoosChat.sendMessageToPlayer(player, "    " + xpMessage);
                    }
                    if (count > 0) {
                        String itemMessage = BoosConfigManager.getItsItemCostMessage();
                        itemMessage = itemMessage.replace("&itemprice&", "").replace("&itemname&", "");
                        final JSON json = getItemStackJson(2, item, count, name, lore, enchants);
                        BoosChat.sendMessageToPlayer(player, "    " + itemMessage);
                        json.send(player);
                    }
                    if (limit > 0) {
                        final int uses = BoosLimitManager.getUses(player, regexCommad);
                        String limitMessage = BoosConfigManager.getItsLimitMessage();
                        limitMessage = limitMessage.replace("&limit&", String.valueOf(limit))
                                .replace("&uses&", String.valueOf(limit - uses));
                        BoosChat.sendMessageToPlayer(player, "    " + limitMessage);
                    }
                    final String yesString = BoosConfigManager.getConfirmCommandMessage();
                    final JSONClickAction yesClick = new JSONClickAction.RunCommand(yesString);
                    final JSONHoverAction yesHover = new JSONHoverAction.ShowStringText(BoosConfigManager.getConfirmCommandHint());
                    final JSONComponent yes = new JSONComponent("    " + yesString);
                    yes.setColor(JSONColor.GREEN).setBold(true);
                    yes.setClickAction(yesClick);
                    yes.setHoverAction(yesHover);
                    yes.send(player);

                    final String noString = BoosConfigManager.getCancelCommandMessage();
                    final JSONClickAction noClick = new JSONClickAction.RunCommand(noString);
                    final JSONHoverAction noHover = new JSONHoverAction.ShowStringText(BoosConfigManager.getCancelCommandHint());
                    final JSONComponent no = new JSONComponent("    " + noString);
                    no.setColor(JSONColor.RED).setBold(true);
                    no.setClickAction(noClick);
                    no.setHoverAction(noHover);
                    no.send(player);

                    event.setCancelled(true);
                    return;
                } else {
                    this.checkRestrictions(event, player, regexCommad, originalCommand,
                            warmupTime, cooldownTime, price, item, count, name, lore, enchants, limit,
                            xpPrice, xpRequirement, playerPoints);
                }
            }
        }
        originalCommand = originalCommand.replace("SdollarS", "$");
        event.setMessage(originalCommand);
    }

    public static String getRegexCommand(final String originalCommand, final Set<String> commands) {
        for (final String group : commands) {
            final String group2 = group.replace("*", ".*");
            if (originalCommand.matches("(?i)" + group2)) {
                return group;
            }
        }
        return originalCommand;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onPlayerChatEvent(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        if (BoosConfigManager.getConfirmCommandEnabled(player)) {
            for (final String key : commandQueue.keySet()) {
                final String[] keyList = key.split("@");
                if (keyList[0].equals(String.valueOf(uuid))) {
                    if (event.getMessage().contains(BoosConfigManager.getConfirmCommandMessage())) {
                        commandQueue.put(key, true);
                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                player.chat(keyList[1]);
                            }
                        });
                        event.setCancelled(true);
                    } else {
                        commandQueue.remove(key);
                        String commandCancelMessage = BoosConfigManager.getCommandCanceledMessage();
                        commandCancelMessage = commandCancelMessage.replace("&command&", keyList[1]);
                        BoosChat.sendMessageToPlayer(player, commandCancelMessage);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    private void start(
            final PlayerCommandPreprocessEvent event, final Player player,
            final String regexCommad, final String originalCommand, final int warmupTime,
            final int cooldownTime) {
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
