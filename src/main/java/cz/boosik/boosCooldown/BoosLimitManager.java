package cz.boosik.boosCooldown;

import org.bukkit.entity.Player;
import util.boosChat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

class BoosLimitManager {

    static boolean blocked(Player player, String regexCommand,
                           String originalCommand, int limit) {
        Date time = getTime(player, regexCommand);
        Date confTime = getTime(regexCommand);
        Calendar calcurrTime = Calendar.getInstance();
        calcurrTime.setTime(getCurrTime());
        Calendar callastTime = Calendar.getInstance();
        Calendar callastTimeGlobal = Calendar.getInstance();
        int uses = getUses(player, regexCommand);
        long limitResetDelay = BoosConfigManager.getLimitResetDelay(
                regexCommand, player);
        long limitResetDelayGlobal = BoosConfigManager
                .getLimitResetDelayGlobal(regexCommand);
        if (time != null) {
            callastTime.setTime(time);
        } else {
            setTime(player, regexCommand);
        }
        if (limit - uses == 1) {
            setTime(player, regexCommand);
            time = getTime(player, regexCommand);
            callastTime.setTime(time);
        }
        if (limitResetDelay > 0) {
            if (secondsBetween(callastTime, calcurrTime, limitResetDelay) <= 0) {
                if (uses != 0) {
                    BoosConfigManager.clearSomething("uses",
                            player.getUniqueId(), regexCommand);
                    uses = getUses(player, regexCommand);
                }
            }
        }

        if (!player.hasPermission("booscooldowns.nolimit")
                || player.hasPermission("booscooldowns.nolimit."
                + originalCommand)) {
            if (limit == -1) {
                return false;
            } else if (limit <= uses) {
                if (limitResetDelay > 0) {
                    long waitSeconds = secondsBetween(callastTime,
                            calcurrTime, limitResetDelay);
                    long waitMinutes = Math.round(waitSeconds / 60) + 1;
                    long waitHours = Math.round(waitMinutes / 60) + 1;
                    String msg = BoosConfigManager.getLimitResetMessage();
                    msg = msg.replaceAll("&command&", originalCommand);
                    if (waitSeconds >= 60 && 3600 >= waitSeconds) {
                        msg = msg.replaceAll("&seconds&",
                                Long.toString(waitMinutes));
                        msg = msg.replaceAll("&unit&",
                                BoosConfigManager.getUnitMinutesMessage());
                    } else if (waitMinutes >= 60) {
                        msg = msg.replaceAll("&seconds&",
                                Long.toString(waitHours));
                        msg = msg.replaceAll("&unit&",
                                BoosConfigManager.getUnitHoursMessage());
                    } else {
                        msg = msg.replaceAll("&seconds&",
                                Long.toString(waitSeconds));
                        msg = msg.replaceAll("&unit&",
                                BoosConfigManager.getUnitSecondsMessage());
                    }
                    boosChat.sendMessageToPlayer(player, msg);
                } else if (limitResetDelayGlobal > 0) {
                    if (confTime != null) {
                        callastTimeGlobal.setTime(confTime);
                        long waitSeconds = secondsBetween(callastTimeGlobal,
                                calcurrTime, limitResetDelayGlobal);
                        long waitMinutes = (long) Math.ceil(waitSeconds / 60.0);
                        long waitHours = (long) Math.ceil(waitMinutes / 60.0);
                        String msg = BoosConfigManager.getLimitResetMessage();
                        msg = msg.replaceAll("&command&", originalCommand);
                        if (waitSeconds >= 60 && 3600 >= waitSeconds) {
                            msg = msg.replaceAll("&seconds&",
                                    Long.toString(waitMinutes));
                            msg = msg.replaceAll("&unit&",
                                    BoosConfigManager.getUnitMinutesMessage());
                        } else if (waitMinutes >= 60) {
                            msg = msg.replaceAll("&seconds&",
                                    Long.toString(waitHours));
                            msg = msg.replaceAll("&unit&",
                                    BoosConfigManager.getUnitHoursMessage());
                        } else {
                            msg = msg.replaceAll("&seconds&",
                                    Long.toString(waitSeconds));
                            msg = msg.replaceAll("&unit&",
                                    BoosConfigManager.getUnitSecondsMessage());
                        }
                        boosChat.sendMessageToPlayer(player, msg);
                    }
                } else {
                    String msg = String.format(BoosConfigManager
                            .getCommandBlockedMessage());
                    boosChat.sendMessageToPlayer(player, msg);
                }
                return true;
            }
        }
        return false;
    }

    private static int getUses(Player player, String regexCommand) {
        int regexCommand2 = regexCommand.toLowerCase().hashCode();
        int uses = 0;
        uses = BoosConfigManager.getConfusers().getInt(
                "users." + player.getUniqueId() + ".uses." + regexCommand2,
                uses);
        return uses;
    }

    static void setUses(Player player, String regexCommand) {
        if (BoosConfigManager.getLimitsEnabled()) {
            if (BoosConfigManager.getCommands(player).contains(regexCommand)) {
                int regexCommand2 = regexCommand.toLowerCase().hashCode();
                int uses = getUses(player, regexCommand);
                uses = uses + 1;
                try {
                    BoosConfigManager.getConfusers().set(
                            "users." + player.getUniqueId() + ".uses."
                                    + regexCommand2, uses);
                } catch (IllegalArgumentException e) {
                    BoosCoolDown
                            .getLog()
                            .warning(
                                    "Player "
                                            + player.getName()
                                            + " used empty command and caused this error!");
                }
            }
        }
    }

    static void getLimitListMessages(Player send, String comm, int lim) {
        if (lim != -1) {
            int uses = getUses(send, comm);
            String message = BoosConfigManager.getLimitListMessage();
            int num = lim - uses;
            if (num < 0) {
                num = 0;
            }
            message = BoosConfigManager.getLimitListMessage();
            message = message.replaceAll("&command&", comm);
            message = message.replaceAll("&limit&", String.valueOf(lim));
            message = message.replaceAll("&times&", String.valueOf(num));
            boosChat.sendMessageToPlayer(send, message);
        }
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
                "users." + player.getUniqueId() + ".lastused." + pre2, null);

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

    private static Date getTime(String regexCommand) {
        String confTime = "";
        confTime = BoosConfigManager.getConfusers().getString(
                "global." + regexCommand + ".reset", null);

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

    private static void setTime(Player player, String regexCommand) {
        int pre2 = regexCommand.toLowerCase().hashCode();
        String currTime = "";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        currTime = sdf.format(cal.getTime());
        BoosConfigManager.getConfusers()
                .set("users." + player.getUniqueId() + ".lastused." + pre2,
                        currTime);
    }

    private static long secondsBetween(Calendar startDate, Calendar endDate,
                                       long limitResetDelay) {
        long secondsBetween = 0;
        secondsBetween = ((startDate.getTimeInMillis() - endDate
                .getTimeInMillis()) / 1000) + limitResetDelay;
        return secondsBetween;
    }

    static void clearAllLimits(int hashedCommand) {
        Set<String> players = BoosConfigManager.getAllPlayers();
        for (String player : players) {
            BoosConfigManager.clearSomething2("uses", player, hashedCommand);
        }
        BoosConfigManager.saveConfusers();
        BoosConfigManager.loadConfusers();
    }

    static void setGlobalLimitResetDate() {
        for (String command : BoosConfigManager.getLimitResetCommandsGlobal()) {
            if (BoosConfigManager.getLimitResetDelayGlobal(command) == -65535) {
                BoosConfigManager.getConfusers().set("global." + command, null);
            } else {
                setTime(command);
            }
        }
        BoosConfigManager.saveConfusers();
        BoosConfigManager.loadConfusers();
    }

    static void setGlobalLimitResetDate(String command) {
        setTime(command);
        BoosConfigManager.saveConfusers();
        BoosConfigManager.loadConfusers();
    }

    private static void setTime(String command) {
        String currTime = "";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        currTime = sdf.format(cal.getTime());
        BoosConfigManager.getConfusers().set("global." + command + ".reset",
                currTime);
    }
}
