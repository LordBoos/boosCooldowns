package nms;

import org.bukkit.plugin.Plugin;

/**
 * All rights reserved.
 *
 * @author ColoredCarrot
 */
public class NMS {

    private static INMSHook hook;
    private static String version;
    private static boolean compatible = true;

    /**
     * Gets the server version and adjusts this API accordingly.
     *
     * @param pl (Plugin) - the instance of your plugin
     * @return (NMSSetupResponse) - the setup response.
     */
    public static NMSSetupResponse setup(Plugin pl) {

        String version;

        try {
            version = pl.getServer().getClass().getPackage().getName().replace('.', ',').split(",")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            return new NMSSetupResponse(null, false);
        }

        hook = new NMSHook();

        return new NMSSetupResponse(version, true);

    }

    /**
     * Gets the NMS hook, if NMS is hooked.
     *
     * @return (INMSHook) - the INMSHook.
     * @throws NMSNotHookedException if NMS is not hooked (by using {@link #setup(Plugin)})
     */
    public static INMSHook getHook()
            throws NMSNotHookedException {

        if (!compatible) {
            throw new NMSNotHookedException();
        }

        return hook;

    }

    /**
     * Gets the found server version.
     *
     * @return (String) - the server version in form of "v1_9_R1" or null
     */
    public static String getVersion() {
        return version;
    }

    /**
     * Checks whether this API is compatible with the found server version.
     *
     * @return
     */
    public static boolean isCompatibleVersionFound() {
        return compatible;
    }

}
