package dk.mada.style.configurators;

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
    /** The default configuration resource path. */
    private static final String SPOTLESS_ECLIPSE_FORMATTER_MADA_XML = "spotless/eclipse-formatter-mada.xml";
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
     * @param formatterConfig the plugin configuration
     * @param configExtractor the configuration extractor
     */
    public SpotlessConfigurator(Logger logger, FormatterConfiguration formatterConfig, ConfigFileExtractor configExtractor) {
        this.logger = logger;
        this.formatterConfig = formatterConfig;

        defaultConfigFile = configExtractor.getLocalConfigFileFromResource(SPOTLESS_ECLIPSE_FORMATTER_MADA_XML);
    }

    /**
     * Configures the spotless extension.
     *
     * @param se the spotless extension
     */
    public void configure(SpotlessExtension se) {
        logger.info("dk.mada.style configure spotless");

        se.java(this::configureJava);
    }

    private void configureJava(JavaExtension je) {
        String include = formatterConfig.include();
        String exclude = formatterConfig.exclude();
        Path configFile = getActiveConfigfile();
        logger.debug("Spotless java config: {}", configFile);
        logger.debug("Spotless java include:{} exclude:{}", include, exclude);

        je.target(include);
        je.targetExclude(exclude);

        je.eclipse().configFile(configFile);
        je.formatAnnotations(); // Note that this *must* come after the java formatter configuration
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
