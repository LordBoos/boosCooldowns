package cz.boosik.boosCooldown.Runnables;

import java.util.TimerTask;

import org.bukkit.entity.Player;

import cz.boosik.boosCooldown.BoosCoolDown;
import cz.boosik.boosCooldown.BoosCoolDownListener;
import cz.boosik.boosCooldown.Managers.BoosWarmUpManager;

public class BoosWarmUpTimer extends TimerTask {

    private final BoosCoolDown bCoolDown;
    private final Player player;
    private final String originalCommand;
    private final String regexCommand;

    public BoosWarmUpTimer(
            final BoosCoolDown bCoolDown, final Player player,
            final String regexCommand, final String originalCommand) {
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

    private class boosWarmUpRunnable implements Runnable {

        @Override
        public void run() {
            if (player.isOnline() && !player.isDead()
                    && BoosWarmUpManager.hasWarmUps(player)) {
                BoosWarmUpManager.setWarmUpOK(player, regexCommand);
                BoosWarmUpManager.removeWarmUpProcess(player.getUniqueId()
                        + "@" + regexCommand);
                BoosCoolDownListener.commandQueue.put(player.getUniqueId() + "@" + originalCommand, true);
                player.chat(originalCommand);
            } else if (player.isOnline() && player.isDead()
                    && BoosWarmUpManager.hasWarmUps(player)) {
                BoosWarmUpManager.removeWarmUp(player, regexCommand);
                BoosWarmUpManager.removeWarmUpProcess(player.getUniqueId()
                        + "@" + regexCommand);
            } else if (!player.isOnline()
                    && BoosWarmUpManager.hasWarmUps(player)) {
                BoosWarmUpManager.removeWarmUp(player, regexCommand);
                BoosWarmUpManager.removeWarmUpProcess(player.getUniqueId()
                        + "@" + regexCommand);
            }
        }
    }

    public String getRegexCommand() {
        return regexCommand;
    }
}
