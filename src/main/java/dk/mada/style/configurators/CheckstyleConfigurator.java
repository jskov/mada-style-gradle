package dk.mada.style.configurators;

import dk.mada.style.config.ConfigFileExtractor;
import dk.mada.style.config.PluginConfiguration.CheckstyleConfiguration;
import java.nio.file.Path;
import java.util.Objects;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.tasks.TaskContainer;

/**
 * Configures Checkstyle with preferences.
 */
public class CheckstyleConfigurator {
    /** The default configuration resource path. */
    private static final String CHECKSTYLE_CHECKSTYLE_MADA_XML = "checkstyle/checkstyle-mada.xml";
    /** The default suppressions resource path. */
    private static final String CHECKSTYLE_SUPPRESSIONS_MADA_XML = "checkstyle/suppressions-mada.xml";
    /** The gradle project. */
    private final Project project;
    /** The gradle logger. */
    private final Logger logger;
    /** The checkstyle configuration. */
    private final CheckstyleConfiguration checkstyleConfig;
    /** The default configuration file shipped with this plugin. */
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
    public CheckstyleConfigurator(
            Project project, CheckstyleConfiguration checkstyleConfig, ConfigFileExtractor configExtractor) {
        this.logger = project.getLogger();
        this.project = project;
        this.checkstyleConfig = checkstyleConfig;
        this.configExtractor = configExtractor;

        defaultConfigFile = configExtractor.getLocalConfigFileFromResource(CHECKSTYLE_CHECKSTYLE_MADA_XML);
        // The suppressions file is references from the config file
        configExtractor.getLocalConfigFileFromResource(CHECKSTYLE_SUPPRESSIONS_MADA_XML);
    }

    /**
     * Configures the checkstyle extension.
     *
     * @param ce the checkstyle extension
     */
    public void configure(CheckstyleExtension ce) {
        Path shippedConfigDir = Objects.requireNonNull(defaultConfigFile.getParent());
        ce.getConfigDirectory().convention(toGradleDir(shippedConfigDir));

        Path activeConfigfile = getActiveConfigfile();
        logger.info("Checkstyle config {}", activeConfigfile);

        ce.setIgnoreFailures(checkstyleConfig.ignoreFailures());
        ce.setConfigFile(activeConfigfile.toFile());

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

    /**
     * Convert Path (directory) to a Gradle Directory.
     *
     * Seems the easiest (only?) way.
     *
     * @param dir the directory
     * @return the Gradle Directory
     */
    private Directory toGradleDir(Path dir) {
        return project.getLayout()
                .getProjectDirectory()
                .dir(dir.toAbsolutePath().toString());
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
