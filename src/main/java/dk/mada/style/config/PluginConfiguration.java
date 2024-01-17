package dk.mada.style.config;

import org.gradle.api.Project;

/**
 * Configuration for the plugin.
 *
 * Expressed entirely via Gradle project properties (from gradle.properties).
 * This allows the plugin to be removed without causing build breakage.
 */
public class PluginConfiguration {
    /** The prefix used for all configuration properties. */
    private static final String DK_MADA_STYLE_PROPPREFIX = "dk.mada.style.";
    /** The Gradle project. */
    private final Project project;

    private final FormatterConfiguration formatterConf;
    private final NullcheckerConfiguration nullcheckerProps;

    /**
     * @param enabled  flag to activate formatter
     */
    public record FormatterConfiguration(boolean enabled, String include, String exclude, String eclipseConfigPath) {
    }
    
    /**
     * @param enabled  flag to activate null-checker
     */
    public record NullcheckerConfiguration(boolean enabled, String packages, String excludePathsRegexp) {
    }

    /**
     * Creates a new instance.
     *
     * @param project the Gradle project
     */
    public PluginConfiguration(Project project) {
        this.project = project;
        
        formatterConf = new FormatterConfiguration(
                getBoolProperty("formatter.enabled", true),
                getProperty("formatter.include", "src/main/java/**/*.java"),
                getProperty("formatter.exclude", ""),
                getProperty("formatter.eclipse-config-path", null)
                );

        nullcheckerProps = new NullcheckerConfiguration(
                getBoolProperty("nullchecker.enabled", true),
                getProperty("nullchecker.packages", "dk"),
                getProperty("nullchecker.excluded-paths-regexp", null)
                );
    }

    /** {@return true if the formatter is active} */
    public boolean isFormatterActive() {
        return formatter().enabled();
    }
    
    /** {@return the formatter configuration} */
    public FormatterConfiguration formatter() {
        return formatterConf;
    }

    /** {@return true if the null-checker is active} */
    public boolean isNullcheckerActive() {
        return nullchecker().enabled();
    }

    /** {@return the null-checker configuration} */
    public NullcheckerConfiguration nullchecker() {
        return nullcheckerProps;
    }

    private boolean getBoolProperty(String name, boolean defaultValue) {
        String value = getProperty(name, null);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.valueOf(value);
    }

    private String getProperty(String name, String defaultValue) {
        Object value = project.findProperty(DK_MADA_STYLE_PROPPREFIX + name);
        if (value == null) {
            value = defaultValue;
        }
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}
