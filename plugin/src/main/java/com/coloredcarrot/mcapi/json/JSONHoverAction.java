package com.coloredcarrot.mcapi.json;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cz.boosik.boosCooldown.BoosCoolDown;

/**
 * Represents a hover action.
 * All rights reserved.
 *
 * @param <T> - the Type of the value associated with the class implementing this interface
 * @author ColoredCarrot
 * @see {@link #getValue()}, {@link #setValue(Object)}
 */
public interface JSONHoverAction<T> {

    /**
     * Gets the value associated with this JSONHoverAction.
     *
     * @return (?) - the value.
     * @see #setValue(Object)
     */
    T getValue();

    /**
     * Sets the value of this JSONHoverAction.
     *
     * @param newValue (?) - the new value
     * @return (JSONHoverAction: ?) - this JSONHoverAction Object, for chaining.
     */
    JSONHoverAction<T> setValue(T newValue);

    /**
     * Gets the value associated with this JSONHoverAction transformed to a String.
     *
     * @return (String) - the value as a String.
     * @see #getValue()
     */
    String getValueString();

    /**
     * Gets the action name associated with this JSONHoverAction.
     * Example: "show_text"
     *
     * @return (String) - the action name.
     */
    String getActionName();

    /**
     * Shows some JSON-formed text when hovering over the text in the chat.
     */
    class ShowText
            implements JSONHoverAction<JSON> {

        /**
         * The action name
         *
         * @see #getActionName()
         */
        public static final String NAME = "show_text";

        private JSON value;

        /**
         * Constructs a {@link JSONHoverAction.ShowText}
         *
         * @param value (JSON) - the value associated with this JSONHoverAction
         */
        public ShowText(final JSON value) {

            this.value = value;

        }

        @Override
        public JSON getValue() {
            return value;
        }

        @Override
        public JSONHoverAction<JSON> setValue(final JSON newValue) {
            value = newValue;
            return this;
        }

        @Override
        public String getValueString() {
            return value.get();
        }

        @Override
        public String getActionName() {
            return NAME;
        }

    }

    /**
     * Shows some JSON-formed text when hovering over the text in the chat.
     */
    class ShowStringText
            implements JSONHoverAction<String> {

        /**
         * The action name
         *
         * @see #getActionName()
         */
        public static final String NAME = "show_text";

        private String value;

        /**
         * Constructs a {@link JSONHoverAction.ShowText}
         *
         * @param value (JSON) - the value associated with this JSONHoverAction
         */
        public ShowStringText(final String value) {

            this.value = value;

        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public JSONHoverAction<String> setValue(final String newValue) {
            value = newValue;
            return this;
        }

        @Override
        public String getValueString() {
            return value;
        }

        @Override
        public String getActionName() {
            return NAME;
        }

    }

    /**
     * Shows an item when hovering over the text in the chat.
     */
    class ShowItem
            implements JSONHoverAction<Material> {

        /**
         * The action name
         *
         * @see #getActionName()
         */
        public static final String NAME = "show_item";

        private Material value;

        /**
         * Constructs a {@link JSONHoverAction.ShowItem}
         *
         * @param value (JSON) - the value associated with this JSONHoverAction
         */
        public ShowItem(final Material value) {

            this.value = value;

        }

        @Override
        public Material getValue() {
            return value;
        }

        @Override
        public JSONHoverAction<Material> setValue(final Material newValue) {
            value = newValue;
            return this;
        }

        @Override
        public String getValueString() {
            return value.toString().toLowerCase();
        }

        @Override
        public String getActionName() {
            return NAME;
        }

    }

    /**
     * Shows an item stack when hovering over the text in the chat.
     */
    class ShowItemStack
            implements JSONHoverAction<ItemStack> {

        /**
         * The action name
         *
         * @see #getActionName()
         */
        public static final String NAME = "show_item";

        private ItemStack value;

        /**
         * Constructs a {@link JSONHoverAction.ShowItem}
         *
         * @param value (JSON) - the value associated with this JSONHoverAction
         */
        public ShowItemStack(final ItemStack value) {

            this.value = value;

        }

        public static String toTitleCase(final String givenString) {
            if (givenString == null || "".equals(givenString)) {
                return "";
            }
            final String[] arr = givenString.split(" ");
            final StringBuffer sb = new StringBuffer();

            for (int i = 0; i < arr.length; i++) {
                sb.append(Character.toUpperCase(arr[i].charAt(0)))
                        .append(arr[i].substring(1)).append(" ");
            }
            return sb.toString().trim();
        }

        @Override
        public ItemStack getValue() {
            return value;
        }

        @Override
        public JSONHoverAction<ItemStack> setValue(final ItemStack newValue) {
            value = newValue;
            return this;
        }

        @Override
        public String getValueString() {
            final String material = value.getType().toString().toLowerCase();
            final ItemMeta itemMeta = value.getItemMeta();
            final Integer damage = itemMeta instanceof Damageable ? ((Damageable) itemMeta).getDamage() : null;

            JsonObject json = new JsonObject();
            json.addProperty("id", "minecraft:"+material);
            json.addProperty("Damage", damage);
            json.addProperty("Count", value.getAmount());

            JsonObject jsonTag = new JsonObject();

            JsonObject jsonDisplay = new JsonObject();
            JsonObject jsonName = new JsonObject();
            jsonName.addProperty("text", itemMeta.getDisplayName() != null && !itemMeta.getDisplayName().equals("") ? itemMeta
                    .getDisplayName() : toTitleCase(value.getType().toString().toLowerCase()));
            jsonDisplay.addProperty("Name", jsonName.toString());

            if (itemMeta.hasLore()) {
                JsonArray jsonLore = new JsonArray();

                for (final String lore : itemMeta.getLore()) {
                    JsonObject jsonLoreItem = new JsonObject();
                    jsonLoreItem.addProperty("text", lore);
                    jsonLore.add(jsonLoreItem.toString());
                }

                jsonDisplay.add("Lore", jsonLore);
            }

            jsonTag.add("display", jsonDisplay);

            if (itemMeta.hasEnchants()) {
                JsonArray jsonEnchantments = new JsonArray();

                for (final Enchantment ench : itemMeta.getEnchants().keySet()) {
                    JsonObject enchantment = new JsonObject();
                    enchantment.addProperty("lvl", itemMeta.getEnchants().get(ench));
                    enchantment.addProperty("id", ench.getKey().toString());
                    jsonEnchantments.add(enchantment);
                }

                jsonTag.add("Enchantments", jsonEnchantments);
            }

            json.add("tag", jsonTag);

            return json.toString();
        }

        @Override
        public String getActionName() {
            return NAME;
        }
    }

}
