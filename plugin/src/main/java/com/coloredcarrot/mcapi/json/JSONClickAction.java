package com.coloredcarrot.mcapi.json;

/**
 * Represents a click action.
 * All rights reserved.
 *
 * @param <T> - the Type of the value associated with the class implementing this interface
 * @author ColoredCarrot
 * @see {@link #getValue()}, {@link #setValue(Object)}
 */
public interface JSONClickAction<T> {

    /**
     * Gets the value associated with this JSONClickAction.
     *
     * @return (?) - the value.
     * @see #setValue(Object)
     */
    public T getValue();

    /**
     * Sets the value of this JSONClickAction.
     *
     * @param newValue (?) - the new value
     * @return (JSONHoverAction: ?) - this JSONClickAction Object, for chaining.
     */
    public JSONClickAction<T> setValue(T newValue);

    /**
     * Gets the value associated with this JSONClickAction transformed to a String.
     *
     * @return (String) - the value as a String.
     * @see #getValue()
     */
    public String getValueString();

    /**
     * Gets the action name associated with this JSONClickAction.
     * Example: "run_command"
     *
     * @return (String) - the action name.
     */
    public String getActionName();

    /**
     * Runs a command as the player who clicks on the text in the chat.
     */
    public class RunCommand
            implements JSONClickAction<String> {

        /**
         * The action name
         *
         * @see #getActionName()
         */
        public static final String NAME = "run_command";

        private String value;

        /**
         * Constructs a {@link JSONClickAction.RunCommand}.
         *
         * @param value (String) - the value associated with this JSONClickAction
         */
        public RunCommand(String value) {

            this.value = value;

        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public JSONClickAction<String> setValue(String newValue) {
            value = newValue;
            return this;
        }

        @Override
        public String getValueString() {
            return "\"" + value + "\"";
        }

        @Override
        public String getActionName() {
            return NAME;
        }

    }

    /**
     * Pastes a command in the chat of the player who clicks on the text in the chat.
     */
    public class SuggestCommand
            implements JSONClickAction<String> {

        /**
         * The action name
         *
         * @see #getActionName()
         */
        public static final String NAME = "suggest_command";

        private String value;

        /**
         * Constructs a {@link JSONClickAction.SuggestCommand}.
         *
         * @param value (String) - the value associated with this JSONClickAction
         */
        public SuggestCommand(String value) {

            this.value = value;

        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public JSONClickAction<String> setValue(String newValue) {
            value = newValue;
            return this;
        }

        @Override
        public String getValueString() {
            return "\"" + value + "\"";
        }

        @Override
        public String getActionName() {
            return NAME;
        }

    }

    /**
     * Opens a URL in the default browser of the player who clicks on the text in the chat.
     */
    public class OpenURL
            implements JSONClickAction<String> {

        /**
         * The action name
         *
         * @see #getActionName()
         */
        public static final String NAME = "open_url";

        private String value;

        /**
         * Constructs a {@link JSONClickAction.OpenURL}.
         *
         * @param value (String) - the value associated with this JSONClickAction
         */
        public OpenURL(String value) {

            this.value = value;

        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public JSONClickAction<String> setValue(String newValue) {
            value = newValue;
            return this;
        }

        @Override
        public String getValueString() {
            return ("\"" + value + "\"").replace(" ", "%20");
        }

        @Override
        public String getActionName() {
            return NAME;
        }

    }

}
