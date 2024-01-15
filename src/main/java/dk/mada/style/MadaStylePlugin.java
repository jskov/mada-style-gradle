package dk.mada.style;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginContainer;

import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.gradle.spotless.SpotlessPlugin;

import dk.mada.style.config.ConfigFileExtractor;
import dk.mada.style.format.FormatterConfig;
import dk.mada.style.format.SpotlessConfigurator;

public class MadaStylePlugin implements Plugin<Project> {
    /** The Gradle logger.*/
    private Logger logger;
    /** The style extension. */
    private MadaStylePluginExtension ext;

    @Override
    public void apply(Project project) {
        logger = project.getLogger();
        ext = project.getExtensions().create("mada", MadaStylePluginExtension.class);

        var configExtractor = new ConfigFileExtractor(logger, project.getGradle().getGradleHomeDir().toPath());
        
        ext.getNullcheckingEnabled().convention(true);
        FormatterConfig formatter = ext.getFormatter();
        formatter.getEnabled().convention(true);

        PluginContainer plugins = project.getPlugins();
        
        plugins.withType(SpotlessPlugin.class, sp -> {
            logger.lifecycle("Configure spotless!");
            project.getExtensions().configure(SpotlessExtension.class, se -> {
                logger.lifecycle("Configure spotless extension");
                new SpotlessConfigurator(logger, configExtractor, ext.getFormatter()).configure(se);
            });
        });
        
        plugins.withType(JavaPlugin.class, jp ->
            project.afterEvaluate(this::configure)
        );
    }
    
    
    private void configure(Project p) {
        if (ext.getNullcheckingEnabled().get()) {
            enableNullchecking(p);
        }
        if (ext.getFormatter().getEnabled().get()) {
            enableFormatting(p);
        }
    }

    private void enableFormatting(Project p) {
        p.getPluginManager().apply("com.diffplug.spotless");
    }

    private void enableNullchecking(Project p) {
    }
}
