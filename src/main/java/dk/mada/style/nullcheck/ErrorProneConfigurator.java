package dk.mada.style.nullcheck;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;

import dk.mada.style.config.ResourceConfigProperties;
import dk.mada.style.config.PluginConfiguration.ErrorProneConfiguration;
import dk.mada.style.config.PluginConfiguration.NullcheckerConfiguration;
import net.ltgt.gradle.errorprone.CheckSeverity;
import net.ltgt.gradle.errorprone.ErrorProneOptions;

/**
 * Configures Spotless with formatter preferences.
 */
public class ErrorProneConfigurator {
    private static final String CONFIG_DATAFILE_DEPENDENCIES_PROPERTIES = "/config/datafile-dependencies.properties";
    /** The gradle project. */
    private final Project project;
    /** The gradle logger. */
    private final Logger logger;
    /** The ErrorProne configuration. */
    private final ErrorProneConfiguration errorProneConfig;
    /** The null-checker configuration. */
    private final NullcheckerConfiguration nullcheckerConfig;

    /**
     * Creates new instance.
     *
     * @param project                  the gradle project
     * @param errorProneConfig the ErrorProne configuration
     * @param nullcheckerConfig the null-checker configuration
     */
    public ErrorProneConfigurator(Project project, ErrorProneConfiguration errorProneConfig, NullcheckerConfiguration nullcheckerConfig) {
        this.project = project;
        this.logger = project.getLogger();
        this.errorProneConfig = errorProneConfig;
        this.nullcheckerConfig = nullcheckerConfig;
    }

    /**
     * Configures the ErrorProne plugin.
     */
    public void configure() {
        logger.info("dk.mada.style configure errorprone");

        Properties depVersions = ResourceConfigProperties.readConfigProperties(CONFIG_DATAFILE_DEPENDENCIES_PROPERTIES);
        project.getDependencies().add("errorprone", addVersion(depVersions, "com.google.errorprone:error_prone_core"));

        if (nullcheckerConfig.enabled()) {
            project.getDependencies().add("annotationProcessor", addVersion(depVersions, "com.uber.nullaway:nullaway"));
        }

        project.getTasks().withType(JavaCompile.class, jc -> {
            boolean isTestCodeCompileTask = jc.getName().toLowerCase(Locale.ROOT).contains("test");
            if (isTestCodeCompileTask && errorProneConfig.ignoreTestSource()) {
                return;
            }

            CompileOptions opts = jc.getOptions();
            // This trick only found by looking at ErrorProne plugin code (hidden by Groovy/Gradle API)
            ErrorProneOptions er = ((ExtensionAware) opts).getExtensions().getByType(ErrorProneOptions.class);

            makeList(errorProneConfig.disabledRules()).forEach(ruleName -> er.check(ruleName, CheckSeverity.OFF));
            er.getExcludedPaths().set(errorProneConfig.excludePathsRegexp());
            er.getDisableWarningsInGeneratedCode().set(errorProneConfig.ignoreGeneratedSource());

            if (nullcheckerConfig.enabled()) {
                er.check("NullAway", CheckSeverity.ERROR);
                er.option("NullAway:AnnotatedPackages", makeValidNoSpaceString(nullcheckerConfig.includePackages()));
                er.option("NullAway:UnannotatedSubPackages", makeValidNoSpaceString(nullcheckerConfig.excludePackages()));
            }
        });
    }

    private static String addVersion(Properties dependencyVersions, String groupArtifact) {
        String version = dependencyVersions.getProperty(groupArtifact);
        return groupArtifact + ":" + Objects.requireNonNull(version, "Did not find version for dependency '" + groupArtifact + "'");
    }

    private static String makeValidNoSpaceString(String s) {
        return Stream.of(s.split(",", -1))
                .map(String::trim)
                .collect(Collectors.joining(","));
    }
    
    private static List<String> makeList(String s) {
        return Stream.of(s.split(",", -1))
                .map(String::trim)
                .toList();
    }
}
