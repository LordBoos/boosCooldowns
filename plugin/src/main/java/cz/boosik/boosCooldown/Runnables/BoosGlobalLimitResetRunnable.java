package cz.boosik.boosCooldown.Runnables;

import org.bukkit.Bukkit;

import cz.boosik.boosCooldown.BoosCoolDown;
import cz.boosik.boosCooldown.Managers.BoosConfigManager;
import cz.boosik.boosCooldown.Managers.BoosLimitManager;

public class BoosGlobalLimitResetRunnable implements Runnable {

    private final String command;

    public BoosGlobalLimitResetRunnable(final String key) {
        this.command = key;
    }

    @Override
    public void run() {
        BoosCoolDown.getLog().info(
                "[boosCooldowns] Reseting limits for command " + command);
        BoosLimitManager.clearAllLimits(command.hashCode());
        BoosLimitManager.setGlobalLimitResetDate(command);
        String msg = BoosConfigManager.getLimitResetNowMessage();
        msg = msg.replaceAll("&command&", command);
        msg = msg.replaceAll("&", "§");
        Bukkit.broadcastMessage(msg);
        BoosCoolDown.startLimitResetTimerGlobal(command);
    }

}
