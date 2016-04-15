package cz.boosik.boosCooldown.Managers;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import cz.boosik.boosCooldown.BoosCoolDown;
import cz.boosik.boosCooldown.Runnables.BoosWarmUpTimer;
import util.boosChat;

public class BoosWarmUpManager {

    private static final ConcurrentHashMap<String, BoosWarmUpTimer> playercommands = new ConcurrentHashMap<>();

    private static void applyPotionEffect(Player player, String regexCommand,
                                          int warmUpSeconds) {
        String potion = BoosConfigManager.getPotionEffect(regexCommand, player);
        if (potion.equals("")) {
            return;
        }
        int potionStrength = BoosConfigManager.getPotionEffectStrength(
                regexCommand, player);
        if (potionStrength == 0) {
            return;
        }
        PotionEffectType effect = PotionEffectType.getByName(potion);
        player.addPotionEffect(
                effect.createEffect(warmUpSeconds * 40, potionStrength - 1),
                true);
    }

    public static void cancelWarmUps(Player player) {
        Map<String, BoosWarmUpTimer> playercommands2 = playercommands;
        Iterator<String> iter = playercommands2.keySet().iterator();
        while (iter.hasNext()) {
            if (iter.next().startsWith(player.getUniqueId() + "@")) {
                killTimer(player);
                iter.remove();
            }
        }
    }

    public static boolean hasWarmUps(Player player) {
        Map<String, BoosWarmUpTimer> playercommands2 = playercommands;
        for (String key : playercommands2.keySet()) {
            if (key.startsWith(player.getUniqueId() + "@")) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkWarmUpOK(Player player, String regexCommand) {
        int pre2 = regexCommand.toLowerCase().hashCode();
        int ok = 0;
        ok = BoosConfigManager.getConfusers().getInt(
                "users." + player.getUniqueId() + ".warmup." + pre2, ok);
        return ok == 1;
    }

    public static boolean isWarmUpProcess(Player player, String regexCommand) {
        regexCommand = regexCommand.toLowerCase();
        return playercommands.containsKey(player.getUniqueId() + "@"
                + regexCommand);
    }

    private static void killTimer(Player player) {
        Map<String, BoosWarmUpTimer> playercommands2 = playercommands;
        for (String key : playercommands2.keySet()) {
            if (key.startsWith(player.getUniqueId() + "@")) {
                playercommands.get(key).cancel();
            }
        }
    }

    public static void removeWarmUp(Player player, String regexCommand) {
        int pre2 = regexCommand.toLowerCase().hashCode();
        BoosConfigManager.getConfusers().set(
                "users." + player.getUniqueId() + ".warmup." + pre2, null);
    }

    public static void removeWarmUpOK(Player player, String regexCommand) {
        int pre2 = regexCommand.toLowerCase().hashCode();
        BoosConfigManager.getConfusers().set(
                "users." + player.getUniqueId() + ".warmup." + pre2, null);
    }

    public static void removeWarmUpProcess(String tag) {
        BoosWarmUpManager.playercommands.remove(tag);
    }

    public static void setWarmUpOK(Player player, String regexCommand) {
        int pre2 = regexCommand.toLowerCase().hashCode();
        BoosConfigManager.getConfusers().set(
                "users." + player.getUniqueId() + ".warmup." + pre2, 1);
    }

    public static void startWarmUp(BoosCoolDown bCoolDown, Player player,
                                   String regexCommand, String originalCommand, int warmUpSeconds) {
        regexCommand = regexCommand.toLowerCase();
        int warmUpSecondsTem = warmUpSeconds;
        long warmUpMinutes = (long) Math.floor(warmUpSeconds / 60.0);
        long warmUpHours = (long) Math.floor(warmUpMinutes / 60.0);
        if (!isWarmUpProcess(player, regexCommand)) {
            BoosWarmUpManager.removeWarmUpOK(player, regexCommand);
            String msg = BoosConfigManager.getWarmUpMessage();
            StringBuilder stringBuilder = new StringBuilder();
            msg = msg.replaceAll("&command&", originalCommand);
            if (warmUpSeconds >= 3600) {
                stringBuilder.append(Long.toString(warmUpHours));
                stringBuilder.append(" ");
                stringBuilder.append(BoosConfigManager.getUnitHoursMessage());
                stringBuilder.append(", ");
                warmUpSeconds = (int) (warmUpSeconds - (warmUpHours * 3600));
            }
            if (warmUpSeconds >= 60) {
                warmUpMinutes = warmUpMinutes - (warmUpHours * 60);
                stringBuilder.append(Long.toString(warmUpMinutes));
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

            boosChat.sendMessageToPlayer(player, msg);

            Timer scheduler = new Timer();
            BoosWarmUpTimer scheduleMe = new BoosWarmUpTimer(bCoolDown, player, regexCommand, originalCommand);
            playercommands.put(player.getUniqueId() + "@" + regexCommand,
                    scheduleMe);
            scheduler.schedule(scheduleMe, warmUpSecondsTem * 1000);
            applyPotionEffect(player, regexCommand, warmUpSecondsTem);
        } else {
            String msg = BoosConfigManager.getWarmUpAlreadyStartedMessage();
            msg = msg.replaceAll("&command&", originalCommand);
            boosChat.sendMessageToPlayer(player, msg);
        }
    }
}
