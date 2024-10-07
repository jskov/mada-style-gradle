package dk.mada.style.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.gradle.api.Project;
import org.jspecify.annotations.Nullable;

/**
 * Configuration for the plugin.
 *
 * Expressed entirely via Gradle project properties (from gradle.properties). This allows the plugin to be removed
 * without causing build breakage.
 */
public class PluginConfiguration {
    /** The prefix used for all configuration properties. */
    private static final String DK_MADA_STYLE_PROPPREFIX = "dk.mada.style.";
    /** The Gradle project. */
    private final Project project;
    /** The parsed CheckStyle configuration. */
    private final CheckstyleConfiguration checkstyleConf;
    /** The parsed ErrorProne configuration. */
    private final ErrorProneConfiguration errorproneConf;
    /** The parsed formatter configuration. */
    private final FormatterConfiguration formatterConf;
    /** The parsed null-checker configuration. */
    private final NullcheckerConfiguration nullcheckerConf;
    /** The parsed sonar configuration. */
    private final SonarConfiguration sonarConf;

    /**
     * Checkstyle configuration.
     *
     * @param enabled               flag to activate checkstyle
     * @param includes              Ant-style pattern for sources to check
     * @param excludes              Ant-style pattern for sources to ignore
     * @param ignoreFailures        flag to ignore failures
     * @param ignoreTestSource      flag to ignore test source files
     * @param ignoreGeneratedSource flag to ignore generated source files
     * @param toolVersion           an optional checkstyle version to use
     * @param configPath            an optional path to a checkstyle configuration file
     */
    public record CheckstyleConfiguration(boolean enabled, List<String> includes, List<String> excludes, boolean ignoreFailures,
            boolean ignoreTestSource,
            boolean ignoreGeneratedSource,
            @Nullable String testSuppressionFiles,
            @Nullable String testSuppressionChecks,
            @Nullable String toolVersion,
            @Nullable String configPath) {
    }

    /**
     * ErrorProne configuration.
     *
     * @param enabled               flag to activate error prone
     * @param ignoreTestSource      flag to ignore test source files
     * @param ignoreGeneratedSource flag to ignore generated source files
     * @param excludePathsRegexp    a regular expression of source paths to ignore
     * @param disabledRules         a comma-separated list of rule names to disable
     */
    public record ErrorProneConfiguration(boolean enabled, boolean ignoreTestSource, boolean ignoreGeneratedSource,
            String excludePathsRegexp, String disabledRules) {
    }

    /**
     * Formatter configuration.
     *
     * @param enabled            flag to activate formatter
     * @param includes           Ant-style patterns for sources to format
     * @param excludes           Ant-style patterns for sources to ignore
     * @param eclipseConfigPath  an optional path to an eclipse configuration file
     * @param eclipse429P2mirror an optional URL to a eclipse update P2 repository
     */
    public record FormatterConfiguration(boolean enabled, List<String> includes, List<String> excludes, @Nullable String eclipseConfigPath,
            @Nullable String eclipse429P2mirror) {
    }

    /**
     * Null-checker configuration.
     *
     * @param enabled                  flag to activate null-checker
     * @param includePackages          a comma-separated list of packages to scan (restricted regexp syntax)
     * @param excludePackages          a comma-separated list of packages to ignore (restricted regexp syntax)
     * @param excludedFieldAnnotations a comma-seaparated list of classes to ignore (restricted regexp syntax)
     */
    public record NullcheckerConfiguration(boolean enabled, String includePackages, String excludePackages,
            String excludedFieldAnnotations) {
    }

    /**
     * SonarSource sonar configuration.
     *
     * @param enabled                  flag to activate sonar
     * @param madaConventionProperties convention properties for sonar
     */
    public record SonarConfiguration(boolean enabled, Map<String, String> madaConventionProperties) {
    }

    /**
     * Creates a new instance.
     *
     * @param project the Gradle project
     */
    public PluginConfiguration(Project project) {
        this.project = project;

        checkstyleConf = new CheckstyleConfiguration(
                getBoolProperty("checkstyle.enabled", true),
                getListProperty("checkstyle.includes", List.of()),
                getListProperty("checkstyle.excludes", List.of()),
                getBoolProperty("checkstyle.ignore-failures", true),
                getBoolProperty("checkstyle.ignore-test-source", false),
                getBoolProperty("checkstyle.ignore-generated-source", false),
                getProperty("checkstyle.test-suppressions.files", "dk/.*/(accept|fixture|unit)/.*java"),
                getProperty("checkstyle.test-suppressions.checks", "(Javadoc|LineLength|MagicNumber)"),
                getNullableProperty("checkstyle.tool-version", null),
                getNullableProperty("checkstyle.config-path", null));

        errorproneConf = new ErrorProneConfiguration(
                getBoolProperty("errorprone.enabled", true),
                getBoolProperty("errorprone.ignore-test-source", false),
                getBoolProperty("errorprone.ignore-generated-source", false),
                getProperty("errorprone.excluded-paths-regexp", ""),
                getProperty("errorprone.disabled-rules", ""
                        // https://github.com/google/error-prone/issues/1542 (Set.of - possible records problem)
                        + "ImmutableEnumChecker,"
                        // The time zone is not relevant
                        + "JavaTimeDefaultTimeZone"));

        formatterConf = new FormatterConfiguration(
                getBoolProperty("formatter.enabled", true),
                getListProperty("formatter.include", List.of("src/main/java/**/*.java", "src/test/java/**/*.java")),
                getListProperty("formatter.exclude", List.of()),
                getNullableProperty("formatter.eclipse-config-path", null),
                getNullableProperty("formatter.eclipse-429-p2-url", null));

        nullcheckerConf = new NullcheckerConfiguration(
                getBoolProperty("null-checker.enabled", true),
                getProperty("null-checker.include-packages", "dk"),
                getProperty("null-checker.exclude-packages", ""),
                getProperty("null-checker.exclude-field-annotations", ""));

        sonarConf = new SonarConfiguration(
                getBoolProperty("sonar.enabled", true),
                Map.of(
                        "sonar.host.url", "https://sonarcloud.io",
                        "sonar.sourceEncoding", "UTF-8"));
    }

    /** {@return the CheckStyle configuration} */
    public CheckstyleConfiguration checkstyle() {
        return checkstyleConf;
    }

    /** {@return true if the CheckStyle checker is active} */
    public boolean isCheckstyleActive() {
        return checkstyle().enabled();
    }

    /** {@return the ErrorProne configuration} */
    public ErrorProneConfiguration errorProne() {
        return errorproneConf;
    }

    /** {@return true if the ErrorProne checker is active} */
    public boolean isErrorProneActive() {
        return errorProne().enabled();
    }

    /** {@return the formatter configuration} */
    public FormatterConfiguration formatter() {
        return formatterConf;
    }

    /** {@return true if the formatter is active} */
    public boolean isFormatterActive() {
        return formatter().enabled();
    }

    /** {@return the null-checker configuration} */
    public NullcheckerConfiguration nullchecker() {
        return nullcheckerConf;
    }

    /** {@return true if the null-checker is active} */
    public boolean isNullcheckerActive() {
        return nullchecker().enabled();
    }

    /** {@return the sonar configuration} */
    public SonarConfiguration sonar() {
        return sonarConf;
    }

    /** {@return true if sonar is active} */
    public boolean isSonarActive() {
        return sonar().enabled();
    }

    private boolean getBoolProperty(String name, boolean defaultValue) {
        String value = getNullableProperty(name, null);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.valueOf(value);
    }

    private String getProperty(String name, String defaultValue) {
        String value = getNullableProperty(name, defaultValue);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    private @Nullable String getNullableProperty(String name, @Nullable String defaultValue) {
        Object value = project.findProperty(DK_MADA_STYLE_PROPPREFIX + name);
        if (value == null) {
            value = defaultValue;
        }
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    private List<String> getListProperty(String name, List<String> defaultValue) {
        String value = getNullableProperty(name, null);
        if (value == null) {
            return defaultValue;
        }
        return Stream.of(value.split(",", -1))
                .map(String::trim)
                .toList();
    }

}
