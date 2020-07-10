package cz.boosik.boosCooldown.Managers;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.coloredcarrot.mcapi.json.JSON;
import com.coloredcarrot.mcapi.json.JSONColor;
import com.coloredcarrot.mcapi.json.JSONComponent;
import com.coloredcarrot.mcapi.json.JSONHoverAction;
import util.BoosChat;

public class BoosItemCostManager {

    private static boolean payItemForCommand(
            final Player player,
            final String originalCommand, final String item, final int count, final String name, final List<String> lore, final List<String> enchants) {

        final ItemStack itemStack = createItemStack(item, count, name, lore, enchants);
        final ItemStack itemStackSingle = createItemStack(item, 1, name, lore, enchants);

        final Inventory inventory = player.getInventory();
        Boolean trans = false;
        if (inventory.containsAtLeast(itemStackSingle, count)) {
            inventory.removeItem(itemStack);
            trans = true;
        }
        if (trans) {
            String msg = String.format(
                    BoosConfigManager.getPaidItemsForCommandMessage(), "");
            final JSON json = getItemStackJson(1, item, count, name, lore, enchants);
            msg = msg.replaceAll("&command&", originalCommand);
            BoosChat.sendMessageToPlayer(player, msg);
            json.send(player);
            return true;
        } else {
            return false;
        }
    }

    public static void payItemForCommand(
            final PlayerCommandPreprocessEvent event,
            final Player player, final String regexCommand, final String originalCommand,
            final String item, final int count, final String name, final List<String> lore, final List<String> enchants) {
        if (count > 0) {
            if (!player.hasPermission("booscooldowns.noitemcost")
                    && !player.hasPermission("booscooldowns.noitemcost."
                    + originalCommand)) {
                if (!payItemForCommand(player, originalCommand,
                        item, count, name, lore, enchants)) {
                    BoosCoolDownManager.cancelCooldown(player, regexCommand);
                    event.setCancelled(true);
                }
            }
        }
    }

    public static boolean has(final Player player, final String item, final int count, final String name, final List<String> lore, final List<String> enchants) {
        if (item.equals("")) {
            return true;
        }
        if (count <= 0) {
            return true;
        }
        final ItemStack itemStack = createItemStack(item, 1, name, lore, enchants);
        final Inventory inventory = player.getInventory();
        return inventory.containsAtLeast(itemStack, count);
    }

    public static ItemStack createItemStack(final String item, final int count, final String name, final List<String> lore, final List<String> enchants) {
        final ItemStack itemStack = new ItemStack(Material.getMaterial(item), count);
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (name != null) {
            itemMeta.setDisplayName(name);
        }
        if (!lore.isEmpty()) {
            itemMeta.setLore(lore);
        }
        if (!enchants.isEmpty()) {
            for (final String enchantString : enchants) {
                final String[] enchantArray = enchantString.split(",");
                final Enchantment enchant = Enchantment.getByName(enchantArray[0]);
                itemMeta.addEnchant(enchant, Integer.valueOf(enchantArray[1]), true);
            }
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static JSON getItemStackJson(final int indent, final String item, final int count, final String name, final List<String> lore, final List<String> enchants) {
        final ItemStack itemStack = createItemStack(item, count, name, lore, enchants);
        JSONColor itemColor;
        if (itemStack.getItemMeta().hasEnchants()) {
            itemColor = JSONColor.fromString("&b");
        } else {
            itemColor = JSONColor.fromString("&f");
        }
        itemColor = name != null && (name.startsWith("&") || name.startsWith("§")) ? JSONColor.fromString(name.substring(0, 2)) : itemColor;
        String indentation = "";
        for (int i = 0; i < indent; i++) {
            indentation += "    ";
        }
        final JSONComponent comp1 = new JSONComponent(indentation);
        final JSONComponent comp2 = new JSONComponent(count + "x ");
        comp2.setColor(JSONColor.YELLOW);
        final JSONComponent comp3 = new JSONComponent("[");
        comp3.setColor(itemColor);
        final JSONComponent comp4 = new JSONComponent(name == null || name.equals("") ? toTitleCase(itemStack
                .getType()
                .toString()
                .toLowerCase()) : name);
        comp4.setColor(itemColor);
        final JSONComponent comp5 = new JSONComponent("]");
        comp5.setColor(itemColor);
        comp3.setHoverAction(new JSONHoverAction.ShowItemStack(itemStack));
        comp4.setHoverAction(new JSONHoverAction.ShowItemStack(itemStack));
        comp5.setHoverAction(new JSONHoverAction.ShowItemStack(itemStack));
        return new JSON(comp1, comp2, comp3, comp4, comp5);
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
}
