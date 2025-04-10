package dk.mada.style;

import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.gradle.spotless.SpotlessPlugin;
import dk.mada.style.config.ConfigFileExtractor;
import dk.mada.style.config.PluginConfiguration;
import dk.mada.style.configurators.CheckstyleConfigurator;
import dk.mada.style.configurators.ErrorProneConfigurator;
import dk.mada.style.configurators.SonarConfigurator;
import dk.mada.style.configurators.SpotlessConfigurator;
import net.ltgt.gradle.errorprone.ErrorPronePlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.plugins.quality.CheckstylePlugin;
import org.sonarqube.gradle.SonarExtension;
import org.sonarqube.gradle.SonarQubePlugin;

/**
 * A plugin defining the style used for dk.mada java code.
 */
public final class MadaStylePlugin implements Plugin<Project> {
    /** Constructs new instance. */
    public MadaStylePlugin() {
        // Explicit default constructor to avoid javadoc warning
    }

    void foo() {
        // empty
    }

    @Override
    public void apply(Project project) {
        project.getPlugins().withType(JavaPlugin.class, jp -> applyPlugins(project));
    }

    private void applyPlugins(Project project) {
        Logger logger = project.getLogger();
        logger.info("Applying mada.style plugin");

        var configuration = new PluginConfiguration(project);
        var configExtractor = new ConfigFileExtractor(
                logger, project.getGradle().getGradleHomeDir().toPath());

        if (configuration.isCheckstyleActive()) {
            project.getPluginManager().apply("checkstyle");

            project.getPlugins()
                    .withType(
                            CheckstylePlugin.class,
                            cp -> lazyConfigureCheckstyle(project, configuration, configExtractor));
        }

        if (configuration.isFormatterActive()) {
            project.getPluginManager().apply("com.diffplug.spotless");

            project.getPlugins().withType(SpotlessPlugin.class, sp -> lazyConfigureFormatter(project, configuration));
        }

        if (configuration.isNullcheckerActive() || configuration.isErrorProneActive()) {
            project.getPluginManager().apply("net.ltgt.errorprone");

            project.getPlugins().withType(ErrorPronePlugin.class, ep -> new ErrorProneConfigurator(
                            project, configuration.errorProne(), configuration.nullchecker())
                    .configure());
        }

        if (configuration.isSonarActive()) {
            // This should be a Gradle property, but is a system property
            // https://sonarsource.atlassian.net/browse/SONARGRADL-134
            System.setProperty("sonar.gradle.skipCompile", "true");

            project.getPluginManager().apply("org.sonarqube");

            project.getPlugins().withType(SonarQubePlugin.class, sp -> lazyConfigureSonar(project, configuration));
        }
    }

    /**
     * Hook checkstyle configuration on activation of its extension. It only gets configured on task activation.
     *
     * @param project         the project
     * @param configuration   the plugin configuration
     * @param configExtractor the configuration extractor
     */
    private void lazyConfigureCheckstyle(
            Project project, PluginConfiguration configuration, ConfigFileExtractor configExtractor) {
        project.getExtensions().configure(CheckstyleExtension.class, ce -> new CheckstyleConfigurator(
                        project, configuration.checkstyle(), configExtractor)
                .configure(ce));
    }

    /**
     * Hook spotless configuration on activation of its extension. It only gets configured on task activation.
     *
     * @param project       the project
     * @param configuration the plugin configuration
     */
    private void lazyConfigureFormatter(Project project, PluginConfiguration configuration) {
        project.getExtensions().configure(SpotlessExtension.class, se -> new SpotlessConfigurator(
                        project.getLogger(), configuration.formatter())
                .configure(se));
    }

    /**
     * Lazy configure sonar extension.
     *
     * @param project       the project
     * @param configuration the plugin configuration
     */
    private void lazyConfigureSonar(Project project, PluginConfiguration configuration) {
        project.getExtensions()
                .configure(SonarExtension.class, se -> new SonarConfigurator(project, configuration.sonar())
                        .configure(se));
    }
}
