package com.coloredcarrot.mcapi.json;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.md_5.bungee.chat.ComponentSerializer;

/**
 * A JSON Object is the base for any JSON message using this library.
 * It contains one or more JSONComponentSimple Objects used to make up the whole JSON message.
 * All rights reserved,
 *
 * @author ColoredCarrot
 * @see JSONComponentSimple
 */
public class JSON {

    private final List<JSONComponentSimple> components = new ArrayList<JSONComponentSimple>();
    private String generatedJSON;
    private boolean generated;

    /**
     * Constructs a new JSON Object with a base JSONComponentSimple Object.
     *
     * @param jsonComponent (JSONComponentSimple) - the base JSONComponentSimple Object
     */
    public JSON(final JSONComponentSimple jsonComponent) {

        if (jsonComponent == null) {
            throw new IllegalArgumentException("component cannot be null!");
        }

        components.add(jsonComponent);

        generatedJSON = "";
        generated = false;

    }

    /**
     * Constructs a new JSON Object with already containing multiple JSONComponentSimple Objects.
     *
     * @param components (JSONComponentSimple[]) - the JSONComponentSimple Objects
     */
    public JSON(final JSONComponentSimple... components) {

        if (components == null) {
            throw new IllegalArgumentException("component cannot be null!");
        }

        if (components.length > 0) {
            for (final JSONComponentSimple component : components) {
                this.components.add(component);
            }
        }

        generatedJSON = "";
        generated = false;

    }

    /**
     * Sends a JSON message to a player.
     *
     * @param to   (Player) - the player to send the message to
     * @param json (String) - the raw JSON to send
     */
    public static void sendJSON(final Player to, final String json) {

        if (to == null) {
            throw new IllegalArgumentException("player cannot be null!");
        }
        if (json == null) {
            throw new IllegalArgumentException("json cannot be null!");
        }
        to.spigot().sendMessage(ComponentSerializer.parse(json));
    }

    /**
     * Sends a JSON Object to a player.
     * This method is in no way different from sendJSON(Player, String); in fact, it actually calls it.
     *
     * @param to   (Player) - the player to send the message to
     * @param json (JSON) - the JSON Object to send
     */
    public static void sendJSON(final Player to, final JSON json) {
        sendJSON(to, json.get());
    }

    /**
     * Combines multiple JSON Objects into a single one, using .clone() on all components.
     *
     * @param jsons (JSON[]) - the JSON Objects to combine
     * @return (JSON) - the combined JSON.
     */
    public static JSON combineToNewJSON(final JSON... jsons) {

        final JSON baseJSON;

        baseJSON = jsons[0].clone();

        for (int i = 1; i < jsons.length; i++) {
            for (final JSONComponentSimple comp : jsons[i].getComponents()) {
                baseJSON.add(comp.clone());
            }
        }

        return baseJSON;

    }

    /**
     * returns a new JSON Object, invoking .clone() on all JSONComponentSimple Objects in this JSON Object.
     */
    @Override
    public JSON clone() {

        final JSONComponentSimple[] comps = new JSONComponentSimple[components.size()];

        for (int i = 0; i < components.size(); i++) {
            comps[i] = components.get(i).clone();
        }

        return new JSON(comps);

    }

    /**
     * Adds a JSONComponentSimple to this JSON Object.
     *
     * @param component (JSONComponentSimple) - the JSONComponentSimple to add.
     * @return (JSON) - this JSON Object, for chaining.
     */
    public JSON add(final JSONComponentSimple component) {

        if (component == null) {
            throw new IllegalArgumentException("component cannot be null!");
        }

        components.add(component);

        generated = false;

        return this;

    }

    /**
     * Removes a JSONComponentSimple from this JSON Object.
     *
     * @param component (JSONComponentSimple) - the JSONComponentSimple to remove.
     * @return (JSON) - this JSON Object, for chaining.
     */
    public JSON remove(final JSONComponentSimple component) {

        if (component == null) {
            throw new IllegalArgumentException("component cannot be null!");
        }

        components.remove(component);

        generated = false;

        return this;

    }

    /**
     * Gets all the JSONComponentSimple Objects included in this JSON Object.
     *
     * @return (List: JSONComponentSimple) - all JSONComponentSimple Objects.
     */
    public List<JSONComponentSimple> getComponents() {
        return components;
    }

    /**
     * Combines this JSON Object with other JSON Objects.
     * Be careful; this doesn't invoke .clone() !
     *
     * @param jsons (JSON[]) - the JSON Objects to add to this one
     * @return JSON - this JSON Object, with all other JSON Objects added.
     */
    public JSON combine(final JSON... jsons) {

        for (final JSON json : jsons) {
            for (final JSONComponentSimple component : json.getComponents()) {
                this.add(component);
            }
        }

        return this;

    }

    /**
     * Generates the JSON String.
     * You should have no need to use this method as it's automatically called on get() and send(Player).
     *
     * @return (JSON) - this JSON Object, for chaining.
     */
    public JSON generate() {

        generatedJSON = "{\"text\":\"\",\"extra\":[";

        for (final JSONComponentSimple component : components) {
            generatedJSON += component.get() + ",";
        }

        generatedJSON = generatedJSON.substring(0, generatedJSON.length() - 1) + "]}";

        generated = true;

        return this;

    }

    /**
     * Generates and then returns the raw JSON message.
     *
     * @return (String) - the raw JSON matching this JSON Object.
     */
    public String get() {

        if (!generated) {
            generate();
        }

        return generatedJSON;

    }

    /**
     * Generates and then sends the raw JSON matching this JSON Object to a player.
     *
     * @param player (Player) - the player to send the message to
     * @return (JSON) - this JSON Object, for chaining.
     */
    public JSON send(final Player player) {

        if (player == null) {
            throw new IllegalArgumentException("player cannot be null!");
        }

        sendJSON(player, get());

        return this;

    }

    /**
     * Returns a non-JSON version of this JSON Object. Does not contain hover- or click actions.
     *
     * @return (String) - the ChatColor version
     * @see JSONComponentSimple#getChatColorVersion()
     */
    public String getChatColorVersion() {

        String s = "";

        for (final JSONComponentSimple comp : components) {
            s += ChatColor.RESET + comp.getChatColorVersion();
        }

        return s;

    }

}
