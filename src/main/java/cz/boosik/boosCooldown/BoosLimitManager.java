package cz.boosik.boosCooldown;

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
		int uses = getUses(player, regexCommand);
		if (player.hasPermission("booscooldowns.nolimit")
				|| player.hasPermission("booscooldowns.nolimit."
						+ originalCommand)) {
		} else {
			if (limit == -1) {
				return false;
			} else if (limit <= uses) {
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
	static void setUses(Player player, String regexCommand,
			String originalCommand) {
		if (BoosConfigManager.getLimitsEnabled()) {
			if (BoosConfigManager.getCommands(player).contains(regexCommand)) {
				int regexCommand2 = regexCommand.toLowerCase().hashCode();
				int uses = getUses(player, regexCommand);
				uses = uses + 1;
				try {
					BoosConfigManager.getConfusers().set(
							"users."
									+ player.getName().toLowerCase().hashCode()
									+ ".uses." + regexCommand2, uses);
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

}
