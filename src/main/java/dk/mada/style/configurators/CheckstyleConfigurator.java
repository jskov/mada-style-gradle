package dk.mada.style.configurators;

import java.nio.file.Path;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.tasks.TaskContainer;

import dk.mada.style.config.ConfigFileExtractor;
import dk.mada.style.config.PluginConfiguration.CheckstyleConfiguration;

/**
 * Configures Checkstyle with preferences.
 */
public class CheckstyleConfigurator {
    /** The default configuration resource path. */
    private static final String CHECKSTYLE_CHECKSTYLE_MADA_XML = "checkstyle/checkstyle-mada.xml";
    /** The gradle project. */
    private final Project project;
    /** The gradle logger. */
    private final Logger logger;
    /** The checkstyle configuration. */
    private final CheckstyleConfiguration checkstyleConfig;
    /** The default configuration file, shipped with this plugin. */
    private final Path defaultConfigFile;
    /** The default file extractor. */
    private final ConfigFileExtractor configExtractor;

    /**
     * Creates new instance.
     *
     * @param project          the gradle project
     * @param checkstyleConfig the checkstyle configuration
     * @param configExtractor  the configuration extractor
     */
    public CheckstyleConfigurator(Project project, CheckstyleConfiguration checkstyleConfig, ConfigFileExtractor configExtractor) {
        this.logger = project.getLogger();
        this.project = project;
        this.checkstyleConfig = checkstyleConfig;
        this.configExtractor = configExtractor;

        defaultConfigFile = configExtractor.getLocalConfigFileFromResource(CHECKSTYLE_CHECKSTYLE_MADA_XML);
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
        TaskContainer taskContainer = project.getTasks();
        if (checkstyleConfig.ignoreTestSource()) {
            taskContainer.named("checkstyleTest", this::disableTask);
        }

        taskContainer.withType(Checkstyle.class, t -> {
            if (checkstyleConfig.ignoreGeneratedSource() && t.getName().endsWith("Apt")) {
                disableTask(t);
            } else {
                t.setExcludes(checkstyleConfig.excludes());
                t.setIncludes(checkstyleConfig.includes());
            }
        });
    }

    private void disableTask(Task t) {
        t.setOnlyIf("disabled by mada style", ta -> false);
    }

    private Path getActiveConfigfile() {
        String configPath = checkstyleConfig.configPath();
        if (configPath != null) {
            return configExtractor.getLocalFileFromConfigPath(configPath);
        } else {
            return defaultConfigFile;
        }
    }
}
