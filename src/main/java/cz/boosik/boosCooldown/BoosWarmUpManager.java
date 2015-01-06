package cz.boosik.boosCooldown;

import java.util.Iterator;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import util.boosChat;

/**
 * T��da obsahuje ve�ker� metody pot�ebn� k ��zen� warmup �asova��. Spou�t�n�,
 * ukon�ov�n� a zji��ov�n� zda je warmup �asova� ji� aktivn�.
 * 
 * @author Jakub Kol��
 * 
 */
public class BoosWarmUpManager {

	private static ConcurrentHashMap<String, BoosWarmUpTimer> playercommands = new ConcurrentHashMap<String, BoosWarmUpTimer>();
	private static ConcurrentHashMap<Player, Location> playerloc = new ConcurrentHashMap<Player, Location>();
	private static ConcurrentHashMap<Player, String> playerworld = new ConcurrentHashMap<Player, String>();

	private static Timer scheduler;

	/**
	 * Metoda aplikuje na hr��e magick� efekt na dobu ur�enou parametrem
	 * warmUpSeconds.
	 * 
	 * @param player
	 *            specifick� hr��
	 * @param regexCommand
	 *            p��kaz z konfigura�n�ho souboru, kter� vyhovuje origin�ln�mu
	 *            p��kazu
	 * @param warmUpSeconds
	 *            doba warmup v sekund�ch, nastaven� pro regexCommand v
	 *            konfiguraci
	 */
	static void applyPotionEffect(Player player, String regexCommand,
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

	/**
	 * Metoda stornuje ve�ker� prob�haj�c� warmup �asova�e specifick�ho hr��e.
	 * 
	 * @param player
	 *            specifick� hr��
	 */
	public static void cancelWarmUps(Player player) {
		Iterator<String> iter = playercommands.keySet().iterator();
		while (iter.hasNext()) {
			if (iter.next().startsWith(player.getUniqueId() + "@")) {
				killTimer(player);
				iter.remove();
			}
		}
	}

	/**
	 * Metoda pro specifick�ho hr��e vyma�e ulo�enou pozici a sv�t.
	 * 
	 * @param player
	 *            specifick� hr��
	 */
	public static void clearLocWorld(Player player) {
		BoosWarmUpManager.playerloc.remove(player);
		BoosWarmUpManager.playerworld.remove(player);
	}

	/**
	 * Metoda vrac� boolean hodnotu v z�vislosti na tom jestli specifikovan�
	 * hr�� m� aktivn� warmup �asova�e nebo ne.
	 * 
	 * @param player
	 *            specifick� hr��
	 * @return true pokud hr�� m� aktivn� warmup �asova�e, jinak false
	 */
	public static boolean hasWarmUps(Player player) {
		for (String key : playercommands.keySet()) {
			if (key.startsWith(player.getUniqueId() + "@")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Metoda zji��uje, jestli je dan� warmup �asova� ozna�en� jako ji� prob�hl�
	 * nebo ne.
	 * 
	 * @param player
	 *            specifick� hr��
	 * @param regexCommand
	 *            p��kaz z konfigura�n�ho souboru, kter� vyhovuje origin�ln�mu
	 *            p��kazu
	 * @return true pokud je warmup �asova� ozna�en jako ji� prob�hl�, jinak
	 *         false
	 */
	static boolean checkWarmUpOK(Player player, String regexCommand) {
		int pre2 = regexCommand.toLowerCase().hashCode();
		int ok = 0;
		ok = BoosConfigManager.getConfusers().getInt(
				"users." + player.getUniqueId() + ".warmup." + pre2, ok);
		if (ok == 1) {
			return true;
		}
		return false;
	}

	/**
	 * Metoda vrac� boolean hodnotu na z�klad� toho jestli je pro specifikovan�
	 * p��kaz specifikovan�ho hr��e aktivn� warmup �asova�.
	 * 
	 * @param player
	 *            specifick� hr��
	 * @param regexCommand
	 *            p��kaz z konfigura�n�ho souboru, kter� vyhovuje origin�ln�mu
	 *            p��kazu
	 * @return true pokud je warmup �asova� aktivn�, jinak false
	 */
	static boolean isWarmUpProcess(Player player, String regexCommand) {
		regexCommand = regexCommand.toLowerCase();
		if (playercommands.containsKey(player.getUniqueId() + "@"
				+ regexCommand)) {
			return true;
		}
		return false;
	}

	/**
	 * Metoda odstran� v�echny �asova�e specifikovan�ho hr��e
	 * 
	 * @param player
	 *            specifick� hr��
	 */
	static void killTimer(Player player) {
		for (String key : playercommands.keySet()) {
			if (key.startsWith(player.getUniqueId() + "@")) {
				playercommands.get(key).cancel();
			}
		}
	}

	/**
	 * Metoda odstran� �asova�e na specifikovan�m p��kazu specifikovan�ho hr��e
	 * 
	 * @param player
	 *            specifick� hr��
	 * @param regexCommand
	 *            p��kaz z konfigura�n�ho souboru, kter� vyhovuje origin�ln�mu
	 *            p��kazu
	 */
	static void removeWarmUp(Player player, String regexCommand) {
		int pre2 = regexCommand.toLowerCase().hashCode();
		BoosConfigManager.getConfusers().set(
				"users." + player.getUniqueId() + ".warmup." + pre2, null);
	}

	/**
	 * Metoda odstran� ukon�en� �asova�e na specifikovan�m p��kazu
	 * specifikovan�ho hr��e
	 * 
	 * @param player
	 *            specifick� hr��
	 * @param regexCommand
	 *            p��kaz z konfigura�n�ho souboru, kter� vyhovuje origin�ln�mu
	 *            p��kazu
	 */
	static void removeWarmUpOK(Player player, String regexCommand) {
		int pre2 = regexCommand.toLowerCase().hashCode();
		BoosConfigManager.getConfusers().set(
				"users." + player.getUniqueId() + ".warmup." + pre2, null);
	}

	/**
	 * Metoda odstra�uje dan� �et�zec z Hashmapy
	 * 
	 * @param tag
	 *            �et�zec, kter� se m� odstranit z Hasmapy
	 */
	static void removeWarmUpProcess(String tag) {
		BoosWarmUpManager.playercommands.remove(tag);
	}

	/**
	 * Metoda ozna�uje warmup �asova� specifikovan�ho p��kazu specifikovan�ho
	 * hr��e jako ji� ukon�en�.
	 * 
	 * @param player
	 *            specifick� hr��
	 * @param regexCommand
	 *            p��kaz z konfigura�n�ho souboru, kter� vyhovuje origin�ln�mu
	 *            p��kazu
	 */
	static void setWarmUpOK(Player player, String regexCommand) {
		int pre2 = regexCommand.toLowerCase().hashCode();
		BoosConfigManager.getConfusers().set(
				"users." + player.getUniqueId() + ".warmup." + pre2, 1);
	}

	/**
	 * Metoda spou�t� warmup �asova� na z�klad� parametr� pomoc� Timer(). Pokud
	 * je ji� warmup aktivn�, ode�le hr��i zpr�vu kter� ho o tom informuje.
	 * 
	 * @param bCoolDown
	 *            instance t��dy BoosCooldown
	 * @param player
	 *            specifick� hr��
	 * @param regexCommand
	 *            p��kaz z konfigura�n�ho souboru, kter� vyhovuje origin�ln�mu
	 *            p��kazu
	 * @param originalCommand
	 *            origin�ln� p��kaz pou�it� hr��em
	 * @param warmUpSeconds
	 *            warmup doba nastaven� pro p��kaz v sekund�ch
	 */
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

			scheduler = new Timer();
			BoosWarmUpTimer scheduleMe = new BoosWarmUpTimer(bCoolDown,
					scheduler, player, regexCommand, originalCommand);
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

	/**
	 * @return
	 */
	public static ConcurrentHashMap<Player, String> getPlayerworld() {
		return playerworld;
	}

	/**
	 * @return
	 */
	public static ConcurrentHashMap<Player, Location> getPlayerloc() {
		return playerloc;
	}
}
