package dk.mada.style.checkstyle;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.quality.CheckstyleExtension;

import dk.mada.style.config.ConfigFileExtractor;
import dk.mada.style.config.PluginConfiguration.CheckstyleConfiguration;

/**
 * Configures Spotless with formatter preferences.
 */
public class CheckstyleConfigurator {
    /** The gradle logger. */
    private final Logger logger;
    /** The checkstyle configuration. */
    private final CheckstyleConfiguration checkstyleConfig;
    /** The default configuration file, shipped with this plugin. */
    private final Path defaultConfigFile;

    /**
     * Creates new instance.
     *
     * @param logger           the gradle logger
     * @param checkstyleConfig the checkstyle configuration
     * @param configExtractor  the configuration extractor
     */
    public CheckstyleConfigurator(Logger logger, CheckstyleConfiguration checkstyleConfig, ConfigFileExtractor configExtractor) {
        this.logger = logger;
        this.checkstyleConfig = checkstyleConfig;

        defaultConfigFile = configExtractor.getLocalConfigFile("checkstyle/checkstyle-mada.xml");
    }

    /**
     * Configures the checkstyle extension.
     *
     * @param ce the checkstyle extension
     */
    public void configure(CheckstyleExtension ce) {
        logger.lifecycle("dk.mada.style configure checkstype");
        logger.lifecycle("Using {}", getActiveConfigfile());
    }

    private Path getActiveConfigfile() {
        String configPath = checkstyleConfig.configPath();
        if (configPath != null) {
            return Paths.get(configPath);
        } else {
            return defaultConfigFile;
        }
    }
}
