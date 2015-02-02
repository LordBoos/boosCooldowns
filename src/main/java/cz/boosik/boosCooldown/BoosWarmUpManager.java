package cz.boosik.boosCooldown;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import util.boosChat;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class BoosWarmUpManager {

    private static final ConcurrentHashMap<String, BoosWarmUpTimer> playercommands = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Player, Location> playerloc = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Player, String> playerworld = new ConcurrentHashMap<>();

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

    public static void clearLocWorld(Player player) {
        BoosWarmUpManager.playerloc.remove(player);
        BoosWarmUpManager.playerworld.remove(player);
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

    static boolean checkWarmUpOK(Player player, String regexCommand) {
        int pre2 = regexCommand.toLowerCase().hashCode();
        int ok = 0;
        ok = BoosConfigManager.getConfusers().getInt(
                "users." + player.getUniqueId() + ".warmup." + pre2, ok);
        return ok == 1;
    }

    static boolean isWarmUpProcess(Player player, String regexCommand) {
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

    static void removeWarmUp(Player player, String regexCommand) {
        int pre2 = regexCommand.toLowerCase().hashCode();
        BoosConfigManager.getConfusers().set(
                "users." + player.getUniqueId() + ".warmup." + pre2, null);
    }

    static void removeWarmUpOK(Player player, String regexCommand) {
        int pre2 = regexCommand.toLowerCase().hashCode();
        BoosConfigManager.getConfusers().set(
                "users." + player.getUniqueId() + ".warmup." + pre2, null);
    }

    static void removeWarmUpProcess(String tag) {
        BoosWarmUpManager.playercommands.remove(tag);
    }

    static void setWarmUpOK(Player player, String regexCommand) {
        int pre2 = regexCommand.toLowerCase().hashCode();
        BoosConfigManager.getConfusers().set(
                "users." + player.getUniqueId() + ".warmup." + pre2, 1);
    }

    static void startWarmUp(BoosCoolDown bCoolDown, Player player,
                            String regexCommand, String originalCommand, int warmUpSeconds) {
        regexCommand = regexCommand.toLowerCase();
        long warmUpMinutes = (long) Math.ceil(warmUpSeconds / 60.0);
        long warmUpHours = (long) Math.ceil(warmUpMinutes / 60.0);
        if (!isWarmUpProcess(player, regexCommand)) {
            BoosWarmUpManager.removeWarmUpOK(player, regexCommand);
            String msg = BoosConfigManager.getWarmUpMessage();
            msg = msg.replaceAll("&command&", originalCommand);
            if (warmUpSeconds >= 60 && 3600 >= warmUpSeconds) {
                msg = msg.replaceAll("&seconds&", Long.toString(warmUpMinutes));
                msg = msg.replaceAll("&unit&",
                        BoosConfigManager.getUnitMinutesMessage());
            } else if (warmUpMinutes >= 60) {
                msg = msg.replaceAll("&seconds&", Long.toString(warmUpHours));
                msg = msg.replaceAll("&unit&",
                        BoosConfigManager.getUnitHoursMessage());
            } else {
                msg = msg.replaceAll("&seconds&", Long.toString(warmUpSeconds));
                msg = msg.replaceAll("&unit&",
                        BoosConfigManager.getUnitSecondsMessage());
            }
            boosChat.sendMessageToPlayer(player, msg);

            Timer scheduler = new Timer();
            BoosWarmUpTimer scheduleMe = new BoosWarmUpTimer(bCoolDown, player, regexCommand, originalCommand);
            playercommands.put(player.getUniqueId() + "@" + regexCommand,
                    scheduleMe);
            scheduler.schedule(scheduleMe, warmUpSeconds * 1000);
            applyPotionEffect(player, regexCommand, warmUpSeconds);
        } else {
            String msg = BoosConfigManager.getWarmUpAlreadyStartedMessage();
            msg = msg.replaceAll("&command&", originalCommand);
            boosChat.sendMessageToPlayer(player, msg);
        }
    }

    public static ConcurrentHashMap<Player, String> getPlayerworld() {
        return playerworld;
    }

    public static ConcurrentHashMap<Player, Location> getPlayerloc() {
        return playerloc;
    }
}
