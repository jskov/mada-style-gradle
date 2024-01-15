package dk.mada.style.format;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

/**
 * DSL container for specification of client generation.
 */
public abstract class FormatterConfig {
    public abstract Property<Boolean> getEnabled();

    /** {@return the OpenApi document file extension. Defaults to .yaml} */
    public abstract Property<String> getInclude();

    /** {@return the flag to controls if code is generated to the source folder} */
    public abstract Property<String> getExclude();
    
    public abstract RegularFileProperty getEclipseConfig();

    public FormatterConfig() { // NOSONAR - must be public, or Groovy/Gradle does not see it
        getEnabled().set(true);
        
        // Only format main code - must see how it behaves with test CSV
        getInclude().set("src/main/java/**/*.java");
        getExclude().set("");
    }
}
