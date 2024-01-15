package dk.mada.style.nullcheck;

import java.nio.file.Path;

import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.logging.Logger;

import com.diffplug.gradle.spotless.JavaExtension;
import com.diffplug.gradle.spotless.SpotlessExtension;

import dk.mada.style.config.ConfigFileExtractor;

/**
 * Configures Spotless with formatter preferences.
 */
public class ErrorProneConfigurator {
    /** The gradle project. */
    private final Project project;
    /** The gradle logger. */
    private final Logger logger;
    /** The null-checker configuration. */
    private final NullcheckerConfig nullcheckerConfig;
    /** The default configuration file, shipped with this plugin. */
//    private final Path defaultConfigFile;

    /**
     * Creates new instance.
     *
     * @param logger          the gradle logger
     * @param configExtractor the configuration extractor
     * @param nullcheckerConfig the formatter configuration
     */
    public ErrorProneConfigurator(Project project, ConfigFileExtractor configExtractor, NullcheckerConfig nullcheckerConfig) {
        this.project = project;
        this.logger = project.getLogger();
        this.nullcheckerConfig = nullcheckerConfig;
    }

    /**
     * Configures the ErrorProne plugin.
     */
    public void configure() {

        project.getDependencies().add("annotationProcessor", "com.uber.nullaway:nullaway");
        project.getDependencies().add("errorprone", "com.google.errorprone:error_prone_core");
        
    }
}
