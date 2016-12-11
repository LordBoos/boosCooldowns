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

    private static boolean payItemForCommand(Player player,
                                             String originalCommand, String item, int count, String name, List<String> lore, List<String> enchants) {

        ItemStack itemStack = createItemStack(item, count, name, lore, enchants);
        ItemStack itemStackSingle = createItemStack(item, 1, name, lore, enchants);

        Inventory inventory = player.getInventory();
        Boolean trans = false;
        if (inventory.containsAtLeast(itemStackSingle, count)) {
            inventory.removeItem(itemStack);
            trans = true;
        }
        if (trans) {
            String msg = String.format(
                    BoosConfigManager.getPaidItemsForCommandMessage(), "");
            JSON json = getItemStackJson(1, item, count, name, lore, enchants);
            msg = msg.replaceAll("&command&", originalCommand);
            BoosChat.sendMessageToPlayer(player, msg);
            json.send(player);
            return true;
        } else {
            return false;
        }
    }

    public static void payItemForCommand(PlayerCommandPreprocessEvent event,
                                         Player player, String regexCommand, String originalCommand,
                                         String item, int count, String name, List<String> lore, List<String> enchants) {
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

    public static boolean has(Player player, String item, int count, String name, List<String> lore, List<String> enchants) {
        if (item.equals("")) {
            return true;
        }
        if (count <= 0) {
            return true;
        }
        ItemStack itemStack = createItemStack(item, 1, name, lore, enchants);
        Inventory inventory = player.getInventory();
        return inventory.containsAtLeast(itemStack, count);
    }

    public static ItemStack createItemStack(String item, int count, String name, List<String> lore, List<String> enchants) {
        ItemStack itemStack = new ItemStack(Material.getMaterial(item), count);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (name != null) {
            itemMeta.setDisplayName(name);
        }
        if (!lore.isEmpty()) {
            itemMeta.setLore(lore);
        }
        if (!enchants.isEmpty()) {
            for (String enchantString : enchants) {
                String[] enchantArray = enchantString.split(",");
                Enchantment enchant = Enchantment.getByName(enchantArray[0]);
                itemMeta.addEnchant(enchant, Integer.valueOf(enchantArray[1]), true);
            }
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static JSON getItemStackJson(int indent, String item, int count, String name, List<String> lore, List<String> enchants) {
        ItemStack itemStack = createItemStack(item, count, name, lore, enchants);
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
        JSONComponent comp1 = new JSONComponent(indentation);
        JSONComponent comp2 = new JSONComponent(String.valueOf(count) + "x ");
        comp2.setColor(JSONColor.YELLOW);
        JSONComponent comp3 = new JSONComponent("[");
        comp3.setColor(itemColor);
        JSONComponent comp4 = new JSONComponent(name == null || name.equals("") ? toTitleCase(itemStack
                .getType()
                .toString()
                .toLowerCase()) : name);
        comp4.setColor(itemColor);
        JSONComponent comp5 = new JSONComponent("]");
        comp5.setColor(itemColor);
        comp3.setHoverAction(new JSONHoverAction.ShowItemStack(itemStack));
        comp4.setHoverAction(new JSONHoverAction.ShowItemStack(itemStack));
        comp5.setHoverAction(new JSONHoverAction.ShowItemStack(itemStack));
        return new JSON(comp1, comp2, comp3, comp4, comp5);
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
}
