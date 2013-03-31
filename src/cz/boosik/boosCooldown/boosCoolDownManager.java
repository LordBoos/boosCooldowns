package cz.boosik.boosCooldown;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import util.boosChat;

public class boosCoolDownManager {

	private static YamlConfiguration confusers;
	private static File confFile;

	static void cancelCooldown(Player player, String pre) {
		int pre2 = pre.toLowerCase().hashCode();
		confusers.set("users." + player.getName().toLowerCase().hashCode()
				+ ".cooldown." + pre2, null);
	}

	private static boolean cd(Player player, String pre, int coolDownSeconds) {
		Date lastTime = getTime(player, pre);
		String link = boosConfigManager.getLink(pre);
		if (lastTime == null) {
			if (link == null) {
				setTime(player, pre);
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
					setTime(player, pre);
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
				msg = msg.replaceAll("&command&", pre);
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

	static void clear() {
		ConfigurationSection userSection = confusers
				.getConfigurationSection("users");
		if (userSection == null)
			return;
		for (String user : userSection.getKeys(false)) {
			// clear cooldown
			ConfigurationSection cooldown = confusers
					.getConfigurationSection("users." + user + ".cooldown");
			if (cooldown != null) {
				for (String key : cooldown.getKeys(false)) {
					confusers.set("users." + user + ".cooldown." + key, null);
				}
			}
			confusers.set("users." + user + ".cooldown", null);

			// clear warmup
			ConfigurationSection warmup = confusers
					.getConfigurationSection("users." + user + ".warmup");
			if (warmup != null) {
				for (String key : warmup.getKeys(false)) {
					confusers.set("users." + user + ".warmup." + key, null);
				}
			}
			confusers.set("users." + user + ".warmup", null);

			confusers.set("users." + user, null);
		}
		save();
		load();
	}

	public static void clearSomething(String co, String player) {
		ConfigurationSection userSection = confusers
				.getConfigurationSection("users."
						+ player.toLowerCase().hashCode() + "." + co);
		if (userSection == null)
			return;
		confusers.set("users." + player.toLowerCase().hashCode() + "." + co,
				null);
		save();
		load();
	}

	static void clearSomething(String co, String player, String command) {
		int pre2 = command.toLowerCase().hashCode();
		confusers.set("users." + player.toLowerCase().hashCode() + "." + co
				+ "." + pre2, 0);
		save();
		load();
	}

	static boolean coolDown(Player player, String pre) {
		pre = pre.toLowerCase();
		int coolDownSeconds = 0;
		coolDownSeconds = getCooldownTime(player, pre);
		if (coolDownSeconds > 0
				&& !player.hasPermission("booscooldowns.nocooldown")
				&& !player.hasPermission("booscooldowns.nocooldown." + pre)) {
			return cd(player, pre, coolDownSeconds);
		}
		return false;
	}

	private static int getCooldownTime(Player player, String pre) {
		int coolDownSeconds = boosConfigManager.getCoolDown(pre, player);
		return coolDownSeconds;
	}

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

	static void getLimits(Player player) {
		int usesNum = 0;
		int limitNum = 0;
		int num;
		String message;
		Set<String> uses = boosConfigManager.getLimits(player);
		if (uses != null) {
			for (String key : uses) {
				usesNum = confusers.getInt("users."
						+ player.getName().toLowerCase().hashCode() + ".uses."
						+ key, usesNum);
					limitNum = boosConfigManager.getLimit(key, player);
				num = limitNum - usesNum;
				if (num < 0) {
					num = 0;
				}
				message = boosConfigManager.getLimitListMessage();
				message = message.replaceAll("&command&", key);
				message = message.replaceAll("&limit&",
						String.valueOf(limitNum));
				message = message.replaceAll("&times&", String.valueOf(num));
				boosChat.sendMessageToPlayer(player, message);
			}
		}
	}

	static Date getTime(Player player, String pre) {
		int pre2 = pre.toLowerCase().hashCode();
		String confTime = "";
		confTime = confusers.getString("users."
				+ player.getName().toLowerCase().hashCode() + ".cooldown."
				+ pre2, null);

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

	static int getUses(Player player, String pre, String message) {
		int pre2 = pre.toLowerCase().hashCode();
		int message2 = message.toLowerCase().hashCode();
		int uses = 0;
		uses = confusers.getInt("users."
				+ player.getName().toLowerCase().hashCode() + ".uses." + pre2
				+ message2, uses);
		return uses;
	}

	static boolean checkCoolDownOK(Player player, String pre, String message) {
		pre = pre.toLowerCase();
		int coolDownSeconds = 0;
		coolDownSeconds = getCooldownTime(player, pre);
		if (coolDownSeconds > 0) {
			Date lastTime = getTime(player, pre);
			if (lastTime == null) {
				return true;
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
					return true;
				} else {
					String msg = boosConfigManager.getCoolDownMessage();
					msg = msg.replaceAll("&command&", pre);
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

	static boolean checkWarmUpOK(Player player, String pre, String message) {
		int pre2 = pre.toLowerCase().hashCode();
		int ok = 0;
		ok = confusers.getInt(
				"users." + player.getName().toLowerCase().hashCode()
						+ ".warmup." + pre2, ok);
		if (ok == 1) {
			return true;
		}
		return false;
	}

	static void load() {
		try {
			confusers.load(confFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	static void removeWarmUp(Player player, String pre, String message) {
		int pre2 = pre.toLowerCase().hashCode();
		confusers.set("users." + player.getName().toLowerCase().hashCode()
				+ ".warmup." + pre2, null);
	}

	static void removeWarmUpOK(Player player, String pre, String message) {
		int pre2 = pre.toLowerCase().hashCode();
		confusers.set("users." + player.getName().toLowerCase().hashCode()
				+ ".warmup." + pre2, null);
	}

	static void save() {
		try {
			confFile.createNewFile();
			confusers.save(confFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static long secondsBetween(Calendar startDate, Calendar endDate) {
		long secondsBetween = 0;

		while (startDate.before(endDate)) {
			startDate.add(Calendar.SECOND, 1);
			secondsBetween++;
		}
		return secondsBetween;
	}

	static void setTime(Player player, String pre) {
		int pre2 = pre.toLowerCase().hashCode();
		String currTime = "";
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		currTime = sdf.format(cal.getTime());
		confusers.set("users." + player.getName().toLowerCase().hashCode()
				+ ".cooldown." + pre2, currTime);
	}

	static void setUses(Player player, String pre, String message) {
		if (boosConfigManager.getLimitsEnabled()) {
			if (boosConfigManager.getLimits(player).contains(pre)) {
				int pre2 = pre.toLowerCase().hashCode();
				int message2 = message.toLowerCase().hashCode();
				int uses = getUses(player, pre, message);
				uses = uses + 1;
				try {
					confusers.set("users."
							+ player.getName().toLowerCase().hashCode()
							+ ".uses." + pre2 + message2, uses);
				} catch (IllegalArgumentException e) {
					boosCoolDown.log.warning("Player " + player.getName()
							+ " used empty command and caused this error!");
				}
			} else {
				return;
			}
		}
	}

	static void setWarmUpOK(Player player, String pre, String message) {
		int pre2 = pre.toLowerCase().hashCode();
		confusers.set("users." + player.getName().toLowerCase().hashCode()
				+ ".warmup." + pre2, 1);
	}

	public static void startAllCooldowns(Player player) {
		for (String a : boosConfigManager.getCooldowns(player)) {
			coolDown(player, a);
		}

	}

	public boosCoolDownManager(boosCoolDown bCoolDown) {
		confFile = new File(bCoolDown.getDataFolder(), "users.yml");

		confusers = new YamlConfiguration();

		if (confFile.exists()) {
			try {
				confusers.load(confFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		} else {
			try {
				confFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
