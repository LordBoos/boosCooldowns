package com.coloredcarrot.mcapi.json;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

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
    public T getValue();

    /**
     * Sets the value of this JSONHoverAction.
     *
     * @param newValue (?) - the new value
     * @return (JSONHoverAction: ?) - this JSONHoverAction Object, for chaining.
     */
    public JSONHoverAction<T> setValue(T newValue);

    /**
     * Gets the value associated with this JSONHoverAction transformed to a String.
     *
     * @return (String) - the value as a String.
     * @see #getValue()
     */
    public String getValueString();

    /**
     * Gets the action name associated with this JSONHoverAction.
     * Example: "show_text"
     *
     * @return (String) - the action name.
     */
    public String getActionName();

    /**
     * Shows some JSON-formed text when hovering over the text in the chat.
     */
    public class ShowText
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
        public ShowText(JSON value) {

            this.value = value;

        }

        @Override
        public JSON getValue() {
            return value;
        }

        @Override
        public JSONHoverAction<JSON> setValue(JSON newValue) {
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
    public class ShowStringText
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
        public ShowStringText(String value) {

            this.value = value;

        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public JSONHoverAction<String> setValue(String newValue) {
            value = newValue;
            return this;
        }

        @Override
        public String getValueString() {
            return value + "\"";
        }

        @Override
        public String getActionName() {
            return NAME;
        }

    }

    /**
     * Shows an item when hovering over the text in the chat.
     */
    public class ShowItem
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
        public ShowItem(Material value) {

            this.value = value;

        }

        @Override
        public Material getValue() {
            return value;
        }

        @Override
        public JSONHoverAction<Material> setValue(Material newValue) {
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
        public ShowItemStack(ItemStack value) {

            this.value = value;

        }

        public static String toTitleCase(String givenString) {
            if (givenString == null || "".equals(givenString)) {
                return "";
            }
            String[] arr = givenString.split(" ");
            StringBuffer sb = new StringBuffer();

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
        public JSONHoverAction<ItemStack> setValue(ItemStack newValue) {
            value = newValue;
            return this;
        }

        @Override
        public String getValueString() {
            String material = value.getData().getItemType().toString().toLowerCase();
            String value2 = "{id:\\\"minecraft:" + material + "\\\",Damage:" + value.getDurability() + ",Count:" + value.getAmount() + ",tag:{";

            if (value.getItemMeta().hasEnchants()) {
                value2 += "ench:[";
                int i = 0;
                int size = value.getItemMeta().getEnchants().keySet().size();
                for (Enchantment ench : value.getItemMeta().getEnchants().keySet()) {
                    if (i + 1 == size) {
                        value2 += "{lvl:" + value.getItemMeta().getEnchants().get(ench) + "s,id:" + ench.getKey() + "s}";
                    } else {
                        value2 += "{lvl:" + value.getItemMeta().getEnchants().get(ench) + "s,id:" + ench.getKey() + "s},";
                    }
                    i++;
                }
                value2 += "],";
            }

            value2 += "display:{Name:\\\"" + (value
                    .getItemMeta()
                    .getDisplayName() != null && value
                    .getItemMeta()
                    .getDisplayName() != "" ? value
                    .getItemMeta()
                    .getDisplayName() : toTitleCase(value.getType().toString().toLowerCase())) + "\\\"";

            if (value.getItemMeta().hasLore()) {
                value2 += ",Lore:[";
                for (String lore : value.getItemMeta().getLore()) {
                    value2 = value2 + (value.getItemMeta().getLore().size() == 1 || value
                            .getItemMeta()
                            .getLore()
                            .get(value
                                    .getItemMeta()
                                    .getLore()
                                    .size() - 1) == lore ? ("\\\"" + lore + "\\\"") : ("\\\"" + lore + "\\\","));
                }

                value2 += "]";
            }

            value2 += "}}}\"";

            return value2;
        }

        @Override
        public String getActionName() {
            return NAME;
        }
    }

}
