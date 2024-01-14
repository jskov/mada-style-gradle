package dk.mada.style;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginContainer;

import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.gradle.spotless.SpotlessPlugin;

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

        System.out.println("SET CONV");
        ext.getNullcheckingEnabled().convention(true);
        FormatterConfig formatter = ext.getFormatter();
        System.out.println("other");
        formatter.getEnabled().convention(true);

        project.getBuildscript().getDependencies()
            .add(ScriptHandler.CLASSPATH_CONFIGURATION, "com.diffplug.spotless:spotless-plugin-gradle:6.23.3");

        PluginContainer plugins = project.getPlugins();
        Properties dataChecksums = readDatafileChecksums();
        
        plugins.withType(SpotlessPlugin.class, sp -> {
            logger.lifecycle("Configure spotless!");
            project.getExtensions().configure(SpotlessExtension.class, se -> {
                logger.lifecycle("Configure spotless extension");
                File gradleHomeDir = project.getGradle().getGradleHomeDir();
                new SpotlessConfigurator(logger, gradleHomeDir, dataChecksums, ext.getFormatter()).configure(se);
            });
        });
        
        plugins.withType(JavaPlugin.class, jp ->
            project.afterEvaluate(this::configure)
        );
    }
    
    
    private Properties readDatafileChecksums() {
        String path = "/config/datafile-checksums.properties";
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException("Failed to find resource " + path);
            }
            Properties p = new Properties();
            p.load(is);
            return p;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read properties from resource " + path, e);
        }
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
