package cz.boosik.boosCooldown.Managers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import util.boosChat;

public class BoosItemCostManager {

    private static boolean payItemForCommand(Player player,
                                             String originalCommand, String item, int count) {
        Material material = Material.getMaterial(item);
        Inventory inventory = player.getInventory();
        Boolean trans = false;
        if (inventory.contains(material, count)) {
            ItemStack itemstack = new ItemStack(material, count);
            inventory.removeItem(itemstack);
            trans = true;
        }
        if (trans) {
            String msg = String.format(
                    BoosConfigManager.getPaidItemsForCommandMessage(), count
                            + " " + item);
            msg = msg.replaceAll("&command&", originalCommand);
            boosChat.sendMessageToPlayer(player, msg);
            return true;
        } else {
            return false;
        }
    }

    public static void payItemForCommand(PlayerCommandPreprocessEvent event,
                                         Player player, String regexCommand, String originalCommand,
                                         String item, int count) {
        if (count > 0) {
            if (!player.hasPermission("booscooldowns.noitemcost")
                    && !player.hasPermission("booscooldowns.noitemcost."
                    + originalCommand)) {
                if (!payItemForCommand(player, originalCommand,
                        item, count)) {
                    BoosCoolDownManager.cancelCooldown(player, regexCommand);
                    event.setCancelled(true);
                }
            }
        }
    }

    public static boolean has(Player player, String item, int count) {
        if (item.equals("")) {
            return true;
        }
        if (count <= 0) {
            return true;
        }
        Material material = Material.getMaterial(item);
        Inventory inventory = player.getInventory();
        return inventory.contains(material, count);
    }
}
