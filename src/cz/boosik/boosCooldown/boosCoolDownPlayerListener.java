package cz.boosik.boosCooldown;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

//import org.bukkit.event.entity.EntityDamageEvent;

public class boosCoolDownPlayerListener extends PlayerListener {
	private final boosCoolDown plugin;

	public boosCoolDownPlayerListener(boosCoolDown instance) {
		plugin = instance;
	}

	@Override
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled()) {
			return;
		}
		String message = event.getMessage();
		Player player = event.getPlayer();
		boolean on = true;

		if (player.isOp()) {
			on = false;
		}
		if (player.hasPermission("boosCoolDown.exception") || player.isOp()) {
			on = false;
		} else {
			on = true;
		}

		if (on) {
			int i = message.indexOf(' ');
			if (i < 0) {
				i = message.length();
			}

			String preCommand = message.substring(0, i);
			String messageCommand = message.substring(i, message.length());

			boolean onCooldown = this.checkCooldown(event, player, preCommand,
					messageCommand);

			if (!onCooldown && messageCommand.length() > 1) {
				int j = messageCommand.indexOf(' ', 1);
				if (j < 0) {
					j = messageCommand.length();
				}

				String preSub = messageCommand.substring(1, j);
				String messageSub = messageCommand.substring(j,
						messageCommand.length());
				preSub = preCommand + ' ' + preSub;

				onCooldown = this.checkCooldown(event, player, preSub,
						messageSub);
			}
		}
	}

	// Returns true if the command is on cooldown, false otherwise
	private boolean checkCooldown(PlayerCommandPreprocessEvent event,
			Player player, String pre, String message) {
		int warmUpSeconds = boosConfigManager.getWarmUp(player, pre);
		if (warmUpSeconds > 0) {
			if (!boosCoolDownManager.checkWarmUpOK(player, pre, message)) {
				if (boosCoolDownManager.checkCoolDownOK(player, pre, message)) {
					boosWarmUpManager.startWarmUp(this.plugin, player, pre,
							message, warmUpSeconds);
					event.setCancelled(true);
					return true;
				} else {
					event.setCancelled(true);
					return true;
				}
			} else {
				if (boosCoolDownManager.coolDown(player, pre, message)) {
					event.setCancelled(true);
					return true;
				} else {
					boosCoolDownManager.removeWarmUpOK(player, pre, message);
				}
			}
		} else {
			if (boosCoolDownManager.coolDown(player, pre, message)) {
				event.setCancelled(true);
				return true;
			}
		}
		return false;
	}

	public void onPlayerCommandMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		boosWarmUpManager.cancelWarmUps(player);
		boosCoolDownManager.cancelCoolDowns(player);

	}
}