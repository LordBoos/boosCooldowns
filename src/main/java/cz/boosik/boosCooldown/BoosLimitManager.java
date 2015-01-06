package cz.boosik.boosCooldown;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import org.bukkit.entity.Player;

import util.boosChat;

/**
 * T��da obsahuje ve�ker� metody pot�ebn� k ��zen� limit�.
 * 
 * @author Jakub Kol��
 * 
 */
public class BoosLimitManager {
	/**
	 * Metoda kontroluje zda je mo�n� pou��t p��kaz, nebo zda je p��kaz ji�
	 * zablokovan�.
	 * 
	 * @param player
	 *            specifick� hr��
	 * @param regexCommand
	 *            p��kaz z konfigura�n�ho souboru, kter� vyhovuje origin�ln�mu
	 *            p��kazu
	 * @param originalCommand
	 *            origin�ln� p��kaz pou�it� hr��em
	 * @param limit
	 *            limit nastaven� pro regexCommand
	 * @return false pokud p��kaz je mo�n� pou��t, true pokud p��kaz nen� mo�n�
	 *         pou��t
	 */
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

		if (player.hasPermission("booscooldowns.nolimit")
				|| player.hasPermission("booscooldowns.nolimit."
						+ originalCommand)) {
		} else {
			if (limit == -1) {
				return false;
			} else if (limit <= uses) {
				if (limitResetDelay > 0) {
					long secondsBetween = secondsBetween(callastTime,
							calcurrTime, limitResetDelay);
					long waitSeconds = secondsBetween;
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
						long secondsBetween = secondsBetween(callastTimeGlobal,
								calcurrTime, limitResetDelayGlobal);
						long waitSeconds = secondsBetween;
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

	/**
	 * Metoda vrac� hodnotu, kter� je ur�ena t�m, kolikr�t ji� hr�� pou�il
	 * specifikovan� p��kaz.
	 * 
	 * @param player
	 *            specifick� hr��
	 * @param regexCommand
	 *            p��kaz z konfigura�n�ho souboru, kter� vyhovuje origin�ln�mu
	 *            p��kazu
	 * @return hodnota p�edstavuj�ci po�et pou�it� specifikovan�ho p��kazu
	 */
	static int getUses(Player player, String regexCommand) {
		int regexCommand2 = regexCommand.toLowerCase().hashCode();
		int uses = 0;
		uses = BoosConfigManager.getConfusers().getInt(
				"users." + player.getUniqueId() + ".uses." + regexCommand2,
				uses);
		return uses;
	}

	/**
	 * Metoda nastavuje po�et pou�it� p��kazu o jedna v�t�� po ka�d�m pou�it�
	 * p��kazu hr��em. Nasteven� hodnoty prob�h� jen pro p��kazy, kter� jsou
	 * definov�ny v konfiguraci.
	 * 
	 * @param player
	 *            specifick� hr��
	 * @param regexCommand
	 *            p��kaz z konfigura�n�ho souboru, kter� vyhovuje origin�ln�mu
	 *            p��kazu
	 * @param originalCommand
	 *            origin�ln� p��kaz pou�it� hr��em
	 */
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
			} else {
				return;
			}
		}
	}

	/**
	 * Metoda odes�l� hr��i zpr�vu o limitovan�m p��kazu, hodnotu tohoto limitu
	 * a kolikr�t je je�t� mo�n� limitovan� p��kaz pou��t.
	 * 
	 * @param send
	 *            hr�� kter�mu bude odesl�n seznam
	 * @param comm
	 *            p��kaz o kter�m si hr�� vy��dal informace
	 * @param lim
	 *            hodnota limitu na p��kazu
	 */
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

	/**
	 * Metoda vrac� sou�asn� p�esn� datum a �as.
	 * 
	 * @return sou�asn� �as a datum
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
	 * Metoda vrac� datum a �as, kdy hr�� naposledy pou�il dan� p��kaz.
	 * 
	 * @param player
	 *            specifikovan� hr��
	 * @param regexCommand
	 *            p��kaz z konfigurace vyhovuj�c� origin�ln�mu p��kazu
	 * @return datum a �as kdy hr�� naposledy pou�il dan� p��kaz
	 */
	static Date getTime(Player player, String regexCommand) {
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

	static Date getTime(String regexCommand) {
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

	/**
	 * Metoda ukl�d� do datab�ze datum a �as kdy hr�� naposledy pou�il dan�
	 * p��kaz.
	 * 
	 * @param player
	 *            specifick� hr��
	 * @param regexCommand
	 *            p��kaz z konfigurace vyhovuj�c� origin�ln�mu p��kazu
	 */
	static void setTime(Player player, String regexCommand) {
		int pre2 = regexCommand.toLowerCase().hashCode();
		String currTime = "";
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		currTime = sdf.format(cal.getTime());
		BoosConfigManager.getConfusers()
				.set("users." + player.getUniqueId() + ".lastused." + pre2,
						currTime);
	}

	/**
	 * Metoda vrac� hodnotu rozd�lu v sekund�ch mezi dv�mi hodnotami datumu a
	 * �asu.
	 * 
	 * @param startDate
	 * @param endDate
	 * @return rozd�l v sekund�ch mezi startDate a endDate
	 */
	static long secondsBetween(Calendar startDate, Calendar endDate,
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

	static void setTime(String command) {
		String currTime = "";
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		currTime = sdf.format(cal.getTime());
		BoosConfigManager.getConfusers().set("global." + command + ".reset",
				currTime);
	}
}
