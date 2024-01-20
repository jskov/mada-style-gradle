package dk.mada.style.configurators;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.quality.CheckstyleExtension;

import dk.mada.style.config.ConfigFileExtractor;
import dk.mada.style.config.PluginConfiguration.CheckstyleConfiguration;

/**
 * Configures Checkstyle with preferences.
 */
public class CheckstyleConfigurator {
    /** The gradle project. */
    private final Project project;
    /** The gradle logger. */
    private final Logger logger;
    /** The checkstyle configuration. */
    private final CheckstyleConfiguration checkstyleConfig;
    /** The default configuration file, shipped with this plugin. */
    private final Path defaultConfigFile;

    /**
     * Creates new instance.
     *
     * @param project          the gradle project
     * @param checkstyleConfig the checkstyle configuration
     * @param configExtractor  the configuration extractor
     */
    public CheckstyleConfigurator(Project project, CheckstyleConfiguration checkstyleConfig, ConfigFileExtractor configExtractor) {
        this.project = project;
        this.checkstyleConfig = checkstyleConfig;
        this.logger = project.getLogger();

        defaultConfigFile = configExtractor.getLocalConfigFile("checkstyle/checkstyle-mada.xml");
    }

    /**
     * Configures the checkstyle extension.
     *
     * @param ce the checkstyle extension
     */
    public void configure(CheckstyleExtension ce) {
        logger.info("Checkstyle config {}", getActiveConfigfile());

        ce.setIgnoreFailures(checkstyleConfig.ignoreFailures());
        ce.setConfigFile(getActiveConfigfile().toFile());
        String toolVersion = checkstyleConfig.toolVersion();
        if (toolVersion != null) {
            ce.setToolVersion(toolVersion);
        }
        if (checkstyleConfig.ignoreTestSource()) {
            project.getTasks().named("checkstyleTest", t -> t.setOnlyIf("disabled by mada style", ta -> false));
        }
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
