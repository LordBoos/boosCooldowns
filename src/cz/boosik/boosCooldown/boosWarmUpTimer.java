package cz.boosik.boosCooldown;

import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.entity.Player;

import util.boosChat;

public class boosWarmUpTimer extends TimerTask {

	boosCoolDown bCoolDown;
	Player player;
	String pre;
	String message;

	public boosWarmUpTimer(boosCoolDown bCoolDown, Timer timer, Player player,
			String pre, String message) {
		this.bCoolDown = bCoolDown;
		this.player = player;
		this.pre = pre;
		this.message = message;
	}

	public boosWarmUpTimer() {
	}

	@Override
	public void run() {
		if (player.isOnline() && boosWarmUpManager.hasWarmUps(player)) {
			boosCoolDownManager.setWarmUpOK(player, pre, message);
			boosWarmUpManager.removeWarmUpProcess(this.player.getName() + "@"
					+ pre);
			player.chat(pre + message);
		} else if (!player.isOnline() && boosWarmUpManager.hasWarmUps(player)){
			boosChat.sendMessageToPlayer(player,
					boosConfigManager.getCancelWarmupOnSprintMessage());
			boosWarmUpManager.cancelWarmUps(player);
		}
	}
}