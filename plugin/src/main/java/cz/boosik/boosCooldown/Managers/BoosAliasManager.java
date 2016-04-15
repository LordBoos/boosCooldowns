package cz.boosik.boosCooldown.Managers;

import java.util.Set;

import org.bukkit.entity.Player;

public class BoosAliasManager {

    public static String checkCommandAlias(String originalCommand,
                                           Set<String> aliases, Player player) {
        String[] splitCommand = originalCommand.split(" ", 4);
        String one = "";
        String two = "";
        String three = "";
        if (splitCommand.length > 1) {
            one = splitCommand[1];
            if (splitCommand.length > 2) {
                two = splitCommand[2];
                if (splitCommand.length > 3) {
                    three = splitCommand[3];
                }
            }
        }
        for (String alias : aliases) {
            String alias2 = alias.replace("*", ".+");
            if (originalCommand.matches("(?i)" + alias2)) {
                originalCommand = BoosConfigManager.getAlias(alias);
                if (originalCommand.contains("$1")) {
                    originalCommand = originalCommand.replace("$1", one);
                }
                if (originalCommand.contains("$2")) {
                    originalCommand = originalCommand.replace("$2", two);
                }
                if (originalCommand.contains("$*")) {
                    originalCommand = originalCommand.replace("$*", three);
                }
                if (originalCommand.contains("$player")) {
                    originalCommand = originalCommand.replace("$player",
                            player.getName());
                }
                if (originalCommand.contains("$world")) {
                    originalCommand = originalCommand.replace("$world", player
                            .getWorld().getName());
                }
            }
        }
        originalCommand = originalCommand.trim().replaceAll(" +", " ");
        return originalCommand;
    }

}
