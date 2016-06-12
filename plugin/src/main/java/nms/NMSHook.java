package nms;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * All rights reserved.
 *
 * @author ColoredCarrot
 */
public class NMSHook
        implements INMSHook {

    public void sendJSON(String json, Player player) {
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object connection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            Class<?> chatSerializer = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent$ChatSerializer");
            Class<?> chatComponent = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent");
            Class<?> packet = Class.forName("net.minecraft.server." + version + ".PacketPlayOutChat");
            Constructor constructor = packet.getConstructor(chatComponent);

            Object text = chatSerializer.getMethod("a", String.class).invoke(chatSerializer, json);
            Object packetFinal = constructor.newInstance(text);

            Field field = packetFinal.getClass().getDeclaredField("a");
            field.setAccessible(true);
            field.set(packetFinal, text);
            connection
                    .getClass()
                    .getMethod("sendPacket", Class.forName("net.minecraft.server." + version + ".Packet"))
                    .invoke(connection, packetFinal);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendActionBar(String json, Player player) {
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object connection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            Class<?> chatSerializer = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent$ChatSerializer");
            Class<?> chatComponent = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent");
            Class<?> packet = Class.forName("net.minecraft.server." + version + ".PacketPlayOutChat");
            Constructor constructor = packet.getConstructor(chatComponent, byte.class);

            Object text = chatSerializer.getMethod("a", String.class).invoke(chatSerializer, json);
            Object packetFinal = constructor.newInstance(text, (byte) 2);

            Field field = packetFinal.getClass().getDeclaredField("a");
            field.setAccessible(true);
            field.set(packetFinal, text);
            connection
                    .getClass()
                    .getMethod("sendPacket", Class.forName("net.minecraft.server." + version + ".Packet"))
                    .invoke(connection, packetFinal);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
