package dk.mada.style.nullcheck;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;

import dk.mada.style.config.ConfigFileExtractor;
import net.ltgt.gradle.errorprone.CheckSeverity;
import net.ltgt.gradle.errorprone.ErrorProneOptions;

/**
 * Configures Spotless with formatter preferences.
 */
public class ErrorProneConfigurator {
    /** The gradle project. */
    private final Project project;
    /** The gradle logger. */
    private final Logger logger;
    /** The null-checker configuration. */
    private final NullcheckerConfig nullcheckerConfig;
    /** The default configuration file, shipped with this plugin. */
//    private final Path defaultConfigFile;

    /**
     * Creates new instance.
     *
     * @param project          the gradle project
     * @param configExtractor the configuration extractor
     * @param nullcheckerConfig the formatter configuration
     */
    public ErrorProneConfigurator(Project project, ConfigFileExtractor configExtractor, NullcheckerConfig nullcheckerConfig) {
        this.project = project;
        this.logger = project.getLogger();
        this.nullcheckerConfig = nullcheckerConfig;
    }

    /**
     * Configures the ErrorProne plugin.
     */
    public void configure() {
        project.getDependencies().add("annotationProcessor", "com.uber.nullaway:nullaway:0.10.18");
        project.getDependencies().add("errorprone", "com.google.errorprone:error_prone_core:2.24.0");
        
        project.getTasks().withType(JavaCompile.class, jc -> {
            CompileOptions opts = jc.getOptions();
            // This trick only found by looking at ErrorProne plugin code (hidden by Groovy/Gradle API)
            ErrorProneOptions er = ((ExtensionAware)opts).getExtensions().getByType(ErrorProneOptions.class);
            
            // Only do null-checking on main code for now
            if (!jc.getName().toLowerCase().contains("test")) {
                logger.lifecycle(" task {} : {}", jc.getName(), er);

                er.getDisableWarningsInGeneratedCode().set(false);
                er.check("NullAway", CheckSeverity.ERROR);
                er.option("NullAway:AnnotatedPackages", makeValidNoSpaceString(nullcheckerConfig.getPackages().get()));
                er.getExcludedPaths().set(nullcheckerConfig.getExcludedPathsRegexp().getOrNull());

                //  https://github.com/google/error-prone/issues/1542 (Set.of)
                //  ? records
                er.check("ImmutableEnumChecker", CheckSeverity.OFF);
                // Timezone is not relevant
                er.check("JavaTimeDefaultTimeZone", CheckSeverity.OFF);
            }
        });
        
        
//                excludedPaths = readProps().getProperty("excludedPaths")
//
//                if (!name.toLowerCase().contains("test")) {
//                    check("NullAway", CheckSeverity.ERROR)
//                    option("NullAway:AnnotatedPackages", "dk.mada")
//                }
//            }

    }
    
    private static String makeValidNoSpaceString(String s) {
        return Stream.of(s.split(",", -1))
                .map(String::trim)
                .collect(Collectors.joining(","));
    }
}
