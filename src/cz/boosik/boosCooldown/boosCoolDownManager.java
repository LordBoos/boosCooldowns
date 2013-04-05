package cz.boosik.boosCooldown;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bukkit.entity.Player;

import util.boosChat;

/**
 * @author Jakub
 *
 */
public class boosCoolDownManager {
	static void cancelCooldown(Player player, String regexCommand) {
		int pre2 = regexCommand.toLowerCase().hashCode();
		boosConfigManager.getConfusers().set(
				"users." + player.getName().toLowerCase().hashCode()
						+ ".cooldown." + pre2, null);
	}

	/**
	 * @param player
	 * @param regexCommand
	 * @param originalCommand
	 * @param coolDownSeconds
	 * @return
	 */
	static boolean cd(Player player, String regexCommand,
			String originalCommand, int coolDownSeconds) {
		Date lastTime = getTime(player, regexCommand);
		String link = boosConfigManager.getLink(regexCommand);
		if (lastTime == null) {
			if (link == null) {
				setTime(player, regexCommand);
			} else {
				List<String> linkGroup = boosConfigManager.getLinkList(link);
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
			long waitMinutes = Math.round(waitSeconds / 60) + 1;
			long waitHours = Math.round(waitMinutes / 60) + 1;
			if (secondsBetween > coolDownSeconds) {
				if (link == null) {
					setTime(player, regexCommand);
				} else {
					List<String> linkGroup = boosConfigManager
							.getLinkList(link);
					for (String a : linkGroup) {
						setTime(player, a);
					}
				}
				return false;
			} else {
				String msg = boosConfigManager.getCoolDownMessage();
				msg = msg.replaceAll("&command&", originalCommand);
				if (waitSeconds >= 60 && 3600 >= waitSeconds) {
					msg = msg.replaceAll("&seconds&",
							Long.toString(waitMinutes));
					msg = msg.replaceAll("&unit&",
							boosConfigManager.getUnitMinutesMessage());
				} else if (waitMinutes >= 60) {
					msg = msg.replaceAll("&seconds&", Long.toString(waitHours));
					msg = msg.replaceAll("&unit&",
							boosConfigManager.getUnitHoursMessage());
				} else {
					String secs = Long.toString(waitSeconds);
					if (secs.equals("0")) {
						secs = "1";
					}
					msg = msg.replaceAll("&seconds&", secs);
					msg = msg.replaceAll("&unit&",
							boosConfigManager.getUnitSecondsMessage());
				}
				boosChat.sendMessageToPlayer(player, msg);
				return true;
			}
		}
	}

	/**
	 * @param player
	 * @param regexCommand
	 * @param originalCommand
	 * @param time
	 * @return
	 */
	static boolean coolDown(Player player, String regexCommand,
			String originalCommand, int time) {
		regexCommand = regexCommand.toLowerCase();
		if (time > 0
				&& !player.hasPermission("booscooldowns.nocooldown")
				&& !player.hasPermission("booscooldowns.nocooldown."
						+ originalCommand)) {
			return cd(player, regexCommand, originalCommand, time);
		}
		return false;
	}

	/**
	 * @return
	 */
	static Date getCurrTime() {
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

	/**
	 * @param player
	 * @param regexCommand
	 * @return
	 */
	static Date getTime(Player player, String regexCommand) {
		int pre2 = regexCommand.toLowerCase().hashCode();
		String confTime = "";
		confTime = boosConfigManager.getConfusers().getString(
				"users." + player.getName().toLowerCase().hashCode()
						+ ".cooldown." + pre2, null);

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

	/**
	 * @param player
	 * @param regexCommand
	 * @param originalCommand
	 * @param time
	 * @return
	 */
	static boolean checkCoolDownOK(Player player, String regexCommand,
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
				long waitMinutes = Math.round(waitSeconds / 60) + 1;
				long waitHours = Math.round(waitMinutes / 60) + 1;
				if (secondsBetween > time) {
					return true;
				} else {
					String msg = boosConfigManager.getCoolDownMessage();
					msg = msg.replaceAll("&command&", originalCommand);
					if (waitSeconds >= 60 && 3600 >= waitSeconds) {
						msg = msg.replaceAll("&seconds&",
								Long.toString(waitMinutes));
						msg = msg.replaceAll("&unit&",
								boosConfigManager.getUnitMinutesMessage());
					} else if (waitMinutes >= 60) {
						msg = msg.replaceAll("&seconds&",
								Long.toString(waitHours));
						msg = msg.replaceAll("&unit&",
								boosConfigManager.getUnitHoursMessage());
					} else {
						msg = msg.replaceAll("&seconds&",
								Long.toString(waitSeconds));
						msg = msg.replaceAll("&unit&",
								boosConfigManager.getUnitSecondsMessage());
					}
					boosChat.sendMessageToPlayer(player, msg);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	static long secondsBetween(Calendar startDate, Calendar endDate) {
		long secondsBetween = 0;

		while (startDate.before(endDate)) {
			startDate.add(Calendar.SECOND, 1);
			secondsBetween++;
		}
		return secondsBetween;
	}

	/**
	 * @param player
	 * @param regexCommand
	 */
	static void setTime(Player player, String regexCommand) {
		int pre2 = regexCommand.toLowerCase().hashCode();
		String currTime = "";
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		currTime = sdf.format(cal.getTime());
		boosConfigManager.getConfusers().set(
				"users." + player.getName().toLowerCase().hashCode()
						+ ".cooldown." + pre2, currTime);
	}

	/**
	 * @param player
	 * @param message
	 */
	public static void startAllCooldowns(Player player, String message) {
		for (String a : boosConfigManager.getCooldowns(player)) {
			int cooldownTime = boosConfigManager.getCoolDown(a, player);
			coolDown(player, a, message, cooldownTime);
		}

	}

}
