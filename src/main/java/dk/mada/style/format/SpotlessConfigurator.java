package dk.mada.style.format;

import java.nio.file.Path;

import org.gradle.api.file.RegularFile;
import org.gradle.api.logging.Logger;

import com.diffplug.gradle.spotless.JavaExtension;
import com.diffplug.gradle.spotless.SpotlessExtension;

import dk.mada.style.config.ConfigFileExtractor;

/**
 * Configures Spotless with formatter preferences.
 */
public class SpotlessConfigurator {
    /** The gradle logger. */
    private final Logger logger;
    /** The formatter configuration. */
    private final FormatterConfig formatterConfig;
    /** The default configuration file, shipped with this plugin. */
    private final Path defaultConfigFile;

    /**
     * Creates new instance.
     *
     * @param logger the gradle logger
     * @param configExtractor the configuration extractor
     * @param formatterConfig the formatter configuration
     */
    public SpotlessConfigurator(Logger logger, ConfigFileExtractor configExtractor, FormatterConfig formatterConfig) {
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
        String include = formatterConfig.getInclude().get();
        String exclude = formatterConfig.getExclude().get();
        Path configFile = getActiveConfigfile();
        logger.info("Spotless java config: {}", configFile);
        logger.info("Spotless java include:{} exclude:{}", include, exclude);

        je.target(include);
        je.targetExclude(exclude);
        je.formatAnnotations();

        je.eclipse().configFile(configFile);
    }

    private Path getActiveConfigfile() {
        Path useConfig;
        RegularFile eclipseConfig = formatterConfig.getEclipseConfig().getOrNull();
        if (eclipseConfig == null) {
            useConfig = defaultConfigFile;
        } else {
            useConfig = eclipseConfig.getAsFile().toPath();
        }
        return useConfig;
    }
}
