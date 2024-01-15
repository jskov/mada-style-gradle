package dk.mada.style.format;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

/**
 * DSL container for formatter configuration.
 */
public abstract class FormatterConfig {
    /** {@return a flag to control the activation of the formatter} */
    public abstract Property<Boolean> getEnabled();

    /** {@return the OpenApi document file extension. Defaults to .yaml} */
    public abstract Property<String> getInclude();

    /** {@return the flag to controls if code is generated to the source folder} */
    public abstract Property<String> getExclude();

    /** {@return the Java Eclipse configuration} */
    public abstract RegularFileProperty getEclipseConfig();

    /**
     * Creates a new instance with default settings.
     */
    public FormatterConfig() { // NOSONAR - must be public, or Groovy/Gradle does not see it
        getEnabled().set(true);

        // Only format main code - must see how it behaves with test CSV
        getInclude().set("src/main/java/**/*.java");
        getExclude().set("");
    }
}
