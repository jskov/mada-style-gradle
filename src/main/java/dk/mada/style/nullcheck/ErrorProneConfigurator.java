package dk.mada.style.nullcheck;

import java.nio.file.Path;

import org.gradle.api.file.RegularFile;
import org.gradle.api.logging.Logger;

import com.diffplug.gradle.spotless.JavaExtension;
import com.diffplug.gradle.spotless.SpotlessExtension;

import dk.mada.style.config.ConfigFileExtractor;

/**
 * Configures Spotless with formatter preferences.
 */
public class ErrorProneConfigurator {
    /** The gradle logger. */
    private final Logger logger;
    /** The null-checker configuration. */
    private final NullcheckerConfig nullcheckerConfig;
    /** The default configuration file, shipped with this plugin. */
    private final Path defaultConfigFile;

    /**
     * Creates new instance.
     *
     * @param logger          the gradle logger
     * @param configExtractor the configuration extractor
     * @param nullcheckerConfig the formatter configuration
     */
    public ErrorProneConfigurator(Logger logger, ConfigFileExtractor configExtractor, NullcheckerConfig nullcheckerConfig) {
        this.logger = logger;
        this.nullcheckerConfig = nullcheckerConfig;

        defaultConfigFile = configExtractor.getLocalConfigFile("spotless/eclipse-formatter-mada.xml");
    }

    /**
     * Configures the spotless extension.
     *
     * @param se the spotless extension
     */
}
