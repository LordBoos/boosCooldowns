package cz.boosik.boosCooldown;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bukkit.entity.Player;

import util.boosChat;

/**
 * T��da obsahuje ve�ker� metody pot�ebn� k ��zen� cooldown �asova��. Spou�t�n�,
 * ukon�ov�n�, zji��ov�n� zda je cooldown �asova� ji� aktivn�.
 * 
 * @author Jakub Kol��
 * 
 */
public class BoosCoolDownManager {
	/**
	 * Metoda ukon�uje specifikovan� cooldown �asova� pro specifikovan�ho hr��e.
	 * 
	 * @param player
	 *            specifick� hr��
	 * @param regexCommand
	 *            p��kaz z konfigurace vyhovuj�c� origin�ln�mu p��kazu
	 */
	static void cancelCooldown(Player player, String regexCommand) {
		int pre2 = regexCommand.toLowerCase().hashCode();
		BoosConfigManager.getConfusers().set(
				"users." + player.getUniqueId() + ".cooldown." + pre2, null);
	}

	/**
	 * Metoda vrac� hodnotu boolean na z�klad� toho, jestli m� specifikovan�
	 * p��kaz aktivn� cooldown �asova�.
	 * 
	 * @param player
	 *            specifikovan� hr��
	 * @param regexCommand
	 *            p��kaz z konfigurace vyhovuj�c� origin�ln�mu p��kazu
	 * @param originalCommand
	 *            origin�ln� p��kaz pou�it� hr��em
	 * @param coolDownSeconds
	 *            cooldown doba v sekund�ch, nastaven� pro regexCommand v
	 *            konfiguraci
	 * @return true pokud je p��kaz na cooldown �asova�i, jinak false
	 */
	static boolean cd(Player player, String regexCommand,
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
			long waitMinutes = (long) Math.ceil(waitSeconds / 60.0);
			long waitHours = (long) Math.ceil(waitMinutes / 60.0);
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
				msg = msg.replaceAll("&command&", originalCommand);
				if (waitSeconds >= 60 && 3600 >= waitSeconds) {
					msg = msg.replaceAll("&seconds&",
							Long.toString(waitMinutes));
					msg = msg.replaceAll("&unit&",
							BoosConfigManager.getUnitMinutesMessage());
				} else if (waitMinutes >= 60) {
					msg = msg.replaceAll("&seconds&", Long.toString(waitHours));
					msg = msg.replaceAll("&unit&",
							BoosConfigManager.getUnitHoursMessage());
				} else {
					String secs = Long.toString(waitSeconds);
					if (secs.equals("0")) {
						secs = "1";
					}
					msg = msg.replaceAll("&seconds&", secs);
					msg = msg.replaceAll("&unit&",
							BoosConfigManager.getUnitSecondsMessage());
				}
				boosChat.sendMessageToPlayer(player, msg);
				return true;
			}
		}
	}

	/**
	 * Metoda kontroluje, jestli hr�� nedisponuje opr�vn�n�mi, kter� obch�zej�
	 * cooldown �asova�e. Pokud t�mito opr�vn�n�mi hr�� disponuje, pak metoda
	 * vrac� false. Pokud hr�� nedisponuje t�mito opr�vn�n�mi, vrac� hodnotu
	 * vr�cenou metodou cd();.
	 * 
	 * @param player
	 *            specifikovan� hr��
	 * @param regexCommand
	 *            p��kaz z konfigurace vyhovuj�c� origin�ln�mu p��kazu
	 * @param originalCommand
	 *            origin�ln� p��kaz pou�it� hr��em
	 * @param time
	 *            cooldown doba v sekund�ch, nastaven� pro regexCommand v
	 *            konfiguraci
	 * @return false pokud hr�� disponuje opr�vn�n�mi, jinak hodnotu vr�cenou
	 *         metodou cd();.
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

	/**
	 * Metoda vrac� hodnotu boolean na z�klad� toho, jestli m� specifikovan�
	 * p��kaz aktivn� cooldown �asova�.
	 * 
	 * @param player
	 *            specifikovan� hr��
	 * @param regexCommand
	 *            p��kaz z konfigurace vyhovuj�c� origin�ln�mu p��kazu
	 * @param originalCommand
	 *            origin�ln� p��kaz pou�it� hr��em
	 * @param time
	 *            cooldown doba v sekund�ch, nastaven� pro regexCommand v
	 *            konfiguraci
	 * @return false pokud m� p��kaz aktivn� cooldown �asova�, jinak false
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
				long waitMinutes = (long) Math.ceil(waitSeconds / 60.0);
				long waitHours = (long) Math.ceil(waitMinutes / 60.0);
				if (secondsBetween > time) {
					return true;
				} else {
					String msg = BoosConfigManager.getCoolDownMessage();
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
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Metoda vrac� hodnotu rozd�lu v sekund�ch mezi dv�mi hodnotami datumu a
	 * �asu.
	 * 
	 * @param startDate
	 * @param endDate
	 * @return rozd�l v sekund�ch mezi startDate a endDate
	 */
	static long secondsBetween(Calendar startDate, Calendar endDate) {
		long secondsBetween = 0;
		secondsBetween = (endDate.getTimeInMillis() - startDate
				.getTimeInMillis()) / 1000;
		return secondsBetween;
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
				.set("users." + player.getUniqueId() + ".cooldown." + pre2,
						currTime);
	}

	/**
	 * Metoda spou�t� ve�ker� cooldown �asova�e pro specifick�ho hr��e.
	 * 
	 * @param player
	 *            specifick� hr��
	 * @param message
	 */
	public static void startAllCooldowns(Player player, String message) {
		for (String a : BoosConfigManager.getCooldowns(player)) {
			int cooldownTime = BoosConfigManager.getCoolDown(a, player);
			coolDown(player, a, message, cooldownTime);
		}

	}

}
