package dk.mada.style;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;

import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.gradle.spotless.SpotlessPlugin;

import dk.mada.style.config.ConfigFileExtractor;
import dk.mada.style.config.PluginConfiguration;
import dk.mada.style.errorprone.ErrorProneConfigurator;
import dk.mada.style.format.SpotlessConfigurator;
import net.ltgt.gradle.errorprone.ErrorPronePlugin;

/**
 * A plugin defining the style used for dk.mada java code.
 */
public class MadaStylePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPlugins().withType(JavaPlugin.class, jp -> applyPlugins(project));
    }

    private void applyPlugins(Project project) {
        var configuration = new PluginConfiguration(project);

        if (configuration.isFormatterActive()) {
            project.getPluginManager().apply("com.diffplug.spotless");

            project.getPlugins().withType(SpotlessPlugin.class, sp -> lazyConfigureFormatter(project, configuration));
        }

        if (configuration.isNullcheckerActive() || configuration.isErrorProneActive()) {
            project.getPluginManager().apply("net.ltgt.errorprone");

            project.getPlugins().withType(ErrorPronePlugin.class,
                    ep -> new ErrorProneConfigurator(project, configuration.errorProne(), configuration.nullchecker()).configure());
        }
    }

    /**
     * Hook spotless configuration on activation of its extension. It only gets configured on task activation.
     *
     * @param project       the project
     * @param configuration the plugin configuration
     */
    private void lazyConfigureFormatter(Project project, PluginConfiguration configuration) {
        var configExtractor = new ConfigFileExtractor(project.getLogger(), project.getGradle().getGradleHomeDir().toPath());

        project.getExtensions().configure(SpotlessExtension.class, se -> {
            new SpotlessConfigurator(project.getLogger(), configuration.formatter(), configExtractor).configure(se);
        });
    }
}
