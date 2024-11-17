package dk.mada.style.configurators;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstylePlugin;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.tasks.JacocoReport;
import org.sonarqube.gradle.SonarExtension;
import org.sonarqube.gradle.SonarTask;

import dk.mada.style.config.PluginConfiguration.SonarConfiguration;

/**
 * Configures Sonar.
 */
public class SonarConfigurator {
    /** The gradle project. */
    private final Project project;
    /** The gradle logger. */
    private final Logger logger;
    /** The sonarqube configuration. */
    private final SonarConfiguration sonarConfig;

    /**
     * Creates new instance.
     *
     * @param project     the gradle project
     * @param sonarConfig the sonarqube configuration
     */
    public SonarConfigurator(Project project, SonarConfiguration sonarConfig) {
        this.project = project;
        this.logger = project.getLogger();
        this.sonarConfig = sonarConfig;
    }

    /**
     * Configures the sonarqube extension.
     *
     * @param se the sonarqube extension
     */
    public void configure(SonarExtension se) {
        logger.info("dk.mada.style configure sonar");

        TaskContainer taskContainer = project.getTasks();
        PluginContainer plugins = project.getPlugins();

        // Make sonar depend on some other check tasks (we want sonar to run last)
        taskContainer.withType(SonarTask.class, sonarTask -> {
            plugins.withType(CheckstylePlugin.class).whenPluginAdded(p -> taskContainer.withType(Checkstyle.class, sonarTask::dependsOn));

            plugins.withType(JacocoPlugin.class).whenPluginAdded(p -> taskContainer.withType(JacocoReport.class, sonarTask::dependsOn));
        });

        Map<String, String> inputProps = project.getProperties().entrySet().stream()
                .filter(e -> !e.getKey().equals("dk.mada.style.sonar.enabled"))
                .filter(e -> e.getKey().startsWith("dk.mada.style.sonar."))
                .collect(Collectors.toMap(e -> e.getKey().replace("dk.mada.style.", ""), e -> Objects.toString(e.getValue())));

        Map<String, String> combinedMadaSonarProps = new HashMap<>();
        combinedMadaSonarProps.putAll(sonarConfig.madaConventionProperties());
        combinedMadaSonarProps.putAll(inputProps);

        logger.info("Set sonar properties: {}", combinedMadaSonarProps);

        se.properties(sp -> combinedMadaSonarProps.forEach(sp::property));
    }
}
