package dk.mada.style.format;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.gradle.api.logging.Logger;

import com.diffplug.gradle.spotless.JavaExtension;
import com.diffplug.gradle.spotless.SpotlessExtension;

import dk.mada.style.config.ConfigFileExtractor;
import dk.mada.style.config.PluginConfiguration.FormatterConfiguration;

/**
 * Configures Spotless with formatter preferences.
 */
public class SpotlessConfigurator {
    /** The gradle logger. */
    private final Logger logger;
    /** The formatter configuration. */
    private final FormatterConfiguration formatterConfig;
    /** The default configuration file, shipped with this plugin. */
    private final Path defaultConfigFile;

    /**
     * Creates new instance.
     *
     * @param logger          the gradle logger
     * @param formatterConfig          the plugin configuration
     * @param configExtractor the configuration extractor
     */
    public SpotlessConfigurator(Logger logger, FormatterConfiguration formatterConfig, ConfigFileExtractor configExtractor) {
        this.logger = logger;
        this.formatterConfig = formatterConfig;

        defaultConfigFile = configExtractor.getLocalConfigFile("spotless/eclipse-formatter-mada.xml");
    }

    /**
     * Configures the spotless extension.
     *
     * @param se the spotless extension
     */
    public void configure(SpotlessExtension se) {
        se.java(this::configureJava);
    }

    private void configureJava(JavaExtension je) {
        String include = formatterConfig.include();
        String exclude = formatterConfig.exclude();
        Path configFile = getActiveConfigfile();
        logger.info("Spotless java config: {}", configFile);
        logger.info("Spotless java include:{} exclude:{}", include, exclude);

        je.target(include);
        je.targetExclude(exclude);
        je.formatAnnotations();

        je.eclipse().configFile(configFile);
    }

    private Path getActiveConfigfile() {
        String configPath = formatterConfig.eclipseConfigPath();
        if (configPath != null) {
            return Paths.get(configPath);
        } else {
            return defaultConfigFile;
        }
    }
}
