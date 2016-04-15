package nms;

/**
 * All rights reserved.
 *
 * @author ColoredCarrot
 */
public class NMSSetupResponse {

    private String version;
    private boolean compatible;

    protected NMSSetupResponse(String version, boolean compatible) {
        this.version = version;
        this.compatible = compatible;
    }

    /**
     * Gets the found server version in form of "v1_9_R1".
     *
     * @return (String) - the server version or null if it's not found.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets if the found server version is compatible with this API.
     *
     * @return
     */
    public boolean isCompatible() {
        return compatible;
    }

}
