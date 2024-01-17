package dk.mada.style;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPlugin;

import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.gradle.spotless.SpotlessPlugin;

import dk.mada.style.config.ConfigFileExtractor;
import dk.mada.style.config.PluginConfiguration;
import dk.mada.style.format.SpotlessConfigurator;
import dk.mada.style.nullcheck.ErrorProneConfigurator;
import net.ltgt.gradle.errorprone.ErrorPronePlugin;

/**
 * A plugin defining the style used for dk.mada java code.
 */
public class MadaStylePlugin implements Plugin<Project> {
    /** The Gradle logger. */
    private Logger logger;
    /** Configuration file extractor. */
    private ConfigFileExtractor configExtractor;
    /** The plugin configurations. */
    private PluginConfiguration configuration;

    @Override
    public void apply(Project project) {
        logger = project.getLogger();
        configuration = new PluginConfiguration(project);

        configExtractor = new ConfigFileExtractor(logger, project.getGradle().getGradleHomeDir().toPath());

        project.getPlugins().withType(JavaPlugin.class, jp -> {
            // plugins need to be activated early to behave correctly
            applyPlugins(project);
            // but they can only be configured after the extension DSL has been parsed
            project.afterEvaluate(this::configurePlugins);
        });
    }

    private void applyPlugins(Project project) {
        if (configuration.isFormatterActive()) {
            project.getPluginManager().apply("com.diffplug.spotless");
        }
        if (configuration.isNullcheckerActive()) {
            project.getPluginManager().apply("net.ltgt.errorprone");
        }
    }
    
    /**
     * Configuration of the project.
     *
     * Note that this is only called on java-projects, and only after project evaluation (so the DSL configuration has been
     * evaluated).
     *
     * @param project the project
     */
    private void configurePlugins(Project project) {
        if (configuration.isFormatterActive()) {
            project.getPlugins().withType(SpotlessPlugin.class, sp -> lazyConfigureFormatter(project));
        }

        if (configuration.isNullcheckerActive()) {
            project.getPlugins().withType(ErrorPronePlugin.class, ep -> {
                logger.lifecycle("configure errorprone!");
                new ErrorProneConfigurator(project, configuration.nullchecker()).configure();
            });
        }
    }

    /**
     * Hook spotless configuration on activation of its extension.
     * It only gets configured on task activation.
     *
     * @param project the project
     */
    private void lazyConfigureFormatter(Project project) {
        project.getExtensions().configure(SpotlessExtension.class, se -> {
            logger.info("Configure spotless extension");
            new SpotlessConfigurator(logger, configuration.formatter(), configExtractor).configure(se);
        });
    }
}
