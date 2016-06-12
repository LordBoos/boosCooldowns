package nms;

import org.bukkit.entity.Player;

/**
 * All rights reserved.
 *
 * @author ColoredCarrot
 */
public interface INMSHook {

    /**
     * Sends a JSON message to a player.
     *
     * @param json (String) - the plain JSON
     * @param player (Player) - the player
     */
    void sendJSON(String json, Player player);

    /**
     * Sends an actionbar to a player.
     *
     * @param json (String) - the plain JSON
     * @param player (Player) - the player
     */
    void sendActionBar(String json, Player player);

}
