package dk.mada.style;

import java.util.Objects;
import java.util.stream.Collectors;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginContainer;

import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.gradle.spotless.SpotlessPlugin;

public class MadaStylePlugin implements Plugin<Project> {
    /** The Gradle logger.*/
    private Logger logger;
    /** The style extension. */
    private MadaStylePluginExtension ext;

    @Override
    public void apply(Project project) {
        logger = project.getLogger();
        ext = project.getExtensions().create("mada", MadaStylePluginExtension.class);
        
        ext.getNullcheckingEnabled().convention(true);
        ext.getFormattingEnabled().convention(true);

        project.getBuildscript().getDependencies()
            .add(ScriptHandler.CLASSPATH_CONFIGURATION, "com.diffplug.spotless:spotless-plugin-gradle:6.23.3");

        PluginContainer plugins = project.getPlugins();
        
        plugins.withType(SpotlessPlugin.class, sp -> {
            logger.lifecycle("Configure spotless!");
            project.getExtensions().configure(SpotlessExtension.class, se -> {
                logger.lifecycle("Configure spotless extension");
            });
        });
        
        plugins.withType(JavaPlugin.class, jp ->
            project.afterEvaluate(this::configure)
        );
    }
    
    private void configure(Project p) {
        logger.lifecycle("x Configure nullcheck:{} format:{}", ext.getNullcheckingEnabled().get(), ext.getFormattingEnabled().get());
        if (ext.getNullcheckingEnabled().get()) {
            enableNullchecking(p);
        }
        if (ext.getFormattingEnabled().get()) {
            enableFormatting(p);
        }
    }

    private void enableFormatting(Project p) {
        p.getPluginManager().apply("com.diffplug.spotless");
    }

    private void enableNullchecking(Project p) {
    }
}
