package cz.boosik.boosCooldown.Managers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bukkit.entity.Player;

import util.BoosChat;

public class BoosCoolDownManager {

    static void cancelCooldown(final Player player, final String regexCommand) {
        final int pre2 = regexCommand.toLowerCase().hashCode();
        BoosConfigManager.getConfusers().set(
                "users." + player.getUniqueId() + ".cooldown." + pre2, null);
    }

    public static boolean isCoolingdown(final Player player, final String regexCommand, final int time) {
        final Date lastTime = getTime(player, regexCommand);
        if (lastTime == null) {
            return false;
        }
        final long secondsBetween = getSecondsBetween(lastTime);
        return (secondsBetween <= time) && secondsBetween != 0;
    }

    private static boolean cd(
            final Player player, final String regexCommand,
            final String originalCommand, final int coolDownSeconds) {
        final Date lastTime = getTime(player, regexCommand);
        final List<String> linkGroup = BoosConfigManager.getSharedCooldowns(
                regexCommand, player);
        if (lastTime == null) {
            if (linkGroup.isEmpty()) {
                setTime(player, regexCommand);
            } else {
                setTime(player, regexCommand);
                for (final String a : linkGroup) {
                    setTime(player, a);
                }
            }
            return false;
        } else {
            final long secondsBetween = getSecondsBetween(lastTime);
            long waitSeconds = coolDownSeconds - secondsBetween;
            long waitMinutes = (long) Math.floor(waitSeconds / 60.0);
            final long waitHours = (long) Math.floor(waitMinutes / 60.0);
            if (secondsBetween > coolDownSeconds) {
                if (linkGroup.isEmpty()) {
                    setTime(player, regexCommand);
                } else {
                    setTime(player, regexCommand);
                    for (final String a : linkGroup) {
                        setTime(player, a);
                    }
                }
                return false;
            } else {
                String msg = BoosConfigManager.getCoolDownMessage();
                final StringBuilder stringBuilder = new StringBuilder();
                msg = msg.replaceAll("&command&", originalCommand);
                if (waitSeconds >= 3600) {
                    stringBuilder.append(waitHours);
                    stringBuilder.append(" ");
                    stringBuilder.append(BoosConfigManager.getUnitHoursMessage());
                    stringBuilder.append(", ");
                    waitSeconds = waitSeconds - (waitHours * 3600);
                }
                if (waitSeconds >= 60) {
                    waitMinutes = waitMinutes - (waitHours * 60);
                    stringBuilder.append(waitMinutes);
                    stringBuilder.append(" ");
                    stringBuilder.append(BoosConfigManager.getUnitMinutesMessage());
                    stringBuilder.append(", ");
                    waitSeconds = waitSeconds - (waitMinutes * 60);
                }
                String secs = Long.toString(waitSeconds);
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
                return true;
            }
        }
    }

    public static boolean coolDown(
            final Player player, String regexCommand,
            final String originalCommand, final int time) {
        regexCommand = regexCommand.toLowerCase();
        return time > 0 && !player.hasPermission("booscooldowns.nocooldown") && !player.hasPermission("booscooldowns.nocooldown." + originalCommand) && cd(
                player,
                regexCommand,
                originalCommand,
                time);
    }

    private static Date getCurrTime() {
        String currTime = "";
        final Calendar cal = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        currTime = sdf.format(cal.getTime());
        Date time = null;

        try {
            time = sdf.parse(currTime);
            return time;
        } catch (final ParseException e) {
            return null;
        }
    }

    public static Date getTime(final Player player, final String regexCommand) {
        final int pre2 = regexCommand.toLowerCase().hashCode();
        String confTime = "";
        confTime = BoosConfigManager.getConfusers().getString(
                "users." + player.getUniqueId() + ".cooldown." + pre2, null);

        if (confTime != null && !confTime.equals("")) {
            final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            Date lastDate = null;

            try {
                lastDate = sdf.parse(confTime);
                return lastDate;
            } catch (final ParseException e) {
                return null;
            }
        }
        return null;
    }

    public static boolean checkCoolDownOK(
            final Player player, String regexCommand,
            final String originalCommand, final int time) {
        regexCommand = regexCommand.toLowerCase();
        if (time > 0) {
            final Date lastTime = getTime(player, regexCommand);
            if (lastTime == null) {
                return true;
            } else {
                final long secondsBetween = getSecondsBetween(lastTime);
                if (secondsBetween > time) {
                    return true;
                } else {
                    String msg = BoosConfigManager.getCoolDownMessage();
                    msg = getFormatedCooldownMessage(originalCommand, time, secondsBetween, msg);
                    BoosChat.sendMessageToPlayer(player, msg);
                    return false;
                }
            }
        }
        return true;
    }

    public static String getFormatedCooldownMessage(final String originalCommand, final int time, final long secondsBetween, String msg) {
        long waitSeconds = time - secondsBetween;
        long waitMinutes = (long) Math.floor(waitSeconds / 60.0);
        final long waitHours = (long) Math.floor(waitMinutes / 60.0);
        final StringBuilder stringBuilder = new StringBuilder();
        msg = msg.replaceAll("&command&", originalCommand);
        if (waitSeconds >= 3600) {
            stringBuilder.append(waitHours);
            stringBuilder.append(" ");
            stringBuilder.append(BoosConfigManager.getUnitHoursMessage());
            stringBuilder.append(", ");
            waitSeconds = waitSeconds - (waitHours * 3600);
        }
        if (waitSeconds >= 60) {
            waitMinutes = waitMinutes - (waitHours * 60);
            stringBuilder.append(waitMinutes);
            stringBuilder.append(" ");
            stringBuilder.append(BoosConfigManager.getUnitMinutesMessage());
            stringBuilder.append(", ");
            waitSeconds = waitSeconds - (waitMinutes * 60);
        }
        String secs = Long.toString(waitSeconds);
        if (secs.equals("0")) {
            secs = "1";
        }
        stringBuilder.append(secs);
        stringBuilder.append(" ");
        stringBuilder.append(BoosConfigManager.getUnitSecondsMessage());

        msg = msg.replaceAll("&seconds&", stringBuilder.toString());
        msg = msg.replaceAll("&unit&", "");
        msg = msg.replaceAll(" +", " ");
        return msg;
    }

    public static long getSecondsBetween(final Date lastTime) {
        final Calendar calcurrTime = Calendar.getInstance();
        calcurrTime.setTime(getCurrTime());
        final Calendar callastTime = Calendar.getInstance();
        callastTime.setTime(lastTime);
        return secondsBetween(callastTime, calcurrTime);
    }

    private static long secondsBetween(final Calendar startDate, final Calendar endDate) {
        long secondsBetween = 0;
        secondsBetween = (endDate.getTimeInMillis() - startDate
                .getTimeInMillis()) / 1000;
        return secondsBetween;
    }

    private static void setTime(final Player player, final String regexCommand) {
        final int pre2 = regexCommand.toLowerCase().hashCode();
        String currTime = "";
        final Calendar cal = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        currTime = sdf.format(cal.getTime());
        BoosConfigManager.getConfusers()
                .set("users." + player.getUniqueId() + ".cooldown." + pre2,
                        currTime);
    }

    public static void startAllCooldowns(final Player player, final String message) {
        for (final String a : BoosConfigManager.getCooldowns(player)) {
            final int cooldownTime = BoosConfigManager.getCoolDown(a, player);
            coolDown(player, a, message, cooldownTime);
        }

    }

}
