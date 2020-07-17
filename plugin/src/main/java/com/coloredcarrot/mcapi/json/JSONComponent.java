package com.coloredcarrot.mcapi.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cz.boosik.boosCooldown.BoosCoolDown;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

/**
 * All rights reserved.
 *
 * @author ColoredCarrot
 */
public class JSONComponent
        extends JSONComponentSimple {

    private JSONHoverAction<?> hoverAction;
    private JSONClickAction<?> clickAction;

    public JSONComponent(final String text) {
        super(text);
    }

    /**
     * Constructs a new JSONComponent with all the attributes this one has.
     */
    @Override
    public JSONComponent clone() {

        return (JSONComponent) new JSONComponent(getText())
                .setHoverAction(hoverAction)
                .setClickAction(clickAction)
                .setColor(getColor())
                .setBold(isBold())
                .setItalic(isItalic())
                .setStrikethrough(isStrikethrough())
                .setUnderlined(isUnderlined())
                .setObfuscated(isObfuscated());

    }

    /**
     * Generates the raw json message matching this JSONComponent.
     * Generally speaking, you should not have a use for this method as get() calls it as well.
     *
     * @return (JSONComponent) - this JSONComponent Object, for chaining.
     * @see JSONComponent#get()
     */
    @Override
    public JSONComponent generate() {

        super.generate();

        JsonObject json = new JsonParser().parse(generatedJSON).getAsJsonObject();

        if (hoverAction != null) {

            JsonObject jsonHover = new JsonObject();
            jsonHover.addProperty("action", hoverAction.getActionName());
            jsonHover.addProperty("value", hoverAction.getValueString());
            json.add("hoverEvent", jsonHover);
        }

        if (clickAction != null) {
            JsonObject jsonClick = new JsonObject();
            jsonClick.addProperty("action", clickAction.getActionName());
            jsonClick.addProperty("value", clickAction.getValueString());
            json.add("clickEvent", jsonClick);
        }

        generatedJSON = json.toString();

        return this;

    }

    /**
     * Gets the hover action for this JSONComponent.
     *
     * @return (JSONHoverAction: ?) - the hover action.
     */
    public JSONHoverAction<?> getHoverAction() {
        return hoverAction;
    }

    /**
     * Sets the hover action for this JSONComponent.
     *
     * @param hoverAction (JSONHoverAction: ?) - the hover action
     * @return (JSONComponent) - this JSONComponent Object, for chaining.
     * @see #getHoverAction()
     */
    public JSONComponent setHoverAction(final JSONHoverAction<?> hoverAction) {
        if (hoverAction == null) {
            throw new IllegalArgumentException("hoverAction cannot be null!");
        }
        this.hoverAction = hoverAction;
        super.generated = false;
        return this;
    }

    /**
     * Gets the click action for this JSONComponent.
     *
     * @return (JSONClickAction: ?) - the click action.
     */
    public JSONClickAction<?> getClickAction() {
        return clickAction;
    }

    /**
     * Sets the click action for this JSONComponent.
     *
     * @param clickAction (JSONClickAction: ?) - the click action
     * @return (JSONComponent) - this JSONComponent Object, for chaining.
     * @see #getClickAction()
     */
    public JSONComponent setClickAction(final JSONClickAction<?> clickAction) {
        if (clickAction == null) {
            throw new IllegalArgumentException("clickAction cannot be null!");
        }
        this.clickAction = clickAction;
        resetGenerationStatus();
        return this;
    }

}
