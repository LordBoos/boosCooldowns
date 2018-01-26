package com.coloredcarrot.mcapi.json;

import org.bukkit.ChatColor;

/**
 * An enum representing a Color that is accepted in the JSON message format.
 * All rights reserved.
 *
 * @author ColoredCarrot
 * @see {@link JSONComponent#setColor(JSONColor)}, {@link JSONComponent#getColor()}
 */
public enum JSONColor {

    AQUA("&b"),
    BLACK("&0"),
    BLUE("&9"),
    DARK_AQUA("&3"),
    DARK_BLUE("&1"),
    DARK_GRAY("&8"),
    DARK_GREEN("&2"),
    DARK_PURPLE("&5"),
    DARK_RED("&4"),
    GOLD("&6"),
    GRAY("&7"),
    GREEN("&a"),
    LIGHT_PURPLE("&d"),
    RED("&c"),
    WHITE("&f"),
    YELLOW("&e");

    private final String code;

    JSONColor(String code) {
        this.code = code;
    }

    public static JSONColor fromString(String text) {
        if (text != null) {
            text = text.replace("§", "&");
            for (JSONColor b : JSONColor.values()) {
                if (text.equalsIgnoreCase(b.code)) {
                    return b;
                }
            }
        }
        return WHITE; // no color found, return default white to prevent errors
    }

    public String getCode() {
        return code;
    }

    /**
     * Transforms super.toString() into lowercase letters which are accepted by the JSON message format.
     */
    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

    public ChatColor toChatColor() {
        return ChatColor.valueOf(name());
    }

}
