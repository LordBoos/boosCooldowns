package cz.boosik.boosCooldown.Managers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bukkit.entity.Player;

import util.boosChat;

public class BoosCoolDownManager {

    static void cancelCooldown(Player player, String regexCommand) {
        int pre2 = regexCommand.toLowerCase().hashCode();
        BoosConfigManager.getConfusers().set(
                "users." + player.getUniqueId() + ".cooldown." + pre2, null);
    }

    public static boolean isCoolingdown(Player player, String regexCommand, int time) {
        Date lastTime = getTime(player, regexCommand);
        if (lastTime == null) {
            return false;
        }
        Calendar calcurrTime = Calendar.getInstance();
        calcurrTime.setTime(getCurrTime());
        Calendar callastTime = Calendar.getInstance();
        callastTime.setTime(lastTime);
        long secondsBetween = secondsBetween(callastTime, calcurrTime);
        if ((secondsBetween > time) || secondsBetween == 0) {
            return false;
        }
        return true;
    }

    private static boolean cd(Player player, String regexCommand,
                              String originalCommand, int coolDownSeconds) {
        Date lastTime = getTime(player, regexCommand);
        List<String> linkGroup = BoosConfigManager.getSharedCooldowns(
                regexCommand, player);
        if (lastTime == null) {
            if (linkGroup.isEmpty()) {
                setTime(player, regexCommand);
            } else {
                setTime(player, regexCommand);
                for (String a : linkGroup) {
                    setTime(player, a);
                }
            }
            return false;
        } else {
            Calendar calcurrTime = Calendar.getInstance();
            calcurrTime.setTime(getCurrTime());
            Calendar callastTime = Calendar.getInstance();
            callastTime.setTime(lastTime);
            long secondsBetween = secondsBetween(callastTime, calcurrTime);
            long waitSeconds = coolDownSeconds - secondsBetween;
            long waitMinutes = (long) Math.floor(waitSeconds / 60.0);
            long waitHours = (long) Math.floor(waitMinutes / 60.0);
            if (secondsBetween > coolDownSeconds) {
                if (linkGroup.isEmpty()) {
                    setTime(player, regexCommand);
                } else {
                    setTime(player, regexCommand);
                    for (String a : linkGroup) {
                        setTime(player, a);
                    }
                }
                return false;
            } else {
                String msg = BoosConfigManager.getCoolDownMessage();
                StringBuilder stringBuilder = new StringBuilder();
                msg = msg.replaceAll("&command&", originalCommand);
                if (waitSeconds >= 3600) {
                    stringBuilder.append(Long.toString(waitHours));
                    stringBuilder.append(" ");
                    stringBuilder.append(BoosConfigManager.getUnitHoursMessage());
                    stringBuilder.append(", ");
                    waitSeconds = waitSeconds - (waitHours * 3600);
                }
                if (waitSeconds >= 60) {
                    waitMinutes = waitMinutes - (waitHours * 60);
                    stringBuilder.append(Long.toString(waitMinutes));
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

                boosChat.sendMessageToPlayer(player, msg);
                return true;
            }
        }
    }

    public static boolean coolDown(Player player, String regexCommand,
                                   String originalCommand, int time) {
        regexCommand = regexCommand.toLowerCase();
        return time > 0 && !player.hasPermission("booscooldowns.nocooldown") && !player.hasPermission("booscooldowns.nocooldown." + originalCommand) && cd(
                player,
                regexCommand,
                originalCommand,
                time);
    }

    private static Date getCurrTime() {
        String currTime = "";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        currTime = sdf.format(cal.getTime());
        Date time = null;

        try {
            time = sdf.parse(currTime);
            return time;
        } catch (ParseException e) {
            return null;
        }
    }

    private static Date getTime(Player player, String regexCommand) {
        int pre2 = regexCommand.toLowerCase().hashCode();
        String confTime = "";
        confTime = BoosConfigManager.getConfusers().getString(
                "users." + player.getUniqueId() + ".cooldown." + pre2, null);

        if (confTime != null && !confTime.equals("")) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            Date lastDate = null;

            try {
                lastDate = sdf.parse(confTime);
                return lastDate;
            } catch (ParseException e) {
                return null;
            }
        }
        return null;
    }

    public static boolean checkCoolDownOK(Player player, String regexCommand,
                                          String originalCommand, int time) {
        regexCommand = regexCommand.toLowerCase();
        if (time > 0) {
            Date lastTime = getTime(player, regexCommand);
            if (lastTime == null) {
                return true;
            } else {
                Calendar calcurrTime = Calendar.getInstance();
                calcurrTime.setTime(getCurrTime());
                Calendar callastTime = Calendar.getInstance();
                callastTime.setTime(lastTime);
                long secondsBetween = secondsBetween(callastTime, calcurrTime);
                long waitSeconds = time - secondsBetween;
                long waitMinutes = (long) Math.floor(waitSeconds / 60.0);
                long waitHours = (long) Math.floor(waitMinutes / 60.0);
                if (secondsBetween > time) {
                    return true;
                } else {
                    String msg = BoosConfigManager.getCoolDownMessage();
                    StringBuilder stringBuilder = new StringBuilder();
                    msg = msg.replaceAll("&command&", originalCommand);
                    if (waitSeconds >= 3600) {
                        stringBuilder.append(Long.toString(waitHours));
                        stringBuilder.append(" ");
                        stringBuilder.append(BoosConfigManager.getUnitHoursMessage());
                        stringBuilder.append(", ");
                        waitSeconds = waitSeconds - (waitHours * 3600);
                    }
                    if (waitSeconds >= 60) {
                        waitMinutes = waitMinutes - (waitHours * 60);
                        stringBuilder.append(Long.toString(waitMinutes));
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

                    boosChat.sendMessageToPlayer(player, msg);
                    return false;
                }
            }
        }
        return true;
    }

    private static long secondsBetween(Calendar startDate, Calendar endDate) {
        long secondsBetween = 0;
        secondsBetween = (endDate.getTimeInMillis() - startDate
                .getTimeInMillis()) / 1000;
        return secondsBetween;
    }

    private static void setTime(Player player, String regexCommand) {
        int pre2 = regexCommand.toLowerCase().hashCode();
        String currTime = "";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        currTime = sdf.format(cal.getTime());
        BoosConfigManager.getConfusers()
                .set("users." + player.getUniqueId() + ".cooldown." + pre2,
                        currTime);
    }

    public static void startAllCooldowns(Player player, String message) {
        for (String a : BoosConfigManager.getCooldowns(player)) {
            int cooldownTime = BoosConfigManager.getCoolDown(a, player);
            coolDown(player, a, message, cooldownTime);
        }

    }

}
