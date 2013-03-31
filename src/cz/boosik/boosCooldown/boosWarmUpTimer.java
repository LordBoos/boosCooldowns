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
				boosCoolDownManager.setWarmUpOK(player, pre);
				boosWarmUpManager.removeWarmUpProcess(player.getName() + "@"
						+ pre);
				boosCoolDownListener.clearLocWorld(player);
				player.chat(pre);
			} else if (player.isOnline() && player.isDead()
					&& boosWarmUpManager.hasWarmUps(player)) {
				boosCoolDownManager.removeWarmUp(player, pre);
				boosWarmUpManager.removeWarmUpProcess(player.getName() + "@"
						+ pre);
				boosCoolDownListener.clearLocWorld(player);
			} else if (!player.isOnline()
					&& boosWarmUpManager.hasWarmUps(player)) {
				boosCoolDownManager.removeWarmUp(player, pre);
				boosWarmUpManager.removeWarmUpProcess(player.getName() + "@"
						+ pre);
				boosCoolDownListener.clearLocWorld(player);
			}
		}
	}
	private boosCoolDown bCoolDown;
	private Player player;

	private String pre;

	public boosWarmUpTimer() {
	}

	public boosWarmUpTimer(boosCoolDown bCoolDown, Timer timer, Player player,
			String pre) {
		this.bCoolDown = bCoolDown;
		this.player = player;
		this.pre = pre;
	}

	@Override
	public void run() {
		bCoolDown.getServer().getScheduler()
				.scheduleSyncDelayedTask(bCoolDown, new boosWarmUpRunnable());
	}
}