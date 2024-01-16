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
    
    public PluginConfiguration(Project project) {
        this.project = project;
        
        formatterConf = new FormatterConfiguration(
                getBoolProperty("formatter.enabled", true)
                );

        nullcheckerProps = new NullcheckerConfiguration(
                getBoolProperty("nullchecker.enabled", true)
                );
}

    public FormatterConfiguration formatter() {
        return formatterConf;
    }

    public NullcheckerConfiguration nullchecker() {
        return nullcheckerProps;
    }

    /**
     * @param enabled  flag to activate formatter
     */
    public record FormatterConfiguration(boolean enabled) {
    }
    
    /**
     * @param enabled  flag to activate null-checker
     */
    public record NullcheckerConfiguration(boolean enabled) {
    }
    
    public boolean getBoolProperty(String name, boolean defaultValue) {
        String value = getProperty(name, null);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.valueOf(value);
    }
    
    public String getProperty(String name, String defaultValue) {
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
