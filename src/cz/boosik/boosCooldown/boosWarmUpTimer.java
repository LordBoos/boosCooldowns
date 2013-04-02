package cz.boosik.boosCooldown;

import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.entity.Player;

public class boosWarmUpTimer extends TimerTask {

	public class boosWarmUpRunnable implements Runnable {
		@Override
		public void run() {
			if (player.isOnline() && !player.isDead()
					&& boosWarmUpManager.hasWarmUps(player)) {
				boosWarmUpManager.setWarmUpOK(player, regexCommand);
				boosWarmUpManager.removeWarmUpProcess(player.getName() + "@"
						+ regexCommand);
				boosWarmUpManager.clearLocWorld(player);
				player.chat(originalCommand);
			} else if (player.isOnline() && player.isDead()
					&& boosWarmUpManager.hasWarmUps(player)) {
				boosWarmUpManager.removeWarmUp(player, regexCommand);
				boosWarmUpManager.removeWarmUpProcess(player.getName() + "@"
						+ regexCommand);
				boosWarmUpManager.clearLocWorld(player);
			} else if (!player.isOnline()
					&& boosWarmUpManager.hasWarmUps(player)) {
				boosWarmUpManager.removeWarmUp(player, regexCommand);
				boosWarmUpManager.removeWarmUpProcess(player.getName() + "@"
						+ regexCommand);
				boosWarmUpManager.clearLocWorld(player);
			}
		}
	}

	private boosCoolDown bCoolDown;
	private Player player;
	private String originalCommand;
	private String regexCommand;

	public boosWarmUpTimer() {
	}

	public boosWarmUpTimer(boosCoolDown bCoolDown, Timer timer, Player player,
			String regexCommand, String originalCommand) {
		this.bCoolDown = bCoolDown;
		this.player = player;
		this.regexCommand = regexCommand;
		this.originalCommand = originalCommand;
	}

	@Override
	public void run() {
		bCoolDown.getServer().getScheduler()
				.scheduleSyncDelayedTask(bCoolDown, new boosWarmUpRunnable());
	}
}