package dk.mada.style;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginContainer;

import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.gradle.spotless.SpotlessPlugin;

import dk.mada.style.config.ConfigFileExtractor;
import dk.mada.style.format.SpotlessConfigurator;

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

        ext.getNullcheckingEnabled().convention(true);

        PluginContainer plugins = project.getPlugins();

        plugins.withType(JavaPlugin.class, jp -> project.afterEvaluate(this::configure));
    }

    private void configure(Project p) {
        if (Boolean.TRUE.equals(ext.getNullcheckingEnabled().get())) {
            enableNullchecking(p);
        }
        if (Boolean.TRUE.equals(ext.getFormatter().getEnabled().get())) {
            enableFormatting(p);
        }
    }

    private void enableFormatting(Project p) {
        p.getPlugins().withType(SpotlessPlugin.class, sp -> {
            p.getExtensions().configure(SpotlessExtension.class, se -> {
                logger.info("Configure spotless extension");
                new SpotlessConfigurator(logger, configExtractor, ext.getFormatter()).configure(se);
            });
        });

        p.getPluginManager().apply("com.diffplug.spotless");
    }

    private void enableNullchecking(Project p) {
    }
}
