package cz.boosik.boosCooldown.Managers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.bukkit.entity.Player;

import cz.boosik.boosCooldown.BoosCoolDown;
import util.BoosChat;

public class BoosLimitManager {

    public static boolean blocked(
            final Player player, final String regexCommand,
            final String originalCommand, final int limit) {
        Date time = getTime(player, regexCommand);
        final Date confTime = getTime(regexCommand);
        final Calendar calcurrTime = Calendar.getInstance();
        calcurrTime.setTime(getCurrTime());
        final Calendar callastTime = Calendar.getInstance();
        final Calendar callastTimeGlobal = Calendar.getInstance();
        int uses = getUses(player, regexCommand);
        final long limitResetDelay = BoosConfigManager.getLimitResetDelay(
                regexCommand, player);
        final long limitResetDelayGlobal = BoosConfigManager
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
                    final long waitSeconds = secondsBetween(callastTime,
                            calcurrTime, limitResetDelay);
                    final long waitMinutes = Math.round(waitSeconds / 60) + 1;
                    final long waitHours = Math.round(waitMinutes / 60) + 1;
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
                    BoosChat.sendMessageToPlayer(player, msg);
                } else if (limitResetDelayGlobal > 0) {
                    if (confTime != null) {
                        callastTimeGlobal.setTime(confTime);
                        final long waitSeconds = secondsBetween(callastTimeGlobal,
                                calcurrTime, limitResetDelayGlobal);
                        final long waitMinutes = (long) Math.ceil(waitSeconds / 60.0);
                        final long waitHours = (long) Math.ceil(waitMinutes / 60.0);
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
                        BoosChat.sendMessageToPlayer(player, msg);
                    }
                } else {
                    final String msg = String.format(BoosConfigManager
                            .getCommandBlockedMessage());
                    BoosChat.sendMessageToPlayer(player, msg);
                }
                return true;
            }
        }
        return false;
    }

    public static int getUses(final Player player, final String regexCommand) {
        final int regexCommand2 = regexCommand.toLowerCase().hashCode();
        int uses = 0;
        uses = BoosConfigManager.getConfusers().getInt(
                "users." + player.getUniqueId() + ".uses." + regexCommand2,
                uses);
        return uses;
    }

    public static void setUses(final Player player, final String regexCommand) {
        if (BoosConfigManager.getLimitsEnabled()) {
            if (BoosConfigManager.getCommands(player).contains(regexCommand)) {
                final int regexCommand2 = regexCommand.toLowerCase().hashCode();
                int uses = getUses(player, regexCommand);
                uses = uses + 1;
                try {
                    BoosConfigManager.getConfusers().set(
                            "users." + player.getUniqueId() + ".uses."
                                    + regexCommand2, uses);
                } catch (final IllegalArgumentException e) {
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

    public static void getLimitListMessages(final Player send, final String comm, final int lim) {
        if (lim != -1) {
            final int uses = getUses(send, comm);
            String message = BoosConfigManager.getLimitListMessage();
            int num = lim - uses;
            if (num < 0) {
                num = 0;
            }
            message = BoosConfigManager.getLimitListMessage();
            message = message.replaceAll("&command&", comm);
            message = message.replaceAll("&limit&", String.valueOf(lim));
            message = message.replaceAll("&times&", String.valueOf(num));
            BoosChat.sendMessageToPlayer(send, message);
        }
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

    private static Date getTime(final Player player, final String regexCommand) {
        final int pre2 = regexCommand.toLowerCase().hashCode();
        String confTime = "";
        confTime = BoosConfigManager.getConfusers().getString(
                "users." + player.getUniqueId() + ".lastused." + pre2, null);

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

    private static Date getTime(final String regexCommand) {
        String confTime = "";
        confTime = BoosConfigManager.getConfusers().getString(
                "global." + regexCommand + ".reset", null);

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

    private static void setTime(final Player player, final String regexCommand) {
        final int pre2 = regexCommand.toLowerCase().hashCode();
        String currTime = "";
        final Calendar cal = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        currTime = sdf.format(cal.getTime());
        BoosConfigManager.getConfusers()
                .set("users." + player.getUniqueId() + ".lastused." + pre2,
                        currTime);
    }

    private static long secondsBetween(
            final Calendar startDate, final Calendar endDate,
            final long limitResetDelay) {
        long secondsBetween = 0;
        secondsBetween = ((startDate.getTimeInMillis() - endDate
                .getTimeInMillis()) / 1000) + limitResetDelay;
        return secondsBetween;
    }

    public static void clearAllLimits(final int hashedCommand) {
        final Set<String> players = BoosConfigManager.getAllPlayers();
        for (final String player : players) {
            BoosConfigManager.clearSomething2("uses", player, hashedCommand);
        }
        BoosConfigManager.saveConfusers();
        BoosConfigManager.loadConfusers();
    }

    public static void setGlobalLimitResetDate() {
        for (final String command : BoosConfigManager.getLimitResetCommandsGlobal()) {
            if (BoosConfigManager.getLimitResetDelayGlobal(command) == -65535) {
                BoosConfigManager.getConfusers().set("global." + command, null);
            } else {
                setTime(command);
            }
        }
        BoosConfigManager.saveConfusers();
        BoosConfigManager.loadConfusers();
    }

    public static void setGlobalLimitResetDate(final String command) {
        setTime(command);
        BoosConfigManager.saveConfusers();
        BoosConfigManager.loadConfusers();
    }

    private static void setTime(final String command) {
        String currTime = "";
        final Calendar cal = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        currTime = sdf.format(cal.getTime());
        BoosConfigManager.getConfusers().set("global." + command + ".reset",
                currTime);
    }
}
