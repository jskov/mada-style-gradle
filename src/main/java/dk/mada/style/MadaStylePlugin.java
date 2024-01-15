package dk.mada.style;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPlugin;

import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.gradle.spotless.SpotlessPlugin;

import dk.mada.style.config.ConfigFileExtractor;
import dk.mada.style.format.SpotlessConfigurator;
import dk.mada.style.nullcheck.ErrorProneConfigurator;
import net.ltgt.gradle.errorprone.ErrorPronePlugin;

/**
 * A plugin defining the style used for dk.mada java code.
 */
public class MadaStylePlugin implements Plugin<Project> {
    /** The Gradle logger. */
    private Logger logger;
    /** The style extension. */
    private MadaStylePluginExtension ext;
    /** Configuration file extractor. */
    private ConfigFileExtractor configExtractor;

    @Override
    public void apply(Project project) {
        logger = project.getLogger();
        ext = project.getExtensions().create("mada", MadaStylePluginExtension.class);
        configExtractor = new ConfigFileExtractor(logger, project.getGradle().getGradleHomeDir().toPath());

        project.getPlugins().withType(JavaPlugin.class, jp -> project.afterEvaluate(this::configure));
    }

    /**
     * Configuration of the project.
     *
     * Note that this is only called on java-projects, and only after project evaluation (so the DSL configuration has been
     * evaluated).
     *
     * @param project the project
     */
    private void configure(Project project) {
        if (Boolean.TRUE.equals(ext.getFormatter().getEnabled().get())) {
            project.getPlugins().withType(SpotlessPlugin.class, sp -> lazyConfigureFormatter(project));

            project.getPluginManager().apply("com.diffplug.spotless");
        }

        if (Boolean.TRUE.equals(ext.getNullChecker().getEnabled().get())) {
            project.getPlugins().withType(ErrorPronePlugin.class, ep -> {
                logger.lifecycle("configure errorprone!");
                new ErrorProneConfigurator(project, configExtractor, ext.getNullChecker()).configure();
            });

            project.getPluginManager().apply("net.ltgt.errorprone");
        }
    }

    private void lazyConfigureFormatter(Project project) {
        project.getExtensions().configure(SpotlessExtension.class, se -> {
            logger.info("Configure spotless extension");
            new SpotlessConfigurator(logger, configExtractor, ext.getFormatter()).configure(se);
        });

    }
}
