package util;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings("ALL")
public class boosChat {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static final List<String> Colors = new LinkedList<>();
    private static Server server;

    public boosChat(Server server) {
        Colors.add("&black&");
        Colors.add("&darkblue&");
        Colors.add("&darkgreen&");
        Colors.add("&darkaqua&");
        Colors.add("&darkred&");
        Colors.add("&purple&");
        Colors.add("&gold&");
        Colors.add("&gray&");
        Colors.add("&darkgray&");
        Colors.add("&blue&");
        Colors.add("&green&");
        Colors.add("&aqua&");
        Colors.add("&red&");
        Colors.add("&pink&");
        Colors.add("&yellow&");
        Colors.add("&white&");
        Colors.add("&0");
        Colors.add("&1");
        Colors.add("&2");
        Colors.add("&3");
        Colors.add("&4");
        Colors.add("&5");
        Colors.add("&6");
        Colors.add("&7");
        Colors.add("&8");
        Colors.add("&9");
        Colors.add("&a");
        Colors.add("&b");
        Colors.add("&c");
        Colors.add("&d");
        Colors.add("&e");
        Colors.add("&f");
        boosChat.server = server;
    }

    public static void broadcastMessage(String message) {
        message = boosChat.replaceColorCodes(message);
        log.info("[bColoredChat] " + message);
        server.broadcastMessage(message);
    }

    private static String replaceColorCodes(String line) {
        line = replaceTags(line);
        line = line.replaceAll("&", "§");
        return line;
    }

    private static String replaceTags(String line) {
        line = line.replaceAll("&black&", "&0");
        line = line.replaceAll("&darkblue&", "&1");
        line = line.replaceAll("&darkgreen&", "&2");
        line = line.replaceAll("&darkaqua&", "&3");
        line = line.replaceAll("&darkred&", "&4");
        line = line.replaceAll("&purple&", "&5");
        line = line.replaceAll("&gold&", "&6");
        line = line.replaceAll("&gray&", "&7");
        line = line.replaceAll("&darkgray&", "&8");
        line = line.replaceAll("&blue&", "&9");
        line = line.replaceAll("&green&", "&a");
        line = line.replaceAll("&aqua&", "&b");
        line = line.replaceAll("&red&", "&c");
        line = line.replaceAll("&pink&", "&d");
        line = line.replaceAll("&yellow&", "&e");
        line = line.replaceAll("&white&", "&f");
        return line;
    }

    public static void sendMessageToCommandSender(CommandSender sender,
                                                  String message) {
        if (sender instanceof Player) {
            boosChat.sendMessageToPlayer((Player) sender, message);
        } else {
            boosChat.sendMessageToServer(message);
        }
    }

    public static void sendMessageToPlayer(Player player, String message) {
        message = boosChat.replaceColorCodes(message);
        player.sendMessage(message);
    }

    private static void sendMessageToServer(String message) {
        message = boosChat.replaceColorCodes(message);
        log.info(message);
    }
}
