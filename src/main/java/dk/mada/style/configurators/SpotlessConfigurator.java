package dk.mada.style.configurators;

import com.diffplug.gradle.spotless.JavaExtension;
import com.diffplug.gradle.spotless.SpotlessExtension;
import dk.mada.style.config.PluginConfiguration.FormatterConfiguration;
import java.util.List;
import org.gradle.api.logging.Logger;

/**
 * Configures Spotless with formatter preferences.
 */
public class SpotlessConfigurator {
    /** The gradle logger. */
    private final Logger logger;
    /** The formatter configuration. */
    private final FormatterConfiguration formatterConfig;

    /**
     * Creates new instance.
     *
     * @param logger          the gradle logger
     * @param formatterConfig the plugin configuration
     */
    public SpotlessConfigurator(Logger logger, FormatterConfiguration formatterConfig) {
        this.logger = logger;
        this.formatterConfig = formatterConfig;
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
        List<String> include = formatterConfig.includes();
        List<String> exclude = formatterConfig.excludes();
        logger.debug("Spotless java include:{} exclude:{}", include, exclude);

        je.target(include);
        je.targetExclude(exclude);

        je.palantirJavaFormat();
        je.formatAnnotations(); // Note that this *must* come after the java formatter configuration
    }
}
