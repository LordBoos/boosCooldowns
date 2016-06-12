package nms;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * All rights reserved.
 *
 * @author ColoredCarrot
 */
public class NMSHook
        implements INMSHook {

    private String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    private Class<?> chatSerializer = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent$ChatSerializer");
    private Class<?> chatComponent = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent");
    private Class<?> packet = Class.forName("net.minecraft.server." + version + ".PacketPlayOutChat");

    public NMSHook() throws ClassNotFoundException {
    }

    public void sendJSON(String json, Player player) {
        try {
            Object nmsPlayer = getNmsPlayer(player);
            Object connection = getConnection(nmsPlayer);

            Constructor constructor = packet.getConstructor(chatComponent);

            Object text = getText(json);

            Object packetFinal = constructor.newInstance(text);

            sendPacket(packetFinal, text, connection);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendActionBar(String json, Player player) {
        try {
            Object nmsPlayer = getNmsPlayer(player);
            Object connection = getConnection(nmsPlayer);

            Constructor constructor = packet.getConstructor(chatComponent, byte.class);

            Object text = getText(json);

            Object packetFinal = constructor.newInstance(text, (byte) 2);

            sendPacket(packetFinal, text, connection);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendPacket(Object packetFinal, Object text, Object connection) throws IllegalAccessException, NoSuchFieldException,
            ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        Field field = packetFinal.getClass().getDeclaredField("a");
        field.setAccessible(true);
        field.set(packetFinal, text);
        connection
                .getClass()
                .getMethod("sendPacket", Class.forName("net.minecraft.server." + version + ".Packet"))
                .invoke(connection, packetFinal);
    }

    private Object getNmsPlayer(Player player) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return player.getClass().getMethod("getHandle").invoke(player);
    }

    private Object getConnection(Object nmsPlayer) throws NoSuchFieldException, IllegalAccessException {
        return nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
    }

    private Object getText(String json) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return chatSerializer.getMethod("a", String.class).invoke(chatSerializer, json);
    }
}
