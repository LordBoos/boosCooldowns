package cz.boosik.boosCooldown;

import org.bukkit.entity.Player;

import java.util.TimerTask;

class BoosWarmUpTimer extends TimerTask {

    private final BoosCoolDown bCoolDown;
    private final Player player;
    private final String originalCommand;
    private final String regexCommand;

    public BoosWarmUpTimer(BoosCoolDown bCoolDown, Player player,
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

    private class boosWarmUpRunnable implements Runnable {

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
