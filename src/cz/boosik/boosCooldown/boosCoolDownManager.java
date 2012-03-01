package cz.boosik.boosCooldown;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import util.boosChat;

public class boosCoolDownManager {

	private static YamlConfiguration confusers;
	private static File confFile;

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

	static void save() {
		try {
			confFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void clear() {
		ConfigurationSection userSection = confusers.getConfigurationSection("users");
		if (userSection == null)
			return;
		for (String user : userSection.getKeys(false)) {
			// clear cooldown
			ConfigurationSection cooldown = confusers.getConfigurationSection("users."+user+".cooldown");
			if (cooldown != null) {
				for (String key : cooldown.getKeys(false)) {
					confusers.set("users."+user+".cooldown."+key, null);
				}
			}
			confusers.set("users."+user+".cooldown", null);
			
			// clear warmup
			ConfigurationSection warmup = confusers.getConfigurationSection("users."+user+".warmup");
			if (warmup != null) {
				for (String key : warmup.getKeys(false)) {
					confusers.set("users."+user+".warmup."+key, null);
				}
			}
			confusers.set("users." + user + ".warmup", null);

			confusers.set("users." + user, null);
		}
		save();
		load();
	}

	static boolean coolDown(Player player, String pre, String message) {
		pre = pre.toLowerCase();
		int coolDownSeconds = 0;
		if (boosCoolDown.isUsingPermissions()) {
			if (!boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown2")
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.cooldown3")
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.cooldown4")
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.cooldown5")) {
				coolDownSeconds = boosConfigManager.getCoolDown(player, pre);
			}
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown2")) {
				coolDownSeconds = boosConfigManager.getCoolDown2(player, pre);
			}
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown3")) {
				coolDownSeconds = boosConfigManager.getCoolDown3(player, pre);
			}
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown4")) {
				coolDownSeconds = boosConfigManager.getCoolDown4(player, pre);
			}
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown5")) {
				coolDownSeconds = boosConfigManager.getCoolDown5(player, pre);
			}
		} else {
			coolDownSeconds = boosConfigManager.getCoolDown(player, pre);
		}
		if (boosCoolDown.isUsingPermissions()) {
			if (coolDownSeconds > 0
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.nocooldown") && !boosCoolDown.getPermissions().has(player,
									"booscooldowns.nocooldown."+pre)) {
				Date lastTime = getTime(player, pre);
				if (lastTime == null) {
					setTime(player, pre);
					return false;
				} else {
					Calendar calcurrTime = Calendar.getInstance();
					calcurrTime.setTime(getCurrTime());
					Calendar callastTime = Calendar.getInstance();
					callastTime.setTime(lastTime);
					long secondsBetween = secondsBetween(callastTime,
							calcurrTime);
					long waitSeconds = coolDownSeconds - secondsBetween;
					long waitMinutes = Math.round(waitSeconds / 60) + 1;
					long waitHours = Math.round(waitMinutes / 60) + 1;
					if (secondsBetween > coolDownSeconds) {
						setTime(player, pre);
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
						return true;
					}
				}
			}
		} else {
			if (coolDownSeconds > 0) {
				Date lastTime = getTime(player, pre);
				if (lastTime == null) {
					setTime(player, pre);
					return false;
				} else {
					Calendar calcurrTime = Calendar.getInstance();
					calcurrTime.setTime(getCurrTime());
					Calendar callastTime = Calendar.getInstance();
					callastTime.setTime(lastTime);
					long secondsBetween = secondsBetween(callastTime,
							calcurrTime);
					long waitSeconds = coolDownSeconds - secondsBetween;
					long waitMinutes = Math.round(waitSeconds / 60) + 1;
					long waitHours = Math.round(waitMinutes / 60) + 1;
					if (secondsBetween > coolDownSeconds) {
						setTime(player, pre);
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
						return true;
					}
				}
			}
		}
		return false;
	}
	
	static void cancelCooldown(Player player, String pre){
		confusers.set("users." + player.getName() + ".cooldown." + pre, null);
	}

	static boolean checkCoolDownOK(Player player, String pre, String message) {
		pre = pre.toLowerCase();
		int coolDownSeconds = 0;
		if (boosCoolDown.isUsingPermissions()) {
			if (!boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown2")
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.cooldown3")
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.cooldown4")
					&& !boosCoolDown.getPermissions().has(player,
							"booscooldowns.cooldown5")) {
				coolDownSeconds = boosConfigManager.getCoolDown(player, pre);
			}
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown2")) {
				coolDownSeconds = boosConfigManager.getCoolDown2(player, pre);
			}
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown3")) {
				coolDownSeconds = boosConfigManager.getCoolDown3(player, pre);
			}
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown4")) {
				coolDownSeconds = boosConfigManager.getCoolDown4(player, pre);
			}
			if (boosCoolDown.getPermissions().has(player,
					"booscooldowns.cooldown5")) {
				coolDownSeconds = boosConfigManager.getCoolDown5(player, pre);
			}
		} else {
			coolDownSeconds = boosConfigManager.getCoolDown(player, pre);
		}
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

	static void setTime(Player player, String pre) {
		pre = pre.toLowerCase();
		String currTime = "";
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		currTime = sdf.format(cal.getTime());
		confusers.set("users." + player.getName() + ".cooldown." + pre,
				currTime);
		save();
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

	static Date getTime(Player player, String pre) {
		pre = pre.toLowerCase();
		String confTime = "";
		confTime = confusers.getString("users." + player.getName()
				+ ".cooldown." + pre, null);

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

	public static long secondsBetween(Calendar startDate, Calendar endDate) {
		long secondsBetween = 0;

		while (startDate.before(endDate)) {
			startDate.add(Calendar.SECOND, 1);
			secondsBetween++;
		}
		return secondsBetween;
	}

	static void setWarmUpOK(Player player, String pre, String message) {
		pre = pre.toLowerCase();
		confusers.set("users." + player.getName() + ".warmup." + pre, 1);
		save();
	}

	static boolean checkWarmUpOK(Player player, String pre, String message) {
		pre = pre.toLowerCase();
		int ok = 0;
		ok = confusers.getInt("users." + player.getName() + ".warmup." + pre,
				ok);
		if (ok == 1) {
			return true;
		}
		return false;
	}

	static void removeWarmUpOK(Player player, String pre, String message) {
		pre = pre.toLowerCase();
		confusers.set("users." + player.getName() + ".warmup." + pre, null);
		save();
	}

	static void removeWarmUp(Player player, String pre, String message) {
		pre = pre.toLowerCase();
		confusers.set("users." + player.getName() + ".warmup." + pre, null);
		save();
	}
}
