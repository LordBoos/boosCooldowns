package com.coloredcarrot.mcapi.json;

/**
 * All rights reserved.
 *
 * @author ColoredCarrot
 */
public class JSONComponent
        extends JSONComponentSimple {

    private JSONHoverAction<?> hoverAction;
    private JSONClickAction<?> clickAction;

    public JSONComponent(String text) {
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

        generatedJSON = generatedJSON.substring(0, generatedJSON.length() - 1);

        if (hoverAction != null) {
            generatedJSON += ",\"hoverEvent\":{\"action\":\"" + hoverAction.getActionName() + "\",\"value\":\"" + hoverAction.getValueString() + "}";
        }

        if (clickAction != null) {
            generatedJSON += ",\"clickEvent\":{\"action\":\"" + clickAction.getActionName() + "\",\"value\":" + clickAction.getValueString() + "}";
        }

        generatedJSON += "}";

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
    public JSONComponent setHoverAction(JSONHoverAction<?> hoverAction) {
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
    public JSONComponent setClickAction(JSONClickAction<?> clickAction) {
        if (clickAction == null) {
            throw new IllegalArgumentException("clickAction cannot be null!");
        }
        this.clickAction = clickAction;
        resetGenerationStatus();
        return this;
    }

}
