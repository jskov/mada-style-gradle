package dk.mada.style;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPlugin;

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
        
        project.getPlugins().withType(JavaPlugin.class, jp ->
            project.afterEvaluate(this::configure)
        );
    }
    
    private void configure(Project p) {
        logger.lifecycle("x Configure nullcheck:{} format:{}", ext.getNullcheckingEnabled().get(), ext.getFormattingEnabled().get());
    }
}
