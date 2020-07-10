package cz.boosik.boosCooldown.Managers;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import cz.boosik.boosCooldown.BoosCoolDown;
import cz.boosik.boosCooldown.Runnables.BoosWarmUpTimer;
import util.BoosChat;

public class BoosWarmUpManager {

    private static final ConcurrentHashMap<String, BoosWarmUpTimer> playercommands = new ConcurrentHashMap<>();

    private static void applyPotionEffect(final Player player, final String regexCommand, final int warmUpSeconds) {
        for (final String potionUnparsed : BoosConfigManager.getPotionEffects(regexCommand, player)) {
            final String[] potionParsed = potionUnparsed.split(",");
            final PotionEffectType type = PotionEffectType.getByName(potionParsed[0]);
            final int duration = potionParsed.length == 3 ? Integer.valueOf(potionParsed[2]) * 20 : warmUpSeconds * 20;
            player.addPotionEffect(new PotionEffect(type, duration, Integer.valueOf(potionParsed[1]) - 1), true);
        }
    }

    public static void cancelWarmUps(final Player player) {
        final Iterator<String> iter = ((Map<String, BoosWarmUpTimer>) playercommands).keySet().iterator();
        while (iter.hasNext()) {
            final String key = iter.next();
            if (key.startsWith(player.getUniqueId() + "@")) {
                if (BoosConfigManager.getCancelPotionsOnWarmupCancel()) {
                    for (final String potionUnparsed : BoosConfigManager.getPotionEffects(playercommands.get(key).getRegexCommand(), player)) {
                        player.removePotionEffect(PotionEffectType.getByName(potionUnparsed.split(",")[0]));
                    }
                }
                killTimer(player);
                iter.remove();
            }
        }
    }

    public static boolean hasWarmUps(final Player player) {
        for (final String key : ((Map<String, BoosWarmUpTimer>) playercommands).keySet()) {
            if (key.startsWith(player.getUniqueId() + "@")) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkWarmUpOK(final Player player, final String regexCommand) {
        final int pre2 = regexCommand.toLowerCase().hashCode();
        int ok = 0;
        ok = BoosConfigManager.getConfusers().getInt(
                "users." + player.getUniqueId() + ".warmup." + pre2, ok);
        return ok == 1;
    }

    public static boolean isWarmUpProcess(final Player player, String regexCommand) {
        regexCommand = regexCommand.toLowerCase();
        return playercommands.containsKey(player.getUniqueId() + "@"
                + regexCommand);
    }

    private static void killTimer(final Player player) {
        for (final String key : ((Map<String, BoosWarmUpTimer>) playercommands).keySet()) {
            if (key.startsWith(player.getUniqueId() + "@")) {
                playercommands.get(key).cancel();
            }
        }
    }

    public static void removeWarmUp(final Player player, final String regexCommand) {
        final int pre2 = regexCommand.toLowerCase().hashCode();
        BoosConfigManager.getConfusers().set(
                "users." + player.getUniqueId() + ".warmup." + pre2, null);
    }

    public static void removeWarmUpOK(final Player player, final String regexCommand) {
        final int pre2 = regexCommand.toLowerCase().hashCode();
        BoosConfigManager.getConfusers().set(
                "users." + player.getUniqueId() + ".warmup." + pre2, null);
    }

    public static void removeWarmUpProcess(final String tag) {
        BoosWarmUpManager.playercommands.remove(tag);
    }

    public static void setWarmUpOK(final Player player, final String regexCommand) {
        final int pre2 = regexCommand.toLowerCase().hashCode();
        BoosConfigManager.getConfusers().set(
                "users." + player.getUniqueId() + ".warmup." + pre2, 1);
    }

    public static void startWarmUp(
            final BoosCoolDown bCoolDown, final Player player,
            String regexCommand, final String originalCommand, int warmUpSeconds) {
        regexCommand = regexCommand.toLowerCase();
        final int warmUpSecondsTem = warmUpSeconds;
        long warmUpMinutes = (long) Math.floor(warmUpSeconds / 60.0);
        final long warmUpHours = (long) Math.floor(warmUpMinutes / 60.0);
        if (!isWarmUpProcess(player, regexCommand)) {
            BoosWarmUpManager.removeWarmUpOK(player, regexCommand);
            String msg = BoosConfigManager.getWarmUpMessage();
            final StringBuilder stringBuilder = new StringBuilder();
            msg = msg.replaceAll("&command&", originalCommand);
            if (warmUpSeconds >= 3600) {
                stringBuilder.append(warmUpHours);
                stringBuilder.append(" ");
                stringBuilder.append(BoosConfigManager.getUnitHoursMessage());
                stringBuilder.append(", ");
                warmUpSeconds = (int) (warmUpSeconds - (warmUpHours * 3600));
            }
            if (warmUpSeconds >= 60) {
                warmUpMinutes = warmUpMinutes - (warmUpHours * 60);
                stringBuilder.append(warmUpMinutes);
                stringBuilder.append(" ");
                stringBuilder.append(BoosConfigManager.getUnitMinutesMessage());
                stringBuilder.append(", ");
                warmUpSeconds = (int) (warmUpSeconds - (warmUpMinutes * 60));
            }
            String secs = Long.toString(warmUpSeconds);
            if (secs.equals("0")) {
                secs = "1";
            }
            stringBuilder.append(secs);
            stringBuilder.append(" ");
            stringBuilder.append(BoosConfigManager.getUnitSecondsMessage());

            msg = msg.replaceAll("&seconds&", stringBuilder.toString());
            msg = msg.replaceAll("&unit&", "");
            msg = msg.replaceAll(" +", " ");

            BoosChat.sendMessageToPlayer(player, msg);

            final Timer scheduler = new Timer();
            final BoosWarmUpTimer scheduleMe = new BoosWarmUpTimer(bCoolDown, player, regexCommand, originalCommand);
            playercommands.put(player.getUniqueId() + "@" + regexCommand,
                    scheduleMe);
            scheduler.schedule(scheduleMe, warmUpSecondsTem * 1000);
            applyPotionEffect(player, regexCommand, warmUpSecondsTem);
        } else {
            String msg = BoosConfigManager.getWarmUpAlreadyStartedMessage();
            msg = msg.replaceAll("&command&", originalCommand);
            BoosChat.sendMessageToPlayer(player, msg);
        }
    }
}
