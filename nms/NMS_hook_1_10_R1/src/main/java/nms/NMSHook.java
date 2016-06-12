package nms;

import org.bukkit.entity.Player;

/**
 * All rights reserved.
 *
 * @author ColoredCarrot
 */
public interface NMSHook {

    /**
     * Sends a JSON message to a player.
     *
     * @param json (String) - the plain JSON
     * @param player (Player) - the player
     */
    public void sendJSON(String json, Player player);

    /**
     * Sends an actionbar to a player.
     *
     * @param json (String) - the plain JSON
     * @param player (Player) - the player
     */
    public void sendActionBar(String json, Player player);

}
