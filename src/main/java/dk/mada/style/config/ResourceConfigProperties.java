package dk.mada.style.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads resource configuration properties.
 */
public final class ResourceConfigProperties {
    private ResourceConfigProperties() {
    }

    /**
     * Reads configuration properties from a named resource.
     *
     * @param name the resource name
     * @return the read properties
     */
    public static Properties readConfigProperties(String name) {
        try (InputStream is = ConfigFileExtractor.class.getResourceAsStream(name)) {
            if (is == null) {
                throw new IllegalStateException("Failed to find resource " + name);
            }
            Properties p = new Properties();
            p.load(is);
            return p;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read properties from resource " + name, e);
        }
    }
}
