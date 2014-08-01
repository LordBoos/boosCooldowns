package cz.boosik.boosCooldown;

import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.entity.Player;

/**
 * T��da staraj�c� se o samotn� �asova�e warmup� pomoc� TimerTask
 * 
 * @author Jakub Kol��
 * 
 */
public class BoosWarmUpTimer extends TimerTask {

	private BoosCoolDown bCoolDown;
	private Player player;
	private String originalCommand;
	private String regexCommand;

	/**
	 * @param bCoolDown
	 *            instance t��dy BoosCoolDown
	 * @param timer
	 * @param player
	 *            specifick� hr��
	 * @param regexCommand
	 *            p��kaz z konfigura�n�ho souboru, kter� vyhovuje origin�ln�mu
	 *            p��kazu
	 * @param originalCommand
	 *            origin�ln� p��kaz kter� hr�� pou�il
	 */
	public BoosWarmUpTimer(BoosCoolDown bCoolDown, Timer timer, Player player,
			String regexCommand, String originalCommand) {
		this.bCoolDown = bCoolDown;
		this.player = player;
		this.regexCommand = regexCommand;
		this.originalCommand = originalCommand;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		bCoolDown.getServer().getScheduler()
				.scheduleSyncDelayedTask(bCoolDown, new boosWarmUpRunnable());
	}

	public class boosWarmUpRunnable implements Runnable {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			if (player.isOnline() && !player.isDead()
					&& BoosWarmUpManager.hasWarmUps(player)) {
				BoosWarmUpManager.setWarmUpOK(player, regexCommand);
				BoosWarmUpManager.removeWarmUpProcess(player.getUniqueId()
						+ "@" + regexCommand);
				BoosWarmUpManager.clearLocWorld(player);
				player.chat(originalCommand);
			} else if (player.isOnline() && player.isDead()
					&& BoosWarmUpManager.hasWarmUps(player)) {
				BoosWarmUpManager.removeWarmUp(player, regexCommand);
				BoosWarmUpManager.removeWarmUpProcess(player.getUniqueId()
						+ "@" + regexCommand);
				BoosWarmUpManager.clearLocWorld(player);
			} else if (!player.isOnline()
					&& BoosWarmUpManager.hasWarmUps(player)) {
				BoosWarmUpManager.removeWarmUp(player, regexCommand);
				BoosWarmUpManager.removeWarmUpProcess(player.getUniqueId()
						+ "@" + regexCommand);
				BoosWarmUpManager.clearLocWorld(player);
			}
		}
	}
}
